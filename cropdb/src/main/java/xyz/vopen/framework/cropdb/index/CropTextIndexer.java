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

package xyz.vopen.framework.cropdb.index;

import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.index.fulltext.EnglishTextTokenizer;
import xyz.vopen.framework.cropdb.index.fulltext.TextTokenizer;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a crop text indexer.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class CropTextIndexer implements CropIndexer {
    private final TextTokenizer textTokenizer;
    private final Map<IndexDescriptor, TextIndex> indexRegistry;

    /**
     * Instantiates a new {@link CropTextIndexer}.
     */
    public CropTextIndexer() {
        this.textTokenizer = new EnglishTextTokenizer();
        this.indexRegistry = new ConcurrentHashMap<>();
    }

    /**
     * Instantiates a new {@link CropTextIndexer}.
     *
     * @param textTokenizer the text tokenizer
     */
    public CropTextIndexer(TextTokenizer textTokenizer) {
        this.textTokenizer = textTokenizer;
        this.indexRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize(CropConfig cropConfig) {
    }

    @Override
    public String getIndexType() {
        return IndexType.FULL_TEXT;
    }

    @Override
    public void validateIndex(Fields fields) {
        if (fields.getFieldNames().size() > 1) {
            throw new IndexingException("text index can only be created on a single field");
        }
    }

    @Override
    public void dropIndex(IndexDescriptor indexDescriptor, CropConfig cropConfig) {
        TextIndex textIndex = findTextIndex(indexDescriptor, cropConfig);
        textIndex.drop();
    }

    @Override
    public void writeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, CropConfig cropConfig) {
        TextIndex textIndex = findTextIndex(indexDescriptor, cropConfig);
        textIndex.write(fieldValues);
    }

    @Override
    public void removeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, CropConfig cropConfig) {
        TextIndex textIndex = findTextIndex(indexDescriptor, cropConfig);
        textIndex.remove(fieldValues);
    }

    @Override
    public LinkedHashSet<CropId> findByFilter(FindPlan findPlan, CropConfig cropConfig) {
        TextIndex textIndex = findTextIndex(findPlan.getIndexDescriptor(), cropConfig);
        return textIndex.findCropIds(findPlan);
    }

    @Override
    public void close() {
        indexRegistry.clear();
    }

    private TextIndex findTextIndex(IndexDescriptor indexDescriptor, CropConfig cropConfig) {
        if (indexRegistry.containsKey(indexDescriptor)) {
            return indexRegistry.get(indexDescriptor);
        }

        TextIndex textIndex = new TextIndex(textTokenizer, indexDescriptor, cropConfig.getCropStore());
        indexRegistry.put(indexDescriptor, textIndex);
        return textIndex;
    }
}
