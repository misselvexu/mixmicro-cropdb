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

package xyz.vopen.framework.cropdb.index;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.common.Fields;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an indexer for {@link Comparable} values.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public abstract class ComparableIndexer implements CropIndexer {
  private final Map<IndexDescriptor, CropIndex> indexRegistry;

  /** Instantiates a new Comparable indexer. */
  public ComparableIndexer() {
    this.indexRegistry = new ConcurrentHashMap<>();
  }

  /**
   * Indicates if it is an unique index.
   *
   * @return the boolean
   */
  abstract boolean isUnique();

  @Override
  public void initialize(CropConfig cropConfig) {}

  @Override
  public void validateIndex(Fields fields) {
    // nothing to validate
  }

  @Override
  public LinkedHashSet<CropId> findByFilter(FindPlan findPlan, CropConfig cropConfig) {
    CropIndex cropIndex = findCropIndex(findPlan.getIndexDescriptor(), cropConfig);
    return cropIndex.findCropIds(findPlan);
  }

  @Override
  public void writeIndexEntry(
      FieldValues fieldValues, IndexDescriptor indexDescriptor, CropConfig cropConfig) {
    CropIndex cropIndex = findCropIndex(indexDescriptor, cropConfig);
    cropIndex.write(fieldValues);
  }

  @Override
  public void removeIndexEntry(
      FieldValues fieldValues, IndexDescriptor indexDescriptor, CropConfig cropConfig) {
    CropIndex cropIndex = findCropIndex(indexDescriptor, cropConfig);
    cropIndex.remove(fieldValues);
  }

  @Override
  public void dropIndex(IndexDescriptor indexDescriptor, CropConfig cropConfig) {
    CropIndex cropIndex = findCropIndex(indexDescriptor, cropConfig);
    cropIndex.drop();
  }

  private CropIndex findCropIndex(IndexDescriptor indexDescriptor, CropConfig cropConfig) {
    if (indexRegistry.containsKey(indexDescriptor)) {
      return indexRegistry.get(indexDescriptor);
    }

    CropIndex cropIndex;
    if (indexDescriptor.isCompoundIndex()) {
      cropIndex = new CompoundIndex(indexDescriptor, cropConfig.getCropStore());
    } else {
      cropIndex = new SingleFieldIndex(indexDescriptor, cropConfig.getCropStore());
    }
    indexRegistry.put(indexDescriptor, cropIndex);
    return cropIndex;
  }
}
