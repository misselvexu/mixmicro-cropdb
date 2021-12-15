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

package xyz.vopen.framework.cropdb.collection.operation;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.*;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventInfo;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventListener;
import xyz.vopen.framework.cropdb.collection.meta.Attributes;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.WriteResult;
import xyz.vopen.framework.cropdb.common.event.EventBus;
import xyz.vopen.framework.cropdb.filters.Filter;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.common.processors.Processor;
import xyz.vopen.framework.cropdb.common.processors.ProcessorChain;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.StoreCatalog;
import xyz.vopen.framework.cropdb.common.util.DocumentUtils;

import java.util.Collection;

import static xyz.vopen.framework.cropdb.collection.UpdateOptions.updateOptions;

/**
 * The collection operations.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
public class CollectionOperations implements AutoCloseable {
    private final String collectionName;
    private final CropConfig cropConfig;
    private final CropMap<CropId, Document> cropMap;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private ProcessorChain processorChain;
    private IndexOperations indexOperations;
    private WriteOperations writeOperations;
    private ReadOperations readOperations;

    /**
     * Instantiates a new Collection operations.
     *
     * @param collectionName the collection name
     * @param cropMap     the crop map
     * @param cropConfig  the crop config
     * @param eventBus       the event bus
     */
    public CollectionOperations(String collectionName,
                                CropMap<CropId, Document> cropMap,
                                CropConfig cropConfig,
                                EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus) {
        this.collectionName = collectionName;
        this.cropMap = cropMap;
        this.cropConfig = cropConfig;
        this.eventBus = eventBus;
        initialize();
    }

    /**
     * Adds a document processor.
     *
     * @param processor the processor
     */
    public void addProcessor(Processor processor) {
        doProcess(processor);
        processorChain.add(processor);
    }

    /**
     * Removes a document processor.
     *
     * @param processor the processor
     */
    public void removeProcessor(Processor processor) {
        processorChain.remove(processor);
        undoProcess(processor);
    }

    /**
     * Creates index.
     *
     * @param fields    the fields
     * @param indexType the index type
     */
    public void createIndex(Fields fields, String indexType) {
        indexOperations.createIndex(fields, indexType);
    }

    /**
     * Finds index descriptor.
     *
     * @param fields the fields
     * @return the index descriptor
     */
    public IndexDescriptor findIndex(Fields fields) {
        return indexOperations.findIndexDescriptor(fields);
    }

    /**
     * Rebuilds index.
     *
     * @param indexDescriptor the index descriptor
     */
    public void rebuildIndex(IndexDescriptor indexDescriptor) {
        indexOperations.buildIndex(indexDescriptor, true);
    }

    /**
     * Lists all indexes.
     *
     * @return the collection
     */
    public Collection<IndexDescriptor> listIndexes() {
        return indexOperations.listIndexes();
    }

    /**
     * Checks if an index exists on the fields.
     *
     * @param fields the fields
     * @return the boolean
     */
    public boolean hasIndex(Fields fields) {
        return indexOperations.hasIndexEntry(fields);
    }

    /**
     * Checks if indexing is going on the fields.
     *
     * @param fields the fields
     * @return the boolean
     */
    public boolean isIndexing(Fields fields) {
        return indexOperations.isIndexing(fields);
    }

    /**
     * Drops index.
     *
     * @param fields the fields
     */
    public void dropIndex(Fields fields) {
        indexOperations.dropIndex(fields);
    }

    /**
     * Drops all indices.
     */
    public void dropAllIndices() {
        indexOperations.dropAllIndices();
    }

    /**
     * Inserts documents to the collection.
     *
     * @param documents the documents
     * @return the write result
     */
    public WriteResult insert(Document[] documents) {
        return writeOperations.insert(documents);
    }

    /**
     * Updates documents in the collection.
     *
     * @param filter        the filter
     * @param update        the update
     * @param updateOptions the update options
     * @return the write result
     */
    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        return writeOperations.update(filter, update, updateOptions);
    }

    /**
     * Removes document from the collection.
     *
     * @param document the document
     * @return the write result
     */
    public WriteResult remove(Document document) {
        return writeOperations.remove(document);
    }

    /**
     * Removes document from collection.
     *
     * @param filter   the filter
     * @param justOnce the just once
     * @return the write result
     */
    public WriteResult remove(Filter filter, boolean justOnce) {
        return writeOperations.remove(filter, justOnce);
    }

    /**
     * Finds documents using filter.
     *
     * @param filter      the filter
     * @param findOptions the find options
     * @return the document cursor
     */
    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        return readOperations.find(filter, findOptions);
    }

    /**
     * Gets document by id.
     *
     * @param cropId the crop id
     * @return the by id
     */
    public Document getById(CropId cropId) {
        return readOperations.getById(cropId);
    }

    /**
     * Drops the collection.
     */
    public void dropCollection() {
        indexOperations.dropAllIndices();
        dropCropMap();
    }

    /**
     * Gets the size of the collection.
     *
     * @return the size
     */
    public long getSize() {
        return cropMap.size();
    }

    /**
     * Gets the additional attributes for the collection.
     *
     * @return the attributes
     */
    public Attributes getAttributes() {
        return cropMap != null ? cropMap.getAttributes() : null;
    }

    /**
     * Sets additional attributes in the collection.
     *
     * @param attributes the attributes
     */
    public void setAttributes(Attributes attributes) {
        cropMap.setAttributes(attributes);
    }

    public void close() {
        if (indexOperations != null) {
            indexOperations.close();
        }
        cropMap.close();
    }

    private void initialize() {
        this.processorChain = new ProcessorChain();
        this.indexOperations = new IndexOperations(collectionName, cropConfig, cropMap, eventBus);
        this.readOperations = new ReadOperations(collectionName, indexOperations,
            cropConfig, cropMap, processorChain);

        DocumentIndexWriter indexWriter = new DocumentIndexWriter(cropConfig, indexOperations);
        this.writeOperations = new WriteOperations(indexWriter, readOperations,
            cropMap, eventBus, processorChain);
    }

    private void dropCropMap() {
        // remove the collection name from the catalog
        StoreCatalog catalog = cropMap.getStore().getCatalog();
        catalog.remove(cropMap.getName());

        // drop the map
        cropMap.drop();
    }

    private void doProcess(Processor processor) {
        for (Document document : find(Filter.ALL, null)) {
            Document processed = processor.processBeforeWrite(document);
            update(DocumentUtils.createUniqueFilter(document), processed, updateOptions(false));
        }
    }

    private void undoProcess(Processor processor) {
        for (Document document : find(Filter.ALL, null)) {
            Document processed = processor.processAfterRead(document);
            update(DocumentUtils.createUniqueFilter(document), processed, updateOptions(false));
        }
    }
}
