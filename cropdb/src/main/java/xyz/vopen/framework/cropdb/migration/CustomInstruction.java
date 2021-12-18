package xyz.vopen.framework.cropdb.migration;

import xyz.vopen.framework.cropdb.CropDB;

/**
 * Represents a custom instruction for database migration.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface CustomInstruction {
  /**
   * Performs the instruction on the crop database.
   *
   * @param cropdb the crop database
   */
  void perform(CropDB cropdb);
}
