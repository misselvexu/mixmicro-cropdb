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
import xyz.vopen.framework.cropdb.sync.message.DataGateMessage;
import xyz.vopen.framework.cropdb.sync.net.DataGateSocket;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
@Slf4j
public class MessageTemplate implements AutoCloseable {
  private final Config config;
  private final ReplicationTemplate replica;
  private DataGateSocket dataGateSocket;
  private MessageDispatcher dispatcher;

  public MessageTemplate(Config config, ReplicationTemplate replica) {
    this.config = config;
    this.replica = replica;
  }

  public void sendMessage(DataGateMessage message) {
    if (dataGateSocket != null && dataGateSocket.isConnected()) {
      if (!dataGateSocket.sendMessage(message)) {
        throw new ReplicationException("failed to deliver message " + message, true);
      }
    }
  }

  public void openConnection() {
    try {
      dataGateSocket = new DataGateSocket(config);
      dispatcher = new MessageDispatcher(config, replica);

      dataGateSocket.setListener(dispatcher);
      dataGateSocket.startConnect();
    } catch (Exception e) {
      log.error("Error while establishing connection from {}", getReplicaId(), e);
      throw new ReplicationException("failed to open connection to server", e, true);
    }
  }

  public void closeConnection(String reason) {
    if (dataGateSocket != null) {
      dataGateSocket.stopConnect(reason);
    }
  }

  @Override
  public void close() {
    if (dataGateSocket != null) {
      dataGateSocket.stopConnect("normal close");
    }

    if (dispatcher != null) {
      dispatcher.close();
    }
  }

  private String getReplicaId() {
    return replica.getReplicaId();
  }
}
