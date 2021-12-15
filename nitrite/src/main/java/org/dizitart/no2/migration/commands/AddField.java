package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.migration.Generator;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@AllArgsConstructor
public class AddField extends BaseCommand implements Command {
    private final String collectionName;
    private final String fieldName;
    private final Object defaultValue;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        IndexDescriptor indexDescriptor = operations.findIndex(Fields.withNames(fieldName));

        for (Pair<NitriteId, Document> pair : nitriteMap.entries()) {
            Document document = pair.getSecond();
            if (defaultValue instanceof Generator) {
                Generator<?> generator = (Generator<?>) defaultValue;
                document.put(fieldName, generator.generate(document));
            } else {
                document.put(fieldName, defaultValue);
            }
            nitriteMap.put(pair.getFirst(), document);
        }

        if (indexDescriptor != null) {
            operations.createIndex(Fields.withNames(fieldName), indexDescriptor.getIndexType());
        }
    }
}
