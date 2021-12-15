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

import lombok.Getter;
import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventInfo;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventListener;
import xyz.vopen.framework.cropdb.collection.meta.Attributes;
import xyz.vopen.framework.cropdb.collection.operation.CollectionOperations;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.WriteResult;
import xyz.vopen.framework.cropdb.common.concurrent.LockService;
import xyz.vopen.framework.cropdb.common.event.EventBus;
import xyz.vopen.framework.cropdb.common.event.CropEventBus;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.exceptions.NotIdentifiableException;
import xyz.vopen.framework.cropdb.filters.Filter;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.IndexOptions;
import xyz.vopen.framework.cropdb.index.IndexType;
import xyz.vopen.framework.cropdb.common.processors.Processor;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.common.util.DocumentUtils;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;

import static xyz.vopen.framework.cropdb.collection.UpdateOptions.updateOptions;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
class DefaultCropCollection implements CropCollection {
    private final String collectionName;
    private final LockService lockService;

    protected CropMap<CropId, Document> cropMap;
    protected CropConfig cropConfig;
    protected CropStore<?> cropStore;

    private Lock writeLock;
    private Lock readLock;
    private CollectionOperations collectionOperations;
    private EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;

    @Getter
    private volatile boolean isDropped;

    DefaultCropCollection(String name, CropMap<CropId, Document> cropMap,
                          CropConfig cropConfig, LockService lockService) {
        this.collectionName = name;
        this.cropConfig = cropConfig;
        this.cropMap = cropMap;
        this.lockService = lockService;

        initialize();
    }

    @Override
    public void addProcessor(Processor processor) {
        ValidationUtils.notNull(processor, "a null processor cannot be added");

        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.addProcessor(processor);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeProcessor(Processor processor) {
        ValidationUtils.notNull(processor, "a null processor cannot be removed");

        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.removeProcessor(processor);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult insert(Document[] documents) {
        ValidationUtils.notNull(documents, "a null document cannot be inserted");
        ValidationUtils.containsNull(documents, "a null document cannot be inserted");

        try {
            writeLock.lock();
            checkOpened();
            return collectionOperations.insert(documents);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult update(Document document, boolean insertIfAbsent) {
        ValidationUtils.notNull(document, "a null document cannot be used for update");

        if (insertIfAbsent) {
            return update(DocumentUtils.createUniqueFilter(document), document, updateOptions(true));
        } else {
            if (document.hasId()) {
                return update(DocumentUtils.createUniqueFilter(document), document, updateOptions(false));
            } else {
                throw new NotIdentifiableException("update operation failed as no id value found for the document");
            }
        }
    }

    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        ValidationUtils.notNull(update, "a null document cannot be used for update");
        ValidationUtils.notNull(updateOptions, "updateOptions cannot be null");

        try {
            writeLock.lock();
            checkOpened();
            return collectionOperations.update(filter, update, updateOptions);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult remove(Document document) {
        ValidationUtils.notNull(document, "a null document cannot be removed");

        if (document.hasId()) {
            try {
                writeLock.lock();
                checkOpened();
                return collectionOperations.remove(document);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new NotIdentifiableException("remove operation failed as no id value found for the document");
        }
    }

    public WriteResult remove(Filter filter, boolean justOne) {
        if ((filter == null || filter == Filter.ALL) && justOne) {
            throw new InvalidOperationException("remove all cannot be combined with just once");
        }

        try {
            writeLock.lock();
            checkOpened();
            return collectionOperations.remove(filter, justOne);
        } finally {
            writeLock.unlock();
        }
    }

    public void clear() {
        try {
            writeLock.lock();
            checkOpened();
            cropMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.find(filter, findOptions);
        } finally {
            readLock.unlock();
        }
    }

    public void createIndex(IndexOptions indexOptions, String... fields) {
        ValidationUtils.notNull(fields, "fields cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            writeLock.lock();
            checkOpened();

            if (indexOptions == null) {
                collectionOperations.createIndex(indexFields, IndexType.UNIQUE);
            } else {
                collectionOperations.createIndex(indexFields, indexOptions.getIndexType());
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void rebuildIndex(String... fields) {
        ValidationUtils.notNull(fields, "fields cannot be null");

        IndexDescriptor indexDescriptor;
        Fields indexFields = Fields.withNames(fields);
        try {
            readLock.lock();
            checkOpened();
            indexDescriptor = collectionOperations.findIndex(indexFields);
        } finally {
            readLock.unlock();
        }

        if (indexDescriptor != null) {
            validateRebuildIndex(indexDescriptor);

            try {
                writeLock.lock();
                checkOpened();
                collectionOperations.rebuildIndex(indexDescriptor);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new IndexingException(Arrays.toString(fields) + " is not indexed");
        }
    }

    public Collection<IndexDescriptor> listIndices() {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

    public boolean hasIndex(String... fields) {
        ValidationUtils.notNull(fields, "fields cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.hasIndex(indexFields);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isIndexing(String... fields) {
        ValidationUtils.notNull(fields, "field cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.isIndexing(indexFields);
        } finally {
            readLock.unlock();
        }
    }

    public void dropIndex(String... fields) {
        ValidationUtils.notNull(fields, "fields cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.dropIndex(indexFields);
        } finally {
            writeLock.unlock();
        }
    }

    public void dropAllIndices() {
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.dropAllIndices();
        } finally {
            writeLock.unlock();
        }
    }

    public Document getById(CropId cropId) {
        ValidationUtils.notNull(cropId, "cropId cannot be null");

        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.getById(cropId);
        } finally {
            readLock.unlock();
        }
    }

    public void drop() {
        try {
            writeLock.lock();
            checkOpened();

            if (collectionOperations != null) {
                // close collection and indexes
                collectionOperations.close();

                // drop collection and indexes
                collectionOperations.dropCollection();
            }

            // set all reference to null
            this.cropMap = null;
            this.cropConfig = null;
            this.collectionOperations = null;
            this.cropStore = null;

            // close event bus
            closeEventBus();
        } finally {
            writeLock.unlock();
        }
        isDropped = true;
    }

    public boolean isOpen() {
        try {
            readLock.lock();
            return cropStore != null && !cropStore.isClosed() && !isDropped;
        } catch (Exception e) {
            throw new CropIOException("failed to close the database", e);
        } finally {
            readLock.unlock();
        }
    }

    public void close() {
        try {
            writeLock.lock();
            if (collectionOperations != null) {
                // close collection and indexes
                collectionOperations.close();
            }

            // set all reference to null
            this.cropMap = null;
            this.cropConfig = null;
            this.collectionOperations = null;
            this.cropStore = null;
            closeEventBus();
        } finally {
            writeLock.unlock();
        }
    }

    public String getName() {
        return collectionName;
    }

    public long size() {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.getSize();
        } finally {
            readLock.unlock();
        }
    }

    public CropStore<?> getStore() {
        try {
            writeLock.lock();
            return cropStore;
        } finally {
            writeLock.unlock();
        }
    }

    public void subscribe(CollectionEventListener listener) {
        ValidationUtils.notNull(listener, "listener cannot be null");
        try {
            writeLock.lock();
            checkOpened();
            eventBus.register(listener);
        } finally {
            writeLock.unlock();
        }
    }

    public void unsubscribe(CollectionEventListener listener) {
        ValidationUtils.notNull(listener, "listener cannot be null");
        try {
            writeLock.lock();
            checkOpened();

            if (eventBus != null) {
                eventBus.deregister(listener);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public Attributes getAttributes() {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.getAttributes();
        } finally {
            readLock.unlock();
        }
    }

    public void setAttributes(Attributes attributes) {
        ValidationUtils.notNull(attributes, "attributes cannot be null");

        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.setAttributes(attributes);
        } finally {
            writeLock.unlock();
        }
    }

    private void closeEventBus() {
        if (eventBus != null) {
            eventBus.close();
        }
        eventBus = null;
    }

    private void initialize() {
        this.isDropped = false;
        this.readLock = lockService.getReadLock(collectionName);
        this.writeLock = lockService.getWriteLock(collectionName);
        this.cropStore = cropConfig.getCropStore();
        this.eventBus = new CollectionEventBus();
        this.collectionOperations = new CollectionOperations(collectionName, cropMap, cropConfig, eventBus);
    }

    private void checkOpened() {
        if (isOpen()) return;
        throw new CropIOException("collection is closed");
    }

    private void validateRebuildIndex(IndexDescriptor indexDescriptor) {
        ValidationUtils.notNull(indexDescriptor, "index cannot be null");

        String[] indexFields = indexDescriptor.getIndexFields().getFieldNames().toArray(new String[0]);
        if (isIndexing(indexFields)) {
            throw new IndexingException("indexing on value " + indexDescriptor.getIndexFields() + " is currently running");
        }
    }

    private static class CollectionEventBus extends CropEventBus<CollectionEventInfo<?>, CollectionEventListener> {

        public void post(CollectionEventInfo<?> collectionEventInfo) {
            for (final CollectionEventListener listener : getListeners()) {
                getEventExecutor().submit(() -> listener.onEvent(collectionEventInfo));
            }
        }
    }
}
