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

package org.dizitart.no2.integration;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.dizitart.no2.integration.TestUtil.*;


/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
public class NitriteBuilderNegativeTest {
    private final String filePath = getRandomTempDbFile();
    private Nitrite db1, db2;

    @Rule
    public Retry retry = new Retry(3);

    @Test(expected = NitriteIOException.class)
    public void testOpenWithLock() {
        db1 = createDb(filePath);
        db2 = createDb(filePath);
    }

    @Test(expected = NitriteIOException.class)
    public void testInvalidDirectory() {
        String filePath = "/ytgr/hfurh/frij.db";
        db1 = createDb(filePath);
    }

    @After
    public void cleanUp() {
        if (db1 != null && !db1.isClosed()) {
            db1.close();
        }

        if (db2 != null && !db2.isClosed()) {
            db2.close();
        }
        deleteDb(filePath);
    }
}
