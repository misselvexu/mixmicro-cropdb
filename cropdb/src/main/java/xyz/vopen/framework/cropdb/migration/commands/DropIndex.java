package xyz.vopen.framework.cropdb.migration.commands;

import lombok.AllArgsConstructor;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.common.Fields;

/**
 * A command to drop an index.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@AllArgsConstructor
public class DropIndex extends BaseCommand implements Command {
    private final String collectionName;
    private final Fields fields;

    @Override
    public void execute(CropDB cropdb) {
        initialize(cropdb, collectionName);

        if (fields == null) {
            operations.dropAllIndices();
        } else {
            operations.dropIndex(fields);
        }
    }
}
