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

package xyz.vopen.framework.cropdb.mvstore.compat.v3;

import org.h2.mvstore.MVMap;

/**
 * The type Mv map builder.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @since 4.0.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
class MVMapBuilder<K, V> extends MVMap.Builder<K, V> {
  /** Instantiates a new Mv map builder. */
  public MVMapBuilder() {
    setKeyType(new CropDataType());
    setValueType(new CropDataType());
  }
}
