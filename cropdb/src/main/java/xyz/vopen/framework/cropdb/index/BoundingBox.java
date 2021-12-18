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

package xyz.vopen.framework.cropdb.index;

import java.io.Serializable;

/**
 * Represents a bounding box for spatial indexing.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public interface BoundingBox extends Serializable {
  /**
   * Gets min x.
   *
   * @return the min x
   */
  float getMinX();

  /**
   * Gets max x.
   *
   * @return the max x
   */
  float getMaxX();

  /**
   * Gets min y.
   *
   * @return the min y
   */
  float getMinY();

  /**
   * Gets max y.
   *
   * @return the max y
   */
  float getMaxY();
}