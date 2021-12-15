package xyz.vopen.framework.cropdb.migration.commands;

import lombok.AllArgsConstructor;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.migration.TypeConverter;

/**
 * A migration command to change the datatype of a document field
 * in a collection.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@AllArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class ChangeDataType extends BaseCommand implements Command {
    private final String collectionName;
    private final String fieldName;
    private final TypeConverter typeConverter;

    @Override
    public void execute(CropDB cropdb) {
        initialize(cropdb, collectionName);

        for (Pair<CropId, Document> entry : cropMap.entries()) {
            Document document = entry.getSecond();
            Object value = document.get(fieldName);
            Object newValue = typeConverter.convert(value);
            document.put(fieldName, newValue);

            cropMap.put(entry.getFirst(), document);
        }

        IndexDescriptor indexDescriptor = operations.findIndex(Fields.withNames(fieldName));
        if (indexDescriptor != null) {
            operations.rebuildIndex(indexDescriptor);
        }
    }
}