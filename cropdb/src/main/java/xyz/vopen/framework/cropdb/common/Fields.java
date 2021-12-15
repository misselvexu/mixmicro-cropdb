package xyz.vopen.framework.cropdb.common;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static xyz.vopen.framework.cropdb.common.Constants.INTERNAL_NAME_SEPARATOR;
import static xyz.vopen.framework.cropdb.common.util.ValidationUtils.notEmpty;

/**
 * Represents a list of document fields.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@EqualsAndHashCode
public class Fields implements Comparable<Fields>, Serializable {
    private static final long serialVersionUID = 1601646404L;

    /**
     * The Field names.
     */
    @Setter(AccessLevel.PACKAGE)
    protected List<String> fieldNames;

    /**
     * Instantiates a new Fields.
     */
    public Fields() {
        fieldNames = new ArrayList<>();
    }

    /**
     * Creates a {@link Fields} instance with field names.
     *
     * @param fields the fields
     * @return the fields
     */
    public static Fields withNames(String... fields) {
        ValidationUtils.notNull(fields, "fields cannot be null");
        ValidationUtils.notEmpty(fields, "fields cannot be empty");

        Fields f = new Fields();
        f.fieldNames.addAll(Arrays.asList(fields));
        return f;
    }


    /**
     * Adds a new field name.
     *
     * @param field the field
     * @return the fields
     */
    public Fields addField(String field) {
        fieldNames.add(field);
        return this;
    }

    /**
     * Gets the field names.
     *
     * @return the field names
     */
    public List<String> getFieldNames() {
        return Collections.unmodifiableList(fieldNames);
    }

    /**
     * Starts with boolean.
     *
     * @param other the other
     * @return the boolean
     */
    public boolean startsWith(Fields other) {
        if (other != null) {
            int length = Math.min(fieldNames.size(), other.fieldNames.size());

            // if other is greater then it is not a prefix of this field
            if (other.fieldNames.size() > length) return false;

            for (int i = 0; i < length; i++) {
                String thisField = fieldNames.get(i);
                String otherField = other.fieldNames.get(i);
                if (!thisField.equals(otherField)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the encoded name for this {@link Fields}.
     *
     * @return the encoded name
     */
    public String getEncodedName() {
        return StringUtils.join(INTERNAL_NAME_SEPARATOR, getFieldNames());
    }

    @Override
    public String toString() {
        return fieldNames.toString();
    }

    @Override
    public int compareTo(Fields other) {
        if (other == null) return 1;
        int fieldsSize = getFieldNames().size();
        int otherFieldsSize = other.getFieldNames().size();
        int result = Integer.compare(fieldsSize, otherFieldsSize);
        if (result == 0) {
            String[] keys = getFieldNames().toArray(new String[0]);
            String[] otherKeys = other.getFieldNames().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                int cmp = keys[i].compareTo(otherKeys[i]);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }

        return result;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(fieldNames);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        fieldNames = (List<String>) stream.readObject();
    }
}
