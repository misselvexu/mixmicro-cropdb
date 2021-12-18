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

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.FilterException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
class RegexFilter extends FieldBasedFilter {
  private final Pattern pattern;

  RegexFilter(String field, String value) {
    super(field, value);
    pattern = Pattern.compile(value);
  }

  @Override
  public boolean apply(Pair<CropId, Document> element) {
    Document document = element.getSecond();
    Object fieldValue = document.get(getField());
    if (fieldValue != null) {
      if (fieldValue instanceof String) {
        Matcher matcher = pattern.matcher((String) fieldValue);
        if (matcher.find()) {
          return true;
        }
        matcher.reset();
      } else {
        throw new FilterException(getField() + " does not contain string value");
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "(" + getField() + " regex " + getValue() + ")";
  }
}
