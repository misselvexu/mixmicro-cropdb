package org.dizitart.no2.migration;

import org.dizitart.no2.Nitrite;

/**
 * Represents a custom instruction for database migration.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface CustomInstruction {
    /**
     * Performs the instruction on the nitrite database.
     *
     * @param nitrite the nitrite database
     */
    void perform(Nitrite nitrite);
}
