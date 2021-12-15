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

import org.apache.commons.io.FileUtils;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
public class NitriteBuilderNegativeTest {
    private Nitrite db;
    private String filePath;

    @Rule
    public Retry retry = new Retry(3);

    @Test(expected = NitriteIOException.class)
    public void testCreateReadonlyDatabase() {
        filePath = getRandomTempDbFile();

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(filePath)
            .readOnly(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .openOrCreate();
        db.close();
    }

    @Test(expected = InvalidOperationException.class)
    public void testCreateReadonlyInMemoryDatabase() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .readOnly(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .openOrCreate();
        db.close();
    }

    @Test(expected = NitriteIOException.class)
    public void testOpenWithLock() {
        filePath = getRandomTempDbFile();

        db = createDb(filePath);
        db = createDb(filePath);
    }

    @Test(expected = NitriteIOException.class)
    public void testInvalidDirectory() {
        filePath = "/ytgr/hfurh/frij.db";
        db = createDb(filePath);
    }

    @After
    public void cleanUp() {
        if (db != null && !db.isClosed()) {
            db.close();
        }

        if (!StringUtils.isNullOrEmpty(filePath)) {
            FileUtils.deleteQuietly(new File(filePath));
        }
    }
}
