/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.collection;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreCatalog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * A factory class to create {@link NitriteCollection}.
 * <p>NOTE: Internal API</p>
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class CollectionFactory {
    private final Map<String, NitriteCollection> collectionMap;
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
     * @param nitriteConfig  the nitrite config
     * @param writeCatalogue the write catalogue
     * @return the collection
     */
    public NitriteCollection getCollection(String name, NitriteConfig nitriteConfig, boolean writeCatalogue) {
        notNull(nitriteConfig, "configuration is null while creating collection");
        notEmpty(name, "collection name is null or empty");

        Lock lock = lockService.getWriteLock(this.getClass().getName());
        try {
            lock.lock();
            if (collectionMap.containsKey(name)) {
                NitriteCollection collection = collectionMap.get(name);
                if (collection.isDropped() || !collection.isOpen()) {
                    collectionMap.remove(name);
                    return createCollection(name, nitriteConfig, writeCatalogue);
                }
                return collectionMap.get(name);
            } else {
                return createCollection(name, nitriteConfig, writeCatalogue);
            }
        } finally {
            lock.unlock();
        }
    }

    private NitriteCollection createCollection(String name, NitriteConfig nitriteConfig, boolean writeCatalog) {
        NitriteStore<?> store = nitriteConfig.getNitriteStore();
        NitriteMap<NitriteId, Document> nitriteMap = store.openMap(name, NitriteId.class, Document.class);
        NitriteCollection collection = new DefaultNitriteCollection(name, nitriteMap, nitriteConfig, lockService);

        if (writeCatalog) {
            // ignore repository request
            if (store.getRepositoryRegistry().contains(name)) {
                nitriteMap.close();
                collection.close();
                throw new ValidationException("a repository with same name already exists");
            }

            for (Set<String> set : store.getKeyedRepositoryRegistry().values()) {
                if (set.contains(name)) {
                    nitriteMap.close();
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
            for (NitriteCollection collection : collectionMap.values()) {
                collection.close();
            }
            collectionMap.clear();
        } catch (Exception e) {
            throw new NitriteIOException("failed to close a collection", e);
        } finally {
            lock.unlock();
        }
    }
}
