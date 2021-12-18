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

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.FindOptions;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.collection.events.CollectionEventListener;
import xyz.vopen.framework.cropdb.collection.meta.Attributes;
import xyz.vopen.framework.cropdb.common.WriteResult;
import xyz.vopen.framework.cropdb.filters.Filter;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.IndexOptions;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;
import xyz.vopen.framework.cropdb.common.processors.Processor;
import xyz.vopen.framework.cropdb.store.CropStore;

import java.util.Collection;

import static xyz.vopen.framework.cropdb.collection.UpdateOptions.updateOptions;
import static xyz.vopen.framework.cropdb.common.util.ValidationUtils.containsNull;
import static xyz.vopen.framework.cropdb.common.util.ValidationUtils.notNull;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
class DefaultObjectRepository<T> implements ObjectRepository<T> {
  private final CropCollection collection;
  private final CropConfig cropConfig;
  private final Class<T> type;
  private RepositoryOperations operations;

  DefaultObjectRepository(Class<T> type, CropCollection collection, CropConfig cropConfig) {
    this.type = type;
    this.collection = collection;
    this.cropConfig = cropConfig;
    initialize();
  }

  @Override
  public void addProcessor(Processor processor) {
    notNull(processor, "a null processor cannot be added");
    collection.addProcessor(processor);
  }

  @Override
  public void removeProcessor(Processor processor) {
    notNull(processor, "a null processor cannot be removed");
    collection.removeProcessor(processor);
  }

  @Override
  public void createIndex(IndexOptions indexOptions, String... fields) {
    collection.createIndex(indexOptions, fields);
  }

  @Override
  public void rebuildIndex(String... fields) {
    collection.rebuildIndex(fields);
  }

  @Override
  public Collection<IndexDescriptor> listIndices() {
    return collection.listIndices();
  }

  @Override
  public boolean hasIndex(String... fields) {
    return collection.hasIndex(fields);
  }

  @Override
  public boolean isIndexing(String... fields) {
    return collection.isIndexing(fields);
  }

  @Override
  public void dropIndex(String... fields) {
    collection.dropIndex(fields);
  }

  @Override
  public void dropAllIndices() {
    collection.dropAllIndices();
  }

  @Override
  public WriteResult insert(T[] elements) {
    notNull(elements, "a null object cannot be inserted");
    containsNull(elements, "a null object cannot be inserted");
    return collection.insert(operations.toDocuments(elements));
  }

  @Override
  public WriteResult update(T element, boolean insertIfAbsent) {
    notNull(element, "a null object cannot be used for update");
    return update(operations.createUniqueFilter(element), element, insertIfAbsent);
  }

  @Override
  public WriteResult update(Filter filter, T update, boolean insertIfAbsent) {
    notNull(update, "a null object cannot be used for update");
    Document updateDocument = operations.toDocument(update, true);
    if (!insertIfAbsent) {
      operations.removeCropId(updateDocument);
    }
    return collection.update(
        operations.asObjectFilter(filter), updateDocument, updateOptions(insertIfAbsent, true));
  }

  @Override
  public WriteResult update(Filter filter, Document update, boolean justOnce) {
    notNull(update, "a null document cannot be used for update");
    operations.removeCropId(update);
    operations.serializeFields(update);
    return collection.update(
        operations.asObjectFilter(filter), update, updateOptions(false, justOnce));
  }

  @Override
  public WriteResult remove(T element) {
    notNull(element, "a null object cannot be removed");
    return remove(operations.createUniqueFilter(element));
  }

  @Override
  public WriteResult remove(Filter filter, boolean justOne) {
    return collection.remove(operations.asObjectFilter(filter), justOne);
  }

  @Override
  public void clear() {
    collection.clear();
  }

  @Override
  public Cursor<T> find(Filter filter, FindOptions findOptions) {
    return operations.find(filter, findOptions, type);
  }

  @Override
  public <I> T getById(I id) {
    Filter idFilter = operations.createIdFilter(id);
    return find(idFilter).firstOrNull();
  }

  @Override
  public void drop() {
    collection.drop();
  }

  @Override
  public boolean isDropped() {
    return collection.isDropped();
  }

  @Override
  public boolean isOpen() {
    return collection.isOpen();
  }

  @Override
  public void close() {
    collection.close();
  }

  @Override
  public long size() {
    return collection.size();
  }

  @Override
  public CropStore<?> getStore() {
    return collection.getStore();
  }

  @Override
  public void subscribe(CollectionEventListener listener) {
    collection.subscribe(listener);
  }

  @Override
  public void unsubscribe(CollectionEventListener listener) {
    collection.unsubscribe(listener);
  }

  @Override
  public Attributes getAttributes() {
    return collection.getAttributes();
  }

  @Override
  public void setAttributes(Attributes attributes) {
    collection.setAttributes(attributes);
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public CropCollection getDocumentCollection() {
    return collection;
  }

  private void initialize() {
    CropMapper cropMapper = cropConfig.cropMapper();
    operations = new RepositoryOperations(type, cropMapper, collection);
    operations.createIndices();
  }
}
