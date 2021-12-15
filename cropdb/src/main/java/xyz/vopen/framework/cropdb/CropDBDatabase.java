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

package xyz.vopen.framework.cropdb;

import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.collection.CollectionFactory;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.Constants;
import xyz.vopen.framework.cropdb.common.concurrent.LockService;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.exceptions.CropException;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.exceptions.CropSecurityException;
import xyz.vopen.framework.cropdb.migration.MigrationManager;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;
import xyz.vopen.framework.cropdb.repository.RepositoryFactory;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.store.StoreMetaData;
import xyz.vopen.framework.cropdb.store.UserAuthenticationService;
import xyz.vopen.framework.cropdb.transaction.Session;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0
 */
@Slf4j
class CropDBDatabase implements CropDB {
    private final CollectionFactory collectionFactory;
    private final RepositoryFactory repositoryFactory;
    private final CropConfig cropConfig;
    private final LockService lockService;
    private CropMap<String, Document> storeInfo;
    private CropStore<?> store;

    CropDBDatabase(CropConfig config) {
        this.cropConfig = config;
        this.lockService = new LockService();
        this.collectionFactory = new CollectionFactory(lockService);
        this.repositoryFactory = new RepositoryFactory(collectionFactory);
        this.initialize(null, null);
    }

    CropDBDatabase(String username, String password, CropConfig config) {
        validateUserCredentials(username, password);
        this.cropConfig = config;
        this.lockService = new LockService();
        this.collectionFactory = new CollectionFactory(lockService);
        this.repositoryFactory = new RepositoryFactory(collectionFactory);
        this.initialize(username, password);
    }

    @Override
    public CropCollection getCollection(String name) {
        validateCollectionName(name);
        checkOpened();
        return collectionFactory.getCollection(name, cropConfig, true);
    }

    @Override
    public <T> ObjectRepository<T> getRepository(Class<T> type) {
        checkOpened();
        return repositoryFactory.getRepository(cropConfig, type);
    }

    @Override
    public <T> ObjectRepository<T> getRepository(Class<T> type, String key) {
        checkOpened();
        return repositoryFactory.getRepository(cropConfig, type, key);
    }

    @Override
    public void destroyCollection(String name) {
        checkOpened();
        store.removeMap(name);
    }

    @Override
    public <T> void destroyRepository(Class<T> type) {
        checkOpened();
        String mapName = ObjectUtils.findRepositoryName(type, null);
        store.removeMap(mapName);
    }

    @Override
    public <T> void destroyRepository(Class<T> type, String key) {
        checkOpened();
        String mapName = ObjectUtils.findRepositoryName(type, key);
        store.removeMap(mapName);
    }

    @Override
    public Set<String> listCollectionNames() {
        checkOpened();
        return store.getCollectionNames();
    }

    @Override
    public Set<String> listRepositories() {
        checkOpened();
        return store.getRepositoryRegistry();
    }

    @Override
    public Map<String, Set<String>> listKeyedRepository() {
        checkOpened();
        return store.getKeyedRepositoryRegistry();
    }

    @Override
    public boolean hasUnsavedChanges() {
        checkOpened();
        return store != null && store.hasUnsavedChanges();
    }

    @Override
    public boolean isClosed() {
        return store == null || store.isClosed();
    }

    @Override
    public CropStore<?> getStore() {
        return store;
    }

    @Override
    public CropConfig getConfig() {
        return cropConfig;
    }

    @Override
    public synchronized void close() {
        checkOpened();
        try {
            store.beforeClose();
            if (hasUnsavedChanges()) {
                log.debug("Unsaved changes detected, committing the changes.");
                commit();
            }

            repositoryFactory.clear();
            collectionFactory.clear();
            storeInfo.close();

            if (cropConfig != null) {
                // close all plugins
                cropConfig.close();
            }

            log.info("Crop database has been closed successfully.");
        } catch (CropIOException e) {
            throw e;
        } catch (Throwable error) {
            throw new CropIOException("error while shutting down crop", error);
        }
    }

    @Override
    public void commit() {
        checkOpened();
        if (store != null) {
            try {
                store.commit();
            } catch (Exception e) {
                throw new CropIOException("failed to commit changes", e);
            }
            log.debug("Unsaved changes committed successfully.");
        }
    }

    @Override
    public StoreMetaData getDatabaseMetaData() {
        Document document = storeInfo.get(Constants.STORE_INFO);
        if (document == null) {
            prepareDatabaseMetaData();
            document = storeInfo.get(Constants.STORE_INFO);
        }
        return new StoreMetaData(document);
    }

    @Override
    public Session createSession() {
        return new Session(this, lockService);
    }

    private void validateUserCredentials(String username, String password) {
        if (StringUtils.isNullOrEmpty(username)) {
            throw new CropSecurityException("username cannot be empty");
        }
        if (StringUtils.isNullOrEmpty(password)) {
            throw new CropSecurityException("password cannot be empty");
        }
    }

    private void initialize(String username, String password) {
        try {
            cropConfig.initialize();
            store = cropConfig.getCropStore();
            boolean isExisting = isExisting();

            store.openOrCreate();
            prepareDatabaseMetaData();

            MigrationManager migrationManager = new MigrationManager(this);
            migrationManager.doMigrate();

            UserAuthenticationService userAuthenticationService = new UserAuthenticationService(store);
            userAuthenticationService.authenticate(username, password, isExisting);
        } catch (CropException e) {
            log.error("Error while initializing the database", e);
            if (store != null && !store.isClosed()) {
                try {
                    store.close();
                } catch (Exception ex) {
                    log.error("Error while closing the database", ex);
                    throw new CropIOException("failed to close database", ex);
                }
            }
            throw e;
        } catch (Exception e) {
            log.error("Error while initializing the database", e);
            if (store != null && !store.isClosed()) {
                try {
                    store.close();
                } catch (Exception ex) {
                    log.error("Error while closing the database");
                    throw new CropIOException("failed to close database", ex);
                }
            }
            throw new CropIOException("failed to initialize database", e);
        }
    }

    private void prepareDatabaseMetaData() {
        storeInfo = this.store.openMap(Constants.STORE_INFO, String.class, Document.class);

        if (storeInfo.isEmpty()) {
            StoreMetaData storeMetadata = new StoreMetaData();
            storeMetadata.setCreateTime(System.currentTimeMillis());
            storeMetadata.setStoreVersion(store.getStoreVersion());
            storeMetadata.setCropdbVersion(Constants.CROPDB_VERSION);
            storeMetadata.setSchemaVersion(cropConfig.getSchemaVersion());

            storeInfo.put(Constants.STORE_INFO, storeMetadata.getInfo());
        }
    }

    private boolean isExisting() {
        String filePath = store.getStoreConfig().filePath();
        if (!StringUtils.isNullOrEmpty(filePath)) {
            File dbFile = new File(filePath);
            return dbFile.exists();
        }
        return false;
    }
}
