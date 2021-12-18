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

import lombok.Getter;
import lombok.Setter;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.DBNull;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.store.CropMap;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an index map.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class IndexMap {
  private CropMap<DBValue, ?> cropMap;
  private NavigableMap<DBValue, ?> navigableMap;

  @Getter @Setter private boolean reverseScan;

  /**
   * Instantiates a new {@link IndexMap}.
   *
   * @param cropMap the crop map
   */
  public IndexMap(CropMap<DBValue, ?> cropMap) {
    this.cropMap = cropMap;
  }

  /**
   * Instantiates a new {@link IndexMap}.
   *
   * @param navigableMap the navigable map
   */
  public IndexMap(NavigableMap<DBValue, ?> navigableMap) {
    this.navigableMap = navigableMap;
  }

  /**
   * Get the largest key that is smaller than the given key, or null if no such key exists.
   *
   * @param <T> the type parameter
   * @param key the key
   * @return the t
   */
  public <T extends Comparable<T>> T lowerKey(T key) {
    DBValue dbKey = key == null ? DBNull.getInstance() : new DBValue(key);
    if (!reverseScan) {
      if (cropMap != null) {
        dbKey = cropMap.lowerKey(dbKey);
      } else if (navigableMap != null) {
        dbKey = navigableMap.lowerKey(dbKey);
      }
    } else {
      if (cropMap != null) {
        dbKey = cropMap.higherKey(dbKey);
      } else if (navigableMap != null) {
        dbKey = navigableMap.higherKey(dbKey);
      }
    }

    return dbKey == null || dbKey instanceof DBNull ? null : (T) dbKey.getValue();
  }

  /**
   * Get the smallest key that is larger than the given key, or null if no such key exists.
   *
   * @param <T> the type parameter
   * @param key the key
   * @return the t
   */
  public <T extends Comparable<T>> T higherKey(T key) {
    DBValue dbKey = key == null ? DBNull.getInstance() : new DBValue(key);
    if (!reverseScan) {
      if (cropMap != null) {
        dbKey = cropMap.higherKey(dbKey);
      } else if (navigableMap != null) {
        dbKey = navigableMap.higherKey(dbKey);
      }
    } else {
      if (cropMap != null) {
        dbKey = cropMap.lowerKey(dbKey);
      } else if (navigableMap != null) {
        dbKey = navigableMap.lowerKey(dbKey);
      }
    }

    return dbKey == null || dbKey instanceof DBNull ? null : (T) dbKey.getValue();
  }

  /**
   * Get the smallest key that is larger or equal to this key.
   *
   * @param <T> the type parameter
   * @param key the key
   * @return the t
   */
  public <T extends Comparable<T>> T ceilingKey(T key) {
    DBValue dbKey = key == null ? DBNull.getInstance() : new DBValue(key);
    if (!reverseScan) {
      if (cropMap != null) {
        dbKey = cropMap.ceilingKey(dbKey);
      } else if (navigableMap != null) {
        dbKey = navigableMap.ceilingKey(dbKey);
      }
    } else {
      if (cropMap != null) {
        dbKey = cropMap.floorKey(dbKey);
      } else if (navigableMap != null) {
        dbKey = navigableMap.floorKey(dbKey);
      }
    }

    return dbKey == null || dbKey instanceof DBNull ? null : (T) dbKey.getValue();
  }

  /**
   * Get the largest key that is smaller or equal to this key.
   *
   * @param <T> the type parameter
   * @param key the key
   * @return the t
   */
  public <T extends Comparable<T>> T floorKey(T key) {
    DBValue dbKey = key == null ? DBNull.getInstance() : new DBValue(key);
    if (!reverseScan) {
      if (cropMap != null) {
        dbKey = cropMap.floorKey(dbKey);
      } else if (navigableMap != null) {
        dbKey = navigableMap.floorKey(dbKey);
      }
    } else {
      if (cropMap != null) {
        dbKey = cropMap.ceilingKey(dbKey);
      } else if (navigableMap != null) {
        dbKey = navigableMap.ceilingKey(dbKey);
      }
    }

    return dbKey == null || dbKey instanceof DBNull ? null : (T) dbKey.getValue();
  }

  /**
   * Gets the value mapped with the specified key or <code>null</code> otherwise.
   *
   * @param comparable the comparable
   * @return the object
   */
  public Object get(Comparable<?> comparable) {
    DBValue dbKey = comparable == null ? DBNull.getInstance() : new DBValue(comparable);
    if (cropMap != null) {
      return cropMap.get(dbKey);
    } else if (navigableMap != null) {
      return navigableMap.get(dbKey);
    }
    return null;
  }

  /**
   * Returns the iterable entries of the indexed items.
   *
   * @return the iterable
   */
  public Iterable<? extends Pair<Comparable<?>, ?>> entries() {
    if (cropMap != null) {
      Iterator<? extends Pair<DBValue, ?>> entryIterator;
      if (!reverseScan) {
        entryIterator = cropMap.entries().iterator();
      } else {
        entryIterator = cropMap.reversedEntries().iterator();
      }

      return (Iterable<Pair<Comparable<?>, ?>>)
          () ->
              new Iterator<Pair<Comparable<?>, ?>>() {
                @Override
                public boolean hasNext() {
                  return entryIterator.hasNext();
                }

                @Override
                public Pair<Comparable<?>, ?> next() {
                  Pair<DBValue, ?> next = entryIterator.next();
                  DBValue dbKey = next.getFirst();
                  if (dbKey instanceof DBNull) {
                    return new Pair<>(null, next.getSecond());
                  } else {
                    return new Pair<>(dbKey.getValue(), next.getSecond());
                  }
                }
              };
    } else if (navigableMap != null) {
      Iterator<? extends Map.Entry<DBValue, ?>> entryIterator;
      if (reverseScan) {
        entryIterator = navigableMap.descendingMap().entrySet().iterator();
      } else {
        entryIterator = navigableMap.entrySet().iterator();
      }

      return (Iterable<Pair<Comparable<?>, ?>>)
          () ->
              new Iterator<Pair<Comparable<?>, ?>>() {
                @Override
                public boolean hasNext() {
                  return entryIterator.hasNext();
                }

                @Override
                public Pair<Comparable<?>, ?> next() {
                  Map.Entry<DBValue, ?> next = entryIterator.next();
                  DBValue dbKey = next.getKey();
                  if (dbKey instanceof DBNull) {
                    return new Pair<>(null, next.getValue());
                  } else {
                    return new Pair<>(dbKey.getValue(), next.getValue());
                  }
                }
              };
    }
    return Collections.EMPTY_SET;
  }

  /**
   * Gets the terminal crop ids from this map.
   *
   * @return the terminal crop ids
   */
  public List<CropId> getTerminalCropIds() {
    List<CropId> terminalResult = new CopyOnWriteArrayList<>();

    // scan each entry of the navigable map and collect all terminal crop-ids
    for (Pair<Comparable<?>, ?> entry : entries()) {
      // if the value is terminal, collect all crop-ids
      if (entry.getSecond() instanceof List) {
        List<CropId> cropIds = (List<CropId>) entry.getSecond();
        terminalResult.addAll(cropIds);
      }

      // if the value is not terminal, scan recursively
      if (entry.getSecond() instanceof NavigableMap) {
        NavigableMap<DBValue, ?> subMap = (NavigableMap<DBValue, ?>) entry.getSecond();
        IndexMap indexMap = new IndexMap(subMap);
        List<CropId> cropIds = indexMap.getTerminalCropIds();
        terminalResult.addAll(cropIds);
      }
    }

    return terminalResult;
  }
}
