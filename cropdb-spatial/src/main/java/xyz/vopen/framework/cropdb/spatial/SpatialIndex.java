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

package xyz.vopen.framework.cropdb.spatial;

import lombok.Getter;
import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.exceptions.FilterException;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.filters.ComparableFilter;
import xyz.vopen.framework.cropdb.filters.IndexScanFilter;
import xyz.vopen.framework.cropdb.index.BoundingBox;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.CropIndex;
import xyz.vopen.framework.cropdb.store.CropRTree;
import xyz.vopen.framework.cropdb.store.CropStore;
import org.locationtech.jts.geom.Geometry;
import xyz.vopen.framework.cropdb.common.util.IndexUtils;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Represents a spatial index in crop.
 *
 * @since 4.0.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class SpatialIndex implements CropIndex {
    @Getter
    private final IndexDescriptor indexDescriptor;
    private final CropStore<?> cropStore;
    private final CropConfig cropConfig;

    /**
     * Instantiates a new {@link SpatialIndex}.
     *
     * @param indexDescriptor the index descriptor
     * @param cropConfig   the crop config
     */
    public SpatialIndex(IndexDescriptor indexDescriptor, CropConfig cropConfig) {
        this.indexDescriptor = indexDescriptor;
        this.cropConfig = cropConfig;
        this.cropStore = cropConfig.getCropStore();
    }

    @Override
    public void write(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        CropRTree<BoundingBox, Geometry> indexMap = findIndexMap();
        if (element == null) {
            indexMap.add(null, fieldValues.getCropId());
        } else {
            Geometry geometry = parseGeometry(firstField, element);
            BoundingBox boundingBox = new CropBoundingBox(geometry);
            indexMap.add(boundingBox, fieldValues.getCropId());
        }
    }

    @Override
    public void remove(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        CropRTree<BoundingBox, Geometry> indexMap = findIndexMap();
        if (element == null) {
            indexMap.remove(null, fieldValues.getCropId());
        } else {
            Geometry geometry = parseGeometry(firstField, element);
            BoundingBox boundingBox = new CropBoundingBox(geometry);
            indexMap.remove(boundingBox, fieldValues.getCropId());
        }
    }

    @Override
    public void drop() {
        CropRTree<BoundingBox, Geometry> indexMap = findIndexMap();
        indexMap.clear();
        indexMap.drop();
    }

    @Override
    public LinkedHashSet<CropId> findCropIds(FindPlan findPlan) {
        IndexScanFilter indexScanFilter = findPlan.getIndexScanFilter();
        if (indexScanFilter == null
            || indexScanFilter.getFilters() == null
            || indexScanFilter.getFilters().isEmpty()) {
            throw new FilterException("no spatial filter found");
        }

        List<ComparableFilter> filters = indexScanFilter.getFilters();
        ComparableFilter filter = filters.get(0);

        if (!(filter instanceof SpatialFilter)) {
            throw new FilterException("spatial filter must be the first filter for index scan");
        }

        RecordStream<CropId> keys = null;
        CropRTree<BoundingBox, Geometry> indexMap = findIndexMap();

        SpatialFilter spatialFilter = (SpatialFilter) filter;
        Geometry geometry = spatialFilter.getValue();
        BoundingBox boundingBox = new CropBoundingBox(geometry);

        if (filter instanceof WithinFilter) {
            keys = indexMap.findContainedKeys(boundingBox);
        } else if (filter instanceof IntersectsFilter) {
            keys = indexMap.findIntersectingKeys(boundingBox);
        }

        LinkedHashSet<CropId> cropIds = new LinkedHashSet<>();
        if (keys != null) {
            for (CropId cropId : keys) {
                cropIds.add(cropId);
            }
        }

        return cropIds;
    }

    private CropRTree<BoundingBox, Geometry> findIndexMap() {
        String mapName = IndexUtils.deriveIndexMapName(indexDescriptor);
        return cropStore.openRTree(mapName, BoundingBox.class, Geometry.class);
    }

    private Geometry parseGeometry(String field, Object fieldValue) {
        if (fieldValue == null) return null;
        if (fieldValue instanceof String) {
            return cropConfig.cropMapper().convert(fieldValue, Geometry.class);
        } else if (fieldValue instanceof Geometry) {
            return (Geometry) fieldValue;
        }
        throw new IndexingException("field " + field + " does not contain Geometry data");
    }
}
