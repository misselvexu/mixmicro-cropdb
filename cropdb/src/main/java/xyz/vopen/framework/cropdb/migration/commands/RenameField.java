package xyz.vopen.framework.cropdb.migration.commands;

import lombok.AllArgsConstructor;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.operation.IndexManager;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;

import java.util.Collection;

/**
 * A command to rename a document field.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@AllArgsConstructor
public class RenameField extends BaseCommand implements Command {
  private final String collectionName;
  private final String oldName;
  private final String newName;

  @Override
  public void execute(CropDB cropdb) {
    initialize(cropdb, collectionName);

    try (IndexManager indexManager = new IndexManager(oldName, cropdb.getConfig())) {
      Fields oldField = Fields.withNames(oldName);
      Collection<IndexDescriptor> matchingIndexDescriptors =
          indexManager.findMatchingIndexDescriptors(oldField);

      for (Pair<CropId, Document> entry : cropMap.entries()) {
        Document document = entry.getSecond();
        if (document.containsKey(oldName)) {
          Object value = document.get(oldName);
          document.put(newName, value);
          document.remove(oldName);

          cropMap.put(entry.getFirst(), document);
        }
      }

      if (!matchingIndexDescriptors.isEmpty()) {
        for (IndexDescriptor matchingIndexDescriptor : matchingIndexDescriptors) {
          String indexType = matchingIndexDescriptor.getIndexType();

          Fields oldIndexFields = matchingIndexDescriptor.getIndexFields();
          Fields newIndexFields = getNewIndexFields(oldIndexFields, oldName, newName);
          operations.dropIndex(matchingIndexDescriptor.getIndexFields());
          operations.createIndex(newIndexFields, indexType);
        }
      }
    }
  }

  private Fields getNewIndexFields(Fields oldIndexFields, String oldName, String newName) {
    Fields newIndexFields = new Fields();
    for (String fieldName : oldIndexFields.getFieldNames()) {
      if (fieldName.equals(oldName)) {
        newIndexFields.addField(newName);
      } else {
        newIndexFields.addField(fieldName);
      }
    }
    return newIndexFields;
  }
}
