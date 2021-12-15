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

package org.dizitart.no2.common.util;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.util.ArrayList;
import java.util.Objects;

import static org.dizitart.no2.common.util.ObjectUtils.newInstance;

/**
 * A utility class for {@link Document}.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
public class DocumentUtils {
    private DocumentUtils(){}

    /**
     * Determines whether a document has recently been updated/created than the other.
     *
     * @param recent the recent document
     * @param older  the older document
     * @return the boolean value
     */
    public static boolean isRecent(Document recent, Document older) {
        if (Objects.deepEquals(recent.getRevision(), older.getRevision())) {
            return recent.getLastModifiedSinceEpoch() >= older.getLastModifiedSinceEpoch();
        }
        return recent.getRevision() > older.getRevision();
    }

    /**
     * Create unique filter to identify the `document`.
     *
     * @param document the document
     * @return the unique filter
     */
    public static Filter createUniqueFilter(Document document) {
        return Filter.byId(document.getId());
    }

    /**
     * Creates an empty document having all fields of a `type` set to `null`.
     *
     * @param <T>           the type parameter
     * @param nitriteMapper the nitrite mapper
     * @param type          the type
     * @return the document
     */
    public static <T> Document skeletonDocument(NitriteMapper nitriteMapper, Class<T> type) {
        if (nitriteMapper.isValueType(type)) {
            return Document.createDocument();
        }
        T dummy = newInstance(type, true);
        Document document = nitriteMapper.convert(dummy, Document.class);
        return removeValues(document);
    }

    public static boolean isSimilar(Document document, Document other, String... fields) {
        boolean result = true;
        if (document == null && other != null) return false;
        if (document != null && other == null) return false;
        if (document == null) return true;

        for (String field : fields) {
            result = result && Objects.deepEquals(document.get(field), other.get(field));
        }
        return result;
    }

    public static FieldValues getValues(Document document, Fields fields) {
        FieldValues fieldValues = new FieldValues();
        fieldValues.setNitriteId(document.getId());
        fieldValues.setFields(fields);
        fieldValues.setValues(new ArrayList<>());

        for (String field : fields.getFieldNames()) {
            Object value = document.get(field);
            fieldValues.getValues().add(new Pair<>(field, value));
        }

        return fieldValues;
    }

    private static Document removeValues(Document document) {
        if (document == null) return null;
        for (Pair<String, Object> entry : document) {
            if (entry.getSecond() instanceof Document) {
                document.put(entry.getFirst(), removeValues((Document) entry.getSecond()));
            } else {
                document.put(entry.getFirst(), null);
            }
        }
        return document;
    }
}
