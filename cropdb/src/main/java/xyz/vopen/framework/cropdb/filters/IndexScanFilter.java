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

package xyz.vopen.framework.cropdb.filters;

import lombok.Getter;
import lombok.ToString;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a set of filter which can be applied on an index.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@ToString
public class IndexScanFilter implements Filter {
  @Getter private final List<ComparableFilter> filters;

  /**
   * Instantiates a new Index scan filter.
   *
   * @param filters the filters
   */
  public IndexScanFilter(Collection<ComparableFilter> filters) {
    this.filters = new ArrayList<>(filters);
  }

  @Override
  public boolean apply(Pair<CropId, Document> element) {
    throw new InvalidOperationException("index scan filter cannot be applied on collection");
  }
}
