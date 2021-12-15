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

package xyz.vopen.framework.cropdb.index;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.module.CropPlugin;

import java.util.LinkedHashSet;

/**
 * Represents an indexer for creating a crop index.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0
 */
public interface CropIndexer extends CropPlugin {
    /**
     * Gets the index type.
     *
     * @return the index type
     */
    String getIndexType();

    /**
     * Validates an index on the fields.
     *
     * @param fields the fields
     */
    void validateIndex(Fields fields);

    /**
     * Drops the index specified by the index descriptor.
     *
     * @param indexDescriptor the index descriptor
     * @param cropConfig   the crop config
     */
    void dropIndex(IndexDescriptor indexDescriptor, CropConfig cropConfig);

    /**
     * Writes an index entry.
     *
     * @param fieldValues     the field values
     * @param indexDescriptor the index descriptor
     * @param cropConfig   the crop config
     */
    void writeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, CropConfig cropConfig);

    /**
     * Removes an index entry.
     *
     * @param fieldValues     the field values
     * @param indexDescriptor the index descriptor
     * @param cropConfig   the crop config
     */
    void removeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, CropConfig cropConfig);

    /**
     * Finds a list of {@link CropId} after executing the {@link FindPlan} on the index.
     *
     * @param findPlan      the find plan
     * @param cropConfig the crop config
     * @return the linked hash set
     */
    LinkedHashSet<CropId> findByFilter(FindPlan findPlan, CropConfig cropConfig);
}
