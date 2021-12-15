package xyz.vopen.framework.cropdb.transaction;

import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.common.concurrent.LockService;
import xyz.vopen.framework.cropdb.exceptions.TransactionException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A crop transaction session. A session is needed to
 * initiate a transaction in crop database.
 *
 * <p>
 * If a session is closed and the transaction is not committed,
 * all opened transactions will get rolled back and all volatile
 * data gets discarded for the session.
 * </p>
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class Session implements AutoCloseable {
    private final CropDB cropdb;
    private final AtomicBoolean active;
    private final LockService lockService;
    private final Map<String, Transaction> transactionMap;

    /**
     * Instantiates a new Session.
     *
     * @param cropdb     the crop
     * @param lockService the lock service
     */
    public Session(CropDB cropdb, LockService lockService) {
        this.cropdb = cropdb;
        this.active = new AtomicBoolean(true);
        this.lockService = lockService;
        this.transactionMap = new HashMap<>();
    }

    /**
     * Begins a new transaction.
     *
     * @return the transaction
     */
    public Transaction beginTransaction() {
        checkState();

        Transaction tx = new CropTransaction(cropdb, lockService);
        transactionMap.put(tx.getId(), tx);
        return tx;
    }

    @Override
    public void close() {
        this.active.compareAndSet(true, false);
        for (Transaction transaction : transactionMap.values()) {
            if (transaction.getState() != State.Closed) {
                transaction.rollback();
            }
        }
    }

    /**
     * Checks state of the session. If the session is not active,
     * it will throw a {@link TransactionException}.
     *
     * @throws TransactionException when the session is not active,
     * and a transaction is initiated in this session.
     */
    public void checkState() {
        if (!active.get()) {
            throw new TransactionException("this session is not active");
        }
    }
}
