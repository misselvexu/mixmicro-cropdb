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

package xyz.vopen.framework.cropdb.repository;

import lombok.Getter;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.exceptions.NotIdentifiableException;
import xyz.vopen.framework.cropdb.repository.annotations.*;

import java.lang.reflect.Field;
import java.util.*;

import static xyz.vopen.framework.cropdb.index.IndexOptions.indexOptions;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class AnnotationScanner {
    private final Set<Index> indices;
    private final Class<?> type;
    private final CropMapper cropMapper;
    private final Reflector reflector;
    private final CropCollection collection;
    private final IndexValidator indexValidator;

    @Getter
    private ObjectIdField objectIdField;

    public AnnotationScanner(Class<?> type, CropCollection collection, CropMapper cropMapper) {
        this.type = type;
        this.cropMapper = cropMapper;
        this.collection = collection;
        this.reflector = new Reflector();
        this.indices = new HashSet<>();
        this.indexValidator = new IndexValidator(reflector);
    }

    public void createIndices() {
        for (Index index : indices) {
            String[] fields = index.value();
            if (!collection.hasIndex(fields)) {
                collection.createIndex(indexOptions(index.type()), fields);
            }
        }
    }

    public void createIdIndex() {
        if (objectIdField != null) {
            String[] fieldNames = objectIdField.getFieldNames(cropMapper);
            if (!collection.hasIndex(fieldNames)) {
                collection.createIndex(fieldNames);
            }
        }
    }

    public void scanIndices() {
        // populate from @Indices
        scanIndicesAnnotation();

        // populate from @Index
        scanIndexAnnotation();

        // populate from @Entity
        scanEntityAnnotation();

        // populate from @Id
        scanIdAnnotation();
    }

    private void scanIndicesAnnotation() {
        List<Indices> indicesList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indicesList = reflector.findInheritedAnnotations(Indices.class, type);
        } else {
            indicesList = new ArrayList<>();
            Indices indices = type.getAnnotation(Indices.class);
            if (indices != null) indicesList.add(indices);
        }

        for (Indices indices : indicesList) {
            Index[] indexList = indices.value();
            populateIndex(Arrays.asList(indexList));
        }
    }

    private void scanIndexAnnotation() {
        List<Index> indexList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indexList = reflector.findInheritedAnnotations(Index.class, type);
        } else {
            indexList = new ArrayList<>();
            Index index = type.getAnnotation(Index.class);
            if (index != null) indexList.add(index);
        }
        populateIndex(indexList);
    }

    private void scanEntityAnnotation() {
        List<Index> indexList = new ArrayList<>();
        if (type.isAnnotationPresent(InheritIndices.class)) {
            List<Entity> entities = reflector.findInheritedAnnotations(Entity.class, type);
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    indexList.addAll(Arrays.asList(entity.indices()));
                }
            }
        } else if (type.isAnnotationPresent(Entity.class)) {
            Entity entity = type.getAnnotation(Entity.class);
            indexList.addAll(Arrays.asList(entity.indices()));
        }

        populateIndex(indexList);
    }

    private void scanIdAnnotation() {
        List<Field> fieldList = reflector.getAllFields(type);

        boolean alreadyIdFound = false;
        for (Field field : fieldList) {
            if (field.isAnnotationPresent(Id.class)) {
                Id id = field.getAnnotation(Id.class);
                String fieldName = StringUtils.isNullOrEmpty(id.fieldName()) ? field.getName() : id.fieldName();
                indexValidator.validate(field.getType(), fieldName, cropMapper);
                if (alreadyIdFound) {
                    throw new NotIdentifiableException("multiple id fields found for the type");
                } else {
                    alreadyIdFound = true;
                    objectIdField = new ObjectIdField();
                    objectIdField.setField(field);
                    objectIdField.setIdFieldName(fieldName);
                    objectIdField.setEmbedded(isEmbeddedId(field));
                }
            }
        }
    }

    private boolean isEmbeddedId(Field field) {
        List<Field> fields = reflector.getAllFields(field.getType());
        if (fields.size() == 0) return false;

        for (Field f : fields) {
            if (f.isAnnotationPresent(Embedded.class)) {
                return true;
            }
        }
        return false;
    }

    private void populateIndex(List<Index> indexList) {
        for (Index index : indexList) {
            String[] names = index.value();
            List<Field> entityFields = new ArrayList<>();

            for (String name : names) {
                Field field = reflector.getField(type, name);
                if (field != null) {
                    entityFields.add(field);
                    indexValidator.validate(field.getType(), field.getName(), cropMapper);
                }
            }

            if (entityFields.size() == names.length) {
                // validation for all field are success
                indices.add(index);
            }
        }
    }
}
