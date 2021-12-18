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

package xyz.vopen.framework.cropdb.common.mapper;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.exceptions.ObjectMappingException;
import xyz.vopen.framework.cropdb.common.util.Iterables;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;

import java.util.*;

/**
 * A {@link CropMapper} based on {@link Mappable} implementation.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0
 */
public class MappableMapper implements CropMapper {
  private final Set<Class<?>> valueTypes;

  /**
   * Instantiates a new {@link MappableMapper}.
   *
   * @param valueTypes the value types
   */
  public MappableMapper(Class<?>... valueTypes) {
    this.valueTypes = new HashSet<>();
    init(Iterables.listOf(valueTypes));
  }

  /**
   * Converts a document to a target object of type <code>Target</code>.
   *
   * @param <Target> the type parameter
   * @param source the source
   * @param type the type
   * @return the target
   */
  protected <Target> Target convertFromDocument(Document source, Class<Target> type) {
    if (source == null) {
      return null;
    }

    if (Mappable.class.isAssignableFrom(type)) {
      Target item = ObjectUtils.newInstance(type, false);
      if (item == null) return null;

      ((Mappable) item).read(this, source);
      return item;
    }

    throw new ObjectMappingException("object must implements Mappable");
  }

  /**
   * Converts an object of type <code>Source</code> to a document.
   *
   * @param <Source> the type parameter
   * @param source the source
   * @return the document
   */
  protected <Source> Document convertToDocument(Source source) {
    if (source instanceof Mappable) {
      Mappable mappable = (Mappable) source;
      return mappable.write(this);
    }

    throw new ObjectMappingException("object must implements Mappable");
  }

  /**
   * Adds a value type to ignore during mapping.
   *
   * @param valueType the value type
   */
  protected void addValueType(Class<?> valueType) {
    this.valueTypes.add(valueType);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <Source, Target> Target convert(Source source, Class<Target> type) {
    if (source == null) {
      return null;
    }

    if (isValue(source)) {
      return (Target) source;
    } else {
      if (Document.class.isAssignableFrom(type)) {
        return (Target) convertToDocument(source);
      } else if (source instanceof Document) {
        return convertFromDocument((Document) source, type);
      }
    }

    throw new ObjectMappingException("object must implements Mappable");
  }

  @Override
  public boolean isValueType(Class<?> type) {
    if (type.isPrimitive() && type != void.class) return true;
    if (valueTypes.contains(type)) return true;
    for (Class<?> valueType : valueTypes) {
      if (valueType.isAssignableFrom(type)) return true;
    }
    return false;
  }

  @Override
  public boolean isValue(Object object) {
    return isValueType(object.getClass());
  }

  @Override
  public void initialize(CropConfig cropConfig) {}

  private void init(List<Class<?>> valueTypes) {
    this.valueTypes.add(Number.class);
    this.valueTypes.add(Boolean.class);
    this.valueTypes.add(Character.class);
    this.valueTypes.add(String.class);
    this.valueTypes.add(byte[].class);
    this.valueTypes.add(Enum.class);
    this.valueTypes.add(CropId.class);
    this.valueTypes.add(Date.class);

    if (valueTypes != null && !valueTypes.isEmpty()) {
      this.valueTypes.addAll(valueTypes);
    }
  }
}
