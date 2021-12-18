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

package xyz.vopen.framework.cropdb.sync.handlers;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.sync.FeedJournal;
import xyz.vopen.framework.cropdb.sync.MessageFactory;
import xyz.vopen.framework.cropdb.sync.MessageTemplate;
import xyz.vopen.framework.cropdb.sync.ReplicationTemplate;
import xyz.vopen.framework.cropdb.sync.crdt.LastWriteWinMap;
import xyz.vopen.framework.cropdb.sync.crdt.LastWriteWinState;
import xyz.vopen.framework.cropdb.sync.message.DataGateFeed;
import xyz.vopen.framework.cropdb.sync.message.Receipt;

import java.util.HashMap;
import java.util.HashSet;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
public interface JournalAware {
  ReplicationTemplate getReplicationTemplate();

  default FeedJournal getJournal() {
    return getReplicationTemplate().getFeedJournal();
  }

  default void retryFailed(Receipt receipt) {
    if (shouldRetry(receipt)) {
      LastWriteWinState state = createState(receipt);

      MessageFactory factory = getReplicationTemplate().getMessageFactory();
      DataGateFeed feedMessage =
          factory.createFeedMessage(
              getReplicationTemplate().getConfig(), getReplicationTemplate().getReplicaId(), state);

      MessageTemplate messageTemplate = getReplicationTemplate().getMessageTemplate();
      messageTemplate.sendMessage(feedMessage);
    }
  }

  default LastWriteWinState createState(Receipt receipt) {
    LastWriteWinState state = new LastWriteWinState();
    state.setTombstones(new HashMap<>());
    state.setChanges(new HashSet<>());

    CropCollection collection = getReplicationTemplate().getCollection();
    LastWriteWinMap crdt = getReplicationTemplate().getCrdt();

    if (receipt != null) {
      if (receipt.getAdded() != null) {
        for (String id : receipt.getAdded()) {
          Document document = collection.getById(CropId.createId(id));
          if (document != null) {
            state.getChanges().add(document);
          }
        }
      }

      if (receipt.getRemoved() != null) {
        for (String id : receipt.getRemoved()) {
          Long timestamp = crdt.getTombstones().get(CropId.createId(id));
          if (timestamp != null) {
            state.getTombstones().put(id, timestamp);
          }
        }
      }
    }

    return state;
  }

  default boolean shouldRetry(Receipt receipt) {
    if (receipt == null) return false;
    if (receipt.getAdded() == null) return false;
    if (receipt.getAdded() == null && receipt.getRemoved() == null) return false;
    return !receipt.getAdded().isEmpty() || !receipt.getRemoved().isEmpty();
  }
}
