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

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.repository.annotations.Embedded;
import xyz.vopen.framework.cropdb.repository.annotations.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import static xyz.vopen.framework.cropdb.common.util.DocumentUtils.skeletonDocument;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class IndexValidator {
    private final Reflector reflector;

    public IndexValidator(Reflector reflector) {
        this.reflector = reflector;
    }

    /**
     * Validate an index field of an {@link Entity} object.
     *
     * @param fieldType     the field type
     * @param field         the field
     * @param cropMapper the crop mapper
     */
    public void validate(Class<?> fieldType, String field, CropMapper cropMapper) {
        if (fieldType.isPrimitive()
            || fieldType == CropId.class
            || fieldType.isInterface()
            || cropMapper.isValueType(fieldType)
            || Modifier.isAbstract(fieldType.getModifiers())
            || fieldType.isArray()
            || Iterable.class.isAssignableFrom(fieldType)) {
            // we will validate the solid class during insertion/update
            return;
        }

        Document document;
        try {
            document = skeletonDocument(cropMapper, fieldType);
            if (document.size() > 0) {
                // compound index
                boolean embeddedFieldFound = false;
                List<Field> fields = reflector.getAllFields(fieldType);
                for (Field indexField : fields) {
                    if (indexField.isAnnotationPresent(Embedded.class)) {
                        embeddedFieldFound = true;
                        break;
                    }
                }

                if (!embeddedFieldFound) {
                    throw new IndexingException("no embedded field found for object id");
                }
            } else {
                if (!Comparable.class.isAssignableFrom(fieldType)) {
                    throw new IndexingException("cannot index on non comparable field " + field);
                }
            }
        } catch (IndexingException ie) {
            throw ie;
        } catch (Throwable e) {
            throw new IndexingException("invalid type specified " + fieldType.getName() + " for indexing", e);
        }
    }
}
