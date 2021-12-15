package xyz.vopen.framework.cropdb.migration;

import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.tuples.Quartet;
import xyz.vopen.framework.cropdb.common.tuples.Triplet;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;

import java.util.List;

/**
 * Represents a migration instruction set for {@link ObjectRepository}.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface RepositoryInstruction extends Instruction {

    /**
     * Adds an instruction to rename the {@link ObjectRepository}.
     *
     * @param entityName the entity name
     * @param key        the key
     * @return the repository instruction
     */
    default RepositoryInstruction renameRepository(String entityName, String key) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RenameRepository);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), entityName, key));
        addStep(migrationStep);
        final RepositoryInstruction parent = this;

        return new RepositoryInstruction() {
            @Override
            public String entityName() {
                return entityName;
            }

            @Override
            public String key() {
                return key;
            }

            @Override
            public void addStep(MigrationStep step) {
                parent.addStep(step);
            }
        };
    }

    /**
     * Adds an instruction to add new field to the entity in
     * the {@link ObjectRepository}.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @return the repository instruction
     */
    default <T> RepositoryInstruction addField(String fieldName) {
        return addField(fieldName, null);
    }

    /**
     * Adds an instruction to add new field with a default value, into the entity in the
     * {@link ObjectRepository}.
     *
     * @param <T>          the type parameter
     * @param fieldName    the field name
     * @param defaultValue the default value
     * @return the repository instruction
     */
    default <T> RepositoryInstruction addField(String fieldName, T defaultValue) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryAddField);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), fieldName, defaultValue));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to add new field with value generator, into the entity in
     * the {@link ObjectRepository}.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @param generator the generator
     * @return the repository instruction
     */
    default <T> RepositoryInstruction addField(String fieldName, Generator<T> generator) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryAddField);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), fieldName, generator));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to rename a field to the entity in the
     * {@link ObjectRepository}.
     *
     * @param oldName the old name
     * @param newName the new name
     * @return the repository instruction
     */
    default RepositoryInstruction renameField(String oldName, String newName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryRenameField);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), oldName, newName));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to delete a field from the entity in
     * the {@link ObjectRepository}.
     *
     * @param fieldName the field name
     * @return the repository instruction
     */
    default RepositoryInstruction deleteField(String fieldName) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryDeleteField);
        migrationStep.setArguments(new Triplet<>(entityName(), key(), fieldName));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to change the datatype of a field of the entity in
     * the {@link ObjectRepository}.
     *
     * @param fieldName the field name
     * @param converter the converter
     * @return the repository instruction
     */
    default RepositoryInstruction changeDataType(String fieldName, TypeConverter converter) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryChangeDataType);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), fieldName, converter));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to change the id field of an entity in the
     * {@link ObjectRepository}
     *
     * @param oldFieldNames the old field names
     * @param newFieldNames the new field names
     * @return the repository instruction
     */
    default RepositoryInstruction changeIdField(List<String> oldFieldNames, List<String> newFieldNames) {
        Fields oldFields = Fields.withNames(oldFieldNames.toArray(new String[0]));
        Fields newFields = Fields.withNames(newFieldNames.toArray(new String[0]));
        return changeIdField(oldFields, newFields);
    }

    /**
     * Adds an instruction to change the id field of an entity in the
     * {@link ObjectRepository}
     *
     * @param oldField the old field names
     * @param newField the new field names
     * @return the repository instruction
     */
    default RepositoryInstruction changeIdField(Fields oldField, Fields newField) {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryChangeIdField);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), oldField, newField));
        addStep(migrationStep);
        return this;
    }



    /**
     * Adds an instruction to drop an index from the {@link ObjectRepository}.
     *
     * @param fieldNames the field names
     * @return the repository instruction
     */
    default RepositoryInstruction dropIndex(String... fieldNames) {
        Fields fields = Fields.withNames(fieldNames);
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryDropIndex);
        migrationStep.setArguments(new Triplet<>(entityName(), key(), fields));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to drop all indices from the {@link ObjectRepository}.
     *
     * @return the repository instruction
     */
    default RepositoryInstruction dropAllIndices() {
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryDropIndices);
        migrationStep.setArguments(new Pair<>(entityName(), key()));
        addStep(migrationStep);
        return this;
    }

    /**
     * Adds an instruction to create an index in the {@link ObjectRepository}.
     *
     * @param indexType  the index type
     * @param fieldNames the field names
     * @return the repository instruction
     */
    default RepositoryInstruction createIndex(String indexType, String... fieldNames) {
        Fields fields = Fields.withNames(fieldNames);
        MigrationStep migrationStep = new MigrationStep();
        migrationStep.setInstructionType(InstructionType.RepositoryCreateIndex);
        migrationStep.setArguments(new Quartet<>(entityName(), key(), fields, indexType));
        addStep(migrationStep);
        return this;
    }

    /**
     * The entity name of the {@link ObjectRepository}.
     *
     * @return the string
     */
    String entityName();

    /**
     * The key of the {@link ObjectRepository}.
     *
     * @return the string
     */
    String key();
}
