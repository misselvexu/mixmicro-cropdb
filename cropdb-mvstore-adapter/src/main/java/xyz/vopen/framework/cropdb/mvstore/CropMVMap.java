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

import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since 1.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class CropMVMap<Key, Value> implements CropMap<Key, Value> {
  private final MVMap<Key, Value> mvMap;
  private final CropStore<?> cropStore;
  private final MVStore mvStore;
  private final AtomicBoolean droppedFlag;
  private final AtomicBoolean closedFlag;

  CropMVMap(MVMap<Key, Value> mvMap, CropStore<?> cropStore) {
    this.mvMap = mvMap;
    this.cropStore = cropStore;
    this.mvStore = mvMap.getStore();
    this.closedFlag = new AtomicBoolean(false);
    this.droppedFlag = new AtomicBoolean(false);
  }

  @Override
  public boolean containsKey(Key key) {
    return mvMap.containsKey(key);
  }

  @Override
  public Value get(Key key) {
    return mvMap.get(key);
  }

  @Override
  public CropStore<?> getStore() {
    return cropStore;
  }

  @Override
  public void clear() {
    MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
    try {
      mvMap.clear();
      updateLastModifiedTime();
    } finally {
      mvStore.deregisterVersionUsage(txCounter);
    }
  }

  @Override
  public String getName() {
    return mvMap.getName();
  }

  @Override
  public RecordStream<Value> values() {
    return RecordStream.fromIterable(mvMap.values());
  }

  @Override
  public Value remove(Key key) {
    MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
    try {
      Value value = mvMap.remove(key);
      updateLastModifiedTime();
      return value;
    } finally {
      mvStore.deregisterVersionUsage(txCounter);
    }
  }

  @Override
  public RecordStream<Key> keys() {
    return RecordStream.fromIterable(mvMap.keySet());
  }

  @Override
  public void put(Key key, Value value) {
    ValidationUtils.notNull(value, "value cannot be null");
    MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
    try {
      mvMap.put(key, value);
      updateLastModifiedTime();
    } finally {
      mvStore.deregisterVersionUsage(txCounter);
    }
  }

  @Override
  public long size() {
    return mvMap.sizeAsLong();
  }

  @Override
  public Value putIfAbsent(Key key, Value value) {
    ValidationUtils.notNull(value, "value cannot be null");
    MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
    try {
      Value v = mvMap.putIfAbsent(key, value);
      updateLastModifiedTime();
      return v;
    } finally {
      mvStore.deregisterVersionUsage(txCounter);
    }
  }

  @Override
  public RecordStream<Pair<Key, Value>> entries() {
    return () ->
        new Iterator<Pair<Key, Value>>() {
          final Iterator<Map.Entry<Key, Value>> entryIterator = mvMap.entrySet().iterator();

          @Override
          public boolean hasNext() {
            return entryIterator.hasNext();
          }

          @Override
          public Pair<Key, Value> next() {
            Map.Entry<Key, Value> entry = entryIterator.next();
            return new Pair<>(entry.getKey(), entry.getValue());
          }
        };
  }

  @Override
  public RecordStream<Pair<Key, Value>> reversedEntries() {
    return () -> new ReverseIterator<>(mvMap);
  }

  @Override
  public Key higherKey(Key key) {
    return mvMap.higherKey(key);
  }

  @Override
  public Key ceilingKey(Key key) {
    return mvMap.ceilingKey(key);
  }

  @Override
  public Key lowerKey(Key key) {
    return mvMap.lowerKey(key);
  }

  @Override
  public Key floorKey(Key key) {
    return mvMap.floorKey(key);
  }

  @Override
  public boolean isEmpty() {
    return mvMap.isEmpty();
  }

  @Override
  public void drop() {
    if (!droppedFlag.get()) {
      droppedFlag.compareAndSet(false, true);
      closedFlag.compareAndSet(false, true);

      MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
      try {
        cropStore.closeMap(getName());
        cropStore.removeMap(getName());
      } finally {
        mvStore.deregisterVersionUsage(txCounter);
      }
    }
  }

  @Override
  public void close() {
    if (!closedFlag.get() && !droppedFlag.get()) {
      closedFlag.compareAndSet(false, true);
      cropStore.closeMap(getName());
    }
  }
}
