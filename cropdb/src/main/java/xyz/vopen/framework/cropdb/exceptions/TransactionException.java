package xyz.vopen.framework.cropdb.exceptions;

/**
 * Exception thrown when a transaction fails.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class TransactionException extends CropException {
    /**
     * Instantiates a new Transaction exception.
     *
     * @param errorMessage the error message
     */
    public TransactionException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new Transaction exception.
     *
     * @param errorMessage the error message
     * @param error        the error
     */
    public TransactionException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
