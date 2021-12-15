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

import lombok.Getter;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.common.DBNull;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.filters.ComparableFilter;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.common.util.IndexUtils;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Represents a crop compound index.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class CompoundIndex implements CropIndex {
    @Getter
    private final IndexDescriptor indexDescriptor;
    private final CropStore<?> cropStore;

    /**
     * Instantiates a new Compound index.
     *
     * @param indexDescriptor the index descriptor
     * @param cropStore    the crop store
     */
    public CompoundIndex(IndexDescriptor indexDescriptor, CropStore<?> cropStore) {
        this.indexDescriptor = indexDescriptor;
        this.cropStore = cropStore;
    }

    public void write(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object firstValue = fieldValues.get(firstField);

        // NOTE: only first field can have array or iterable value, subsequent fields can not
        validateIndexField(firstValue, firstField);

        CropMap<DBValue, NavigableMap<DBValue, ?>> indexMap = findIndexMap();
        if (firstValue == null) {
            addIndexElement(indexMap, fieldValues, DBNull.getInstance());
        } else if (firstValue instanceof Comparable) {
            //wrap around a db value
            DBValue dbValue = new DBValue((Comparable<?>) firstValue);
            addIndexElement(indexMap, fieldValues, dbValue);
        } else if (firstValue.getClass().isArray()) {
            Object[] array = ObjectUtils.convertToObjectArray(firstValue);

            for (Object item : array) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                addIndexElement(indexMap, fieldValues, dbValue);
            }
        } else if (firstValue instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) firstValue;

            for (Object item : iterable) {
                // wrap around db value
                DBValue dbValue = item != null ? new DBValue((Comparable<?>) item) : DBNull.getInstance();
                addIndexElement(indexMap, fieldValues, dbValue);
            }
        }
    }

    @Override
    public void remove(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object firstValue = fieldValues.get(firstField);

        // NOTE: only first field can have array or iterable value, subsequent fields can not
        validateIndexField(firstValue, firstField);
        CropMap<DBValue, NavigableMap<DBValue, ?>> indexMap = findIndexMap();

        if (firstValue == null) {
            removeIndexElement(indexMap, fieldValues, DBNull.getInstance());
        } else if (firstValue instanceof Comparable) {
            // wrap around db value
            DBValue dbValue = new DBValue((Comparable<?>) firstValue);
            removeIndexElement(indexMap, fieldValues, dbValue);
        } else if (firstValue.getClass().isArray()) {
            Object[] array = ObjectUtils.convertToObjectArray(firstValue);

            for (Object item : array) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                removeIndexElement(indexMap, fieldValues, dbValue);
            }
        } else if (firstValue instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) firstValue;

            for (Object item : iterable) {
                // wrap around db value
                DBValue dbValue = item != null ? new DBValue((Comparable<?>) item) : DBNull.getInstance();
                removeIndexElement(indexMap, fieldValues, dbValue);
            }
        }
    }

    @Override
    public void drop() {
        CropMap<DBValue, NavigableMap<DBValue, ?>> indexMap = findIndexMap();
        indexMap.clear();
        indexMap.drop();
    }

    @Override
    public LinkedHashSet<CropId> findCropIds(FindPlan findPlan) {
        if (findPlan.getIndexScanFilter() == null) return new LinkedHashSet<>();

        CropMap<DBValue, NavigableMap<DBValue, ?>> indexMap = findIndexMap();
        return scanIndex(findPlan, indexMap);
    }

    private void addIndexElement(CropMap<DBValue, NavigableMap<DBValue, ?>> indexMap,
                                 FieldValues fieldValues, DBValue element) {
        NavigableMap<DBValue, ?> subMap = indexMap.get(element);
        if (subMap == null) {
            // index are always in ascending order
            subMap = new ConcurrentSkipListMap<>();
        }

        populateSubMap(subMap, fieldValues, 1);
        indexMap.put(element, subMap);
    }

    private void removeIndexElement(CropMap<DBValue, NavigableMap<DBValue, ?>> indexMap,
                                    FieldValues fieldValues, DBValue element) {
        NavigableMap<DBValue, ?> subMap = indexMap.get(element);
        if (subMap != null && !subMap.isEmpty()) {
            deleteFromSubMap(subMap, fieldValues, 1);
            indexMap.put(element, subMap);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void populateSubMap(NavigableMap subMap, FieldValues fieldValues, int startIndex) {
        if (startIndex >= fieldValues.getValues().size()) return;

        Pair<String, Object> pair = fieldValues.getValues().get(startIndex);
        Object value = pair.getSecond();
        DBValue dbValue;
        if (value == null) {
            dbValue = DBNull.getInstance();
        } else {
            if (Iterable.class.isAssignableFrom(value.getClass()) || value.getClass().isArray()) {
                throw new IndexingException("compound multikey index is supported on the first field of the index only");
            }

            if (!(value instanceof Comparable)) {
                throw new IndexingException(value + " is not comparable");
            }
            dbValue = new DBValue((Comparable<?>) value);
        }

        if (startIndex == fieldValues.getValues().size() - 1) {
            // terminal field
            List<CropId> cropIds = (List<CropId>) subMap.get(dbValue);
            cropIds = addCropIds(cropIds, fieldValues);
            subMap.put(dbValue, cropIds);
        } else {
            // intermediate fields
            NavigableMap subMap2 = (NavigableMap) subMap.get(dbValue);
            if (subMap2 == null) {
                // index are always in ascending order
                subMap2 = new ConcurrentSkipListMap<>();
            }

            subMap.put(dbValue, subMap2);
            populateSubMap(subMap2, fieldValues, startIndex + 1);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void deleteFromSubMap(NavigableMap subMap, FieldValues fieldValues, int startIndex) {
        Pair<String, Object> pair = fieldValues.getValues().get(startIndex);
        Object value = pair.getSecond();
        DBValue dbValue;
        if (value == null) {
            dbValue = DBNull.getInstance();
        } else {
            if (!(value instanceof Comparable)) {
                return;
            }
            dbValue = new DBValue((Comparable<?>) value);
        }

        if (startIndex == fieldValues.getValues().size() - 1) {
            // terminal field
            List<CropId> cropIds = (List<CropId>) subMap.get(dbValue);
            cropIds = removeCropIds(cropIds, fieldValues);
            if (cropIds == null || cropIds.isEmpty()) {
                subMap.remove(dbValue);
            } else {
                subMap.put(dbValue, cropIds);
            }
        } else {
            // intermediate fields
            NavigableMap subMap2 = (NavigableMap) subMap.get(dbValue);
            if (subMap2 == null) {
                return;
            }

            deleteFromSubMap(subMap2, fieldValues, startIndex + 1);
            subMap.put(dbValue, subMap2);
        }
    }

    private CropMap<DBValue, NavigableMap<DBValue, ?>> findIndexMap() {
        String mapName = IndexUtils.deriveIndexMapName(indexDescriptor);
        return cropStore.openMap(mapName, DBValue.class, ConcurrentSkipListMap.class);
    }

    private LinkedHashSet<CropId> scanIndex(FindPlan findPlan,
                                            CropMap<DBValue, NavigableMap<DBValue, ?>> indexMap) {
        List<ComparableFilter> filters = findPlan.getIndexScanFilter().getFilters();
        IndexMap iMap = new IndexMap(indexMap);
        IndexScanner indexScanner = new IndexScanner(iMap);
        return indexScanner.doScan(filters, findPlan.getIndexScanOrder());
    }
}
