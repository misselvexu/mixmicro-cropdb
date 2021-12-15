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

package xyz.vopen.framework.cropdb.store;

import lombok.Data;
import lombok.Getter;
import xyz.vopen.framework.cropdb.collection.Document;

import java.util.HashSet;
import java.util.Set;

import static xyz.vopen.framework.cropdb.common.Constants.TAG_MAP_METADATA;

/**
 * The Map metadata.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Data
public class MapMetaData implements MetaData {
    @Getter
    private Set<String> mapNames;

    /**
     * Instantiates a new {@link MapMetaData}.
     *
     * @param metadata the metadata
     */
    public MapMetaData(Document metadata) {
        populateInfo(metadata);
    }

    @Override
    public Document getInfo() {
        return Document.createDocument(TAG_MAP_METADATA, mapNames);
    }

    @SuppressWarnings("unchecked")
    private void populateInfo(Document metadata) {
        mapNames = (Set<String>) metadata.get(TAG_MAP_METADATA, Set.class);
        if (mapNames == null) {
            mapNames = new HashSet<>();
        }
    }
}
