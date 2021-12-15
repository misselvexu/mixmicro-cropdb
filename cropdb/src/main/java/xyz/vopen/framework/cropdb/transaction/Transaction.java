package xyz.vopen.framework.cropdb.transaction;

import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;

/**
 * Represents an ACID transaction on crop database.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface Transaction extends AutoCloseable {
    /**
     * Gets the transaction id.
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the current state of the transaction.
     *
     * @return the state
     */
    State getState();

    /**
     * Gets a {@link CropCollection} to perform ACID operations on it.
     *
     * @param name the name
     * @return the collection
     */
    CropCollection getCollection(String name);

    /**
     * Gets an {@link ObjectRepository} to perform ACID operations on it.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @return the repository
     */
    <T> ObjectRepository<T> getRepository(Class<T> type);

    /**
     * Gets an {@link ObjectRepository} to perform ACID operations on it.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @param key  the key
     * @return the repository
     */
    <T> ObjectRepository<T> getRepository(Class<T> type, String key);

    /**
     * Completes the transaction and commits the data to the underlying store.
     */
    void commit();

    /**
     * Rolls back the changes.
     */
    void rollback();

    /**
     * Closes this {@link Transaction}.
     * */
    void close();
}
