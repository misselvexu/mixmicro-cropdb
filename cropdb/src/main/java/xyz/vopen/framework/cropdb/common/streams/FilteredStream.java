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
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.filters.Filter;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a filtered crop document stream.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0
 */
public class FilteredStream implements RecordStream<Pair<CropId, Document>> {
  private final RecordStream<Pair<CropId, Document>> recordStream;
  private final Filter filter;

  /**
   * Instantiates a new Filtered stream.
   *
   * @param recordStream the record stream
   * @param filter the filter
   */
  public FilteredStream(RecordStream<Pair<CropId, Document>> recordStream, Filter filter) {
    this.recordStream = recordStream;
    this.filter = filter;
  }

  @Override
  public Iterator<Pair<CropId, Document>> iterator() {
    Iterator<Pair<CropId, Document>> iterator =
        recordStream == null ? Collections.emptyIterator() : recordStream.iterator();

    // filter can be null from read operation when coll scan filter is null
    if (filter == null || filter == Filter.ALL) {
      return iterator;
    }
    return new FilteredIterator(iterator, filter);
  }

  /** The type Filtered iterator. */
  private static class FilteredIterator implements Iterator<Pair<CropId, Document>> {
    private final Iterator<Pair<CropId, Document>> iterator;
    private final Filter filter;
    private Pair<CropId, Document> nextPair;
    private boolean nextPairSet = false;

    /**
     * Instantiates a new Filtered iterator.
     *
     * @param iterator the iterator
     * @param filter the filter
     */
    public FilteredIterator(Iterator<Pair<CropId, Document>> iterator, Filter filter) {
      this.iterator = iterator;
      this.filter = filter;
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

    @Override
    public void remove() {
      if (nextPairSet) {
        throw new InvalidOperationException("remove operation cannot be called here");
      }
      iterator.remove();
    }

    private boolean setNextId() {
      while (iterator.hasNext()) {
        final Pair<CropId, Document> pair = iterator.next();
        if (filter.apply(pair)) {
          nextPair = pair;
          nextPairSet = true;
          return true;
        }
      }
      return false;
    }
  }
}
