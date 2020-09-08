/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.rocksdb;

import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexType;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class IndexEntryTest {

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testIndexEquals() {
        IndexEntry index = new IndexEntry(IndexType.Fulltext, "test", "testColl");
        IndexEntry index2 = new IndexEntry(IndexType.Fulltext, "test", "testColl");
        assertEquals(index, index2);
    }

    @Test
    public void testIndexCompare() {
        IndexEntry index = new IndexEntry(IndexType.Fulltext, "test", "testColl");
        IndexEntry index2 = new IndexEntry(IndexType.Fulltext, "test", "testColl");

        assertEquals(index.toString(), "IndexEntry(indexType=Fulltext, field=test, collectionName=testColl)");
        assertEquals(index.compareTo(index2), 0);
    }
}
