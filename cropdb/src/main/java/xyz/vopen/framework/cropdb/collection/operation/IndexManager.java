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

package xyz.vopen.framework.cropdb.collection.operation;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.IndexMeta;
import xyz.vopen.framework.cropdb.index.CropIndexer;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.common.util.IndexUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the index manager for a collection.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class IndexManager implements AutoCloseable {
  private final CropConfig cropConfig;
  private final CropStore<?> cropStore;
  private final String collectionName;
  private final CropMap<Fields, IndexMeta> indexMetaMap;
  private Collection<IndexDescriptor> indexDescriptorCache;

  /**
   * Instantiates a new {@link IndexManager}.
   *
   * @param collectionName the collection name
   * @param cropConfig the crop config
   */
  public IndexManager(String collectionName, CropConfig cropConfig) {
    this.collectionName = collectionName;
    this.cropConfig = cropConfig;
    this.cropStore = cropConfig.getCropStore();
    this.indexMetaMap = getIndexMetaMap();
    initialize();
  }

  /**
   * Checks if an index descriptor already exists on the fields.
   *
   * @param fields the fields
   * @return the boolean
   */
  public boolean hasIndexDescriptor(Fields fields) {
    return !findMatchingIndexDescriptors(fields).isEmpty();
  }

  /**
   * Gets all defined index descriptors for the collection.
   *
   * @return the index descriptors
   */
  public Collection<IndexDescriptor> getIndexDescriptors() {
    if (indexDescriptorCache == null) {
      indexDescriptorCache = listIndexDescriptors();
    }
    return indexDescriptorCache;
  }

  public Collection<IndexDescriptor> findMatchingIndexDescriptors(Fields fields) {
    List<IndexDescriptor> indexDescriptors = new ArrayList<>();

    for (IndexDescriptor indexDescriptor : getIndexDescriptors()) {
      if (indexDescriptor.getIndexFields().startsWith(fields)) {
        indexDescriptors.add(indexDescriptor);
      }
    }

    return indexDescriptors;
  }

  public IndexDescriptor findExactIndexDescriptor(Fields fields) {
    IndexMeta meta = indexMetaMap.get(fields);
    if (meta != null) {
      return meta.getIndexDescriptor();
    }
    return null;
  }

  @Override
  public void close() {
    // close all index maps
    Iterable<IndexMeta> indexMetas = indexMetaMap.values();
    for (IndexMeta indexMeta : indexMetas) {
      if (indexMeta != null && indexMeta.getIndexDescriptor() != null) {
        String indexMapName = indexMeta.getIndexMap();
        CropMap<?, ?> indexMap = cropStore.openMap(indexMapName, Object.class, Object.class);
        indexMap.close();
      }
    }

    // close index meta
    indexMetaMap.close();
  }

  /**
   * Is dirty index boolean.
   *
   * @param fields the fields
   * @return the boolean
   */
  boolean isDirtyIndex(Fields fields) {
    IndexMeta meta = indexMetaMap.get(fields);
    return meta != null && meta.getIsDirty().get();
  }

  /**
   * List index descriptors collection.
   *
   * @return the collection
   */
  Collection<IndexDescriptor> listIndexDescriptors() {
    Set<IndexDescriptor> indexSet = new LinkedHashSet<>();
    Iterable<IndexMeta> iterable = indexMetaMap.values();
    for (IndexMeta indexMeta : iterable) {
      indexSet.add(indexMeta.getIndexDescriptor());
    }
    return Collections.unmodifiableSet(indexSet);
  }

  /**
   * Create index descriptor index descriptor.
   *
   * @param fields the fields
   * @param indexType the index type
   * @return the index descriptor
   */
  IndexDescriptor createIndexDescriptor(Fields fields, String indexType) {
    validateIndexRequest(fields, indexType);
    IndexDescriptor index = new IndexDescriptor(indexType, fields, collectionName);

    IndexMeta indexMeta = new IndexMeta();
    indexMeta.setIndexDescriptor(index);
    indexMeta.setIsDirty(new AtomicBoolean(false));
    indexMeta.setIndexMap(IndexUtils.deriveIndexMapName(index));

    indexMetaMap.put(fields, indexMeta);

    updateIndexDescriptorCache();
    return index;
  }

  /**
   * Drop index descriptor.
   *
   * @param fields the fields
   */
  void dropIndexDescriptor(Fields fields) {
    IndexMeta meta = indexMetaMap.get(fields);
    if (meta != null && meta.getIndexDescriptor() != null) {
      String indexMapName = meta.getIndexMap();
      CropMap<?, ?> indexMap = cropStore.openMap(indexMapName, Object.class, Object.class);
      indexMap.drop();
    }

    indexMetaMap.remove(fields);
    updateIndexDescriptorCache();
  }

  void dropIndexMeta() {
    indexMetaMap.clear();
    indexMetaMap.drop();
  }

  /**
   * Begin indexing.
   *
   * @param fields the fields
   */
  void beginIndexing(Fields fields) {
    markDirty(fields, true);
  }

  /**
   * End indexing.
   *
   * @param fields the fields
   */
  void endIndexing(Fields fields) {
    markDirty(fields, false);
  }

  private void initialize() {
    updateIndexDescriptorCache();
  }

  private void markDirty(Fields fields, boolean dirty) {
    IndexMeta meta = indexMetaMap.get(fields);
    if (meta != null && meta.getIndexDescriptor() != null) {
      meta.getIsDirty().set(dirty);
    }
  }

  private CropMap<Fields, IndexMeta> getIndexMetaMap() {
    String mapName = IndexUtils.deriveIndexMetaMapName(this.collectionName);
    return this.cropStore.openMap(mapName, Fields.class, IndexMeta.class);
  }

  private void updateIndexDescriptorCache() {
    indexDescriptorCache = listIndexDescriptors();
  }

  private void validateIndexRequest(Fields fields, String indexType) {
    CropIndexer indexer = cropConfig.findIndexer(indexType);
    indexer.validateIndex(fields);
  }
}
