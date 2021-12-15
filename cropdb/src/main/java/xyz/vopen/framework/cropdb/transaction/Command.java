package xyz.vopen.framework.cropdb.transaction;

/**
 * Represents an operation in a transaction.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
interface Command {
    /**
     * Executes the command during transaction commit or rollback.
     */
    void execute();
}
