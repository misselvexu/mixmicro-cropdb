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

import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.index.IndexMap;

import java.util.ArrayList;
import java.util.List;

import static xyz.vopen.framework.cropdb.common.util.ObjectUtils.deepEquals;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
public class EqualsFilter extends ComparableFilter {
    EqualsFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    public boolean apply(Pair<CropId, Document> element) {
        Document document = element.getSecond();
        Object fieldValue = document.get(getField());
        return deepEquals(fieldValue, getValue());
    }

    @Override
    public List<?> applyOnIndex(IndexMap indexMap) {
        Object value = indexMap.get((Comparable<?>) getValue());
        if (value instanceof List) {
            return ((List<?>) value);
        }

        List<Object> result = new ArrayList<>();
        result.add(value);
        return result;
    }

    @Override
    public String toString() {
        return "(" + getField() + " == " + getValue() + ")";
    }
}
