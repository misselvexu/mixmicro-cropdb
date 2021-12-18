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

package xyz.vopen.framework.cropdb.collection.meta;

/**
 * Interface to be implemented by database objects that wish to be aware of their meta data.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 1.0
 */
public interface MetadataAware {
  /**
   * Returns the meta data attributes of an object.
   *
   * @return the meta data attributes.
   */
  Attributes getAttributes();

  /**
   * Sets new meta data attributes.
   *
   * @param attributes new meta data attributes.
   */
  void setAttributes(Attributes attributes);
}
