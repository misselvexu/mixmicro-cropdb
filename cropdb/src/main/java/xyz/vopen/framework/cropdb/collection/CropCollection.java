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

package xyz.vopen.framework.cropdb.collection;

import xyz.vopen.framework.cropdb.collection.events.CollectionEventListener;
import xyz.vopen.framework.cropdb.collection.events.EventAware;
import xyz.vopen.framework.cropdb.collection.events.EventType;
import xyz.vopen.framework.cropdb.common.PersistentCollection;
import xyz.vopen.framework.cropdb.common.WriteResult;
import xyz.vopen.framework.cropdb.common.event.EventBus;
import xyz.vopen.framework.cropdb.exceptions.InvalidIdException;
import xyz.vopen.framework.cropdb.exceptions.UniqueConstraintException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;
import xyz.vopen.framework.cropdb.filters.Filter;
import xyz.vopen.framework.cropdb.common.SortOrder;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a named document collection stored in crop database.
 * It persists documents into the database. Each document is associated
 * with a unique {@link CropId} in a collection.
 * <p>
 * A crop collection supports indexing. Every crop collection is also
 * observable.
 * </p>
 * <b>Create a collection</b>
 * <pre>
 * {@code
 * Crop db = Crop.builder()
 *    .loadModule(MVStoreModule("/tmp/tmp.db"))
 *    .openOrCreate("user", "password");
 *    
 * CropCollection collection = db.getCollection("products");
 * }
 *</pre>
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @see EventAware
 * @see Document
 * @see CropId
 * @see CollectionEventListener
 * @see EventBus
 * @since 1.0
 */
public interface CropCollection extends PersistentCollection<Document> {
    /**
     * Insert documents into a collection. If the document contains a {@code _id} value, then
     * the value will be used as a unique key to identify the document in the collection.
     * If the document does not contain any {@code _id} value, then crop will generate a new
     * {@link CropId} and will add it to the document.
     * <p>
     * If any of the value is already indexed in the collection, then after insertion the
     * index will also be updated.
     * <p>
     * NOTE: These operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Insert}.
     * </p>
     * 
     * @param document  the document to insert
     * @param documents other documents to insert in a batch.
     * @return the result of write operation.
     * @throws ValidationException       if {@code document} is {@code null}.
     * @throws InvalidIdException        if the {@code _id} value contains {@code null} value.
     * @throws InvalidIdException        if the {@code _id} value contains non comparable type, i.e.
     *                                   type that does not implement {@link Comparable}.
     * @throws InvalidIdException        if the {@code _id} contains value, which is not of the same java
     *                                   type as of other documents' {@code _id} in the collection.
     * @throws UniqueConstraintException if the value of {@code _id} value clashes with the id
     *                                   of another document in the collection.
     * @throws UniqueConstraintException if a value of the document is indexed and it
     *                                   violates the unique constraint in the collection(if any).
     * @see CropId
     * @see WriteResult
     */
    default WriteResult insert(Document document, Document... documents) {
        ValidationUtils.notNull(document, "a null document cannot be inserted");
        if (documents != null) {
            ValidationUtils.containsNull(documents, "a null document cannot be inserted");
        }

        List<Document> documentList = new ArrayList<>();
        documentList.add(document);

        if (documents != null && documents.length > 0) {
            Collections.addAll(documentList, documents);
        }

        return insert(documentList.toArray(new Document[0]));
    }

    /**
     * Update documents in the collection.
     * <p>
     * If the {@code filter} is {@code null}, it will update all documents in the collection.
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update}.
     * </p>
     *
     * @param filter the filter to apply to select documents from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws ValidationException if the {@code update} document is {@code null}.
     */
    default WriteResult update(Filter filter, Document update) {
        return update(filter, update, new UpdateOptions());
    }

    /**
     * Updates document in the collection. Update operation can be customized
     * with the help of {@code updateOptions}.
     * <p>
     * If the {@code filter} is {@code null}, it will update all documents in the collection unless
     * {@code justOnce} is set to {@code true} in {@code updateOptions}.
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Update} or {@link EventType#Insert}.
     * </p>
     *
     * @param filter        the filter to apply to select documents from the collection.
     * @param update        the modifications to apply.
     * @param updateOptions the update options to customize the operation.
     * @return the result of the update operation.
     * @throws ValidationException if the {@code update} document is {@code null}.
     * @throws ValidationException if {@code updateOptions} is {@code null}.
     * @see UpdateOptions
     */
    WriteResult update(Filter filter, Document update, UpdateOptions updateOptions);

    /**
     * Removes matching elements from the collection.
     * <p>
     * If the {@code filter} is {@code null}, it will remove all objects from the collection.
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Remove}.
     * </p>
     *
     * @param filter the filter to apply to select elements from collection.
     * @return the result of the remove operation.
     */
    default WriteResult remove(Filter filter) {
        return remove(filter, false);
    }

    /**
     * Removes document from a collection. Remove operation can be customized by
     * {@code removeOptions}.
     * <p>
     * If the {@code filter} is {@code null}, it will remove all documents in the collection unless
     * {@code justOnce} is set to {@code true} in {@code removeOptions}.
     * <p>
     * NOTE: This operations will notify all {@link CollectionEventListener}
     * instances registered to this collection with change type
     * {@link EventType#Remove}.
     * </p>
     *
     * @param filter  the filter to apply to select documents from collection.
     * @param justOne indicates if only one element will be removed or all of them.
     * @return the result of the remove operation.
     */
    WriteResult remove(Filter filter, boolean justOne);

    /**
     * Returns a cursor to all documents in the collection.
     *
     * @return a cursor to all documents in the collection.
     */
    default DocumentCursor find() {
        return find(Filter.ALL, null);
    }

    /**
     * Applies a filter on the collection and returns a cursor to the
     * selected documents.
     * <p>
     * See {@link Filter} for all available filters.
     * <p>
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     * </p>
     *
     * @param filter the filter to apply to select documents from collection.
     * @return a cursor to all selected documents.
     * @throws ValidationException if {@code filter} is null.
     * @see Filter
     * @see DocumentCursor#project(Document)
     */
    default DocumentCursor find(Filter filter) {
        return find(filter, null);
    }

    /**
     * Returns a customized cursor to all documents in the collection.
     *
     * @param findOptions specifies pagination, sort options for the cursor.
     * @return a cursor to all selected documents.
     * @see SortOrder
     */
    default DocumentCursor find(FindOptions findOptions) {
        return find(Filter.ALL, findOptions);
    }

    /**
     * Applies a filter on the collection and returns a customized cursor to the
     * selected documents.
     *
     * <p>
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     * </p>
     *
     * @param filter      the filter to apply to select documents from collection.
     * @param findOptions specifies pagination, sort options for the cursor.
     * @return a cursor to all selected documents.
     */
    DocumentCursor find(Filter filter, FindOptions findOptions);

    /**
     * Gets a single element from the collection by its id. If no element
     * is found, it will return {@code null}.
     *
     * @param cropId the crop id
     * @return the unique document associated with the crop id.
     * @throws ValidationException if `cropId` is {@code null}.
     */
    Document getById(CropId cropId);

    /**
     * Returns the name of the {@link CropCollection}.
     *
     * @return the name.
     */
    String getName();
}
