package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;

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
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        IndexDescriptor indexDescriptor = operations.findIndex(Fields.withNames(fieldName));
        for (Pair<NitriteId, Document> entry : nitriteMap.entries()) {
            Document document = entry.getSecond();
            document.remove(fieldName);
            nitriteMap.put(entry.getFirst(), document);
        }

        if (indexDescriptor != null) {
            operations.dropIndex(Fields.withNames(fieldName));
        }
    }
}
