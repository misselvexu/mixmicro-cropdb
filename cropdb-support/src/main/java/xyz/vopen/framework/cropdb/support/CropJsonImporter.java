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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.codec.binary.Hex;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.common.PersistentCollection;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;
import xyz.vopen.framework.cropdb.common.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static xyz.vopen.framework.cropdb.index.IndexOptions.indexOptions;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
class CropJsonImporter {
    private JsonParser parser;
    private final CropDB db;

    public CropJsonImporter(CropDB db) {
        this.db = db;
    }

    public void setParser(JsonParser parser) {
        this.parser = parser;
    }

    public void importData() throws IOException, ClassNotFoundException {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();

            if (Constants.TAG_COLLECTIONS.equals(fieldName)) {
                readCollection();
            }

            if (Constants.TAG_REPOSITORIES.equals(fieldName)) {
                readRepository();
            }

            if (Constants.TAG_KEYED_REPOSITORIES.equals(fieldName)) {
                readKeyedRepository();
            }
        }
    }

    private void readRepository() throws IOException, ClassNotFoundException {
        ObjectRepository<?> repository = null;
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (Constants.TAG_TYPE.equals(fieldName)) {
                    // move to next token
                    parser.nextToken();

                    String typeId = parser.getText();
                    Class<?> type = Class.forName(typeId);
                    repository = db.getRepository(type);
                }

                if (Constants.TAG_INDICES.equals(fieldName)) {
                    readIndices(repository);
                }

                if (Constants.TAG_DATA.equals(fieldName) && repository != null) {
                    readCollectionData(repository.getDocumentCollection());
                }
            }
        }
    }

    private void readKeyedRepository() throws IOException, ClassNotFoundException {
        ObjectRepository<?> repository = null;
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            String key = null;

            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (Constants.TAG_KEY.equals(fieldName)) {
                    parser.nextToken();
                    key = parser.getText();
                }

                if (key != null && Constants.TAG_TYPE.equals(fieldName)) {
                    // move to next token
                    parser.nextToken();

                    String typeId = parser.getText();
                    Class<?> type = Class.forName(typeId);
                    repository = db.getRepository(type, key);
                }

                if (Constants.TAG_INDICES.equals(fieldName)) {
                    readIndices(repository);
                }

                if (Constants.TAG_DATA.equals(fieldName) && repository != null) {
                    readCollectionData(repository.getDocumentCollection());
                }
            }
        }
    }

    private void readCollection() throws IOException {
        CropCollection collection = null;
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (Constants.TAG_NAME.equals(fieldName)) {
                    // move to next token
                    parser.nextToken();

                    String collectionName = parser.getText();
                    collection = db.getCollection(collectionName);
                }

                if (Constants.TAG_INDICES.equals(fieldName)) {
                    readIndices(collection);
                }

                if (Constants.TAG_DATA.equals(fieldName)) {
                    readCollectionData(collection);
                }
            }
        }
    }

    private void readIndices(PersistentCollection<?> collection) throws IOException {
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (Constants.TAG_INDEX.equals(fieldName)) {
                    parser.nextToken();
                    String data = parser.readValueAs(String.class);
                    IndexDescriptor index = (IndexDescriptor) readEncodedObject(data);
                    if (index != null) {
                        String[] fieldNames = index.getIndexFields().getFieldNames().toArray(new String[0]);
                        if (collection != null && index.getIndexFields() != null && !collection.hasIndex(fieldNames)) {
                            collection.createIndex(indexOptions(index.getIndexType()), fieldNames);
                        }
                    }
                }
            }
        }
    }

    private void readCollectionData(CropCollection collection) throws IOException {
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (Constants.TAG_KEY.equals(fieldName)) {
                    parser.nextToken();
                    parser.readValueAs(String.class);
                }

                if (Constants.TAG_VALUE.equals(fieldName)) {
                    parser.nextToken();
                    String data = parser.readValueAs(String.class);
                    Document document = (Document) readEncodedObject(data);
                    if (collection != null) {
                        collection.insert(document);
                    }
                }
            }
        }
    }

    private Object readEncodedObject(String hexString) {
        try {
            byte[] data = Hex.decodeHex(hexString);
            try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
                try (ObjectInputStream ois = new ObjectInputStream(is)) {
                    return ois.readObject();
                }
            }
        } catch (Exception e) {
            throw new CropIOException("error while reading data", e);
        }
    }
}
