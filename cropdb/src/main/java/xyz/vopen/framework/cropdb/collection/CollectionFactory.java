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

package xyz.vopen.framework.cropdb.collection;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.common.concurrent.LockService;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.store.StoreCatalog;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import static xyz.vopen.framework.cropdb.common.util.ValidationUtils.notEmpty;

/**
 * A factory class to create {@link CropCollection}.
 * <p>NOTE: Internal API</p>
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class CollectionFactory {
    private final Map<String, CropCollection> collectionMap;
    private final LockService lockService;

    /**
     * Instantiates a new {@link CollectionFactory}.
     *
     * @param lockService the lock service
     */
    public CollectionFactory(LockService lockService) {
        this.collectionMap = new HashMap<>();
        this.lockService = lockService;
    }

    /**
     * Gets or creates a collection.
     *
     * @param name           the name
     * @param cropConfig  the crop config
     * @param writeCatalogue the write catalogue
     * @return the collection
     */
    public CropCollection getCollection(String name, CropConfig cropConfig, boolean writeCatalogue) {
        ValidationUtils.notNull(cropConfig, "configuration is null while creating collection");
        ValidationUtils.notEmpty(name, "collection name is null or empty");

        Lock lock = lockService.getWriteLock(this.getClass().getName());
        try {
            lock.lock();
            if (collectionMap.containsKey(name)) {
                CropCollection collection = collectionMap.get(name);
                if (collection.isDropped() || !collection.isOpen()) {
                    collectionMap.remove(name);
                    return createCollection(name, cropConfig, writeCatalogue);
                }
                return collectionMap.get(name);
            } else {
                return createCollection(name, cropConfig, writeCatalogue);
            }
        } finally {
            lock.unlock();
        }
    }

    private CropCollection createCollection(String name, CropConfig cropConfig, boolean writeCatalog) {
        CropStore<?> store = cropConfig.getCropStore();
        CropMap<CropId, Document> cropMap = store.openMap(name, CropId.class, Document.class);
        CropCollection collection = new DefaultCropCollection(name, cropMap, cropConfig, lockService);

        if (writeCatalog) {
            // ignore repository request
            if (store.getRepositoryRegistry().contains(name)) {
                cropMap.close();
                collection.close();
                throw new ValidationException("a repository with same name already exists");
            }

            for (Set<String> set : store.getKeyedRepositoryRegistry().values()) {
                if (set.contains(name)) {
                    cropMap.close();
                    collection.close();
                    throw new ValidationException("a keyed repository with same name already exists");
                }
            }

            collectionMap.put(name, collection);
            StoreCatalog storeCatalog = store.getCatalog();
            storeCatalog.writeCollectionEntry(name);
        }

        return collection;
    }

    /**
     * Clears the internal registry holding collection information.
     */
    public void clear() {
        Lock lock = lockService.getWriteLock(this.getClass().getName());
        try {
            lock.lock();
            for (CropCollection collection : collectionMap.values()) {
                collection.close();
            }
            collectionMap.clear();
        } catch (Exception e) {
            throw new CropIOException("failed to close a collection", e);
        } finally {
            lock.unlock();
        }
    }
}
