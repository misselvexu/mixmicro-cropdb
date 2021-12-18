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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.vopen.framework.cropdb.store.StoreConfig;
import xyz.vopen.framework.cropdb.store.events.StoreEventListener;
import org.h2.mvstore.FileStore;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents MV store configuration
 *
 * @since 4.0.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@Accessors(fluent = true)
public class MVStoreConfig implements StoreConfig {
  @Getter
  @Setter(AccessLevel.PACKAGE)
  private Set<StoreEventListener> eventListeners;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private String filePath;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private int autoCommitBufferSize;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private char[] encryptionKey;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private Boolean isReadOnly = false;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private boolean compress;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private boolean compressHigh;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private boolean autoCommit;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private boolean autoCompact;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private boolean recoveryMode;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private int cacheSize;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private int cacheConcurrency;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private int pageSplitSize;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private FileStore fileStore;

  MVStoreConfig() {
    eventListeners = new HashSet<>();
  }

  @Override
  public void addStoreEventListener(StoreEventListener listener) {
    eventListeners.add(listener);
  }
}
