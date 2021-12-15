package xyz.vopen.framework.cropdb.migration.commands;

import xyz.vopen.framework.cropdb.CropDB;

/**
 * Represents a database migration command.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface Command extends AutoCloseable {
    /**
     * Executes a migration step on the database.
     *
     * @param cropdb the crop database instance
     */
    void execute(CropDB cropdb);

    default void close() {
        // this is just to make Command a functional interface
        // and make close() not throw checked exception
    }
}
