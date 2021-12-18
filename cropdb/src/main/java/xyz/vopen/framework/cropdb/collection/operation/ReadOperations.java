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

package xyz.vopen.framework.cropdb.collection.operation;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.*;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.filters.EqualsFilter;
import xyz.vopen.framework.cropdb.filters.Filter;
import xyz.vopen.framework.cropdb.filters.LogicalFilter;
import xyz.vopen.framework.cropdb.filters.CropFilter;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.CropIndexer;
import xyz.vopen.framework.cropdb.common.processors.ProcessorChain;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.common.streams.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
class ReadOperations {
  private final String collectionName;
  private final CropConfig cropConfig;
  private final CropMap<CropId, Document> cropMap;
  private final FindOptimizer findOptimizer;
  private final IndexOperations indexOperations;
  private final ProcessorChain processorChain;

  ReadOperations(
      String collectionName,
      IndexOperations indexOperations,
      CropConfig cropConfig,
      CropMap<CropId, Document> cropMap,
      ProcessorChain processorChain) {
    this.cropMap = cropMap;
    this.cropConfig = cropConfig;
    this.collectionName = collectionName;
    this.indexOperations = indexOperations;
    this.findOptimizer = new FindOptimizer();
    this.processorChain = processorChain;
  }

  public DocumentCursor find(Filter filter, FindOptions findOptions) {
    if (filter == null) {
      filter = Filter.ALL;
    }

    prepareFilter(filter);
    Collection<IndexDescriptor> indexDescriptors = indexOperations.listIndexes();
    FindPlan findPlan = findOptimizer.optimize(filter, findOptions, indexDescriptors);
    return createCursor(findPlan);
  }

  Document getById(CropId cropId) {
    return cropMap.get(cropId);
  }

  private void prepareFilter(Filter filter) {
    if (filter instanceof CropFilter) {
      CropFilter cropFilter = (CropFilter) filter;
      prepareCropFilter(cropFilter);

      if (filter instanceof LogicalFilter) {
        LogicalFilter logicalFilter = (LogicalFilter) filter;
        prepareLogicalFilter(logicalFilter);
      }
    }
  }

  private void prepareCropFilter(CropFilter cropFilter) {
    cropFilter.setCropConfig(cropConfig);
    cropFilter.setCollectionName(collectionName);
  }

  private void prepareLogicalFilter(LogicalFilter logicalFilter) {
    List<Filter> filters = logicalFilter.getFilters();
    for (Filter filter : filters) {
      if (filter instanceof CropFilter) {
        CropFilter cropFilter = (CropFilter) filter;
        cropFilter.setObjectFilter(logicalFilter.getObjectFilter());
      }
      prepareFilter(filter);
    }
  }

  private RecordStream<Pair<CropId, Document>> findSuitableStream(FindPlan findPlan) {
    RecordStream<Pair<CropId, Document>> rawStream;

    if (!findPlan.getSubPlans().isEmpty()) {
      // or filters get all sub stream by finding suitable stream of all sub plans
      List<RecordStream<Pair<CropId, Document>>> subStreams = new ArrayList<>();
      for (FindPlan subPlan : findPlan.getSubPlans()) {
        RecordStream<Pair<CropId, Document>> suitableStream = findSuitableStream(subPlan);
        subStreams.add(suitableStream);
      }
      // union of all suitable stream of all sub plans
      rawStream = new UnionStream(subStreams);

      // return only distinct items
      rawStream = new DistinctStream(rawStream);
    } else {
      // and or single filter
      if (findPlan.getByIdFilter() != null) {
        EqualsFilter byIdFilter = findPlan.getByIdFilter();
        CropId cropId = CropId.createId((String) byIdFilter.getValue());
        rawStream = RecordStream.single(Pair.pair(cropId, cropMap.get(cropId)));
      } else {
        IndexDescriptor indexDescriptor = findPlan.getIndexDescriptor();
        if (indexDescriptor != null) {
          // get optimized filter
          CropIndexer indexer = cropConfig.findIndexer(indexDescriptor.getIndexType());
          LinkedHashSet<CropId> cropIds = indexer.findByFilter(findPlan, cropConfig);

          // create indexed stream from optimized filter
          rawStream = new IndexedStream(cropIds, cropMap);
        } else {
          rawStream = cropMap.entries();
        }
      }

      if (findPlan.getCollectionScanFilter() != null) {
        rawStream = new FilteredStream(rawStream, findPlan.getCollectionScanFilter());
      }
    }

    // sort and bound stage
    if (rawStream != null) {
      if (findPlan.getBlockingSortOrder() != null && !findPlan.getBlockingSortOrder().isEmpty()) {
        rawStream = new SortedDocumentStream(findPlan, rawStream);
      }

      if (findPlan.getLimit() != null || findPlan.getSkip() != null) {
        long limit = findPlan.getLimit() == null ? Long.MAX_VALUE : findPlan.getLimit();
        long skip = findPlan.getSkip() == null ? 0 : findPlan.getSkip();
        rawStream = new BoundedDocumentStream(skip, limit, rawStream);
      }
    }

    return rawStream;
  }

  private DocumentCursor createCursor(FindPlan findPlan) {
    RecordStream<Pair<CropId, Document>> recordStream = findSuitableStream(findPlan);
    DocumentStream cursor = new DocumentStream(recordStream, processorChain);
    cursor.setFindPlan(findPlan);
    return cursor;
  }
}
