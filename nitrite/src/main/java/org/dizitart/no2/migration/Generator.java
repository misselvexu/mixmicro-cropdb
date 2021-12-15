package org.dizitart.no2.migration;

import org.dizitart.no2.collection.Document;

/**
 * Represents a default value generator for the document fields during field manipulation instruction.
 *
 * @param <T> the type parameter
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface Generator<T> {
    /**
     * Generates a new value for a field in the document.
     *
     * @param document the document
     * @return the value
     */
    T generate(Document document);
}
