package xyz.vopen.framework.cropdb.migration;

import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.tuples.Triplet;
import xyz.vopen.framework.cropdb.common.util.SecureString;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;

/**
 * Represents a migration instruction set for the crop database.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface DatabaseInstruction extends Instruction {

  /**
   * Adds an instruction to set an user authentication to the database.
   *
   * @param username the username
   * @param password the password
   * @return the database instruction
   */
  default DatabaseInstruction addPassword(String username, String password) {
    MigrationStep migrationStep = new MigrationStep();
    migrationStep.setInstructionType(InstructionType.AddPassword);
    migrationStep.setArguments(new Pair<>(username, new SecureString(password)));
    addStep(migrationStep);
    return this;
  }

  /**
   * Adds an instruction to change the password for the user authentication to the database.
   *
   * @param username the username
   * @param oldPassword the old password
   * @param newPassword the new password
   * @return the database instruction
   */
  default DatabaseInstruction changePassword(
      String username, String oldPassword, String newPassword) {
    MigrationStep migrationStep = new MigrationStep();
    migrationStep.setInstructionType(InstructionType.ChangePassword);
    migrationStep.setArguments(
        new Triplet<>(username, new SecureString(oldPassword), new SecureString(newPassword)));
    addStep(migrationStep);
    return this;
  }

  /**
   * Adds an instruction to drop a {@link CropCollection} from the database.
   *
   * @param collectionName the collection name
   * @return the database instruction
   */
  default DatabaseInstruction dropCollection(String collectionName) {
    MigrationStep migrationStep = new MigrationStep();
    migrationStep.setInstructionType(InstructionType.DropCollection);
    migrationStep.setArguments(collectionName);
    addStep(migrationStep);
    return this;
  }

  /**
   * Adds an instruction to drop an {@link ObjectRepository} from the database.
   *
   * @param type the type
   * @return the database instruction
   */
  default DatabaseInstruction dropRepository(Class<?> type) {
    return dropRepository(type.getName());
  }

  /**
   * Adds an instruction to drop an {@link ObjectRepository} from the database.
   *
   * @param typeName the type name
   * @return the database instruction
   */
  default DatabaseInstruction dropRepository(String typeName) {
    return dropRepository(typeName, null);
  }

  /**
   * Adds an instruction to drop a keyed {@link ObjectRepository} from the database.
   *
   * @param type the type
   * @param key the key
   * @return the database instruction
   */
  default DatabaseInstruction dropRepository(Class<?> type, String key) {
    return dropRepository(type.getName(), key);
  }

  /**
   * Adds an instruction to drop a keyed {@link ObjectRepository} from the database.
   *
   * @param typeName the type name
   * @param key the key
   * @return the database instruction
   */
  default DatabaseInstruction dropRepository(String typeName, String key) {
    MigrationStep migrationStep = new MigrationStep();
    migrationStep.setInstructionType(InstructionType.DropRepository);
    migrationStep.setArguments(new Pair<>(typeName, key));
    addStep(migrationStep);
    return this;
  }

  /**
   * Adds a custom instruction to perform a user defined operation on the database.
   *
   * @param instruction the instruction
   * @return the database instruction
   */
  default DatabaseInstruction customInstruction(CustomInstruction instruction) {
    MigrationStep migrationStep = new MigrationStep();
    migrationStep.setInstructionType(InstructionType.Custom);
    migrationStep.setArguments(instruction);
    addStep(migrationStep);
    return this;
  }
}
