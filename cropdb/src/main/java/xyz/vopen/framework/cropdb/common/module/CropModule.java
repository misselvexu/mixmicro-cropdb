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

package xyz.vopen.framework.cropdb.common.module;

import xyz.vopen.framework.cropdb.common.util.Iterables;

import java.util.Set;

/**
 * Represents a crop plugin modules which may contains one or more crop plugins.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface CropModule {
  /**
   * Creates a {@link CropModule} from a set of {@link CropPlugin}s.
   *
   * @param plugins the plugins
   * @return the crop module
   */
  static CropModule module(CropPlugin... plugins) {
    return () -> Iterables.setOf(plugins);
  }

  /**
   * Returns the set of {@link CropPlugin} encapsulated by this module.
   *
   * @return the set
   */
  Set<CropPlugin> plugins();
}
