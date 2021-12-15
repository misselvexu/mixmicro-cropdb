package org.dizitart.no2.exceptions;

/**
 * Exception thrown when a migration step fails.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class MigrationException extends NitriteException {
    /**
     * Instantiates a new Migration exception.
     *
     * @param errorMessage the error message
     */
    public MigrationException(String errorMessage) {
        super(errorMessage);
    }
}
