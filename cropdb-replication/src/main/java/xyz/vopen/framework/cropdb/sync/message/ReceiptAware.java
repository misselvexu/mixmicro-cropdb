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

package xyz.vopen.framework.cropdb.sync.message;

import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.sync.crdt.LastWriteWinState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
public interface ReceiptAware extends DataGateMessage {
  LastWriteWinState getFeed();

  default Receipt calculateReceipt() {
    Set<String> added = new HashSet<>();
    Set<String> removed = new HashSet<>();

    if (getFeed() != null) {
      if (getFeed().getChanges() != null) {
        for (Document change : getFeed().getChanges()) {
          added.add(change.getId().getIdValue());
        }
      }

      if (getFeed().getTombstones() != null) {
        for (Map.Entry<String, Long> entry : getFeed().getTombstones().entrySet()) {
          removed.add(entry.getKey());
        }
      }
    }

    return new Receipt(added, removed);
  }
}
