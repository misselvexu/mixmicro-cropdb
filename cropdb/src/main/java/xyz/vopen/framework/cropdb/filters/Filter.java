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
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.FilterException;
import xyz.vopen.framework.cropdb.collection.FindOptions;
import xyz.vopen.framework.cropdb.common.Constants;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import static xyz.vopen.framework.cropdb.common.util.ValidationUtils.notEmpty;

/**
 * An interface to specify filtering criteria during find operation. When a filter is applied to a
 * collection, based on the criteria it returns a set of matching records.
 *
 * <p>Each filtering criteria is based on a value of a document. If the value is indexed, the find
 * operation takes the advantage of it and only scans the index map for that value. But if the value
 * is not indexed, it scans the whole collection.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @see CropCollection#find(Filter) CropCollection#find(Filter)
 * @see CropCollection#find(Filter, FindOptions) CropCollection#find(Filter,
 *     xyz.vopen.framework.cropdb.collection.FindOptions)
 * @since 1.0
 */
public interface Filter {
  /** A filter to select all elements. */
  Filter ALL = element -> true;

  /**
   * Filter by id.
   *
   * @param cropId the crop id
   * @return the filter
   */
  static Filter byId(CropId cropId) {
    return new EqualsFilter(Constants.DOC_ID, cropId.getIdValue());
  }

  /**
   * And filter.
   *
   * @param filters the filters
   * @return the filter
   */
  static Filter and(Filter... filters) {
    ValidationUtils.notEmpty(filters, "at least two filters must be specified");
    if (filters.length < 2) {
      throw new FilterException("at least two filters must be specified");
    }

    return new AndFilter(filters);
  }

  /**
   * Or filter.
   *
   * @param filters the filters
   * @return the filter
   */
  static Filter or(Filter... filters) {
    ValidationUtils.notEmpty(filters, "at least two filters must be specified");
    if (filters.length < 2) {
      throw new FilterException("at least two filters must be specified");
    }

    return new OrFilter(filters);
  }

  /**
   * Filters a document map and returns <code>true</code> if the criteria matches.
   *
   * @param element the entry to check.
   * @return boolean value to indicate if the filtering criteria matches the document.
   */
  boolean apply(Pair<CropId, Document> element);

  /**
   * Creates a not filter which performs a logical NOT operation on a filter and selects the
   * documents that *_do not_* satisfy the criteria. This also includes documents that do not
   * contain the value.
   *
   * <p>
   *
   * @return the not filter
   */
  default Filter not() {
    return new NotFilter(this);
  }
}
