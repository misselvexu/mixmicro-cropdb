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
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a bounded document stream.
 *
 * @since 1.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
public class BoundedDocumentStream implements RecordStream<Pair<CropId, Document>> {
    private final RecordStream<Pair<CropId, Document>> recordStream;
    private final long skip;
    private final long limit;

    /**
     * Instantiates a new Bounded document stream.
     *
     * @param skip         the skip
     * @param limit        the limit
     * @param recordStream the record stream
     */
    public BoundedDocumentStream(Long skip, Long limit, RecordStream<Pair<CropId, Document>> recordStream) {
        this.skip = skip;
        this.limit = limit;

        if (skip < 0) {
            throw new ValidationException("skip parameter must not be negative");
        }
        if (limit < 0) {
            throw new ValidationException("limit parameter must not be negative");
        }

        this.recordStream = recordStream;
    }

    @Override
    public Iterator<Pair<CropId, Document>> iterator() {
        Iterator<Pair<CropId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new BoundedIterator<>(iterator, skip, limit);
    }

    private static class BoundedIterator<T> implements Iterator<T> {
        private final Iterator<? extends T> iterator;
        private final long skip;
        private final long limit;
        private long pos;

        /**
         * Instantiates a new Bounded iterator.
         *
         * @param iterator the iterator
         * @param skip     the skip
         * @param limit    the limit
         */
        public BoundedIterator(final Iterator<? extends T> iterator, final long skip, final long limit) {
            if (iterator == null) {
                throw new ValidationException("iterator must not be null");
            }
            if (skip < 0) {
                throw new ValidationException("skip parameter must not be negative.");
            }
            if (limit < 0) {
                throw new ValidationException("limit parameter must not be negative.");
            }

            this.iterator = iterator;
            this.skip = skip;
            this.limit = limit;
            pos = 0;
            initialize();
        }

        private void initialize() {
            while (pos < skip && iterator.hasNext()) {
                iterator.next();
                pos++;
            }
        }

        @Override
        public boolean hasNext() {
            if (checkBounds()) {
                return false;
            }
            return iterator.hasNext();
        }

        private boolean checkBounds() {
            return pos - skip + 1 > limit;
        }

        @Override
        public T next() {
            if (checkBounds()) {
                throw new NoSuchElementException();
            }
            final T next = iterator.next();
            pos++;
            return next;
        }

        @Override
        public void remove() {
            if (pos <= skip) {
                throw new IllegalStateException("remove() cannot be called before calling next()");
            }
            iterator.remove();
        }
    }

}
