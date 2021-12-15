package xyz.vopen.framework.cropdb.collection.operation;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventInfo;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventListener;
import xyz.vopen.framework.cropdb.collection.events.EventType;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.event.EventBus;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.util.DocumentUtils;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.CropIndexer;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.common.concurrent.ThreadPoolManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class IndexOperations implements AutoCloseable {
    private final String collectionName;
    private final CropConfig cropConfig;
    private final CropMap<CropId, Document> cropMap;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private final Map<Fields, AtomicBoolean> indexBuildTracker;
    private IndexManager indexManager;

    IndexOperations(String collectionName, CropConfig cropConfig,
                    CropMap<CropId, Document> cropMap,
                    EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus) {
        this.collectionName = collectionName;
        this.cropConfig = cropConfig;
        this.cropMap = cropMap;
        this.eventBus = eventBus;
        this.indexBuildTracker = new ConcurrentHashMap<>();
        this.indexManager = new IndexManager(collectionName, cropConfig);
    }

    @Override
    public void close() {
        indexManager.close();
    }

    void createIndex(Fields fields, String indexType) {
        IndexDescriptor indexDescriptor = indexManager.findExactIndexDescriptor(fields);
        if (indexDescriptor == null) {
            // if no index create index
            indexDescriptor = indexManager.createIndexDescriptor(fields, indexType);
        } else {
            // if index already there throw
            throw new IndexingException("index already exists on " + fields);
        }

        buildIndex(indexDescriptor, false);
    }

    // call to this method is already synchronized, only one thread per field
    // can access it only if rebuild is already not running for that field
    void buildIndex(IndexDescriptor indexDescriptor, boolean rebuild) {
        final Fields fields = indexDescriptor.getIndexFields();
        if (getBuildFlag(fields).compareAndSet(false, true)) {
            buildIndexInternal(indexDescriptor, rebuild);
            return;
        }
        throw new IndexingException("indexing is already running on " + indexDescriptor.getIndexFields());
    }

    void dropIndex(Fields fields) {
        if (getBuildFlag(fields).get()) {
            throw new IndexingException("cannot drop index as indexing is running on " + fields);
        }

        IndexDescriptor indexDescriptor = findIndexDescriptor(fields);
        if (indexDescriptor != null) {
            String indexType = indexDescriptor.getIndexType();
            CropIndexer cropIndexer = cropConfig.findIndexer(indexType);
            cropIndexer.dropIndex(indexDescriptor, cropConfig);

            indexManager.dropIndexDescriptor(fields);
            indexBuildTracker.remove(fields);
        } else {
            throw new IndexingException(fields + " is not indexed");
        }
    }

    void dropAllIndices() {
        for (Map.Entry<Fields, AtomicBoolean> entry : indexBuildTracker.entrySet()) {
            if (entry.getValue() != null && entry.getValue().get()) {
                throw new IndexingException("cannot drop index as indexing is running on " + entry.getKey());
            }
        }

        // we can drop all indices in parallel
        List<Future<?>> futures = new ArrayList<>();
        for (IndexDescriptor index : listIndexes()) {
            futures.add(ThreadPoolManager.runAsync(() -> dropIndex(index.getIndexFields())));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IndexingException("failed to drop all indices", e);
            }
        }

        indexManager.dropIndexMeta();
        indexBuildTracker.clear();

        // recreate index manager to discard old native resources
        // special measure for RocksDB adapter
        this.indexManager = new IndexManager(collectionName, cropConfig);
    }

    boolean isIndexing(Fields field) {
        // has an index will only return true, if there is an index on
        // the value and indexing is not running on it
        return indexManager.hasIndexDescriptor(field)
            && getBuildFlag(field).get();
    }

    boolean hasIndexEntry(Fields field) {
        return indexManager.hasIndexDescriptor(field);
    }

    Collection<IndexDescriptor> listIndexes() {
        return indexManager.getIndexDescriptors();
    }

    IndexDescriptor findIndexDescriptor(Fields field) {
        return indexManager.findExactIndexDescriptor(field);
    }

    AtomicBoolean getBuildFlag(Fields field) {
        AtomicBoolean flag = indexBuildTracker.get(field);
        if (flag != null) return flag;

        flag = new AtomicBoolean(false);
        indexBuildTracker.put(field, flag);
        return flag;
    }

    boolean shouldRebuildIndex(Fields fields) {
        return indexManager.isDirtyIndex(fields) && !getBuildFlag(fields).get();
    }

    private void buildIndexInternal(IndexDescriptor indexDescriptor, boolean rebuild) {
        Fields fields = indexDescriptor.getIndexFields();
        try {
            alert(EventType.IndexStart, fields);
            // first put dirty marker
            indexManager.beginIndexing(fields);

            String indexType = indexDescriptor.getIndexType();
            CropIndexer cropIndexer = cropConfig.findIndexer(indexType);

            // if rebuild drop existing index
            if (rebuild) {
                cropIndexer.dropIndex(indexDescriptor, cropConfig);
            }

            for (Pair<CropId, Document> entry : cropMap.entries()) {
                Document document = entry.getSecond();
                FieldValues fieldValues = DocumentUtils.getValues(document, indexDescriptor.getIndexFields());
                cropIndexer.writeIndexEntry(fieldValues, indexDescriptor, cropConfig);
            }
        } finally {
            // remove dirty marker to denote indexing completed successfully
            // if dirty marker is found in any index, it needs to be rebuild
            indexManager.endIndexing(fields);
            getBuildFlag(fields).set(false);
            alert(EventType.IndexEnd, fields);
        }
    }

    private void alert(EventType eventType, Fields field) {
        CollectionEventInfo<Fields> eventInfo = new CollectionEventInfo<>();
        eventInfo.setItem(field);
        eventInfo.setTimestamp(System.currentTimeMillis());
        eventInfo.setEventType(eventType);
        if (eventBus != null) {
            eventBus.post(eventInfo);
        }
    }
}
