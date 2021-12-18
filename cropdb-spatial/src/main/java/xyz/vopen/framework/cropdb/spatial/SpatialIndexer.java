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

package xyz.vopen.framework.cropdb.spatial;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.CropIndexer;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a spatial data indexer.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0.0
 */
public class SpatialIndexer implements CropIndexer {
  /** Spatial index type. */
  public static final String SPATIAL_INDEX = "Spatial";

  private final Map<IndexDescriptor, SpatialIndex> indexRegistry;

  /** Instantiates a new {@link SpatialIndexer}. */
  public SpatialIndexer() {
    this.indexRegistry = new ConcurrentHashMap<>();
  }

  @Override
  public String getIndexType() {
    return SPATIAL_INDEX;
  }

  @Override
  public void validateIndex(Fields fields) {
    if (fields.getFieldNames().size() > 1) {
      throw new IndexingException("spatial index can only be created on a single field");
    }
  }

  @Override
  public void dropIndex(IndexDescriptor indexDescriptor, CropConfig cropConfig) {
    SpatialIndex spatialIndex = findSpatialIndex(indexDescriptor, cropConfig);
    spatialIndex.drop();
  }

  @Override
  public void writeIndexEntry(
      FieldValues fieldValues, IndexDescriptor indexDescriptor, CropConfig cropConfig) {
    SpatialIndex spatialIndex = findSpatialIndex(indexDescriptor, cropConfig);
    spatialIndex.write(fieldValues);
  }

  @Override
  public void removeIndexEntry(
      FieldValues fieldValues, IndexDescriptor indexDescriptor, CropConfig cropConfig) {
    SpatialIndex spatialIndex = findSpatialIndex(indexDescriptor, cropConfig);
    spatialIndex.remove(fieldValues);
  }

  @Override
  public LinkedHashSet<CropId> findByFilter(FindPlan findPlan, CropConfig cropConfig) {
    SpatialIndex spatialIndex = findSpatialIndex(findPlan.getIndexDescriptor(), cropConfig);
    return spatialIndex.findCropIds(findPlan);
  }

  @Override
  public void initialize(CropConfig cropConfig) {}

  private SpatialIndex findSpatialIndex(IndexDescriptor indexDescriptor, CropConfig cropConfig) {
    if (indexRegistry.containsKey(indexDescriptor)) {
      return indexRegistry.get(indexDescriptor);
    }

    SpatialIndex cropIndex = new SpatialIndex(indexDescriptor, cropConfig);
    indexRegistry.put(indexDescriptor, cropIndex);
    return cropIndex;
  }
}
