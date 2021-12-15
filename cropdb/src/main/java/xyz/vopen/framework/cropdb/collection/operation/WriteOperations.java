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

import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.DocumentCursor;
import xyz.vopen.framework.cropdb.collection.UpdateOptions;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventInfo;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventListener;
import xyz.vopen.framework.cropdb.collection.events.EventType;
import xyz.vopen.framework.cropdb.common.WriteResult;
import xyz.vopen.framework.cropdb.common.event.EventBus;
import xyz.vopen.framework.cropdb.common.processors.ProcessorChain;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.exceptions.UniqueConstraintException;
import xyz.vopen.framework.cropdb.filters.Filter;
import xyz.vopen.framework.cropdb.store.CropMap;

import java.util.ArrayList;
import java.util.List;

import static xyz.vopen.framework.cropdb.common.Constants.*;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@Slf4j
class WriteOperations {
    private final DocumentIndexWriter documentIndexWriter;
    private final ReadOperations readOperations;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private final CropMap<CropId, Document> cropMap;
    private final ProcessorChain processorChain;

    WriteOperations(DocumentIndexWriter documentIndexWriter,
                    ReadOperations readOperations,
                    CropMap<CropId, Document> cropMap,
                    EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus,
                    ProcessorChain processorChain) {
        this.documentIndexWriter = documentIndexWriter;
        this.readOperations = readOperations;
        this.eventBus = eventBus;
        this.cropMap = cropMap;
        this.processorChain = processorChain;
    }

    WriteResult insert(Document... documents) {
        List<CropId> cropIds = new ArrayList<>(documents.length);
        log.debug("Total {} document(s) to be inserted in {}", documents.length, cropMap.getName());

        for (Document document : documents) {
            Document newDoc = document.clone();
            CropId cropId = newDoc.getId();
            String source = newDoc.getSource();
            long time = System.currentTimeMillis();

            if (!REPLICATOR.contentEquals(newDoc.getSource())) {
                // if replicator is not inserting the document that means
                // it is being inserted by user, so update metadata
                newDoc.remove(DOC_SOURCE);
                newDoc.put(DOC_REVISION, 1);
                newDoc.put(DOC_MODIFIED, time);
            } else {
                // if replicator is inserting the document, remove the source
                // but keep the revision intact
                newDoc.remove(DOC_SOURCE);
            }

            // run processors
            Document unprocessed = newDoc.clone();
            Document processed = processorChain.processBeforeWrite(unprocessed);
            log.debug("Document processed from {} to {} before insert", newDoc, processed);

            log.debug("Inserting processed document {} in {}", processed, cropMap.getName());
            Document already = cropMap.putIfAbsent(cropId, processed);

            if (already != null) {
                log.warn("Another document {} already exists with same id {}", already, cropId);

                throw new UniqueConstraintException("id constraint violation, " +
                    "entry with same id already exists in " + cropMap.getName());
            } else {
                try {
                    documentIndexWriter.writeIndexEntry(processed);
                } catch (UniqueConstraintException | IndexingException e) {
                    log.error("Index operation has failed during insertion for the document "
                        + document + " in " + cropMap.getName(), e);
                    cropMap.remove(cropId);
                    throw e;
                }
            }

            cropIds.add(cropId);

            CollectionEventInfo<Document> eventInfo = new CollectionEventInfo<>();
            eventInfo.setItem(newDoc);
            eventInfo.setTimestamp(time);
            eventInfo.setEventType(EventType.Insert);
            eventInfo.setOriginator(source);
            alert(EventType.Insert, eventInfo);
        }

        WriteResultImpl result = new WriteResultImpl();
        result.setCropIds(cropIds);

        log.debug("Returning write result {} for collection {}", result, cropMap.getName());
        return result;
    }

    WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        DocumentCursor cursor = readOperations.find(filter, null);

        WriteResultImpl writeResult = new WriteResultImpl();
        Document document = update.clone();
        document.remove(DOC_ID);

        if (!REPLICATOR.contentEquals(document.getSource())) {
            document.remove(DOC_REVISION);
        }

        if (document.size() == 0) {
            alert(EventType.Update, new CollectionEventInfo<>());
            return writeResult;
        }

        long count = 0;
        for (Document doc : cursor) {
            if (doc != null) {
                count++;

                if (count > 1 && updateOptions.isJustOnce()) {
                    break;
                }

                Document newDoc = doc.clone();
                Document oldDocument = doc.clone();
                String source = document.getSource();
                long time = System.currentTimeMillis();

                CropId cropId = newDoc.getId();
                log.debug("Document to update {} in {}", newDoc, cropMap.getName());

                if (!REPLICATOR.contentEquals(document.getSource())) {
                    document.remove(DOC_SOURCE);
                    newDoc.merge(document);
                    int rev = newDoc.getRevision();
                    newDoc.put(DOC_REVISION, rev + 1);
                    newDoc.put(DOC_MODIFIED, time);
                } else {
                    document.remove(DOC_SOURCE);
                    newDoc.merge(document);
                }

                // run processor
                Document unprocessed = newDoc.clone();
                Document processed = processorChain.processBeforeWrite(unprocessed);
                log.debug("Document processed from {} to {} before update", newDoc, processed);

                cropMap.put(cropId, processed);
                log.debug("Document {} updated in {}", processed, cropMap.getName());

                // if 'update' only contains id value, affected count = 0
                if (document.size() > 0) {
                    writeResult.addToList(cropId);
                }

                try {
                    documentIndexWriter.updateIndexEntry(oldDocument, processed);
                } catch (UniqueConstraintException | IndexingException e) {
                    log.error("Index operation failed during update, reverting changes for the document "
                        + oldDocument + " in " + cropMap.getName(), e);
                    cropMap.put(cropId, oldDocument);
                    documentIndexWriter.updateIndexEntry(processed, oldDocument);
                    throw e;
                }

                CollectionEventInfo<Document> eventInfo = new CollectionEventInfo<>();
                eventInfo.setItem(newDoc);
                eventInfo.setEventType(EventType.Update);
                eventInfo.setTimestamp(time);
                eventInfo.setOriginator(source);
                alert(EventType.Update, eventInfo);
            }
        }

        if (count == 0) {
            log.debug("No document found to update by the filter {} in {}", filter, cropMap.getName());
            if (updateOptions.isInsertIfAbsent()) {
                return insert(update);
            } else {
                return writeResult;
            }
        }

        log.debug("Filter {} updated total {} document(s) with options {} in {}",
            filter, count, updateOptions, cropMap.getName());

        log.debug("Returning write result {} for collection {}", writeResult, cropMap.getName());
        return writeResult;
    }

    WriteResult remove(Filter filter, boolean justOnce) {
        DocumentCursor cursor = readOperations.find(filter, null);
        WriteResultImpl result = new WriteResultImpl();

        long count = 0;
        for (Document document : cursor) {
            if (document != null) {
                count++;

                // run processor
                Document unprocessed = document.clone();
                Document processed = processorChain.processAfterRead(unprocessed);
                log.debug("Document processed from {} to {} after remove", document, processed);

                CollectionEventInfo<Document> eventInfo = removeAndCreateEvent(processed, result);
                if (eventInfo != null) {
                    alert(EventType.Remove, eventInfo);
                }

                if (justOnce) {
                    break;
                }
            }
        }

        if (count == 0) {
            log.debug("No document found to remove by the filter {} in {}", filter, cropMap.getName());
            return result;
        }

        log.debug("Filter {} removed total {} document(s) with options {} from {}",
            filter, count, justOnce, cropMap.getName());

        log.debug("Returning write result {} for collection {}", result, cropMap.getName());
        return result;
    }

    WriteResult remove(Document document) {
        WriteResultImpl result = new WriteResultImpl();
        CollectionEventInfo<Document> eventInfo = removeAndCreateEvent(document, result);
        if (eventInfo != null) {
            eventInfo.setOriginator(document.getSource());
            alert(EventType.Remove, eventInfo);
        }
        return result;
    }

    private CollectionEventInfo<Document> removeAndCreateEvent(Document document, WriteResultImpl writeResult) {
        CropId cropId = document.getId();
        document = cropMap.remove(cropId);
        if (document != null) {
            long time = System.currentTimeMillis();
            documentIndexWriter.removeIndexEntry(document);
            writeResult.addToList(cropId);

            int rev = document.getRevision();
            document.put(DOC_REVISION, rev + 1);
            document.put(DOC_MODIFIED, time);

            log.debug("Document removed {} from {}", document, cropMap.getName());

            CollectionEventInfo<Document> eventInfo = new CollectionEventInfo<>();
            Document eventDoc = document.clone();
            eventInfo.setItem(eventDoc);
            eventInfo.setEventType(EventType.Remove);
            eventInfo.setTimestamp(time);
            return eventInfo;
        }
        return null;
    }

    private void alert(EventType action, CollectionEventInfo<?> changedItem) {
        log.debug("Notifying {} event for item {} from {}", action, changedItem, cropMap.getName());
        if (eventBus != null) {
            eventBus.post(changedItem);
        }
    }
}
