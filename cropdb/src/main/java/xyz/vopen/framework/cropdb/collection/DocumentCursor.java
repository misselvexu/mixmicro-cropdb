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

import xyz.vopen.framework.cropdb.common.Lookup;
import xyz.vopen.framework.cropdb.common.RecordStream;

/**
 * An interface to iterate over database {@code find()} results. It provides a
 * mechanism to iterate over all {@link CropId}s of the result.
 * <pre>
 * {@code
 * // create/open a database
 * Crop db = Crop.builder()
 *      .openOrCreate("user", "password");
 *
 * // create/open a database
 * Crop db = Crop.builder()
 *  .openOrCreate("user", "password");
 *
 * // create a collection named - test
 * CropCollection collection = db.getCollection("test");
 *
 * // returns all ids un-filtered
 * DocumentCursor result = collection.find();
 *
 * for (Document doc : result) {
 *  // use your logic with the retrieved doc here
 * }
 *
 * }*
 * </pre>
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface DocumentCursor extends RecordStream<Document> {
    /**
     * Gets a filter plan for the query.
     *
     * @return the filter plan
     */
    FindPlan getFindPlan();

    /**
     * Gets a lazy iterable containing all the selected keys of the result documents.
     *
     * @param projection the selected keys of a result document.
     * @return a lazy iterable of documents.
     */
    RecordStream<Document> project(Document projection);

    /**
     * Performs a left outer join with a foreign cursor with the specified lookup parameters.
     * <p>
     * It performs an equality match on the localString to the foreignString from the documents of the foreign cursor.
     * If an input document does not contain the localString, the join treats the field as having a value of `null`
     * for matching purposes.
     *
     * @param foreignCursor the foreign cursor for the join.
     * @param lookup        the lookup parameter for the join operation.
     * @return a lazy iterable of joined documents.
     * @since 2.1.0
     */
    RecordStream<Document> join(DocumentCursor foreignCursor, Lookup lookup);
}
