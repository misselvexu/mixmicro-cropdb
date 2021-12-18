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

import java.util.*;

/**
 * Represents a document stream of distinct elements
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class DistinctStream implements RecordStream<Pair<CropId, Document>> {
  private final RecordStream<Pair<CropId, Document>> rawStream;

  /**
   * Instantiates a new DistinctStream.
   *
   * @param rawStream the raw stream
   */
  public DistinctStream(RecordStream<Pair<CropId, Document>> rawStream) {
    this.rawStream = rawStream;
  }

  @Override
  public Iterator<Pair<CropId, Document>> iterator() {
    Iterator<Pair<CropId, Document>> iterator =
        rawStream == null ? Collections.emptyIterator() : rawStream.iterator();
    return new DistinctStreamIterator(iterator);
  }

  private static class DistinctStreamIterator implements Iterator<Pair<CropId, Document>> {
    private final Iterator<Pair<CropId, Document>> iterator;
    private final Set<CropId> scannedIds;
    private Pair<CropId, Document> nextPair;
    private boolean nextPairSet = false;

    /**
     * Instantiates a new DistinctStreamIterator.
     *
     * @param iterator the iterator
     */
    public DistinctStreamIterator(Iterator<Pair<CropId, Document>> iterator) {
      this.iterator = iterator;
      this.scannedIds = new HashSet<>(); // fastest lookup for ids - O(1)
    }

    @Override
    public boolean hasNext() {
      return nextPairSet || setNextId();
    }

    @Override
    public Pair<CropId, Document> next() {
      if (!nextPairSet && !setNextId()) {
        throw new NoSuchElementException();
      }
      nextPairSet = false;
      return nextPair;
    }

    private boolean setNextId() {
      while (iterator.hasNext()) {
        final Pair<CropId, Document> pair = iterator.next();
        if (!scannedIds.contains(pair.getFirst())) {
          scannedIds.add(pair.getFirst());
          nextPair = pair;
          nextPairSet = true;
          return true;
        }
      }
      return false;
    }
  }
}
