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
import xyz.vopen.framework.cropdb.sync.event.ReplicationEvent;
import xyz.vopen.framework.cropdb.sync.event.ReplicationEventListener;
import xyz.vopen.framework.cropdb.sync.event.ReplicationEventType;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
@Slf4j
public final class Replica implements AutoCloseable {
  private final ReplicationTemplate replicationTemplate;

  Replica(Config config) {
    this.replicationTemplate = new ReplicationTemplate(config);
  }

  public static ReplicaBuilder builder() {
    return new ReplicaBuilder();
  }

  public void connect() {
    try {
      replicationTemplate.connect();
    } catch (Exception e) {
      log.error("Error while connecting the replica {}", getReplicaId(), e);
      replicationTemplate.postEvent(new ReplicationEvent(ReplicationEventType.Error, e));
      if (e instanceof ReplicationException) {
        throw e;
      }
      throw new ReplicationException("failed to open connection", e, true);
    }
  }

  public void disconnect() {
    try {
      replicationTemplate.disconnect();
    } catch (Exception e) {
      replicationTemplate.postEvent(new ReplicationEvent(ReplicationEventType.Error, e));
      log.error("Error while disconnecting the replica {}", getReplicaId(), e);
      if (e instanceof ReplicationException) {
        throw e;
      }
      throw new ReplicationException("failed to disconnect the replica", e, true);
    }
  }

  public void subscribe(ReplicationEventListener listener) {
    replicationTemplate.subscribe(listener);
  }

  public void unsubscribe(ReplicationEventListener listener) {
    replicationTemplate.unsubscribe(listener);
  }

  private String getReplicaId() {
    return replicationTemplate.getReplicaId();
  }

  public boolean isConnected() {
    return replicationTemplate.isConnected();
  }

  @Override
  public void close() {
    replicationTemplate.stopReplication("Normal shutdown");
    replicationTemplate.close();
  }
}
