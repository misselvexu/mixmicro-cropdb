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
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.common.processors.ProcessorChain;

import java.util.Collections;
import java.util.Iterator;

/**
 * Represents a projected crop document stream.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 1.0
 */
public class ProjectedDocumentStream implements RecordStream<Document> {
  private final RecordStream<Pair<CropId, Document>> recordStream;
  private final Document projection;
  private final ProcessorChain processorChain;

  /**
   * Instantiates a new Projected document stream.
   *
   * @param recordStream the record stream
   * @param projection the projection
   * @param processorChain the processor chain
   */
  public ProjectedDocumentStream(
      RecordStream<Pair<CropId, Document>> recordStream,
      Document projection,
      ProcessorChain processorChain) {
    this.recordStream = recordStream;
    this.projection = projection;
    this.processorChain = processorChain;
  }

  @Override
  public Iterator<Document> iterator() {
    Iterator<Pair<CropId, Document>> iterator =
        recordStream == null ? Collections.emptyIterator() : recordStream.iterator();
    return new ProjectedDocumentIterator(iterator, processorChain, projection);
  }

  @Override
  public String toString() {
    return toList().toString();
  }

  private static class ProjectedDocumentIterator implements Iterator<Document> {
    private final Iterator<Pair<CropId, Document>> iterator;
    private final ProcessorChain processorChain;
    private Document nextElement = null;
    private final Document projection;

    /**
     * Instantiates a new Projected document iterator.
     *
     * @param iterator the iterator
     * @param processorChain the processor chain
     */
    ProjectedDocumentIterator(
        Iterator<Pair<CropId, Document>> iterator,
        ProcessorChain processorChain,
        Document projection) {
      this.iterator = iterator;
      this.processorChain = processorChain;
      this.projection = projection;
      nextMatch();
    }

    @Override
    public boolean hasNext() {
      return nextElement != null;
    }

    @Override
    public Document next() {
      Document returnValue = nextElement.clone();
      nextMatch();
      return returnValue;
    }

    private void nextMatch() {
      while (iterator.hasNext()) {
        Pair<CropId, Document> next = iterator.next();
        Document document = next.getSecond();
        if (document != null) {
          Document projected = project(document.clone());
          if (projected != null) {
            nextElement = projected;
            return;
          }
        }
      }

      nextElement = null;
    }

    @Override
    public void remove() {
      throw new InvalidOperationException("remove on a cursor is not supported");
    }

    private Document project(Document original) {
      if (projection == null) return original;
      Document result = original.clone();

      for (Pair<String, Object> pair : original) {
        if (!projection.containsKey(pair.getFirst())) {
          result.remove(pair.getFirst());
        }
      }

      // process the result
      result = processorChain.processAfterRead(result);
      return result;
    }
  }
}
