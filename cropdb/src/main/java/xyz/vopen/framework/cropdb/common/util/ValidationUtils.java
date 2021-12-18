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

package xyz.vopen.framework.cropdb.common.util;

import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;

import java.util.Collection;

import static xyz.vopen.framework.cropdb.common.util.ObjectUtils.convertToObjectArray;
import static xyz.vopen.framework.cropdb.common.util.StringUtils.isNullOrEmpty;

/**
 * A validation utility class.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
public class ValidationUtils {
  private ValidationUtils() {}

  /**
   * Validates if a string is empty or `null`.
   *
   * @param value the string value
   * @param message the error message
   */
  public static void notEmpty(String value, String message) {
    if (StringUtils.isNullOrEmpty(value)) {
      throw new ValidationException(message);
    }
  }

  /**
   * Validates if a {@link CharSequence} is empty or `null`.
   *
   * @param value the value
   * @param message the message
   */
  public static void notEmpty(CharSequence value, String message) {
    if (StringUtils.isNullOrEmpty(value)) {
      throw new ValidationException(message);
    }
  }

  /**
   * Validates if a {@link CharSequence} is empty or `null`.
   *
   * @param value the value
   * @param message the message
   */
  public static void notEmpty(Collection<?> value, String message) {
    if (value.isEmpty()) {
      throw new ValidationException(message);
    }
  }

  public static <T> void notEmpty(T[] value, String message) {
    if (value.length == 0) {
      throw new ValidationException(message);
    }
  }

  /**
   * Validates if an object is `null`.
   *
   * @param value the object
   * @param message the message
   */
  public static void notNull(Object value, String message) {
    if (value == null) {
      throw new ValidationException(message);
    }
  }

  /**
   * Validates if an array contains `null` item.
   *
   * @param <T> the type parameter
   * @param array the array to check for `null` object
   * @param message the message
   */
  public static <T> void containsNull(T[] array, String message) {
    for (T element : array) {
      if (element == null) {
        throw new ValidationException(message);
      }
    }
  }

  public static void validateIterableIndexField(Iterable<?> fieldValue, String field) {
    if (fieldValue != null) {
      for (Object value : fieldValue) {
        if (value == null) continue;
        validateArrayIndexItem(value, field);
      }
    }
  }

  public static void validateStringIterableIndexField(Iterable<?> fieldValue, String field) {
    if (fieldValue != null) {
      for (Object value : fieldValue) {
        if (value == null) continue;
        validateStringArrayItem(value, field);
      }
    }
  }

  public static void validateArrayIndexField(Object arrayValue, String field) {
    if (arrayValue != null) {
      Object[] array = convertToObjectArray(arrayValue);
      for (Object value : array) {
        if (value == null) continue;
        validateArrayIndexItem(value, field);
      }
    }
  }

  public static void validateStringArrayIndexField(Object arrayValue, String field) {
    if (arrayValue != null) {
      Object[] array = convertToObjectArray(arrayValue);
      for (Object value : array) {
        if (value == null) continue;
        validateStringArrayItem(value, field);
      }
    }
  }

  public static void validateFilterArrayField(Object arrayValue, String field) {
    if (arrayValue != null) {
      Object[] array = convertToObjectArray(arrayValue);
      for (Object value : array) {
        if (value == null) continue;
        validateArrayFilterItem(value, field);
      }
    }
  }

  public static void validateFilterIterableField(Iterable<?> fieldValue, String field) {
    if (fieldValue != null) {
      for (Object value : fieldValue) {
        if (value == null) continue;
        validateArrayFilterItem(value, field);
      }
    }
  }

  private static void validateArrayIndexItem(Object value, String field) {
    if (value instanceof Iterable || value.getClass().isArray()) {
      throw new InvalidOperationException(
          "nested array index on iterable field " + field + " is not supported");
    }

    if (!(value instanceof Comparable)) {
      throw new IndexingException(
          "cannot index on an array field containing non comparable values " + field);
    }
  }

  private static void validateStringArrayItem(Object value, String field) {
    if (!(value instanceof String) && (value instanceof Iterable || value.getClass().isArray())) {
      throw new InvalidOperationException(
          "nested array index on iterable field " + field + " is not supported");
    }

    if (!(value instanceof String)) {
      throw new IndexingException(
          "cannot index on an array field containing non string values " + field);
    }
  }

  private static void validateArrayFilterItem(Object value, String field) {
    if (value instanceof Iterable || value.getClass().isArray()) {
      throw new InvalidOperationException("nested array is not supported");
    }

    if (!(value instanceof Comparable)) {
      throw new IndexingException("cannot filter using non comparable values " + field);
    }
  }
}
