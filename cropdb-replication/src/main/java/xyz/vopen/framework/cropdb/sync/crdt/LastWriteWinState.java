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

package xyz.vopen.framework.cropdb.sync.crdt;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.sync.module.DocumentDeserializer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
@Data
public class LastWriteWinState {
  @JsonDeserialize(contentUsing = DocumentDeserializer.class)
  private Set<Document> changes;

  private Map<String, Long> tombstones;

  public LastWriteWinState() {
    changes = new LinkedHashSet<>();
    tombstones = new LinkedHashMap<>();
  }
}
