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

package xyz.vopen.framework.cropdb.index;

import lombok.Getter;
import xyz.vopen.framework.cropdb.collection.FindPlan;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.FieldValues;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.exceptions.FilterException;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.filters.ComparableFilter;
import xyz.vopen.framework.cropdb.filters.TextFilter;
import xyz.vopen.framework.cropdb.index.fulltext.TextTokenizer;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.common.util.IndexUtils;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a crop full-text index.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class TextIndex implements CropIndex {
  @Getter private final IndexDescriptor indexDescriptor;
  private final CropStore<?> cropStore;
  private final TextTokenizer textTokenizer;

  /**
   * Instantiates a new {@link TextIndex}.
   *
   * @param textTokenizer the text tokenizer
   * @param indexDescriptor the index descriptor
   * @param cropStore the crop store
   */
  public TextIndex(
      TextTokenizer textTokenizer, IndexDescriptor indexDescriptor, CropStore<?> cropStore) {
    this.textTokenizer = textTokenizer;
    this.indexDescriptor = indexDescriptor;
    this.cropStore = cropStore;
  }

  @Override
  public void write(FieldValues fieldValues) {
    Fields fields = fieldValues.getFields();
    List<String> fieldNames = fields.getFieldNames();

    String firstField = fieldNames.get(0);
    Object element = fieldValues.get(firstField);

    CropMap<String, List<?>> indexMap = findIndexMap();

    if (element == null) {
      addIndexElement(indexMap, fieldValues, null);
    } else if (element instanceof String) {
      addIndexElement(indexMap, fieldValues, (String) element);
    } else if (element.getClass().isArray()) {
      ValidationUtils.validateStringArrayIndexField(element, firstField);
      Object[] array = ObjectUtils.convertToObjectArray(element);

      for (Object item : array) {
        addIndexElement(indexMap, fieldValues, (String) item);
      }
    } else if (element instanceof Iterable) {
      ValidationUtils.validateStringIterableIndexField((Iterable<?>) element, firstField);
      Iterable<?> iterable = (Iterable<?>) element;

      for (Object item : iterable) {
        addIndexElement(indexMap, fieldValues, (String) item);
      }
    } else {
      throw new IndexingException("string data is expected");
    }
  }

  @Override
  public void remove(FieldValues fieldValues) {
    Fields fields = fieldValues.getFields();
    List<String> fieldNames = fields.getFieldNames();

    String firstField = fieldNames.get(0);
    Object element = fieldValues.get(firstField);

    CropMap<String, List<?>> indexMap = findIndexMap();
    if (element == null) {
      removeIndexElement(indexMap, fieldValues, null);
    } else if (element instanceof String) {
      removeIndexElement(indexMap, fieldValues, (String) element);
    } else if (element.getClass().isArray()) {
      ValidationUtils.validateStringArrayIndexField(element, firstField);
      Object[] array = ObjectUtils.convertToObjectArray(element);

      for (Object item : array) {
        removeIndexElement(indexMap, fieldValues, (String) item);
      }
    } else if (element instanceof Iterable) {
      ValidationUtils.validateStringIterableIndexField((Iterable<?>) element, firstField);
      Iterable<?> iterable = (Iterable<?>) element;

      for (Object item : iterable) {
        removeIndexElement(indexMap, fieldValues, (String) item);
      }
    } else {
      throw new IndexingException("string data is expected");
    }
  }

  @Override
  public void drop() {
    CropMap<String, List<?>> indexMap = findIndexMap();
    indexMap.clear();
    indexMap.drop();
  }

  @Override
  public LinkedHashSet<CropId> findCropIds(FindPlan findPlan) {
    if (findPlan.getIndexScanFilter() == null) return new LinkedHashSet<>();

    CropMap<String, List<?>> indexMap = findIndexMap();
    List<ComparableFilter> filters = findPlan.getIndexScanFilter().getFilters();

    if (filters.size() == 1 && filters.get(0) instanceof TextFilter) {
      TextFilter textFilter = (TextFilter) filters.get(0);
      textFilter.setTextTokenizer(textTokenizer);
      return textFilter.applyOnIndex(indexMap);
    }
    throw new FilterException("invalid filter found for full-text index");
  }

  private CropMap<String, List<?>> findIndexMap() {
    String mapName = IndexUtils.deriveIndexMapName(indexDescriptor);
    return cropStore.openMap(mapName, String.class, CopyOnWriteArrayList.class);
  }

  @SuppressWarnings("unchecked")
  private void addIndexElement(
      CropMap<String, List<?>> indexMap, FieldValues fieldValues, String value) {
    Set<String> words = decompose(value);

    for (String word : words) {
      List<CropId> cropIds = (List<CropId>) indexMap.get(word);

      if (cropIds == null) {
        cropIds = new CopyOnWriteArrayList<>();
      }

      cropIds = addCropIds(cropIds, fieldValues);
      indexMap.put(word, cropIds);
    }
  }

  @SuppressWarnings("unchecked")
  private void removeIndexElement(
      CropMap<String, List<?>> indexMap, FieldValues fieldValues, String value) {
    Set<String> words = decompose(value);
    for (String word : words) {
      List<CropId> cropIds = (List<CropId>) indexMap.get(word);
      if (cropIds != null && !cropIds.isEmpty()) {
        cropIds.remove(fieldValues.getCropId());
        if (cropIds.isEmpty()) {
          indexMap.remove(word);
        } else {
          indexMap.put(word, cropIds);
        }
      }
    }
  }

  private Set<String> decompose(Object fieldValue) {
    Set<String> result = new HashSet<>();
    if (fieldValue == null) {
      result.add(null);
    } else if (fieldValue instanceof String) {
      result.add((String) fieldValue);
    } else if (fieldValue instanceof Iterable) {
      Iterable<?> iterable = (Iterable<?>) fieldValue;
      for (Object item : iterable) {
        result.addAll(decompose(item));
      }
    } else if (fieldValue.getClass().isArray()) {
      Object[] array = ObjectUtils.convertToObjectArray(fieldValue);
      for (Object item : array) {
        result.addAll(decompose(item));
      }
    }

    Set<String> words = new HashSet<>();
    for (String item : result) {
      words.addAll(textTokenizer.tokenize(item));
    }

    return words;
  }
}
