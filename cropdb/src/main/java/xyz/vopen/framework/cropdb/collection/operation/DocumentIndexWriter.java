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

package xyz.vopen.framework.cropdb.collection.operation;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.util.DocumentUtils;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.CropIndexer;

import java.util.Collection;

/**
 *
 * @since 4.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class DocumentIndexWriter {
    private final CropConfig cropConfig;
    private final IndexOperations indexOperations;

    DocumentIndexWriter(CropConfig cropConfig,
                        IndexOperations indexOperations) {
        this.cropConfig = cropConfig;
        this.indexOperations = indexOperations;
    }

    void writeIndexEntry(Document document) {
        Collection<IndexDescriptor> indexEntries = indexOperations.listIndexes();
        if (indexEntries != null) {
            for (IndexDescriptor indexDescriptor : indexEntries) {
                String indexType = indexDescriptor.getIndexType();
                CropIndexer cropIndexer = cropConfig.findIndexer(indexType);

                writeIndexEntryInternal(indexDescriptor, document, cropIndexer);
            }
        }
    }

    void removeIndexEntry(Document document) {
        Collection<IndexDescriptor> indexEntries = indexOperations.listIndexes();
        if (indexEntries != null) {
            for (IndexDescriptor indexDescriptor : indexEntries) {
                String indexType = indexDescriptor.getIndexType();
                CropIndexer cropIndexer = cropConfig.findIndexer(indexType);

                removeIndexEntryInternal(indexDescriptor, document, cropIndexer);
            }
        }
    }

    void updateIndexEntry(Document oldDocument, Document newDocument) {
        Collection<IndexDescriptor> indexEntries = indexOperations.listIndexes();
        if (indexEntries != null) {
            for (IndexDescriptor indexDescriptor : indexEntries) {
                String indexType = indexDescriptor.getIndexType();
                CropIndexer cropIndexer = cropConfig.findIndexer(indexType);

                removeIndexEntryInternal(indexDescriptor, oldDocument, cropIndexer);
                writeIndexEntryInternal(indexDescriptor, newDocument, cropIndexer);
            }
        }
    }

    private void writeIndexEntryInternal(IndexDescriptor indexDescriptor, Document document,
                                         CropIndexer cropIndexer) {
        if (indexDescriptor != null) {
            Fields fields = indexDescriptor.getIndexFields();
            FieldValues fieldValues = DocumentUtils.getValues(document, fields);

            // if dirty index and currently indexing is not running, rebuild
            if (indexOperations.shouldRebuildIndex(fields)) {
                // rebuild will also take care of the current document
                indexOperations.buildIndex(indexDescriptor, true);
            } else if (cropIndexer != null) {
                cropIndexer.writeIndexEntry(fieldValues, indexDescriptor, cropConfig);
            }
        }
    }

    private void removeIndexEntryInternal(IndexDescriptor indexDescriptor, Document document,
                                          CropIndexer cropIndexer) {
        if (indexDescriptor != null) {
            Fields fields = indexDescriptor.getIndexFields();
            FieldValues fieldValues = DocumentUtils.getValues(document, fields);

            // if dirty index and currently indexing is not running, rebuild
            if (indexOperations.shouldRebuildIndex(fields)) {
                // rebuild will also take care of the current document
                indexOperations.buildIndex(indexDescriptor, true);
            } else if (cropIndexer != null) {
                cropIndexer.removeIndexEntry(fieldValues, indexDescriptor, cropConfig);
            }
        }
    }

}
