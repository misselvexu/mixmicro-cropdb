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

package xyz.vopen.framework.cropdb.index;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.exceptions.FilterException;
import xyz.vopen.framework.cropdb.filters.ComparableFilter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Represents an {@link IndexMap} scanner.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class IndexScanner {
  private final IndexMap indexMap;

  /**
   * Instantiates a new {@link IndexScanner}.
   *
   * @param indexMap the index map
   */
  public IndexScanner(IndexMap indexMap) {
    this.indexMap = indexMap;
  }

  /**
   * Scans the {@link IndexMap} and returns the {@link CropId}s of the matching elements.
   *
   * @param filters the filters
   * @param indexScanOrder the index scan order
   * @return the linked hash set
   */
  @SuppressWarnings("unchecked")
  public LinkedHashSet<CropId> doScan(
      List<ComparableFilter> filters, Map<String, Boolean> indexScanOrder) {
    // linked-hash-set to return only unique ids preserving the order in index
    LinkedHashSet<CropId> cropIds = new LinkedHashSet<>();

    if (filters != null && !filters.isEmpty()) {
      // get the first filter to start scanning
      ComparableFilter comparableFilter = filters.get(0);

      if (comparableFilter != null) {
        // set the scan order of the index map
        boolean reverseScan =
            (indexScanOrder != null && indexScanOrder.containsKey(comparableFilter.getField()))
                ? indexScanOrder.get(comparableFilter.getField())
                : false;
        indexMap.setReverseScan(reverseScan);

        // apply the filter on the index map
        // result can be list of crop ids or list of navigable maps
        List<?> scanResult = comparableFilter.applyOnIndex(indexMap);
        if (isEmptyList(scanResult)) {
          // if list is empty then no need for further scanning
          return cropIds;
        } else if (isCropIdList(scanResult)) {
          // if this is a list of crop ids then add those to the
          // result and no further scanning is required as we have
          // reached the terminal crop ids
          List<CropId> idList = (List<CropId>) scanResult;
          cropIds.addAll(idList);
        } else if (isNavigableMapList(scanResult)) {
          // if this is a list of sub maps, then take each of the sub map
          // and the next filter and scan the sub map
          List<NavigableMap<DBValue, ?>> subMaps = (List<NavigableMap<DBValue, ?>>) scanResult;
          List<ComparableFilter> remainingFilter = filters.subList(1, filters.size());

          for (NavigableMap<DBValue, ?> subMap : subMaps) {
            // create an index map from the sub map and scan to get the
            // terminal crop ids
            IndexMap indexMap = new IndexMap(subMap);
            IndexScanner subMapScanner = new IndexScanner(indexMap);
            LinkedHashSet<CropId> subResult = subMapScanner.doScan(remainingFilter, indexScanOrder);
            cropIds.addAll(subResult);
          }
        }
      } else {
        // filter is not comparable filter, so index scanning can not continue
        throw new FilterException(
            "index scan is not supported for " + comparableFilter.getClass().getName());
      }
    } else {
      // if no more filter is left, get all terminal crop ids from
      // index map and return them in the order.
      List<CropId> terminalResult = indexMap.getTerminalCropIds();
      cropIds.addAll(terminalResult);
    }

    return cropIds;
  }

  private boolean isEmptyList(List<?> list) {
    return list == null || list.isEmpty();
  }

  private boolean isCropIdList(List<?> list) {
    Object value = list.get(0);
    return value instanceof CropId;
  }

  private boolean isNavigableMapList(List<?> list) {
    Object value = list.get(0);
    return value instanceof NavigableMap;
  }
}
