/*
 * Copyright (c) 2017-2021 Crop author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xyz.vopen.framework.cropdb.common.streams;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.DBNull;
import xyz.vopen.framework.cropdb.common.SortOrder;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts documents based on the sort order provided.
 *
 * <p>
 * By default null is considered the lowest value.
 * </p>
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class DocumentSorter implements Comparator<Pair<CropId, Document>> {
    private final Collator collator;
    private final List<Pair<String, SortOrder>> sortOrder;

    /**
     * Instantiates a new Document sorter.
     *
     * @param collator  the collator
     * @param sortOrder the sort order
     */
    public DocumentSorter(Collator collator, List<Pair<String, SortOrder>> sortOrder) {
        this.collator = collator;
        this.sortOrder = sortOrder;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public int compare(Pair<CropId, Document> pair1, Pair<CropId, Document> pair2) {
        if (sortOrder != null && !sortOrder.isEmpty()) {
            for (Pair<String, SortOrder> pair : sortOrder) {
                Document doc1 = pair1.getSecond();
                Document doc2 = pair2.getSecond();

                Object value1 = doc1.get(pair.getFirst());
                Object value2 = doc2.get(pair.getFirst());

                // handle null values
                int result;
                if ((value1 == null || value1 instanceof DBNull) && value2 != null) {
                    result = -1;
                } else if (value1 != null && (value2 == null || value2 instanceof DBNull)) {
                    result = 1;
                } else if (value1 == null) {
                    result = -1;
                } else {

                    // validate comparable
                    if (value1.getClass().isArray() || value1 instanceof Iterable
                        || value2.getClass().isArray() || value2 instanceof Iterable) {
                        throw new ValidationException("cannot sort on an array or collection object");
                    }

                    // compare values
                    Comparable c1 = (Comparable) value1;
                    Comparable c2 = (Comparable) value2;

                    if (c1 instanceof String && c2 instanceof String && collator != null) {
                        result = collator.compare(c1, c2);
                    } else {
                        result = c1.compareTo(c2);
                    }
                }

                if (pair.getSecond() == SortOrder.Descending) {
                    result *= -1;
                }

                if (result != 0) {
                    return result;
                }
            }
        }
        return 0;
    }
}
