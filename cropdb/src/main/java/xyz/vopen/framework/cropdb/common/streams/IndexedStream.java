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
import xyz.vopen.framework.cropdb.store.CropMap;

import java.util.Iterator;
import java.util.Set;

/**
 * Represents a crop crop stream backed by an index.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class IndexedStream implements RecordStream<Pair<CropId, Document>> {
    private final CropMap<CropId, Document> cropMap;
    private final Set<CropId> cropIds;

    /**
     * Instantiates a new Indexed stream.
     *
     * @param cropIds the crop ids
     * @param cropMap the crop map
     */
    public IndexedStream(Set<CropId> cropIds,
                  CropMap<CropId, Document> cropMap) {
        this.cropIds = cropIds;
        this.cropMap = cropMap;
    }

    @Override
    public Iterator<Pair<CropId, Document>> iterator() {
        return new IndexedStreamIterator(cropIds.iterator(), cropMap);
    }

    /**
     * The type Indexed stream iterator.
     */
    private static class IndexedStreamIterator implements Iterator<Pair<CropId, Document>> {
        private final Iterator<CropId> iterator;
        private final CropMap<CropId, Document> cropMap;

        /**
         * Instantiates a new Indexed stream iterator.
         *
         * @param iterator   the iterator
         * @param cropMap the crop map
         */
        IndexedStreamIterator(Iterator<CropId> iterator,
                              CropMap<CropId, Document> cropMap) {
            this.iterator = iterator;
            this.cropMap = cropMap;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Pair<CropId, Document> next() {
            CropId id = iterator.next();
            Document document = cropMap.get(id);
            return new Pair<>(id, document);
        }
    }
}
