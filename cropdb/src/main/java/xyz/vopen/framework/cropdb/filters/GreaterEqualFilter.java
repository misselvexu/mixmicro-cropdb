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
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.FilterException;
import xyz.vopen.framework.cropdb.index.IndexMap;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

import static xyz.vopen.framework.cropdb.common.util.Numbers.compare;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
class GreaterEqualFilter extends ComparableFilter {
  GreaterEqualFilter(String field, Comparable<?> value) {
    super(field, value);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public boolean apply(Pair<CropId, Document> element) {
    Comparable comparable = getComparable();
    Document document = element.getSecond();
    Object fieldValue = document.get(getField());
    if (fieldValue != null) {
      if (fieldValue instanceof Number && comparable instanceof Number) {
        return compare((Number) fieldValue, (Number) comparable) >= 0;
      } else if (fieldValue instanceof Comparable) {
        Comparable arg = (Comparable) fieldValue;
        return arg.compareTo(comparable) >= 0;
      } else {
        throw new FilterException(fieldValue + " is not comparable");
      }
    }

    return false;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public List<?> applyOnIndex(IndexMap indexMap) {
    Comparable comparable = getComparable();
    List<NavigableMap<Comparable<?>, Object>> subMap = new ArrayList<>();

    // maintain the find sorting order
    List<CropId> cropIds = new ArrayList<>();

    Comparable ceilingKey = indexMap.ceilingKey(comparable);
    while (ceilingKey != null) {
      // get the starting value, it can be a navigable-map (compound index)
      // or list (single field index)
      Object value = indexMap.get(ceilingKey);
      processIndexValue(value, subMap, cropIds);

      ceilingKey = indexMap.higherKey(ceilingKey);
    }

    if (!subMap.isEmpty()) {
      // if sub-map is populated then filtering on compound index, return sub-map
      return subMap;
    } else {
      // else it is filtering on either single field index,
      // or it is a terminal filter on compound index, return only crop-ids
      return cropIds;
    }
  }

  @Override
  public String toString() {
    return "(" + getField() + " >= " + getValue() + ")";
  }
}
