/*
 * Copyright (c) 2017-2021 Crop author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xyz.vopen.framework.cropdb.common.streams;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.DocumentCursor;
import xyz.vopen.framework.cropdb.common.Lookup;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.common.processors.ProcessorChain;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a joined document stream.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 1.0
 */
public class JoinedDocumentStream implements RecordStream<Document> {
    private final RecordStream<Pair<CropId, Document>> recordStream;
    private final DocumentCursor foreignCursor;
    private final Lookup lookup;
    private final ProcessorChain processorChain;

    /**
     * Instantiates a new Joined document stream.
     *
     * @param recordStream   the record stream
     * @param foreignCursor  the foreign cursor
     * @param lookup         the lookup
     * @param processorChain the processor chain
     */
    JoinedDocumentStream(RecordStream<Pair<CropId, Document>> recordStream,
                         DocumentCursor foreignCursor,
                         Lookup lookup, ProcessorChain processorChain) {
        this.recordStream = recordStream;
        this.foreignCursor = foreignCursor;
        this.lookup = lookup;
        this.processorChain = processorChain;
    }


    @Override
    public Iterator<Document> iterator() {
        Iterator<Pair<CropId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new JoinedDocumentIterator(iterator, processorChain, foreignCursor, lookup);
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private static class JoinedDocumentIterator implements Iterator<Document> {
        private final Iterator<Pair<CropId, Document>> iterator;
        private final ProcessorChain processorChain;
        private final DocumentCursor foreignCursor;
        private final Lookup lookup;

        /**
         * Instantiates a new Joined document iterator.
         *
         * @param iterator       the iterator
         * @param processorChain the processor chain
         */
        public JoinedDocumentIterator(Iterator<Pair<CropId, Document>> iterator,
                                      ProcessorChain processorChain,
                                      DocumentCursor foreignCursor,
                                      Lookup lookup) {
            this.iterator = iterator;
            this.processorChain = processorChain;
            this.foreignCursor = foreignCursor;
            this.lookup = lookup;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Document next() {
            Pair<CropId, Document> next = iterator.next();
            Document document = next.getSecond();
            if (document != null) {
                Document unprocessed = document.clone();

                // process the document
                Document processed = processorChain.processAfterRead(unprocessed);
                return join(processed, foreignCursor, lookup);
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on a cursor is not supported");
        }

        private Document join(Document localDocument, DocumentCursor foreignCursor, Lookup lookup) {
            Object localObject = localDocument.get(lookup.getLocalField());
            if (localObject == null) return localDocument;
            Set<Document> target = new HashSet<>();

            for (Document foreignDocument : foreignCursor) {
                Object foreignObject = foreignDocument.get(lookup.getForeignField());
                if (foreignObject != null) {
                    if (ObjectUtils.deepEquals(foreignObject, localObject)) {
                        target.add(foreignDocument);
                    }
                }
            }
            if (!target.isEmpty()) {
                localDocument.put(lookup.getTargetField(), target);
            }
            return localDocument;
        }
    }
}

