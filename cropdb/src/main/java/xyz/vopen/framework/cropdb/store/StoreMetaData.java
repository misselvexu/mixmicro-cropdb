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

package xyz.vopen.framework.cropdb.store;

import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.vopen.framework.cropdb.collection.Document;

/**
 * The crop database metadata.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0
 */
@Data
@NoArgsConstructor
public class StoreMetaData implements MetaData {
    private Long createTime;
    private String storeVersion;
    private String cropdbVersion;
    private Integer schemaVersion;

    /**
     * Instantiates a new {@link StoreMetaData}.
     *
     * @param document the document
     */
    public StoreMetaData(Document document) {
        populateInfo(document);
    }

    /**
     * Gets the database info ina document.
     *
     * @return the info
     */
    public Document getInfo() {
        return Document.createDocument()
            .put("createTime", createTime)
            .put("storeVersion", storeVersion)
            .put("cropdbVersion", cropdbVersion)
            .put("schemaVersion", schemaVersion);
    }

    private void populateInfo(Document document) {
        createTime = document.get("createTime", Long.class);
        storeVersion = document.get("storeVersion", String.class);
        cropdbVersion = document.get("cropdbVersion", String.class);
        schemaVersion = document.get("schemaVersion", Integer.class);
    }
}
