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

package xyz.vopen.framework.cropdb.repository;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.CollectionFactory;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.store.StoreCatalog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static xyz.vopen.framework.cropdb.common.util.ObjectUtils.findRepositoryName;

/**
 * The {@link ObjectRepository} factory.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
public class RepositoryFactory {
    private final Map<String, ObjectRepository<?>> repositoryMap;
    private final CollectionFactory collectionFactory;
    private final ReentrantLock lock;

    /**
     * Instantiates a new {@link RepositoryFactory}.
     *
     * @param collectionFactory the collection factory
     */
    public RepositoryFactory(CollectionFactory collectionFactory) {
        this.collectionFactory = collectionFactory;
        this.repositoryMap = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    /**
     * Gets an {@link ObjectRepository} by type.
     *
     * @param <T>           the type parameter
     * @param cropConfig the crop config
     * @param type          the type
     * @return the repository
     */
    public <T> ObjectRepository<T> getRepository(CropConfig cropConfig, Class<T> type) {
        return getRepository(cropConfig, type, null);
    }

    /**
     * Gets an {@link ObjectRepository} by type and a key.
     *
     * @param <T>           the type parameter
     * @param cropConfig the crop config
     * @param type          the type
     * @param key           the key
     * @return the repository
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectRepository<T> getRepository(CropConfig cropConfig, Class<T> type, String key) {
        if (type == null) {
            throw new ValidationException("type cannot be null");
        }

        if (cropConfig == null) {
            throw new ValidationException("cropConfig cannot be null");
        }

        String collectionName = findRepositoryName(type, key);

        try {
            lock.lock();
            if (repositoryMap.containsKey(collectionName)) {
                ObjectRepository<T> repository = (ObjectRepository<T>) repositoryMap.get(collectionName);
                if (repository.isDropped() || !repository.isOpen()) {
                    repositoryMap.remove(collectionName);
                    return createRepository(cropConfig, type, collectionName, key);
                } else {
                    return repository;
                }
            } else {
                return createRepository(cropConfig, type, collectionName, key);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Closes all opened {@link ObjectRepository}s and clear internal data from this class.
     */
    public void clear() {
        try {
            lock.lock();
            for (ObjectRepository<?> repository : repositoryMap.values()) {
                repository.close();
            }
            repositoryMap.clear();
        } catch (Exception e) {
            throw new CropIOException("failed to close an object repository", e);
        } finally {
            lock.unlock();
        }
    }

    private <T> ObjectRepository<T> createRepository(CropConfig cropConfig, Class<T> type,
                                                     String collectionName, String key) {
        CropMapper cropMapper = cropConfig.cropMapper();
        CropStore<?> store = cropConfig.getCropStore();
        if (cropMapper.isValueType(type)) {
            throw new ValidationException("a value type cannot be used to create repository");
        }

        if (store.getCollectionNames().contains(collectionName)) {
            throw new ValidationException("a collection with same entity name already exists");
        }

        CropCollection cropCollection = collectionFactory.getCollection(collectionName,
            cropConfig, false);
        ObjectRepository<T> repository = new DefaultObjectRepository<>(type, cropCollection, cropConfig);
        repositoryMap.put(collectionName, repository);

        writeCatalog(store, collectionName, key);
        return repository;
    }

    private void writeCatalog(CropStore<?> store, String name, String key) {
        StoreCatalog storeCatalog = store.getCatalog();
        if (StringUtils.isNullOrEmpty(key)) {
            storeCatalog.writeRepositoryEntry(name);
        } else {
            storeCatalog.writeKeyedRepositoryEntries(name);
        }
    }
}
