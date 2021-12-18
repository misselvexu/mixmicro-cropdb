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

package xyz.vopen.framework.cropdb.sync;

import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventInfo;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventListener;
import xyz.vopen.framework.cropdb.sync.crdt.LastWriteWinState;
import xyz.vopen.framework.cropdb.sync.event.ReplicationEvent;
import xyz.vopen.framework.cropdb.sync.event.ReplicationEventType;
import xyz.vopen.framework.cropdb.sync.message.DataGateFeed;
import xyz.vopen.framework.cropdb.common.Constants;

import java.util.Collections;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
@Slf4j
class ReplicaChangeListener implements CollectionEventListener {
  private final ReplicationTemplate replicationTemplate;
  private final MessageTemplate messageTemplate;

  public ReplicaChangeListener(
      ReplicationTemplate replicationTemplate, MessageTemplate messageTemplate) {
    this.replicationTemplate = replicationTemplate;
    this.messageTemplate = messageTemplate;
  }

  @Override
  public void onEvent(CollectionEventInfo<?> eventInfo) {
    try {
      if (eventInfo != null) {
        if (!Constants.REPLICATOR.equals(eventInfo.getOriginator())) {
          switch (eventInfo.getEventType()) {
            case Insert:
            case Update:
              Document document = (Document) eventInfo.getItem();
              handleModifyEvent(document);
              break;
            case Remove:
              document = (Document) eventInfo.getItem();
              handleRemoveEvent(document);
              break;
            case IndexStart:
            case IndexEnd:
              break;
          }
        }
      }
    } catch (Exception e) {
      log.error("Error while processing collection event", e);
      replicationTemplate.postEvent(new ReplicationEvent(ReplicationEventType.Error, e));
    }
  }

  private void handleRemoveEvent(Document document) {
    LastWriteWinState state = new LastWriteWinState();
    CropId cropId = document.getId();
    Long deleteTime = document.getLastModifiedSinceEpoch();

    if (replicationTemplate.getCrdt() != null) {
      replicationTemplate.getCrdt().getTombstones().put(cropId, deleteTime);
      state.setTombstones(Collections.singletonMap(cropId.getIdValue(), deleteTime));
      sendFeed(state);
    }
  }

  private void handleModifyEvent(Document document) {
    LastWriteWinState state = new LastWriteWinState();
    state.setChanges(Collections.singleton(document));
    sendFeed(state);
  }

  private void sendFeed(LastWriteWinState state) {
    if (replicationTemplate.shouldExchangeFeed() && messageTemplate != null) {
      MessageFactory factory = replicationTemplate.getMessageFactory();
      DataGateFeed feedMessage =
          factory.createFeedMessage(
              replicationTemplate.getConfig(), replicationTemplate.getReplicaId(), state);

      FeedJournal journal = replicationTemplate.getFeedJournal();
      messageTemplate.sendMessage(feedMessage);
      journal.write(state);
    }
  }
}
