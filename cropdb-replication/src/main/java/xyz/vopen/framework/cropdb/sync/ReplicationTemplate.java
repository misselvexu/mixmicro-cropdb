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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.collection.meta.Attributes;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.sync.crdt.LastWriteWinMap;
import xyz.vopen.framework.cropdb.sync.event.ReplicationEvent;
import xyz.vopen.framework.cropdb.sync.event.ReplicationEventBus;
import xyz.vopen.framework.cropdb.sync.event.ReplicationEventListener;
import xyz.vopen.framework.cropdb.sync.message.Connect;
import xyz.vopen.framework.cropdb.sync.message.Disconnect;
import xyz.vopen.framework.cropdb.sync.message.Receipt;
import xyz.vopen.framework.cropdb.sync.event.ReplicationEventType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
@Slf4j
@Getter
public class ReplicationTemplate implements ReplicationOperation {
  private final Config config;
  private MessageFactory messageFactory;
  private MessageTemplate messageTemplate;
  private LastWriteWinMap crdt;
  private FeedJournal feedJournal;

  @Getter(AccessLevel.NONE)
  private BatchChangeScheduler batchChangeScheduler;

  @Getter(AccessLevel.NONE)
  private String replicaId;

  @Getter(AccessLevel.NONE)
  private ReplicaChangeListener replicaChangeListener;

  @Getter(AccessLevel.NONE)
  private AtomicBoolean connected;

  @Getter(AccessLevel.NONE)
  private AtomicBoolean exchangeFlag;

  @Getter(AccessLevel.NONE)
  private AtomicBoolean acceptCheckpoint;

  @Getter(AccessLevel.NONE)
  private ReplicationEventBus eventBus;

  public ReplicationTemplate(Config config) {
    this.config = config;
    initialize();
  }

  public void connect() {
    messageTemplate.openConnection();
    Connect message = messageFactory.createConnect(config, getReplicaId());
    messageTemplate.sendMessage(message);
    eventBus.post(new ReplicationEvent(ReplicationEventType.Started));
  }

  public void setConnected() {
    connected.compareAndSet(false, true);
  }

  public boolean isConnected() {
    return connected.get();
  }

  public void disconnect() {
    Disconnect message = messageFactory.createDisconnect(config, getReplicaId());
    messageTemplate.sendMessage(message);
    stopReplication("User disconnect");
  }

  public void stopReplication(String reason) {
    batchChangeScheduler.stop();
    eventBus.post(new ReplicationEvent(ReplicationEventType.Stopped));
    connected.set(false);
    exchangeFlag.set(false);
    acceptCheckpoint.set(false);
    messageTemplate.closeConnection(reason);
  }

  public void sendChanges() {
    batchChangeScheduler.schedule();
  }

  public void startFeedExchange() {
    this.exchangeFlag.compareAndSet(false, true);
  }

  public boolean shouldExchangeFeed() {
    return exchangeFlag.get();
  }

  public String getReplicaId() {
    if (StringUtils.isNullOrEmpty(replicaId)) {
      Attributes attributes = getAttributes();
      if (!attributes.hasKey(Attributes.REPLICA)) {
        attributes.set(Attributes.REPLICA, UUID.randomUUID().toString());
      }
      replicaId = attributes.get(Attributes.REPLICA);
    }
    return replicaId;
  }

  public void setAcceptCheckpoint() {
    acceptCheckpoint.compareAndSet(false, true);
  }

  public boolean shouldAcceptCheckpoint() {
    return acceptCheckpoint.get();
  }

  @Override
  public CropCollection getCollection() {
    return config.getCollection();
  }

  public void subscribe(ReplicationEventListener listener) {
    eventBus.register(listener);
  }

  public void unsubscribe(ReplicationEventListener listener) {
    eventBus.deregister(listener);
  }

  public void postEvent(ReplicationEvent event) {
    eventBus.post(event);
  }

  public void close() {
    eventBus.close();
    messageTemplate.close();
    batchChangeScheduler.stop();
    this.getCollection().unsubscribe(replicaChangeListener);
  }

  public void collectGarbage(Long ttl) {
    if (ttl != null && ttl > 0) {
      long collectTime = System.currentTimeMillis() - ttl;
      if (crdt != null && crdt.getTombstones() != null) {
        Set<CropId> removeSet = new HashSet<>();
        for (Pair<CropId, Long> entry : crdt.getTombstones().entries()) {
          if (entry.getSecond() < collectTime) {
            removeSet.add(entry.getFirst());
          }
        }

        Receipt garbage = new Receipt();
        for (CropId cropId : removeSet) {
          crdt.getTombstones().remove(cropId);
          garbage.getRemoved().add(cropId.getIdValue());
        }

        feedJournal.accumulate(garbage);
      }
    }
  }

  private void initialize() {
    this.messageFactory = new MessageFactory();
    this.connected = new AtomicBoolean(false);
    this.exchangeFlag = new AtomicBoolean(false);
    this.acceptCheckpoint = new AtomicBoolean(false);
    this.eventBus = new ReplicationEventBus();
    this.messageTemplate = new MessageTemplate(config, this);
    this.crdt = createReplicatedDataType();
    this.feedJournal = new FeedJournal(this);
    this.batchChangeScheduler = new BatchChangeScheduler(this);
    this.replicaChangeListener = new ReplicaChangeListener(this, messageTemplate);
    this.getCollection().subscribe(replicaChangeListener);
  }
}
