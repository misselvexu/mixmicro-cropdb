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

package xyz.vopen.framework.cropdb.repository;

import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.Lookup;
import xyz.vopen.framework.cropdb.common.RecordStream;

/**
 * A collection of {@link CropId}s of the database records, as a result of a find operation.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
public interface Cursor<T> extends RecordStream<T> {
  /**
   * Gets a filter plan for the query.
   *
   * @return the filter plan
   */
  FindPlan getFindPlan();

  /**
   * Projects the result of one type into an {@link Iterable} of other type.
   *
   * @param <P> the type of the target objects.
   * @param projectionType the projection type.
   * @return java.lang.Iterable of projected objects.
   */
  <P> RecordStream<P> project(Class<P> projectionType);

  /**
   * Performs a left outer join with a foreign cursor with the specified lookup parameters.
   *
   * <p>It performs an equality match on the localString to the foreignString from the objects of
   * the foreign cursor. If an input object does not contain the localString, the join treats the
   * field as having a value of <code>null</code> for matching purposes.
   *
   * @param <Foreign> the type of the foreign object.
   * @param <Joined> the type of the joined object.
   * @param foreignCursor the foreign cursor for the join.
   * @param lookup the lookup parameter for the join operation.
   * @param type the type of the joined record.
   * @return a lazy iterable of joined objects.
   * @since 2.1.0
   */
  <Foreign, Joined> RecordStream<Joined> join(
      Cursor<Foreign> foreignCursor, Lookup lookup, Class<Joined> type);
}
