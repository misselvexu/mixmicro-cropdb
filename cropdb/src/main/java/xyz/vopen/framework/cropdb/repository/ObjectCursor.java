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
import xyz.vopen.framework.cropdb.collection.DocumentCursor;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.common.Lookup;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;

import java.lang.reflect.Modifier;
import java.util.Iterator;

import static xyz.vopen.framework.cropdb.common.util.DocumentUtils.skeletonDocument;
import static xyz.vopen.framework.cropdb.common.util.ValidationUtils.notNull;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>. */
class ObjectCursor<T> implements Cursor<T> {
  private final DocumentCursor cursor;
  private final CropMapper cropMapper;
  private final Class<T> type;

  public ObjectCursor(CropMapper cropMapper, DocumentCursor cursor, Class<T> type) {
    this.cropMapper = cropMapper;
    this.cursor = cursor;
    this.type = type;
  }

  @Override
  public long size() {
    return cursor.size();
  }

  @Override
  public FindPlan getFindPlan() {
    return cursor.getFindPlan();
  }

  @Override
  public <P> RecordStream<P> project(Class<P> projectionType) {
    notNull(projectionType, "projection cannot be null");
    Document dummyDoc = emptyDocument(cropMapper, projectionType);
    return new MutatedObjectStream<>(cropMapper, cursor.project(dummyDoc), projectionType);
  }

  @Override
  public <Foreign, Joined> RecordStream<Joined> join(
      Cursor<Foreign> foreignCursor, Lookup lookup, Class<Joined> type) {
    ObjectCursor<Foreign> foreignObjectCursor = (ObjectCursor<Foreign>) foreignCursor;
    return new MutatedObjectStream<>(
        cropMapper, cursor.join(foreignObjectCursor.cursor, lookup), type);
  }

  @Override
  public Iterator<T> iterator() {
    return new ObjectCursorIterator(cursor.iterator());
  }

  private <D> Document emptyDocument(CropMapper cropMapper, Class<D> type) {
    if (type.isPrimitive()) {
      throw new ValidationException("cannot project to primitive type");
    } else if (type.isInterface()) {
      throw new ValidationException("cannot project to interface");
    } else if (type.isArray()) {
      throw new ValidationException("cannot project to array");
    } else if (Modifier.isAbstract(type.getModifiers())) {
      throw new ValidationException("cannot project to abstract type");
    } else if (cropMapper.isValueType(type)) {
      throw new ValidationException("cannot to project to crop mapper's value type");
    }

    Document dummyDoc = skeletonDocument(cropMapper, type);
    if (dummyDoc == null) {
      throw new ValidationException("cannot project to empty type");
    } else {
      return dummyDoc;
    }
  }

  private class ObjectCursorIterator implements Iterator<T> {
    private final Iterator<Document> documentIterator;

    ObjectCursorIterator(Iterator<Document> documentIterator) {
      this.documentIterator = documentIterator;
    }

    @Override
    public boolean hasNext() {
      return documentIterator.hasNext();
    }

    @Override
    public T next() {
      Document document = documentIterator.next();
      return cropMapper.convert(document, type);
    }

    @Override
    public void remove() {
      throw new InvalidOperationException("remove on a cursor is not supported");
    }
  }
}
