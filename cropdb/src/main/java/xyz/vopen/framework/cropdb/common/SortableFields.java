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

package xyz.vopen.framework.cropdb.common;

import lombok.AccessLevel;
import lombok.Setter;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static xyz.vopen.framework.cropdb.common.util.ValidationUtils.notEmpty;

/**
 * Represents a list of document field with
 * sorting direction for find query.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class SortableFields extends Fields {
    @Setter(AccessLevel.PACKAGE)
    private List<Pair<String, SortOrder>> sortingOrders;

    /**
     * Instantiates a new {@link SortableFields}.
     */
    public SortableFields() {
        super();
        sortingOrders = new ArrayList<>();
    }

    /**
     * Creates a {@link SortableFields} instance with field names.
     *
     * @param fields the fields
     * @return the fields
     */
    public static SortableFields withNames(String... fields) {
        ValidationUtils.notNull(fields, "fields cannot be null");
        ValidationUtils.notEmpty(fields, "fields cannot be empty");

        SortableFields sortableFields = new SortableFields();
        for (String field : fields) {
            sortableFields.addField(field, SortOrder.Ascending);
        }
        return sortableFields;
    }

    /**
     * Adds the sort order for a field.
     *
     * @param field     the field
     * @param sortOrder the sort order
     * @return the sortable fields
     */
    public SortableFields addField(String field, SortOrder sortOrder) {
        super.fieldNames.add(field);
        this.sortingOrders.add(Pair.pair(field, sortOrder));
        return this;
    }

    /**
     * Gets the sort by field specifications.
     *
     * @return the sort specs
     */
    public List<Pair<String, SortOrder>> getSortingOrders() {
        return Collections.unmodifiableList(sortingOrders);
    }
}
