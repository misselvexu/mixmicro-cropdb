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

import lombok.Getter;
import lombok.Setter;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.DocumentCursor;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.common.Lookup;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;
import xyz.vopen.framework.cropdb.common.processors.ProcessorChain;

import java.util.Collections;
import java.util.Iterator;

/**
 * Represents a crop document stream.
 *
 * @since 4.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
public class DocumentStream implements DocumentCursor {
    private final RecordStream<Pair<CropId, Document>> recordStream;
    private final ProcessorChain processorChain;

    @Getter @Setter
    private FindPlan findPlan;

    /**
     * Instantiates a new Document stream.
     *
     * @param recordStream   the record stream
     * @param processorChain the processor chain
     */
    public DocumentStream(RecordStream<Pair<CropId, Document>> recordStream,
                          ProcessorChain processorChain) {
        this.recordStream = recordStream;
        this.processorChain = processorChain;
    }

    @Override
    public RecordStream<Document> project(Document projection) {
        validateProjection(projection);
        return new ProjectedDocumentStream(recordStream, projection, processorChain);
    }

    @Override
    public RecordStream<Document> join(DocumentCursor foreignCursor, Lookup lookup) {
        return new JoinedDocumentStream(recordStream, foreignCursor, lookup, processorChain);
    }

    @Override
    public Iterator<Document> iterator() {
        Iterator<Pair<CropId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new DocumentCursorIterator(iterator, processorChain);
    }

    private void validateProjection(Document projection) {
        for (Pair<String, Object> kvp : projection) {
            validateKeyValuePair(kvp);
        }
    }

    private void validateKeyValuePair(Pair<String, Object> kvp) {
        if (kvp.getSecond() != null) {
            if (!(kvp.getSecond() instanceof Document)) {
                throw new ValidationException("projection contains non-null values");
            } else {
                validateProjection((Document) kvp.getSecond());
            }
        }
    }

    private static class DocumentCursorIterator implements Iterator<Document> {
        private final Iterator<Pair<CropId, Document>> iterator;
        private final ProcessorChain processorChain;

        /**
         * Instantiates a new Document cursor iterator.
         *
         * @param iterator       the iterator
         * @param processorChain the processor chain
         */
        DocumentCursorIterator(Iterator<Pair<CropId, Document>> iterator,
                               ProcessorChain processorChain) {
            this.iterator = iterator;
            this.processorChain = processorChain;
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
                Document copy = document.clone();
                copy = processorChain.processAfterRead(copy);
                return copy;
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on cursor is not supported");
        }
    }
}
