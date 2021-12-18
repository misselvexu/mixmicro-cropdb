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

package xyz.vopen.framework.cropdb.collection.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a collection event data.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEventInfo<T> {
  /**
   * Specifies the item triggering the event.
   *
   * @param item the item that changed.
   * @return the item.
   */
  private T item;

  /**
   * Specifies the event type.
   *
   * @param eventType the type of the event.
   * @return the type of the event.
   */
  private EventType eventType;

  /**
   * Specifies the unix timestamp of the change.
   *
   * @param timestamp the unix timestamp of the change.
   * @return the unix timestamp of the change.
   */
  private long timestamp;

  /**
   * Specifies the name of the originator who has initiated this event.
   *
   * @param originator name of originator of the event.
   * @return name of the originator.
   * @since 4.0.0
   */
  private String originator;

  public CollectionEventInfo(EventType eventType) {
    this.eventType = eventType;
  }
}
