package org.dizitart.no2.transaction;

/**
 * The transaction state.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public enum State {
    /**
     * Transaction is active.
     */
    Active,

    /**
     * Transaction is partially committed.
     */
    PartiallyCommitted,

    /**
     * Transaction is fully committed.
     */
    Committed,

    /**
     * Transaction is closed.
     */
    Closed,

    /**
     * Transaction is failed.
     */
    Failed,

    /**
     * Transaction is aborted.
     */
    Aborted,
}
