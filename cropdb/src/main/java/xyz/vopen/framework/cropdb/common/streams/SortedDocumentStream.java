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

package xyz.vopen.framework.cropdb.common.streams;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.util.Iterables;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a sorted crop document stream
 *
 * @since 4.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
public class SortedDocumentStream implements RecordStream<Pair<CropId, Document>> {
    private final FindPlan findPlan;
    private final RecordStream<Pair<CropId, Document>> recordStream;

    public SortedDocumentStream(FindPlan findPlan,
                                RecordStream<Pair<CropId, Document>> recordStream) {
        this.findPlan = findPlan;
        this.recordStream = recordStream;
    }

    @Override
    public Iterator<Pair<CropId, Document>> iterator() {
        if (recordStream == null) return Collections.emptyIterator();

        DocumentSorter documentSorter = new DocumentSorter(findPlan.getCollator(),
            findPlan.getBlockingSortOrder());

        List<Pair<CropId, Document>> recordList = Iterables.toList(recordStream);
        Collections.sort(recordList, documentSorter);

        return recordList.iterator();
    }
}
