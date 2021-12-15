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

package xyz.vopen.framework.cropdb.collection;

import lombok.Data;
import xyz.vopen.framework.cropdb.common.SortOrder;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.filters.EqualsFilter;
import xyz.vopen.framework.cropdb.filters.Filter;
import xyz.vopen.framework.cropdb.filters.IndexScanFilter;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an execution plan of a find operation after optimization.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0.0
 */
@Data
public class FindPlan {
    private EqualsFilter byIdFilter;
    private IndexScanFilter indexScanFilter;
    private Filter collectionScanFilter;

    private IndexDescriptor indexDescriptor;
    private Map<String, Boolean> indexScanOrder;
    private List<Pair<String, SortOrder>> blockingSortOrder;

    private Long skip;
    private Long limit;

    private Collator collator;

    private List<FindPlan> subPlans;

    /**
     * Instantiates a new {@link FindPlan}.
     */
    public FindPlan() {
        this.subPlans = new ArrayList<>();
        this.blockingSortOrder = new ArrayList<>();
    }
}
