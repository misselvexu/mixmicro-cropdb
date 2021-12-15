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

package xyz.vopen.framework.cropdb.support;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.codec.binary.Hex;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.DocumentCursor;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.common.PersistentCollection;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;
import xyz.vopen.framework.cropdb.common.Constants;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class CropJsonExporter {
    private final CropDB db;
    private JsonGenerator generator;
    private ExportOptions options;

    public CropJsonExporter(CropDB db) {
        this.db = db;
    }

    public void setGenerator(JsonGenerator generator) {
        this.generator = generator;
    }

    public void exportData() throws IOException, ClassNotFoundException {
        List<PersistentCollection<?>> collections = options.getCollections();
        Set<String> collectionNames;
        Set<String> repositoryNames;
        Map<String, Set<String>> keyedRepositoryNames;
        if (collections.isEmpty()) {
            collectionNames = db.listCollectionNames();
            repositoryNames = db.listRepositories();
            keyedRepositoryNames = db.listKeyedRepository();
        } else {
            collectionNames = new HashSet<>();
            repositoryNames = new HashSet<>();
            keyedRepositoryNames = new HashMap<>();
            for (PersistentCollection<?> collection : collections) {
                String name;
                if (collection instanceof CropCollection) {
                    CropCollection cropCollection = (CropCollection) collection;
                    name = cropCollection.getName();
                    collectionNames.add(name);
                } else if (collection instanceof ObjectRepository) {
                    ObjectRepository<?> repository = (ObjectRepository<?>) collection;
                    name = repository.getDocumentCollection().getName();
                    if (name.contains(Constants.KEY_OBJ_SEPARATOR)) {
                        String key = ObjectUtils.getKeyName(name);
                        String type = ObjectUtils.getKeyedRepositoryType(name);
                        Set<String> types;
                        if (keyedRepositoryNames.containsKey(key)) {
                            types = keyedRepositoryNames.get(key);
                        } else {
                            types = new LinkedHashSet<>();
                        }
                        types.add(type);
                        keyedRepositoryNames.put(key, types);
                    } else {
                        repositoryNames.add(name);
                    }
                }
            }
        }
        exportData(collectionNames, repositoryNames, keyedRepositoryNames);
        generator.close();
    }

    private void exportData(Set<String> collectionNames,
                            Set<String> repositoryNames,
                            Map<String, Set<String>> keyedRepositoryNames) throws IOException, ClassNotFoundException {
        generator.writeStartObject();

        generator.writeFieldName(Constants.TAG_COLLECTIONS);
        generator.writeStartArray();
        for (String collectionName : collectionNames) {
            CropCollection cropCollection = db.getCollection(collectionName);
            writeCollection(cropCollection);
        }
        generator.writeEndArray();

        generator.writeFieldName(Constants.TAG_REPOSITORIES);
        generator.writeStartArray();
        for (String repoName : repositoryNames) {
            Class<?> type = Class.forName(repoName);
            ObjectRepository<?> repository = db.getRepository(type);
            writeRepository(repository);
        }
        generator.writeEndArray();

        generator.writeFieldName(Constants.TAG_KEYED_REPOSITORIES);
        generator.writeStartArray();
        for (Map.Entry<String, Set<String>> entry : keyedRepositoryNames.entrySet()) {
            String key = entry.getKey();
            Set<String> typeNames = entry.getValue();
            for (String typeName : typeNames) {
                Class<?> type = Class.forName(typeName);
                ObjectRepository<?> repository = db.getRepository(type, key);
                writeKeyedRepository(key, repository);
            }
        }
        generator.writeEndArray();

        generator.writeEndObject();
    }


    private void writeRepository(ObjectRepository<?> repository) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(Constants.TAG_TYPE);
        generator.writeString(repository.getType().getName());

        Collection<IndexDescriptor> indices = repository.listIndices();
        writeIndices(indices);

        DocumentCursor cursor = repository.getDocumentCollection().find();
        writeContent(cursor);
        generator.writeEndObject();
    }

    private void writeKeyedRepository(String key, ObjectRepository<?> repository) throws IOException {
        generator.writeStartObject();

        generator.writeFieldName(Constants.TAG_KEY);
        generator.writeString(key);

        generator.writeFieldName(Constants.TAG_TYPE);
        generator.writeString(repository.getType().getName());

        Collection<IndexDescriptor> indices = repository.listIndices();
        writeIndices(indices);

        DocumentCursor cursor = repository.getDocumentCollection().find();
        writeContent(cursor);
        generator.writeEndObject();
    }

    private void writeCollection(CropCollection cropCollection) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(Constants.TAG_NAME);
        generator.writeString(cropCollection.getName());

        Collection<IndexDescriptor> indices = cropCollection.listIndices();
        writeIndices(indices);

        DocumentCursor cursor = cropCollection.find();
        writeContent(cursor);
        generator.writeEndObject();
    }

    private void writeIndices(Collection<IndexDescriptor> indices) throws IOException {
        generator.writeFieldName(Constants.TAG_INDICES);
        generator.writeStartArray();
        if (options.isExportIndices()) {
            for (IndexDescriptor index : indices) {
                generator.writeStartObject();
                generator.writeFieldName(Constants.TAG_INDEX);
                generator.writeObject(writeEncodedObject(index));
                generator.writeEndObject();
            }
        }
        generator.writeEndArray();
    }

    private void writeContent(DocumentCursor cursor) throws IOException {
        generator.writeFieldName(Constants.TAG_DATA);
        generator.writeStartArray();
        if (options.isExportData()) {
            for (Document document : cursor) {
                generator.writeStartObject();
                generator.writeFieldName(Constants.TAG_KEY);
                generator.writeObject(writeEncodedObject(document.get(Constants.DOC_ID)));

                generator.writeFieldName(Constants.TAG_VALUE);
                generator.writeObject(writeEncodedObject(document));
                generator.writeEndObject();
            }
        }
        generator.writeEndArray();
    }

    public void setOptions(ExportOptions options) {
        this.options = options;
    }

    private String writeEncodedObject(Object object) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
                oos.writeObject(object);
                byte[] data = os.toByteArray();
                return Hex.encodeHexString(data);
            }
        } catch (IOException e) {
            throw new CropIOException("failed to write object", e);
        }
    }
}
