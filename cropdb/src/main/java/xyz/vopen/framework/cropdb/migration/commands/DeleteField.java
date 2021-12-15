package xyz.vopen.framework.cropdb.migration.commands;

import lombok.AllArgsConstructor;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;

/**
 * A command to delete a field from the document of
 * a collection.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@AllArgsConstructor
public class DeleteField extends BaseCommand implements Command {
    private final String collectionName;
    private final String fieldName;

    @Override
    public void execute(CropDB cropdb) {
        initialize(cropdb, collectionName);

        IndexDescriptor indexDescriptor = operations.findIndex(Fields.withNames(fieldName));
        for (Pair<CropId, Document> entry : cropMap.entries()) {
            Document document = entry.getSecond();
            document.remove(fieldName);
            cropMap.put(entry.getFirst(), document);
        }

        if (indexDescriptor != null) {
            operations.dropIndex(Fields.withNames(fieldName));
        }
    }
}
