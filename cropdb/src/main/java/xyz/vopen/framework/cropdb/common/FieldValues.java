package xyz.vopen.framework.cropdb.common;

import lombok.Data;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@link Fields} and their corresponding values from a document.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Data
public class FieldValues {
    private CropId cropId;
    private Fields fields;
    private List<Pair<String, Object>> values;

    /**
     * Instantiates a new Field values.
     */
    public FieldValues() {
        values = new ArrayList<>();
    }

    /**
     * Get the value of the field.
     *
     * @param field the field
     * @return the value
     */
    public Object get(String field) {
        if (fields.getFieldNames().contains(field)) {
            for (Pair<String, Object> value : values) {
                if (value.getFirst().equals(field)) {
                    return value.getSecond();
                }
            }
        }
        return null;
    }

    /**
     * Gets the {@link Fields}.
     *
     * @return the fields
     */
    public Fields getFields() {
        if (fields != null) {
            return fields;
        }

        this.fields = new Fields();
        List<String> fieldNames = new ArrayList<>();
        for (Pair<String, Object> value : getValues()) {
            if (!StringUtils.isNullOrEmpty(value.getFirst())) {
                fieldNames.add(value.getFirst());
            }
        }
        fields.setFieldNames(fieldNames);
        return fields;
    }
}
