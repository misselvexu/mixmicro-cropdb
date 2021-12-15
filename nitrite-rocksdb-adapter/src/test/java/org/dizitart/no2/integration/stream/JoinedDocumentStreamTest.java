/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
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

package org.dizitart.no2.integration.stream;

import org.dizitart.no2.integration.collection.BaseCollectionTest;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.streams.JoinedDocumentStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.junit.Test;

import java.util.Iterator;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class JoinedDocumentStreamTest extends BaseCollectionTest {

    @Test
    public void testFindResult() {
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        RecordStream<Document> result = collection.find().join(collection.find(), new Lookup());
        assertTrue(result instanceof JoinedDocumentStream);
    }

    @Test(expected = InvalidOperationException.class)
    public void testIteratorRemove() {
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        RecordStream<Document> cursor = collection.find().join(collection.find(), new Lookup());
        assertNotNull(cursor.toString());
        Iterator<Document> iterator = cursor.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
