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
import lombok.Setter;
import xyz.vopen.framework.cropdb.common.module.CropModule;
import xyz.vopen.framework.cropdb.common.module.CropPlugin;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.store.StoreModule;
import xyz.vopen.framework.cropdb.common.util.Iterables;

import java.util.Set;

/**
 * A {@link CropModule} which provides h2's mvstore as a storage engine.
 *
 * @since 4.0.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class MVStoreModule implements StoreModule {
  @Setter(AccessLevel.PACKAGE)
  private MVStoreConfig storeConfig;

  public MVStoreModule(String path) {
    this.storeConfig = new MVStoreConfig();
    this.storeConfig.filePath(path);
  }

  @Override
  public Set<CropPlugin> plugins() {
    return Iterables.setOf(getStore());
  }

  public static MVStoreModuleBuilder withConfig() {
    return new MVStoreModuleBuilder();
  }

  public CropStore<?> getStore() {
    CropMVStore store = new CropMVStore();
    store.setStoreConfig(storeConfig);
    return store;
  }
}
