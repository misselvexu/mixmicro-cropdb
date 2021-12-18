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

package xyz.vopen.framework.cropdb.store;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.RecordStream;

/**
 * Represents an R-Tree in the crop database.
 *
 * @param <Key> the type parameter
 * @param <Value> the type parameter
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0
 */
public interface CropRTree<Key, Value> extends AutoCloseable {
  /**
   * Adds a key to the rtree.
   *
   * @param key the key
   * @param cropId the crop id
   */
  void add(Key key, CropId cropId);

  /**
   * Removes a key from the rtree.
   *
   * @param key the key
   * @param cropId the crop id
   */
  void remove(Key key, CropId cropId);

  /**
   * Finds the intersecting keys from the rtree.
   *
   * @param key the key
   * @return the record stream
   */
  RecordStream<CropId> findIntersectingKeys(Key key);

  /**
   * Finds the contained keys from the rtree.
   *
   * @param key the key
   * @return the record stream
   */
  RecordStream<CropId> findContainedKeys(Key key);

  /**
   * Gets the size of the rtree.
   *
   * @return the size
   */
  long size();

  /** Closes this {@link CropRTree} instance. */
  void close();

  /** Clears the data. */
  void clear();

  /** Drops this instance. */
  void drop();
}
