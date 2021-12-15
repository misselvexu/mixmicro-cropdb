/*
 * Copyright (c) 2021-2022. CropDB author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.vopen.framework.cropdb.collection;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.common.Constants;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.util.Iterables;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;
import xyz.vopen.framework.cropdb.exceptions.InvalidIdException;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import static xyz.vopen.framework.cropdb.collection.CropId.*;

/**
 * A default implementation of crop document.
 *
 * @since 4.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class CropDocument extends LinkedHashMap<String, Object> implements Document {
    private static final long serialVersionUID = 1477462374L;
    private static final List<String> reservedFields = Iterables.listOf(Constants.DOC_ID, Constants.DOC_REVISION, Constants.DOC_SOURCE, Constants.DOC_MODIFIED);

    CropDocument() {
        super();
    }

    CropDocument(Map<String, Object> objectMap) {
        super(objectMap);
    }

    @Override
    public Document put(String field, Object value) {
        // field name cannot be empty or null
        if (StringUtils.isNullOrEmpty(field)) {
            throw new InvalidOperationException("document does not support empty or null key");
        }

        // _id field can not be set manually
        if (Constants.DOC_ID.contentEquals(field) && !validId(value)) {
            throw new InvalidOperationException("_id is an auto generated value and cannot be set");
        }

        // value must be serializable
        if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
            throw new ValidationException("type " + value.getClass().getName()
                + " does not implement java.io.Serializable");
        }

        // if field name contains field separator, split the fields, and put the value
        // accordingly associated with th embedded field.
        if (isEmbedded(field)) {
            String regex = MessageFormat.format("\\{0}", CropConfig.getFieldSeparator());
            String[] splits = field.split(regex);
            deepPut(splits, value);
        } else {
            super.put(field, value);
        }
        return this;
    }

    @Override
    public Object get(String field) {
        if (field != null
            && isEmbedded(field)
            && !containsKey(field)) {
            // if field is an embedded field, get it by deep scan
            return deepGet(field);
        }
        return super.get(field);
    }

    @Override
    public <T> T get(String field, Class<T> type) {
        ValidationUtils.notNull(type, "type cannot be null");
        return type.cast(get(field));
    }

    @Override
    public CropId getId() {
        String id;
        try {
            // if _id field is not populated already, create a new id
            // and set, otherwise return the existing id
            if (!containsKey(Constants.DOC_ID)) {
                id = newId().getIdValue();
                super.put(Constants.DOC_ID, id);
            } else {
                id = (String) get(Constants.DOC_ID);
            }

            // create a crop id instance from the string value
            return createId(id);
        } catch (ClassCastException cce) {
            throw new InvalidIdException("invalid _id found " + get(Constants.DOC_ID));
        }
    }

    @Override
    public Set<String> getFields() {
        // get all fields except from the reserved ones
        return getFieldsInternal("");
    }

    @Override
    public boolean hasId() {
        return super.containsKey(Constants.DOC_ID);
    }

    @Override
    public void remove(String field) {
        if (isEmbedded(field)) {
            // if the field is an embedded field,
            // run a deep scan and remove the last field
            String regex = MessageFormat.format("\\{0}", CropConfig.getFieldSeparator());
            String[] splits = field.split(regex);
            deepRemove(splits);
        } else {
            // remove the field from this document
            super.remove(field);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Document clone() {
        Map<String, Object> cloned = (Map<String, Object>) super.clone();

        // create the clone of any embedded documents as well
        for (Map.Entry<String, Object> entry : cloned.entrySet()) {
            if (entry.getValue() instanceof Document) {
                Document value = (Document) entry.getValue();

                // this will recursively take care any embedded document
                // of the clone as well
                Document clonedValue = value.clone();
                cloned.put(entry.getKey(), clonedValue);
            }
        }
        return new CropDocument(cloned);
    }

    @Override
    public Document merge(Document document) {
        if (document instanceof CropDocument) {
            super.putAll((CropDocument) document);
        }
        return this;
    }

    @Override
    public boolean containsKey(String key) {
//        return getFields().contains(key);
        return super.containsKey(key);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;

        if (!(other instanceof CropDocument))
            return false;

        CropDocument m = (CropDocument) other;
        if (m.size() != size())
            return false;

        try {
            for (Map.Entry<String, Object> e : entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!Objects.deepEquals(value, m.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }

        return true;
    }

    @Override
    public Iterator<Pair<String, Object>> iterator() {
        return new PairIterator(super.entrySet().iterator());
    }

    private Set<String> getFieldsInternal(String prefix) {
        Set<String> fields = new HashSet<>();

        // iterate top level keys
        for (Pair<String, Object> entry : this) {
            // ignore the reserved fields
            if (reservedFields.contains(entry.getFirst())) continue;

            Object value = entry.getSecond();
            if (value instanceof CropDocument) {
                // if the value is a document, traverse its fields recursively,
                // prefix would be the field name of the document
                if (StringUtils.isNullOrEmpty(prefix)) {
                    // level-1 fields
                    fields.addAll(((CropDocument) value).getFieldsInternal(entry.getFirst()));
                } else {
                    // level-n fields, separated by field separator
                    fields.addAll(((CropDocument) value).getFieldsInternal(prefix
                        + CropConfig.getFieldSeparator() + entry.getFirst()));
                }
            } else if (!(value instanceof Iterable)) {
                // if there is no more embedded document, add the field to the list
                // and if this is an embedded document then prefix its name by parent fields,
                // separated by field separator
                if (StringUtils.isNullOrEmpty(prefix)) {
                    fields.add(entry.getFirst());
                } else {
                    fields.add(prefix + CropConfig.getFieldSeparator() + entry.getFirst());
                }
            }
        }
        return fields;
    }

    private Object deepGet(String field) {
        if (isEmbedded(field)) {
            // for embedded field, run a deep scan
            return getByEmbeddedKey(field);
        } else {
            return null;
        }
    }

    private void deepPut(String[] splits, Object value) {
        if (splits.length == 0) {
            throw new ValidationException("invalid key provided");
        }
        String key = splits[0];
        if (splits.length == 1) {
            // if last key, simply put in the current document
            put(key, value);
        } else {
            // get the object for the current level
            Object val = get(key);

            // get the remaining embedded fields for next level scan
            String[] remaining = Arrays.copyOfRange(splits, 1, splits.length);

            if (val instanceof CropDocument) {
                // if the current level value is embedded doc, scan to the next level
                ((CropDocument) val).deepPut(remaining, value);
            } else if (val == null) {
                // if current level value is null, create a new document
                // and try to create next level embedded doc by next level scan
                CropDocument subDoc = new CropDocument();
                subDoc.deepPut(remaining, value);

                // put the newly created document in current level
                put(key, subDoc);
            }
        }
    }

    private void deepRemove(String[] splits) {
        if (splits.length == 0) {
            throw new ValidationException("invalid key provided");
        }
        String key = splits[0];
        if (splits.length == 1) {
            // if last key, simply remove the current document
            remove(key);
        } else {
            // get the object for the current level
            Object val = get(key);

            // get the remaining embedded fields for next level scan
            String[] remaining = Arrays.copyOfRange(splits, 1, splits.length);

            if (val instanceof CropDocument) {
                // if the current level value is embedded doc, scan to the next level
                CropDocument subDoc = (CropDocument) val;
                subDoc.deepRemove(remaining);
                if (subDoc.size() == 0) {
                    // if the next level document is an empty one
                    // remove the current level document also
                    super.remove(key);
                }
            } else if (val == null) {
                // if current level value is null, remove the key
                super.remove(key);
            }
        }
    }

    private Object getByEmbeddedKey(String embeddedKey) {
        String regex = MessageFormat.format("\\{0}", CropConfig.getFieldSeparator());

        // split the key
        String[] path = embeddedKey.split(regex);
        if (path.length < 1) {
            return null;
        }

        // get current level value and scan to next level using remaining keys
        return recursiveGet(get(path[0]), Arrays.copyOfRange(path, 1, path.length));
    }

    @SuppressWarnings("unchecked")
    private Object recursiveGet(Object object, String[] remainingPath) {
        if (object == null) {
            return null;
        }

        if (remainingPath.length == 0) {
            return object;
        }

        if (object instanceof Document) {
            // if the current level value is document, scan to the next level with remaining keys
            return recursiveGet(((Document) object).get(remainingPath[0]),
                Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
        }

        if (object.getClass().isArray()) {
            // if the current level value is an array

            // get the first key
            String accessor = remainingPath[0];

            // convert current value to object array
            Object[] array = ObjectUtils.convertToObjectArray(object);

            if (isInteger(accessor)) {
                // if the current key is an integer

                // convert the key as an integer index
                int index = asInteger(accessor);

                // check index lower bound
                if (index < 0) {
                    throw new ValidationException("invalid array index " + index + " to access item inside a document");
                }

                // check index upper bound
                if (index >= array.length) {
                    throw new ValidationException("index " + index +
                        " is not less than the size of the array " + array.length);
                }

                // get the value at the index from the array
                // if there are remaining keys, scan to the next level
                return recursiveGet(array[index], Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
            } else {
                // if the current key is not an integer, then decompose the
                // object array into a list and scan each of the element of the
                // list using remaining keys and return a list of all returned
                // elements from each of the list items.
                return decompose(Iterables.listOf(array), remainingPath);
            }
        }

        if (object instanceof Iterable) {
            // if the current level value is an iterable

            // get the first key
            String accessor = remainingPath[0];

            // convert current value to object iterable
            Iterable<Object> iterable = (Iterable<Object>) object;

            // create a list from the iterable
            List<Object> collection = Iterables.toList(iterable);

            if (isInteger(accessor)) {
                // if the current key is an integer

                // convert the key as an integer index
                int index = asInteger(accessor);

                // check index lower bound
                if (index < 0) {
                    throw new ValidationException("invalid collection index " + index + " to access item inside a document");
                }

                // check index upper bound
                if (index >= collection.size()) {
                    throw new ValidationException("index " + accessor +
                        " is not less than the size of the list " + collection.size());
                }

                // get the value at the index from the list
                // if there are remaining keys, scan to the next level
                return recursiveGet(collection.get(index), Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
            } else {
                // if the current key is not an integer, then decompose the
                // list and scan each of the element of the
                // list using remaining keys and return a list of all returned
                // elements from each of the list items.
                return decompose(collection, remainingPath);
            }
        }

        // if no match found return null
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Object> decompose(List<Object> collection, String[] remainingPath) {
        Set<Object> items = new HashSet<>();

        // iterate each item
        for (Object item : collection) {

            // scan the item using remaining keys
            Object result = recursiveGet(item, remainingPath);

            if (result != null) {
                if (result instanceof Iterable) {
                    // if the result is iterable, return everything as a list
                    List<Object> list = Iterables.toList((Iterable<Object>) result);
                    items.addAll(list);
                } else if (result.getClass().isArray()) {
                    // if the result is an array, return everything as list
                    List<Object> list = Arrays.asList(ObjectUtils.convertToObjectArray(result));
                    items.addAll(list);
                } else {
                    // if its neither a iterable not an array, return the item
                    items.add(result);
                }
            }
        }
        return new ArrayList<>(items);
    }

    private int asInteger(String number) {
        try {
            // parse the string as an integer
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            // if parsing fails, return invalid integer for document access
            return -1;
        }
    }

    private boolean isInteger(String value) {
        try {
            // try parse the string as an integer
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            // if parsing fails, then value is not an integer
            return false;
        }
    }

    private boolean isEmbedded(String field) {
        // if the field contains separator character, then it is an embedded field
        return field.contains(CropConfig.getFieldSeparator());
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(size());
        for (Pair<String, Object> pair : this) {
            stream.writeObject(pair);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            Pair<String, Object> pair = (Pair<String, Object>) stream.readObject();
            super.put(pair.getFirst(), pair.getSecond());
        }
    }

    private static class PairIterator implements Iterator<Pair<String, Object>> {
        private final Iterator<Map.Entry<String, Object>> iterator;

        PairIterator(Iterator<Map.Entry<String, Object>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Pair<String, Object> next() {
            Map.Entry<String, Object> next = iterator.next();
            return new Pair<>(next.getKey(), next.getValue());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
