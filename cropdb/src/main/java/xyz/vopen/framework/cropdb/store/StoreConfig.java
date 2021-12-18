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

import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.store.events.StoreEventListener;

/**
 * Represents a {@link CropStore} configuration.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0
 */
public interface StoreConfig {
  /**
   * Gets file path for the store.
   *
   * @return the file path
   */
  String filePath();

  /**
   * Indicates if the {@link CropStore} is a readonly store.
   *
   * @return <code>true</code>, if readonly store; otherwise <code>false</code>.
   */
  Boolean isReadOnly();

  /**
   * Adds a {@link StoreEventListener} instance and subscribe it to store event.
   *
   * @param listener the listener
   */
  void addStoreEventListener(StoreEventListener listener);

  /**
   * Indicates if the {@link CropStore} is an in-memory store.
   *
   * @return <code>true</code>, if in-memory store; otherwise <code>false</code>.
   */
  default boolean isInMemory() {
    return StringUtils.isNullOrEmpty(filePath());
  }
}
