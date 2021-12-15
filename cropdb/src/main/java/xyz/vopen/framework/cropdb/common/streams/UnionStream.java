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

import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;

import java.util.*;

/**
 * Represents an union of multiple distinct crop document stream.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class UnionStream implements RecordStream<Pair<CropId, Document>> {
    private final Collection<RecordStream<Pair<CropId, Document>>> streams;

    /**
     * Instantiates a new Union stream.
     *
     * @param streams the streams
     */
    public UnionStream(Collection<RecordStream<Pair<CropId, Document>>> streams) {
        this.streams = streams;
    }

    @Override
    public Iterator<Pair<CropId, Document>> iterator() {
        Queue<Iterator<Pair<CropId, Document>>> iteratorQueue = new LinkedList<>();
        for (RecordStream<Pair<CropId, Document>> stream : streams) {
            iteratorQueue.add(stream.iterator());
        }
        return new UnionStreamIterator(iteratorQueue);
    }

    /**
     * The type Union stream iterator.
     */
    private static class UnionStreamIterator implements Iterator<Pair<CropId, Document>> {
        private final Queue<Iterator<Pair<CropId, Document>>> iteratorQueue;
        private Iterator<Pair<CropId, Document>> currentIterator;

        /**
         * Instantiates a new Union stream iterator.
         *
         * @param iteratorQueue the iterator queue
         */
        public UnionStreamIterator(Queue<Iterator<Pair<CropId, Document>>> iteratorQueue) {
            this.iteratorQueue = iteratorQueue;
        }

        @Override
        public boolean hasNext() {
            updateCurrentIterator();
            return currentIterator.hasNext();
        }

        @Override
        public Pair<CropId, Document> next() {
            updateCurrentIterator();
            return currentIterator.next();
        }


        private void updateCurrentIterator() {
            if (currentIterator == null) {
                if (iteratorQueue.isEmpty()) {
                    currentIterator = Collections.emptyIterator();
                } else {
                    currentIterator = iteratorQueue.remove();
                }
            }

            while (!currentIterator.hasNext() && !iteratorQueue.isEmpty()) {
                currentIterator = iteratorQueue.remove();
            }
        }
    }
}
