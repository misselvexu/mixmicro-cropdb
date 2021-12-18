/*
 * Copyright (c) 2019-2020. Crop author or authors.
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

package xyz.vopen.framework.cropdb.mvstore;

import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.index.BoundingBox;
import xyz.vopen.framework.cropdb.store.AbstractCropStore;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropRTree;
import xyz.vopen.framework.cropdb.store.events.StoreEventListener;
import xyz.vopen.framework.cropdb.store.events.StoreEvents;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 1.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@Slf4j
public class CropMVStore extends AbstractCropStore<MVStoreConfig> {
  private MVStore mvStore;
  private final Map<String, CropMap<?, ?>> cropMapRegistry;
  private final Map<String, CropRTree<?, ?>> cropRTreeMapRegistry;

  public CropMVStore() {
    super();
    this.cropMapRegistry = new ConcurrentHashMap<>();
    this.cropRTreeMapRegistry = new ConcurrentHashMap<>();
  }

  @Override
  public void openOrCreate() {
    this.mvStore = MVStoreUtils.openOrCreate(getStoreConfig());
    initEventBus();
    alert(StoreEvents.Opened);
  }

  @Override
  public boolean isClosed() {
    return mvStore == null || mvStore.isClosed();
  }

  @Override
  public boolean hasUnsavedChanges() {
    return mvStore != null && mvStore.hasUnsavedChanges();
  }

  @Override
  public boolean isReadOnly() {
    return mvStore.isReadOnly();
  }

  @Override
  public void commit() {
    mvStore.commit();
    alert(StoreEvents.Commit);
  }

  @Override
  public void close() {
    if (getStoreConfig().autoCompact()) {
      compact();
    }

    // close crop maps
    for (CropMap<?, ?> cropMap : cropMapRegistry.values()) {
      cropMap.close();
    }

    for (CropRTree<?, ?> rTree : cropRTreeMapRegistry.values()) {
      rTree.close();
    }

    cropMapRegistry.clear();
    cropRTreeMapRegistry.clear();

    mvStore.close();
    alert(StoreEvents.Closed);
    eventBus.close();
  }

  @Override
  public boolean hasMap(String mapName) {
    return mvStore.hasMap(mapName);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <Key, Value> CropMap<Key, Value> openMap(
      String mapName, Class<?> keyType, Class<?> valueType) {
    if (cropMapRegistry.containsKey(mapName)) {
      return (CropMVMap<Key, Value>) cropMapRegistry.get(mapName);
    }

    MVMap<Key, Value> mvMap = mvStore.openMap(mapName);
    CropMVMap<Key, Value> cropMVMap = new CropMVMap<>(mvMap, this);
    cropMapRegistry.put(mapName, cropMVMap);
    return cropMVMap;
  }

  @Override
  public void closeMap(String mapName) {
    if (!StringUtils.isNullOrEmpty(mapName)) {
      cropMapRegistry.remove(mapName);
    }
  }

  @Override
  public void closeRTree(String rTreeName) {
    if (!StringUtils.isNullOrEmpty(rTreeName)) {
      cropRTreeMapRegistry.remove(rTreeName);
    }
  }

  @Override
  public void removeMap(String name) {
    MVMap<?, ?> mvMap = mvStore.openMap(name);
    mvStore.removeMap(mvMap);
    getCatalog().remove(name);
    cropMapRegistry.remove(name);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <Key extends BoundingBox, Value> CropRTree<Key, Value> openRTree(
      String mapName, Class<?> keyType, Class<?> valueType) {
    if (cropRTreeMapRegistry.containsKey(mapName)) {
      return (CropMVRTreeMap) cropRTreeMapRegistry.get(mapName);
    }

    MVRTreeMap<Value> map = mvStore.openMap(mapName, new MVRTreeMap.Builder<>());
    CropMVRTreeMap<Key, Value> cropMVRTreeMap = new CropMVRTreeMap(map, this);
    cropRTreeMapRegistry.put(mapName, cropMVRTreeMap);
    return cropMVRTreeMap;
  }

  @Override
  public String getStoreVersion() {
    return "MVStore/" + org.h2.engine.Constants.VERSION;
  }

  public void compact() {
    mvStore.compactMoveChunks();
  }

  private void initEventBus() {
    if (getStoreConfig().eventListeners() != null) {
      for (StoreEventListener eventListener : getStoreConfig().eventListeners()) {
        eventBus.register(eventListener);
      }
    }
  }
}
