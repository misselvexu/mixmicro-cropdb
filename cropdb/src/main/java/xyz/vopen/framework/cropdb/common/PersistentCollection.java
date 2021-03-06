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

package xyz.vopen.framework.cropdb.common;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventListener;
import xyz.vopen.framework.cropdb.collection.events.EventAware;
import xyz.vopen.framework.cropdb.collection.events.EventType;
import xyz.vopen.framework.cropdb.collection.meta.MetadataAware;
import xyz.vopen.framework.cropdb.common.processors.Processor;
import xyz.vopen.framework.cropdb.common.util.Iterables;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.IndexOptions;
import xyz.vopen.framework.cropdb.index.IndexType;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;
import xyz.vopen.framework.cropdb.exceptions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The interface Persistent collection.
 *
 * @param <T> the type parameter
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @see CropCollection
 * @see ObjectRepository
 * @since 1.0
 */
public interface PersistentCollection<T> extends EventAware, MetadataAware, AutoCloseable {

  /**
   * Adds a data processor to this collection.
   *
   * @param processor the processor
   */
  void addProcessor(Processor processor);

  /**
   * Removes a data processor from this collection.
   *
   * @param processor the processor
   */
  void removeProcessor(Processor processor);

  /**
   * Creates an unique index on the {@code fields}, if not already exists.
   *
   * @param fields the fields to be indexed.
   * @throws IndexingException if an index already exists on the field.
   */
  default void createIndex(String... fields) {
    createIndex(null, fields);
  }

  /**
   * Creates an index on the {@code fields}, if not already exists. If {@code indexOptions} is
   * {@code null}, it will use default options.
   *
   * <p>The default indexing option is -
   *
   * <ul>
   *   <li>{@code indexOptions.setIndexType(IndexType.Unique);}
   * </ul>
   *
   * <p>NOTE:
   *
   * <ul>
   *   <li><b>_id</b> value of the document is always indexed. But full-text indexing is not
   *       supported on <b>_id</b> value.
   *   <li>Indexing on non-comparable value is not supported.
   * </ul>
   *
   * @param indexOptions index options.
   * @param fields the fields to be indexed.
   * @throws IndexingException if an index already exists on the field.
   * @see IndexOptions
   * @see IndexType
   */
  void createIndex(IndexOptions indexOptions, String... fields);

  /**
   * Rebuilds index on the {@code field} if it exists.
   *
   * @param fields the fields to be indexed.
   * @throws IndexingException if the {@code field} is not indexed.
   */
  void rebuildIndex(String... fields);

  /**
   * Gets a set of all indices in the collection.
   *
   * @return a set of all indices.
   * @see IndexDescriptor
   */
  Collection<IndexDescriptor> listIndices();

  /**
   * Checks if the {@code fields} is already indexed or not.
   *
   * @param fields the fields to check.
   * @return {@code true} if the {@code field} is indexed; otherwise, {@code false}.
   */
  boolean hasIndex(String... fields);

  /**
   * Checks if indexing operation is currently ongoing for the {@code fields}.
   *
   * @param fields the fields to check.
   * @return {@code true} if indexing is currently running; otherwise, {@code false}.
   */
  boolean isIndexing(String... fields);

  /**
   * Drops the index on the {@code fields}.
   *
   * @param fields the index on the {@code fields} to drop.
   * @throws IndexingException if indexing is currently running on the {@code fields}.
   * @throws IndexingException if the {@code fields} are not indexed.
   */
  void dropIndex(String... fields);

  /**
   * Drops all indices from the collection.
   *
   * @throws IndexingException if indexing is running on any value.
   */
  void dropAllIndices();

  /**
   * Inserts elements into this collection. If the element has an <b>_id</b> field, then the value
   * will be used as an unique key to identify the element in the collection. If the element does
   * not have any <b>_id</b> field, then crop will generate a new {@link CropId} and will add it to
   * the <b>_id</b> field.
   *
   * <p>If any of the value is already indexed in the collection, then after insertion the index
   * will also be updated.
   *
   * <p>NOTE: This operations will notify all {@link CollectionEventListener} instances registered
   * to this collection with change type {@link EventType#Insert}.
   *
   * @param elements an array of element for batch insertion.
   * @return the result of the write operation.
   * @throws ValidationException if elements is null.
   * @throws InvalidIdException if the <b>_id</b> field's value contains {@code null}.
   * @throws InvalidIdException if the <b>_id</b> field's value contains non comparable type, i.e.
   *     type that does not implement {@link Comparable}.
   * @throws InvalidIdException if the <b>_id</b> field contains value which is not of the same java
   *     type as of other element's <b>_id</b> field value in the collection.
   * @throws UniqueConstraintException if the value of <b>_id</b> field clashes with the <b>_id</b>
   *     field of another element in the repository.
   * @throws UniqueConstraintException if a value of the element is indexed and it violates the
   *     unique constraint in the collection(if any).
   * @see CropId
   * @see WriteResult
   */
  WriteResult insert(T[] elements);

  /**
   * Updates the {@code element} in the collection. Specified {@code element} must have an id.
   *
   * <p>NOTE: This operations will notify all {@link CollectionEventListener} instances registered
   * to this collection with change type {@link EventType#Update}.
   *
   * @param element the element to update.
   * @return the result of the update operation.
   * @throws ValidationException if the element is {@code null}.
   * @throws NotIdentifiableException if the element does not have any id.
   */
  default WriteResult update(T element) {
    return update(element, false);
  }

  /**
   * Updates {@code element} in the collection. Specified {@code element} must have an id. If the
   * {@code element} is not found in the collection, it will be inserted only if {@code
   * insertIfAbsent} is set to {@code true}.
   *
   * <p>NOTE: This operations will notify all {@link CollectionEventListener} instances registered
   * to this collection with change type {@link EventType#Update} or {@link EventType#Insert}.
   *
   * @param element the element to update.
   * @param insertIfAbsent if set to {@code true}, {@code element} will be inserted if not found.
   * @return the result of the update operation.
   * @throws ValidationException if the {@code element} is {@code null}.
   * @throws NotIdentifiableException if the {@code element} does not have any id field.
   */
  WriteResult update(T element, boolean insertIfAbsent);

  /**
   * Updates {@code elements} in the collection. Specified {@code elements} must have an id. If the
   * {@code elements} are not found in the collection, it will be inserted only if {@code
   * insertIfAbsent} is set to {@code true}.
   *
   * @param elements the elements to update.
   * @param insertIfAbsent if set to {@code true}, {@code elements} will be inserted if not found.
   * @return the result of the update operation.
   * @throws ValidationException if the {@code elements} is {@code null}.
   * @throws NotIdentifiableException if the {@code elements} does not have any id field.
   */
  default WriteResult update(T[] elements, boolean insertIfAbsent) {
    ValidationUtils.notNull(elements, "a null element cannot be updated");
    ValidationUtils.containsNull(elements, "a null element cannot be updated");

    List<CropId> affectedIds = new ArrayList<>();

    for (T element : elements) {
      WriteResult writeResult = update(element, insertIfAbsent);
      affectedIds.addAll(Iterables.toList(writeResult));
    }

    return affectedIds::iterator;
  }

  /**
   * Deletes the {@code element} from the collection. The {@code element} must have an id.
   *
   * <p>NOTE: This operations will notify all {@link CollectionEventListener} instances registered
   * to this collection with change type {@link EventType#Remove}.
   *
   * @param element the element
   * @return the result of the remove operation.
   * @throws NotIdentifiableException if the {@code element} does not have any id field.
   */
  WriteResult remove(T element);

  /** Removes all element from the collection. */
  void clear();

  /**
   * Drops the collection and all of its indices.
   *
   * <p>Any further access to a dropped collection would result into a {@link
   * IllegalStateException}.
   */
  void drop();

  /**
   * Returns {@code true} if the collection is dropped; otherwise, {@code false}.
   *
   * @return a boolean value indicating if the collection has been dropped or not.
   */
  boolean isDropped();

  /**
   * Returns {@code true} if the collection is open; otherwise, {@code false}.
   *
   * @return a boolean value indicating if the collection has been closed or not.
   */
  boolean isOpen();

  /**
   * Returns the size of the {@link PersistentCollection}.
   *
   * @return the size.
   */
  long size();

  /** Closes this {@link PersistentCollection}. */
  void close();

  /**
   * Returns the {@link CropStore} instance for this collection.
   *
   * @return the {@link CropStore} instance.
   */
  CropStore<?> getStore();
}
