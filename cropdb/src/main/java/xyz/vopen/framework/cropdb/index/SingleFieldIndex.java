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
import xyz.vopen.framework.cropdb.filters.ComparableFilter;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.common.util.IndexUtils;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a crop index on a single field.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class SingleFieldIndex implements CropIndex {
    @Getter
    private final IndexDescriptor indexDescriptor;
    private final CropStore<?> cropStore;

    /**
     * Instantiates a new {@link SingleFieldIndex}.
     *
     * @param indexDescriptor the index descriptor
     * @param cropStore    the crop store
     */
    public SingleFieldIndex(IndexDescriptor indexDescriptor, CropStore<?> cropStore) {
        this.indexDescriptor = indexDescriptor;
        this.cropStore = cropStore;
    }

    @Override
    public void write(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        CropMap<DBValue, List<?>> indexMap = findIndexMap();

        if (element == null) {
            addIndexElement(indexMap, fieldValues, DBNull.getInstance());
        } else if (element instanceof Comparable) {
            // wrap around db value
            DBValue dbValue = new DBValue((Comparable<?>) element);
            addIndexElement(indexMap, fieldValues, dbValue);
        } else if (element.getClass().isArray()) {
            Object[] array = ObjectUtils.convertToObjectArray(element);

            for (Object item : array) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                addIndexElement(indexMap, fieldValues, dbValue);
            }
        } else if (element instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) element;

            for (Object item : iterable) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                addIndexElement(indexMap, fieldValues, dbValue);
            }
        }
    }

    @Override
    public void remove(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        CropMap<DBValue, List<?>> indexMap = findIndexMap();
        if (element == null) {
            removeIndexElement(indexMap, fieldValues, DBNull.getInstance());
        } else if (element instanceof Comparable) {
            // wrap around db value
            DBValue dbValue = new DBValue((Comparable<?>) element);
            removeIndexElement(indexMap, fieldValues, dbValue);
        } else if (element.getClass().isArray()) {
            Object[] array = ObjectUtils.convertToObjectArray(element);

            for (Object item : array) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                removeIndexElement(indexMap, fieldValues, dbValue);
            }
        } else if (element instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) element;

            for (Object item : iterable) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                removeIndexElement(indexMap, fieldValues, dbValue);
            }
        }
    }

    @Override
    public void drop() {
        CropMap<DBValue, List<?>> indexMap = findIndexMap();
        indexMap.clear();
        indexMap.drop();
    }

    @Override
    public LinkedHashSet<CropId> findCropIds(FindPlan findPlan) {
        if (findPlan.getIndexScanFilter() == null) return new LinkedHashSet<>();

        CropMap<DBValue, List<?>> indexMap = findIndexMap();
        return scanIndex(findPlan, indexMap);
    }

    @SuppressWarnings("unchecked")
    private void addIndexElement(CropMap<DBValue, List<?>> indexMap,
                                 FieldValues fieldValues, DBValue element) {
        List<CropId> cropIds = (List<CropId>) indexMap.get(element);
        cropIds = addCropIds(cropIds, fieldValues);
        indexMap.put(element, cropIds);
    }

    @SuppressWarnings("unchecked")
    private void removeIndexElement(CropMap<DBValue, List<?>> indexMap,
                                    FieldValues fieldValues, DBValue element) {
        List<CropId> cropIds = (List<CropId>) indexMap.get(element);
        if (cropIds != null && !cropIds.isEmpty()) {
            cropIds.remove(fieldValues.getCropId());
            if (cropIds.size() == 0) {
                indexMap.remove(element);
            } else {
                indexMap.put(element, cropIds);
            }
        }
    }

    private CropMap<DBValue, List<?>> findIndexMap() {
        String mapName = IndexUtils.deriveIndexMapName(indexDescriptor);
        return cropStore.openMap(mapName, DBValue.class, CopyOnWriteArrayList.class);
    }

    private LinkedHashSet<CropId> scanIndex(FindPlan findPlan,
                                            CropMap<DBValue, List<?>> indexMap) {
        List<ComparableFilter> filters = findPlan.getIndexScanFilter().getFilters();
        IndexMap iMap = new IndexMap(indexMap);
        IndexScanner indexScanner = new IndexScanner(iMap);
        return indexScanner.doScan(filters, findPlan.getIndexScanOrder());
    }
}
