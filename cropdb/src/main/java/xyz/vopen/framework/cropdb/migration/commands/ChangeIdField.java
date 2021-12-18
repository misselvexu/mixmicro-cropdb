package xyz.vopen.framework.cropdb.migration.commands;

import lombok.AllArgsConstructor;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.index.IndexType;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;

/**
 * A command to change the id fields of an entity in an {@link ObjectRepository}.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@AllArgsConstructor
public class ChangeIdField extends BaseCommand implements Command {
  private final String collectionName;
  private final Fields oldFields;
  private final Fields newFields;

  @Override
  public void execute(CropDB cropdb) {
    initialize(cropdb, collectionName);

    boolean hasIndex = operations.hasIndex(oldFields);
    if (hasIndex) {
      operations.dropIndex(oldFields);
    }

    operations.createIndex(newFields, IndexType.UNIQUE);
  }
}
