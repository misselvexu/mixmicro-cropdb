package xyz.vopen.framework.cropdb.migration.commands;

import lombok.AllArgsConstructor;
import xyz.vopen.framework.cropdb.CropDB;

/**
 * A command to drop a crop collection.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@AllArgsConstructor
public class Drop extends BaseCommand implements Command {
    private final String collectionName;

    @Override
    public void execute(CropDB cropdb) {
        initialize(cropdb, collectionName);
        operations.dropCollection();
    }
}
