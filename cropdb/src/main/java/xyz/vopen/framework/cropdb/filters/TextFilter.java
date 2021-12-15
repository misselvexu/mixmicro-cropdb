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

package xyz.vopen.framework.cropdb.filters;

import lombok.Setter;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.exceptions.FilterException;
import xyz.vopen.framework.cropdb.index.IndexMap;
import xyz.vopen.framework.cropdb.index.fulltext.TextTokenizer;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.util.*;

/**
 * Represents a crop full-text search filter.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
public class TextFilter extends StringFilter {
    @Setter
    private TextTokenizer textTokenizer;

    /**
     * Instantiates a new Text filter.
     *
     * @param field the field
     * @param value the value
     */
    TextFilter(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean apply(Pair<CropId, Document> element) {
        ValidationUtils.notNull(getField(), "field cannot be null");
        ValidationUtils.notNull(getStringValue(), "search term cannot be null");
        String searchString = getStringValue();
        Object docValue = element.getSecond().get(getField());

        if (!(docValue instanceof String)) {
            throw new FilterException("text filter can not be applied on non string field " + getField());
        }

        String docString = (String) docValue;

        if (searchString.startsWith("*") || searchString.endsWith("*")) {
            searchString = searchString.replace("*", "");
        }

        return docString.toLowerCase().contains(searchString.toLowerCase());
    }

    @Override
    public String toString() {
        return "(" + getField() + " like " + getValue() + ")";
    }

    /**
     * Apply on index linked hash set.
     *
     * @param indexMap the index map
     * @return the linked hash set
     */
    public LinkedHashSet<CropId> applyOnIndex(CropMap<String, List<?>> indexMap) {
        ValidationUtils.notNull(getField(), "field cannot be null");
        ValidationUtils.notNull(getStringValue(), "search term cannot be null");
        String searchString = getStringValue();

        if (searchString.startsWith("*") || searchString.endsWith("*")) {
            return searchByWildCard(indexMap, searchString);
        } else {
            return searchExactByIndex(indexMap, searchString);
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<CropId> searchExactByIndex(CropMap<String, List<?>> indexMap, String searchString) {

        Set<String> words = textTokenizer.tokenize(searchString);
        Map<CropId, Integer> scoreMap = new HashMap<>();
        for (String word : words) {
            List<CropId> cropIds = (List<CropId>) indexMap.get(word);
            if (cropIds != null) {
                for (CropId id : cropIds) {
                    Integer score = scoreMap.get(id);
                    if (score == null) {
                        scoreMap.put(id, 1);
                    } else {
                        scoreMap.put(id, score + 1);
                    }
                }
            }
        }

        return sortedIdsByScore(scoreMap);
    }

    private LinkedHashSet<CropId> searchByWildCard(CropMap<String, List<?>> indexMap, String searchString) {
        if (searchString.contentEquals("*")) {
            throw new FilterException("* is not a valid search string");
        }

        StringTokenizer stringTokenizer = StringUtils.stringTokenizer(searchString);
        if (stringTokenizer.countTokens() > 1) {
            throw new FilterException("multiple words with wildcard is not supported");
        }

        if (searchString.startsWith("*") && !searchString.endsWith("*")) {
            return searchByLeadingWildCard(indexMap, searchString);
        } else if (searchString.endsWith("*") && !searchString.startsWith("*")) {
            return searchByTrailingWildCard(indexMap, searchString);
        } else {
            String term = searchString.substring(1, searchString.length() - 1);
            return searchContains(indexMap, term);
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<CropId> searchByLeadingWildCard(CropMap<String, List<?>> indexMap, String searchString) {
        if (searchString.equalsIgnoreCase("*")) {
            throw new FilterException("invalid search term '*'");
        }

        LinkedHashSet<CropId> idSet = new LinkedHashSet<>();
        String term = searchString.substring(1);

        for (Pair<String, List<?>> entry : indexMap.entries()) {
            String key = entry.getFirst();
            if (key.endsWith(term.toLowerCase())) {
                idSet.addAll((List<CropId>) entry.getSecond());
            }
        }
        return idSet;
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<CropId> searchByTrailingWildCard(CropMap<String, List<?>> indexMap, String searchString) {
        if (searchString.equalsIgnoreCase("*")) {
            throw new FilterException("invalid search term '*'");
        }

        LinkedHashSet<CropId> idSet = new LinkedHashSet<>();
        String term = searchString.substring(0, searchString.length() - 1);

        for (Pair<String, List<?>> entry : indexMap.entries()) {
            String key = entry.getFirst();
            if (key.startsWith(term.toLowerCase())) {
                idSet.addAll((List<CropId>) entry.getSecond());
            }
        }
        return idSet;
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<CropId> searchContains(CropMap<String, List<?>> indexMap, String term) {
        LinkedHashSet<CropId> idSet = new LinkedHashSet<>();

        for (Pair<String, List<?>> entry : indexMap.entries()) {
            String key = entry.getFirst();
            if (key.contains(term.toLowerCase())) {
                idSet.addAll((List<CropId>) entry.getSecond());
            }
        }
        return idSet;
    }

    private LinkedHashSet<CropId> sortedIdsByScore(Map<CropId, Integer> unsortedMap) {
        List<Map.Entry<CropId, Integer>> list = new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, (e1, e2) -> (e2.getValue()).compareTo(e1.getValue()));

        LinkedHashSet<CropId> result = new LinkedHashSet<>();
        for (Map.Entry<CropId, Integer> entry : list) {
            result.add(entry.getKey());
        }

        return result;
    }

    @Override
    public List<?> applyOnIndex(IndexMap indexMap) {
        return null;
    }
}
