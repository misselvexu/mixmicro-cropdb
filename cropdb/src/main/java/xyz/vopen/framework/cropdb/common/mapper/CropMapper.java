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

import xyz.vopen.framework.cropdb.common.module.CropPlugin;

/**
 * Represents a mapper which will convert a object of one type to an object of another type.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0
 */
public interface CropMapper extends CropPlugin {
  /**
   * Converts an object of type <code>Source</code> to an object of type <code>Target</code>.
   *
   * @param <Source> the type parameter
   * @param <Target> the type parameter
   * @param source the source
   * @param type the type
   * @return the target
   */
  <Source, Target> Target convert(Source source, Class<Target> type);

  /**
   * Checks if the provided type is registered as a value type.
   *
   * @param type the type
   * @return the boolean
   */
  boolean isValueType(Class<?> type);

  /**
   * Checks if an object is of a value type.
   *
   * @param object the object
   * @return the boolean
   */
  boolean isValue(Object object);
}
