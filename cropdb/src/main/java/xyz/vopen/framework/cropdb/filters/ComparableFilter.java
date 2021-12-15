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

package xyz.vopen.framework.cropdb.filters;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.exceptions.FilterException;
import xyz.vopen.framework.cropdb.index.IndexMap;

import java.util.List;
import java.util.NavigableMap;

/**
 * Represents a filter based on document field holding {@link Comparable} values.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public abstract class ComparableFilter extends FieldBasedFilter {
    /**
     * Instantiates a new Comparable filter.
     *
     * @param field the field
     * @param value the value
     */
    public ComparableFilter(String field, Object value) {
        super(field, value);
    }

    /**
     * Gets the {@link Comparable} value to filter.
     *
     * @return the comparable
     */
    @SuppressWarnings("rawtypes")
    public Comparable getComparable() {
        if (getValue() == null) {
            throw new FilterException("value parameter must not be null");
        }
        return (Comparable) getValue();
    }

    /**
     * Apply this filter on an crop index.
     *
     * @param indexMap the index scanner
     * @return the object
     */
    public abstract List<?> applyOnIndex(IndexMap indexMap);

    /**
     * Process values after index scanning.
     *
     * @param value      the value
     * @param subMap     the sub map
     * @param cropIds the crop ids
     */
    @SuppressWarnings("unchecked")
    protected void processIndexValue(Object value,
                                     List<NavigableMap<Comparable<?>, Object>> subMap,
                                     List<CropId> cropIds) {
        if (value instanceof List) {
            // if its is list then add it directly to crop ids
            List<CropId> result = (List<CropId>) value;
            cropIds.addAll(result);
        }

        if (value instanceof NavigableMap) {
            subMap.add((NavigableMap<Comparable<?>, Object>) value);
        }
    }
}
