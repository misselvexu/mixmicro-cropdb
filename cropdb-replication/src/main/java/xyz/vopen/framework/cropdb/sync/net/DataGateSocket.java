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

package xyz.vopen.framework.cropdb.sync.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import xyz.vopen.framework.cropdb.sync.Config;
import xyz.vopen.framework.cropdb.sync.ReplicationException;
import xyz.vopen.framework.cropdb.sync.message.DataGateMessage;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
@Slf4j
public class DataGateSocket {
  private static final int RECONNECT_INTERVAL = 10 * 1000;
  private static final long RECONNECT_MAX_TIME = 120 * 1000;
  private final OkHttpClient httpClient;
  private final Request request;
  private final Lock lock;
  private final ObjectMapper objectMapper;
  private final Config config;
  private final Callable<Boolean> networkConnectivityChecker;
  private WebSocket mWebSocket;
  private int currentStatus = Status.DISCONNECTED;
  private boolean manualClose;
  private DataGateSocketListener listener;
  private int reconnectCount = 0;
  private Timer reconnectTimer;
  private CountDownLatch latch;
  private final WebSocketListener webSocketListener =
      new WebSocketListener() {
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull final Response response) {
          mWebSocket = webSocket;
          setCurrentStatus(Status.CONNECTED);
          if (latch != null) {
            latch.countDown();
          }

          connected();
          if (listener != null) {
            listener.onOpen(response);
          }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull final String text) {
          if (listener != null) {
            listener.onMessage(text);
          }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
          if (listener != null) {
            listener.onMessage(bytes);
          }
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
          if (listener != null) {
            listener.onClosing(code, reason);
          }
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
          if (listener != null) {
            listener.onClosed(code, reason);
          }
        }

        @Override
        public void onFailure(
            @NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
          if (listener != null) {
            listener.onFailure(t, response);
          }
        }
      };

  public DataGateSocket(Config config) {
    this.config = config;
    this.networkConnectivityChecker = config.getNetworkConnectivityChecker();
    this.lock = new ReentrantLock();
    this.httpClient = createClient();
    this.request = config.getRequestBuilder().build();
    this.objectMapper = config.getObjectMapper();
  }

  public void setListener(DataGateSocketListener listener) {
    this.listener = listener;
  }

  public boolean isConnected() {
    return currentStatus == Status.CONNECTED;
  }

  public int getCurrentStatus() {
    return currentStatus;
  }

  public void setCurrentStatus(int status) {
    this.currentStatus = status;
  }

  public void startConnect() {
    manualClose = false;
    buildConnect();
  }

  public void stopConnect(String reason) {
    manualClose = true;
    disconnect(reason);
  }

  public boolean sendMessage(DataGateMessage message) {
    boolean isSent = false;
    try {
      if (mWebSocket != null && isConnected()) {
        String text = objectMapper.writeValueAsString(message);
        log.debug("Sending message to server {}", text);
        isSent = mWebSocket.send(text);

        if (!isSent) {
          tryReconnect();
        }
      }
    } catch (Exception e) {
      log.error("Error while sending message", e);
      isSent = false;
    }
    return isSent;
  }

  private void initWebSocket() {
    if (httpClient != null) {
      httpClient.dispatcher().cancelAll();
    }
    try {
      lock.lockInterruptibly();
      try {
        latch = new CountDownLatch(1);
        if (httpClient != null) {
          httpClient.newWebSocket(request, webSocketListener);
        }
        latch.await();
      } finally {
        lock.unlock();
      }
    } catch (InterruptedException ignored) {
    }
  }

  private OkHttpClient createClient() {
    OkHttpClient.Builder builder =
        new OkHttpClient.Builder()
            .connectTimeout(config.getTimeout().getTime(), config.getTimeout().getTimeUnit())
            .readTimeout(config.getTimeout().getTime(), config.getTimeout().getTimeUnit())
            .writeTimeout(config.getTimeout().getTime(), config.getTimeout().getTimeUnit())
            .pingInterval(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false);

    if (config.getProxy() != null) {
      builder.proxy(config.getProxy());
    }

    if (config.isAcceptAllCertificates()) {
      try {
        final TrustManager[] trustAllCerts =
            new TrustManager[] {
              new X509TrustManager() {

                @Override
                @SuppressWarnings("TrustAllX509TrustManager")
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType) {}

                @Override
                @SuppressWarnings("TrustAllX509TrustManager")
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType) {}

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                  return new java.security.cert.X509Certificate[] {};
                }
              }
            };

        // SSLContext needs to be compatible with TLS 1.2
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

        builder.hostnameVerifier((hostname, session) -> true);
      } catch (Exception e) {
        throw new ReplicationException("error while configuring SSLSocketFactory", e, true);
      }
    }

    return builder.build();
  }

  private void tryReconnect() {
    if (manualClose) {
      return;
    }

    if (isNetworkDisconnected()) {
      setCurrentStatus(Status.DISCONNECTED);
      return;
    }

    setCurrentStatus(Status.RECONNECT);
    reconnectTimer = new Timer();

    long delay = (long) reconnectCount * RECONNECT_INTERVAL;
    reconnectTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            if (listener != null) {
              listener.onReconnect();
            }
            buildConnect();
          }
        },
        Math.min(delay, RECONNECT_MAX_TIME));
    reconnectCount++;
  }

  private void cancelReconnect() {
    if (reconnectTimer != null) {
      reconnectTimer.cancel();
    }
    reconnectCount = 0;
  }

  private void connected() {
    cancelReconnect();
  }

  private void disconnect(String reason) {
    if (currentStatus == Status.DISCONNECTED) {
      return;
    }

    cancelReconnect();

    if (mWebSocket != null) {
      boolean isClosed = mWebSocket.close(Status.CODE.NORMAL_CLOSE, reason);
      if (!isClosed) {
        if (listener != null) {
          listener.onClosed(Status.CODE.ABNORMAL_CLOSE, reason);
        }
      }
    }

    setCurrentStatus(Status.DISCONNECTED);
  }

  private void buildConnect() {
    if (isNetworkDisconnected()) {
      setCurrentStatus(Status.DISCONNECTED);
      return;
    }

    switch (getCurrentStatus()) {
      case Status.CONNECTED:
      case Status.CONNECTING:
        break;
      default:
        setCurrentStatus(Status.CONNECTING);
        initWebSocket();
    }
  }

  private boolean isNetworkDisconnected() {
    try {
      return !networkConnectivityChecker.call();
    } catch (Exception e) {
      log.error("Network connectivity failed", e);
      return true;
    }
  }
}
