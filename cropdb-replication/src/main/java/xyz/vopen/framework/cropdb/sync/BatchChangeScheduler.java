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

import xyz.vopen.framework.cropdb.sync.crdt.LastWriteWinState;
import xyz.vopen.framework.cropdb.sync.message.BatchChangeContinue;
import xyz.vopen.framework.cropdb.sync.message.BatchChangeEnd;
import xyz.vopen.framework.cropdb.sync.message.BatchChangeStart;

import java.util.Timer;
import java.util.TimerTask;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
class BatchChangeScheduler {
  private Timer timer;
  private final ReplicationTemplate replica;
  private final MessageFactory factory;
  private final MessageTemplate messageTemplate;
  private final FeedJournal journal;

  public BatchChangeScheduler(ReplicationTemplate replica) {
    this.replica = replica;
    this.factory = replica.getMessageFactory();
    this.messageTemplate = replica.getMessageTemplate();
    this.journal = replica.getFeedJournal();
  }

  public void schedule() {
    if (replica.isConnected()) {
      Long lastSyncTime = replica.getLastSyncTime();

      BatchChangeStart message = createStart(factory, lastSyncTime);
      messageTemplate.sendMessage(message);
      journal.write(message.getFeed());

      timer = new Timer();
      timer.scheduleAtFixedRate(
          new TimerTask() {
            boolean hasMore = true;
            int start = replica.getConfig().getChunkSize();

            @Override
            public void run() {
              LastWriteWinState state =
                  replica
                      .getCrdt()
                      .getChangesSince(lastSyncTime, start, replica.getConfig().getChunkSize());
              if (state.getChanges().size() == 0 && state.getTombstones().size() == 0) {
                hasMore = false;
              }

              if (hasMore) {
                BatchChangeContinue message =
                    factory.createChangeContinue(
                        replica.getConfig(), replica.getReplicaId(), "", state);

                messageTemplate.sendMessage(message);
                journal.write(state);
                start = start + replica.getConfig().getChunkSize();
              }

              if (!hasMore) {
                timer.cancel();
              }
            }
          },
          0,
          replica.getConfig().getDebounce());

      BatchChangeEnd endMessage =
          factory.createChangeEnd(replica.getConfig(), replica.getReplicaId(), "", lastSyncTime);
      messageTemplate.sendMessage(endMessage);
    }
  }

  public void stop() {
    if (timer != null) {
      timer.cancel();
    }
  }

  private BatchChangeStart createStart(MessageFactory factory, Long lastSyncTime) {
    BatchChangeStart startMessage =
        factory.createChangeStart(replica.getConfig(), replica.getReplicaId(), "");

    LastWriteWinState state =
        replica.getCrdt().getChangesSince(lastSyncTime, 0, replica.getConfig().getChunkSize());

    startMessage.setFeed(state);
    return startMessage;
  }
}
