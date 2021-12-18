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
import xyz.vopen.framework.cropdb.index.IndexMap;

import java.util.*;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
class NotInFilter extends ComparableArrayFilter {
  @Getter private final Set<Comparable<?>> comparableSet;

  NotInFilter(String field, Comparable<?>... values) {
    super(field, values);
    this.comparableSet = new HashSet<>();
    Collections.addAll(this.comparableSet, values);
  }

  @Override
  public boolean apply(Pair<CropId, Document> element) {
    Document document = element.getSecond();
    Object fieldValue = document.get(getField());

    if (fieldValue instanceof Comparable) {
      Comparable<?> comparable = (Comparable<?>) fieldValue;
      return !comparableSet.contains(comparable);
    }
    return true;
  }

  public List<?> applyOnIndex(IndexMap indexMap) {
    List<NavigableMap<Comparable<?>, Object>> subMap = new ArrayList<>();
    List<CropId> cropIds = new ArrayList<>();

    for (Pair<Comparable<?>, ?> entry : indexMap.entries()) {
      if (!comparableSet.contains(entry.getFirst())) {
        processIndexValue(entry.getSecond(), subMap, cropIds);
      }
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
    return "(" + getField() + " not in " + Arrays.toString((Comparable<?>[]) getValue()) + ")";
  }
}
