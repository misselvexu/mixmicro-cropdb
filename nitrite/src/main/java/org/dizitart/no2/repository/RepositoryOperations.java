/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.repository;

import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.NitriteFilter;

import java.lang.reflect.Field;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ObjectUtils.isCompatibleTypes;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * The {@link ObjectRepository} operations.
 * <p>
 * This class is for internal use only.
 * </p>
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class RepositoryOperations {
    private final NitriteMapper nitriteMapper;
    private final Class<?> type;
    private final NitriteCollection collection;
    private final AnnotationScanner annotationScanner;
    private ObjectIdField objectIdField;

    /**
     * Instantiates a new {@link RepositoryOperations}.
     *
     * @param type          the type
     * @param nitriteMapper the nitrite mapper
     * @param collection    the collection
     */
    public RepositoryOperations(Class<?> type,
                                NitriteMapper nitriteMapper,
                                NitriteCollection collection) {
        this.type = type;
        this.nitriteMapper = nitriteMapper;
        this.collection = collection;
        this.annotationScanner = new AnnotationScanner(type, collection, nitriteMapper);
        validateCollection();
    }

    /**
     * Create indices.
     */
    public void createIndices() {
        annotationScanner.scanIndices();
        annotationScanner.createIndices();
        annotationScanner.createIdIndex();
        objectIdField = annotationScanner.getObjectIdField();
    }

    /**
     * Serialize fields.
     *
     * @param document the document
     */
    public void serializeFields(Document document) {
        if (document != null) {
            for (Pair<String, Object> pair : document) {
                String key = pair.getFirst();
                Object value = pair.getSecond();
                Object serializedValue;
                serializedValue = nitriteMapper.convert(value, Document.class);
                document.put(key, serializedValue);
            }
        }
    }

    /**
     * To documents document [ ].
     *
     * @param <T>    the type parameter
     * @param others the others
     * @return the document [ ]
     */
    public <T> Document[] toDocuments(T[] others) {
        if (others == null || others.length == 0) return null;
        Document[] documents = new Document[others.length];
        for (int i = 0; i < others.length; i++) {
            documents[i] = toDocument(others[i], false);
        }
        return documents;
    }

    /**
     * To document document.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @param update the update
     * @return the document
     */
    public <T> Document toDocument(T object, boolean update) {
        Document document = nitriteMapper.convert(object, Document.class);

        if (objectIdField != null) {
            Field idField = objectIdField.getField();

            if (idField.getType() == NitriteId.class) {
                try {
                    idField.setAccessible(true);
                    if (idField.get(object) == null) {
                        NitriteId id = document.getId();
                        idField.set(object, id);
                        document.put(objectIdField.getIdFieldName(), nitriteMapper.convert(id, Comparable.class));
                    } else if (!update) {
                        throw new InvalidIdException("auto generated id should not be set manually");
                    }
                } catch (IllegalAccessException iae) {
                    throw new InvalidIdException("auto generated id value cannot be accessed");
                }
            }

            Object idValue = document.get(objectIdField.getIdFieldName());
            if (idValue == null) {
                throw new InvalidIdException("id cannot be null");
            }
            if (idValue instanceof String && isNullOrEmpty((String) idValue)) {
                throw new InvalidIdException("id value cannot be empty string");
            }
        }
        return document;
    }

    /**
     * Create unique filter filter.
     *
     * @param object the object
     * @return the filter
     */
    public Filter createUniqueFilter(Object object) {
        if (objectIdField == null) {
            throw new NotIdentifiableException("update operation failed as no id value found for the object");
        }

        Field idField = objectIdField.getField();
        idField.setAccessible(true);
        try {
            Object value = idField.get(object);
            if (value == null) {
                throw new InvalidIdException("id value cannot be null");
            }
            return objectIdField.createUniqueFilter(value, nitriteMapper);
        } catch (IllegalAccessException iae) {
            throw new InvalidIdException("id field is not accessible");
        }
    }

    /**
     * Remove nitrite id.
     *
     * @param document the document
     */
    public void removeNitriteId(Document document) {
        document.remove(DOC_ID);
        if (objectIdField != null) {
            Field idField = objectIdField.getField();
            if (idField != null && !objectIdField.isEmbedded()
                && idField.getType() == NitriteId.class) {
                document.remove(idField.getName());
            }
        }
    }

    /**
     * Create id filter filter.
     *
     * @param <I> the type parameter
     * @param id  the id
     * @return the filter
     */
    public <I> Filter createIdFilter(I id) {
        if (objectIdField != null) {
            if (id == null) {
                throw new InvalidIdException("a null id is not a valid id");
            }
            if (!isCompatibleTypes(id.getClass(), objectIdField.getField().getType())) {
                throw new InvalidIdException("a value of invalid type is provided as id");
            }

            return objectIdField.createUniqueFilter(id, nitriteMapper);
        } else {
            throw new NotIdentifiableException(type.getName() + " does not have any id field");
        }
    }

    /**
     * As object filter filter.
     *
     * @param filter the filter
     * @return the filter
     */
    public Filter asObjectFilter(Filter filter) {
        if (filter instanceof NitriteFilter) {
            NitriteFilter nitriteFilter = (NitriteFilter) filter;
            nitriteFilter.setObjectFilter(true);
            return nitriteFilter;
        }
        return filter;
    }

    /**
     * Find cursor.
     *
     * @param <T>         the type parameter
     * @param filter      the filter
     * @param findOptions the find options
     * @param type        the type
     * @return the cursor
     */
    public <T> Cursor<T> find(Filter filter, FindOptions findOptions, Class<T> type) {
        DocumentCursor documentCursor = collection.find(asObjectFilter(filter), findOptions);
        return new ObjectCursor<>(nitriteMapper, documentCursor, type);
    }

    private void validateCollection() {
        if (collection == null) {
            throw new ValidationException("repository has not been initialized properly");
        }
    }
}
