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

import lombok.Getter;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.tuples.Pair;

/**
 * Represents an OR filter.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
@Getter
public class OrFilter extends LogicalFilter {
    /**
     * Instantiates a new Or filter.
     *
     * @param filters the filters
     */
    OrFilter(Filter... filters) {
        super(filters);
    }

    @Override
    public boolean apply(Pair<CropId, Document> element) {
        boolean result = false;
        for (Filter filter : getFilters()) {
            result = result || filter.apply(element);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < getFilters().size(); i++) {
            Filter filter = getFilters().get(i);
            if (i == 0) {
                stringBuilder.append(filter.toString());
            } else {
                stringBuilder.append(" || ").append(filter.toString());
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
