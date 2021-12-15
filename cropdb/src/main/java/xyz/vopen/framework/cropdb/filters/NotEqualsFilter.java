package xyz.vopen.framework.cropdb.filters;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.index.IndexMap;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class NotEqualsFilter extends ComparableFilter {
    protected NotEqualsFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    public boolean apply(Pair<CropId, Document> element) {
        Document document = element.getSecond();
        Object fieldValue = document.get(getField());
        return !ObjectUtils.deepEquals(fieldValue, getValue());
    }

    public List<?> applyOnIndex(IndexMap indexMap) {
        List<NavigableMap<Comparable<?>, Object>> subMap = new ArrayList<>();
        List<CropId> cropIds = new ArrayList<>();

        for (Pair<Comparable<?>, ?> entry : indexMap.entries()) {
            if (!ObjectUtils.deepEquals(getValue(), entry.getFirst())) {
                processIndexValue(entry.getSecond(), subMap, cropIds);
            }
        }

        if (!subMap.isEmpty()) {
            // if sub-map is populated then filtering on compound index, return sub-map
            return subMap;
        } else {
            // else it is filtering on either single field index,
            // or it is a terminal filter on compound index, return only crop-ids
            return cropIds;
        }
    }

    @Override
    public String toString() {
        return "(" + getField() + " != " + getValue() + ")";
    }
}
