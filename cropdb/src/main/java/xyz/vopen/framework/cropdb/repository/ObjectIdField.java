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
import lombok.Setter;
import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.filters.Filter;
import xyz.vopen.framework.cropdb.filters.CropFilter;
import xyz.vopen.framework.cropdb.repository.annotations.Embedded;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static xyz.vopen.framework.cropdb.filters.FluentFilter.where;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class ObjectIdField {
    private final Reflector reflector;
    private final IndexValidator indexValidator;
    private String[] embeddedFieldNames;

    @Getter
    @Setter
    private Field field;

    @Getter
    @Setter
    private boolean isEmbedded;

    @Getter
    @Setter
    private String idFieldName;

    public ObjectIdField() {
        this.reflector = new Reflector();
        this.indexValidator = new IndexValidator(reflector);
    }

    public String[] getFieldNames(CropMapper cropMapper) {
        if (embeddedFieldNames != null) {
            return embeddedFieldNames;
        }

        if (!isEmbedded) {
            embeddedFieldNames = new String[]{ idFieldName };
            return embeddedFieldNames;
        }

        List<Field> fieldList = reflector.getAllFields(field.getType());
        NavigableMap<Integer, String> orderedFieldName = new TreeMap<>();

        boolean embeddedFieldFound = false;
        for (Field field : fieldList) {
            if (field.isAnnotationPresent(Embedded.class)) {
                embeddedFieldFound = true;
                Embedded embedded = field.getAnnotation(Embedded.class);
                int order = embedded.order();
                String fieldName = StringUtils.isNullOrEmpty(embedded.fieldName())
                    ? field.getName() : embedded.fieldName();

                String name = this.idFieldName + CropConfig.getFieldSeparator() + fieldName;
                indexValidator.validate(field.getType(), name, cropMapper);

                orderedFieldName.put(order, name);
            }
        }

        if (!embeddedFieldFound) {
            throw new IndexingException("no embedded field found for " + field.getName());
        }

        embeddedFieldNames = orderedFieldName.values().toArray(new String[0]);
        return embeddedFieldNames;
    }

    public Filter createUniqueFilter(Object value, CropMapper cropMapper) {
        if (embeddedFieldNames.length == 1) {
            return where(idFieldName).eq(value);
        } else {
            Document document = cropMapper.convert(value, Document.class);
            Filter[] filters = new Filter[embeddedFieldNames.length];

            int index = 0;
            for (String field : embeddedFieldNames) {
                String docFieldName = getEmbeddedFieldName(field);
                Object fieldValue = document.get(docFieldName);
                filters[index++] = where(field).eq(fieldValue);
            }

            CropFilter cropFilter = (CropFilter) Filter.and(filters);
            cropFilter.setObjectFilter(true);
            return cropFilter;
        }
    }

    private String getEmbeddedFieldName(String fieldName) {
        if (fieldName.contains(CropConfig.getFieldSeparator())) {
            return fieldName.substring(fieldName.indexOf(CropConfig.getFieldSeparator()) + 1);
        } else {
            return fieldName;
        }
    }
}
