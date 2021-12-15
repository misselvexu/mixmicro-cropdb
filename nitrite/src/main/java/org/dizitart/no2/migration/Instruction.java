package org.dizitart.no2.migration;

/**
 * Represents a collection of database migration steps.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
interface Instruction {
    /**
     * Adds a migration step to the instruction set.
     *
     * @param step the step
     */
    void addStep(MigrationStep step);
}
