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

package xyz.vopen.framework.cropdb.common.mapper;

import xyz.vopen.framework.cropdb.collection.Document;

/**
 * An object that serializes itself to a {@link Document} and vice versa.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.0
 */
public interface Mappable {
  /**
   * Writes the instance data to a {@link Document} and returns it.
   *
   * @param mapper the {@link CropMapper} instance used.
   * @return the document generated.
   */
  Document write(CropMapper mapper);

  /**
   * Reads the `document` and populate all fields of this instance.
   *
   * @param mapper the {@link CropMapper} instance used.
   * @param document the document.
   */
  void read(CropMapper mapper, Document document);
}
