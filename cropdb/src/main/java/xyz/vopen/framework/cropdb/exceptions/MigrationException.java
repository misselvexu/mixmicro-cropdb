package xyz.vopen.framework.cropdb.exceptions;

/**
 * Exception thrown when a migration step fails.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class MigrationException extends CropException {
  /**
   * Instantiates a new Migration exception.
   *
   * @param errorMessage the error message
   */
  public MigrationException(String errorMessage) {
    super(errorMessage);
  }
}
