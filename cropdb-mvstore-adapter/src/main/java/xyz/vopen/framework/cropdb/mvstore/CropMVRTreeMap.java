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

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.index.BoundingBox;
import xyz.vopen.framework.cropdb.store.CropRTree;
import xyz.vopen.framework.cropdb.store.CropStore;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;

import java.util.Iterator;

/**
 * @since 1.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class CropMVRTreeMap<Key extends BoundingBox, Value> implements CropRTree<Key, Value> {
    private final MVRTreeMap<Key> mvMap;
    private final CropStore<?> cropStore;
    private final MVStore mvStore;

    CropMVRTreeMap(MVRTreeMap<Key> mvMap, CropStore<?> cropStore) {
        this.mvMap = mvMap;
        this.cropStore = cropStore;
        this.mvStore = mvMap.getStore();
    }

    @Override
    public void add(Key key, CropId cropId) {
        if (cropId != null && cropId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(cropId.getIdValue()));
            MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
            try {
                mvMap.add(spatialKey, key);
            } finally {
                mvStore.deregisterVersionUsage(txCounter);
            }
        }
    }

    @Override
    public void remove(Key key, CropId cropId) {
        if (cropId != null && cropId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(cropId.getIdValue()));
            MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
            try {
                mvMap.remove(spatialKey);
            } finally {
                mvStore.deregisterVersionUsage(txCounter);
            }
        }
    }

    @Override
    public RecordStream<CropId> findIntersectingKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        MVRTreeMap.RTreeCursor treeCursor = mvMap.findIntersectingKeys(spatialKey);
        return getRecordStream(treeCursor);
    }

    @Override
    public RecordStream<CropId> findContainedKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        MVRTreeMap.RTreeCursor treeCursor = mvMap.findContainedKeys(spatialKey);
        return getRecordStream(treeCursor);
    }

    @Override
    public long size() {
        return mvMap.sizeAsLong();
    }

    private SpatialKey getKey(Key key, long id) {
        if (key == null) {
            return new SpatialKey(id);
        } else {
            return new SpatialKey(id, key.getMinX(),
                key.getMaxX(), key.getMinY(), key.getMaxY());
        }
    }

    private RecordStream<CropId> getRecordStream(MVRTreeMap.RTreeCursor treeCursor) {
        return RecordStream.fromIterable(() -> new Iterator<CropId>() {
            @Override
            public boolean hasNext() {
                return treeCursor.hasNext();
            }

            @Override
            public CropId next() {
                SpatialKey next = treeCursor.next();
                return CropId.createId(Long.toString(next.getId()));
            }
        });
    }

    @Override
    public void close() {
        cropStore.closeRTree(mvMap.getName());
    }

    @Override
    public void clear() {
        mvMap.clear();
    }

    @Override
    public void drop() {
        mvMap.clear();
        cropStore.closeRTree(mvMap.getName());
        cropStore.removeRTree(mvMap.getName());
    }
}
