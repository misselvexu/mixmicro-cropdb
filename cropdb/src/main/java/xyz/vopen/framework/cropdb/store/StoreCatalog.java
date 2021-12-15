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

import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static xyz.vopen.framework.cropdb.common.Constants.*;

/**
 * The crop store catalog containing the name of all collections,
 * repositories and keyed-repositories.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0.0
 */
public class StoreCatalog {
    private final CropMap<String, Document> catalogMap;

    /**
     * Instantiates a new {@link StoreCatalog}.
     *
     * @param cropStore the crop store
     */
    public StoreCatalog(CropStore<?> cropStore) {
        this.catalogMap = cropStore.openMap(COLLECTION_CATALOG, String.class, Document.class);
    }

    /**
     * Writes a new collection entry to the catalog.
     *
     * @param name the name
     */
    public void writeCollectionEntry(String name) {
        Document document = catalogMap.get(TAG_COLLECTIONS);
        if (document == null) {
            document = Document.createDocument();
        }

        // parse the document to create collection meta data object
        MapMetaData metaData = new MapMetaData(document);
        metaData.getMapNames().add(name);

        // convert the meta data object to document and save
        catalogMap.put(TAG_COLLECTIONS, metaData.getInfo());
    }

    /**
     * Writes a repository entry to the catalog.
     *
     * @param name the name
     */
    public void writeRepositoryEntry(String name) {
        Document document = catalogMap.get(TAG_REPOSITORIES);
        if (document == null) {
            document = Document.createDocument();
        }

        // parse the document to create collection meta data object
        MapMetaData metaData = new MapMetaData(document);
        metaData.getMapNames().add(name);

        // convert the meta data object to document and save
        catalogMap.put(TAG_REPOSITORIES, metaData.getInfo());
    }

    /**
     * Writes a keyed repository entries to the catalog
     *
     * @param name the name
     */
    public void writeKeyedRepositoryEntries(String name) {
        Document document = catalogMap.get(TAG_KEYED_REPOSITORIES);
        if (document == null) {
            document = Document.createDocument();
        }

        // parse the document to create collection meta data object
        MapMetaData metaData = new MapMetaData(document);
        metaData.getMapNames().add(name);

        catalogMap.put(TAG_KEYED_REPOSITORIES, metaData.getInfo());
    }

    /**
     * Gets all collection names.
     *
     * @return the collection names
     */
    public Set<String> getCollectionNames() {
        Document document = catalogMap.get(TAG_COLLECTIONS);
        if (document == null) return new HashSet<>();

        MapMetaData metaData = new MapMetaData(document);
        return metaData.getMapNames();
    }

    /**
     * Gets all repository names.
     *
     * @return the repository names
     */
    public Set<String> getRepositoryNames() {
        Document document = catalogMap.get(TAG_REPOSITORIES);
        if (document == null) return new HashSet<>();

        MapMetaData metaData = new MapMetaData(document);
        return metaData.getMapNames();
    }

    /**
     * Gets all keyed repository names.
     *
     * @return the keyed repository names
     */
    public Map<String, Set<String>> getKeyedRepositoryNames() {
        Document document = catalogMap.get(TAG_KEYED_REPOSITORIES);
        if (document == null) return new HashMap<>();

        MapMetaData metaData = new MapMetaData(document);
        Set<String> keyedRepositoryNames = metaData.getMapNames();

        Map<String, Set<String>> resultMap = new HashMap<>();
        for (String field : keyedRepositoryNames) {
            String key = ObjectUtils.getKeyName(field);
            String type = ObjectUtils.getKeyedRepositoryType(field);

            Set<String> types;
            if (resultMap.containsKey(key)) {
                types = resultMap.get(key);
            } else {
                types = new HashSet<>();
            }
            types.add(type);
            resultMap.put(key, types);
        }
        return resultMap;
    }

    /**
     * Removes the entry from the catalog specified by name.
     *
     * @param name the name
     */
    public void remove(String name) {
        // iterate over all types of catalog and find which type contains the name
        // remove the name from there
        for (Pair<String, Document> entry : catalogMap.entries()) {
            String catalogue = entry.getFirst();
            Document document = entry.getSecond();

            MapMetaData metaData = new MapMetaData(document);

            if (metaData.getMapNames().contains(name)) {
                metaData.getMapNames().remove(name);
                catalogMap.put(catalogue, metaData.getInfo());
                break;
            }
        }
    }
}
