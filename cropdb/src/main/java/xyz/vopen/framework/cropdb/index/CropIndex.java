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

package xyz.vopen.framework.cropdb.index;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.exceptions.UniqueConstraintException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a crop index.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface CropIndex {
    /**
     * Gets index descriptor.
     *
     * @return the index descriptor
     */
    IndexDescriptor getIndexDescriptor();

    /**
     * Writes a {@link FieldValues} in the index.
     *
     * @param fieldValues the field values
     */
    void write(FieldValues fieldValues);

    /**
     * Removes a {@link FieldValues} from the index.
     *
     * @param fieldValues the field values
     */
    void remove(FieldValues fieldValues);

    /**
     * Drops this index.
     */
    void drop();

    /**
     * Finds a set of {@link CropId}s from the index after executing the {@link FindPlan}.
     *
     * @param findPlan the find plan
     * @return the linked hash set
     */
    LinkedHashSet<CropId> findCropIds(FindPlan findPlan);

    /**
     * Indicates if this is an unique index.
     *
     * @return the boolean
     */
    default boolean isUnique() {
        return getIndexDescriptor().getIndexType().equalsIgnoreCase(IndexType.UNIQUE);
    }

    /**
     * Validates the index field.
     *
     * @param value the value
     * @param field the field
     */
    default void validateIndexField(Object value, String field) {
        if (value == null) return;
        if (value instanceof Iterable) {
            ValidationUtils.validateIterableIndexField((Iterable<?>) value, field);
        } else if (value.getClass().isArray()) {
            ValidationUtils.validateArrayIndexField(value, field);
        } else {
            if (!(value instanceof Comparable)) {
                throw new ValidationException(value + " is not comparable");
            }
        }
    }

    /**
     * Adds a {@link CropId} of the {@link FieldValues} to the existing indexed list of {@link CropId}s.
     *
     * @param cropIds  the crop ids
     * @param fieldValues the field values
     * @return the list
     */
    default List<CropId> addCropIds(List<CropId> cropIds, FieldValues fieldValues) {
        if (cropIds == null) {
            cropIds = new CopyOnWriteArrayList<>();
        }

        if (isUnique() && cropIds.size() == 1
            && !cropIds.contains(fieldValues.getCropId())) {
            // if key is already exists for unique type, throw error
            throw new UniqueConstraintException("unique key constraint violation for " + fieldValues.getFields());
        }

        // index always are in ascending format
        cropIds.add(fieldValues.getCropId());
        return cropIds;
    }

    /**
     * Removes a {@link CropId} of the {@link FieldValues} from the existing indexed list of {@link CropId}s.
     *
     * @param cropIds  the crop ids
     * @param fieldValues the field values
     * @return the list
     */
    default List<CropId> removeCropIds(List<CropId> cropIds, FieldValues fieldValues) {
        if (cropIds != null && !cropIds.isEmpty()) {
            cropIds.remove(fieldValues.getCropId());
        }
        return cropIds;
    }
}
