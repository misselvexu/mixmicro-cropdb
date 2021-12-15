package xyz.vopen.framework.cropdb.migration.commands;

import lombok.AllArgsConstructor;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.migration.Generator;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@AllArgsConstructor
public class AddField extends BaseCommand implements Command {
    private final String collectionName;
    private final String fieldName;
    private final Object defaultValue;

    @Override
    public void execute(CropDB cropdb) {
        initialize(cropdb, collectionName);

        IndexDescriptor indexDescriptor = operations.findIndex(Fields.withNames(fieldName));

        for (Pair<CropId, Document> pair : cropMap.entries()) {
            Document document = pair.getSecond();
            if (defaultValue instanceof Generator) {
                Generator<?> generator = (Generator<?>) defaultValue;
                document.put(fieldName, generator.generate(document));
            } else {
                document.put(fieldName, defaultValue);
            }
            cropMap.put(pair.getFirst(), document);
        }

        if (indexDescriptor != null) {
            operations.createIndex(Fields.withNames(fieldName), indexDescriptor.getIndexType());
        }
    }
}
