package xyz.vopen.framework.cropdb.migration.commands;

import lombok.AllArgsConstructor;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.common.Fields;

/**
 * A command to create an index.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@AllArgsConstructor
public class CreateIndex extends BaseCommand implements Command {
    private final String collectionName;
    private final Fields fields;
    private final String indexType;

    @Override
    public void execute(CropDB cropdb) {
        initialize(cropdb, collectionName);

        operations.createIndex(fields, indexType);
    }
}
