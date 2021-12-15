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

package xyz.vopen.framework.cropdb.repository;

import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;

import java.util.Iterator;

import static xyz.vopen.framework.cropdb.common.Constants.DOC_ID;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
class MutatedObjectStream<T> implements RecordStream<T> {
    private final RecordStream<Document> recordIterable;
    private final Class<T> mutationType;
    private final CropMapper cropMapper;

    MutatedObjectStream(CropMapper cropMapper,
                        RecordStream<Document> recordIterable,
                        Class<T> mutationType) {
        this.recordIterable = recordIterable;
        this.mutationType = mutationType;
        this.cropMapper = cropMapper;
    }

    @Override
    public Iterator<T> iterator() {
        return new MutatedObjectIterator(cropMapper);
    }

    private class MutatedObjectIterator implements Iterator<T> {
        private final CropMapper cropMapper;
        private final Iterator<Document> documentIterator;

        MutatedObjectIterator(CropMapper cropMapper) {
            this.cropMapper = cropMapper;
            this.documentIterator = recordIterable.iterator();
        }

        @Override
        public boolean hasNext() {
            return documentIterator.hasNext();
        }

        @Override
        public T next() {
            Document item = documentIterator.next();
            if (item != null) {
                Document record = item.clone();
                record.remove(DOC_ID);
                return cropMapper.convert(record, mutationType);
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on a cursor is not supported");
        }
    }
}
