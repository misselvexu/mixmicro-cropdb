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

package xyz.vopen.framework.cropdb.filters;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import static xyz.vopen.framework.cropdb.common.util.ValidationUtils.notEmpty;

/**
 * Represents a filter based on value of a crop document field.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class FieldBasedFilter extends CropFilter {
  private String field;

  @Getter(AccessLevel.NONE)
  private Object value;

  @Getter(AccessLevel.NONE)
  private boolean processed = false;

  /**
   * Instantiates a new Field based filter.
   *
   * @param field the field
   * @param value the value
   */
  protected FieldBasedFilter(String field, Object value) {
    this.field = field;
    this.value = value;
  }

  /**
   * Gets the value fo the filter.
   *
   * @return the value
   */
  public Object getValue() {
    if (this.processed) return value;

    if (value == null) return null;

    if (getObjectFilter()) {
      CropMapper cropMapper = getCropConfig().cropMapper();
      validateSearchTerm(cropMapper, field, value);
      if (cropMapper.isValue(value)) {
        value = cropMapper.convert(value, Comparable.class);
      }
    }

    this.processed = true;
    return value;
  }

  protected void validateSearchTerm(CropMapper cropMapper, String field, Object value) {
    ValidationUtils.notNull(field, "field cannot be null");
    ValidationUtils.notEmpty(field, "field cannot be empty");

    if (value != null) {
      if (!cropMapper.isValue(value) && !(value instanceof Comparable)) {
        throw new ValidationException("search term is not comparable " + value);
      }
    }
  }
}
