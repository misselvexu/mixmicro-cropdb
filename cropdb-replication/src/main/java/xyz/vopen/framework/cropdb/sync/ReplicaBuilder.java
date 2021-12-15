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

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;
import xyz.vopen.framework.cropdb.sync.module.DocumentModule;

import java.math.BigInteger;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
public class ReplicaBuilder {
    private CropCollection collection;
    private String replicationServer;
    private String authToken;
    private TimeSpan timeout;
    private TimeSpan debounce;
    private Integer chunkSize;
    private String userName;
    private ObjectMapper objectMapper;
    private Proxy proxy;
    private boolean acceptAllCertificates = false;
    private Callable<Boolean> networkConnectivityChecker = () -> true;

    ReplicaBuilder() {
        chunkSize = 10;
        timeout = new TimeSpan(5, TimeUnit.SECONDS);
        debounce = new TimeSpan(1, TimeUnit.SECONDS);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new DocumentModule());
    }

    public ReplicaBuilder of(CropCollection collection) {
        this.collection = collection;
        return this;
    }

    public ReplicaBuilder of(ObjectRepository<?> repository) {
        return of(repository.getDocumentCollection());
    }

    public ReplicaBuilder remote(String replicationServer) {
        this.replicationServer = replicationServer;
        return this;
    }

    public ReplicaBuilder jwtAuth(String userName, String authToken) {
        this.authToken = authToken;
        this.userName = userName;
        return this;
    }

    public ReplicaBuilder basicAuth(String userName, String password) {
        this.authToken = toHex(userName + ":" + password);
        this.userName = userName;
        return this;
    }

    public ReplicaBuilder timeout(TimeSpan timeSpan) {
        this.timeout = timeSpan;
        return this;
    }

    public ReplicaBuilder chunkSize(Integer size) {
        this.chunkSize = size;
        return this;
    }

    public ReplicaBuilder debounce(TimeSpan timeSpan) {
        this.debounce = timeSpan;
        return this;
    }

    public ReplicaBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public ReplicaBuilder proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public ReplicaBuilder acceptAllCertificates(boolean accept) {
        this.acceptAllCertificates = accept;
        return this;
    }

    public ReplicaBuilder networkConnectivityChecker(Callable<Boolean> callable) {
        this.networkConnectivityChecker = callable;
        return this;
    }

    public Replica create() {
        if (collection != null) {
            Request.Builder builder = createRequestBuilder();

            Config config = new Config();
            config.setCollection(collection);
            config.setChunkSize(chunkSize);
            config.setUserName(userName);
            config.setDebounce(getTimeoutInMillis(debounce));
            config.setObjectMapper(objectMapper);
            config.setTimeout(timeout);
            config.setRequestBuilder(builder);
            config.setProxy(proxy);
            config.setAcceptAllCertificates(acceptAllCertificates);
            config.setAuthToken(authToken);
            config.setNetworkConnectivityChecker(networkConnectivityChecker);
            return new Replica(config);
        } else {
            throw new ReplicationException("no collection or repository has been specified for replication", true);
        }
    }

    private Request.Builder createRequestBuilder() {
        Request.Builder builder = new Request.Builder();
        builder.url(replicationServer);
        return builder;
    }

    private String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(StandardCharsets.UTF_8)));
    }

    private int getTimeoutInMillis(TimeSpan connectTimeout) {
        return Math.toIntExact(connectTimeout.getTimeUnit().toMillis(connectTimeout.getTime()));
    }
}
