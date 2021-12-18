package xyz.vopen.framework.cropdb.migration.commands;

import lombok.AllArgsConstructor;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.collection.operation.CollectionOperations;
import xyz.vopen.framework.cropdb.collection.operation.IndexManager;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.store.CropMap;

import java.util.Collection;

/**
 * A command to rename a {@link CropCollection}.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@AllArgsConstructor
public class Rename extends BaseCommand implements Command {
  private final String oldName;
  private final String newName;

  @Override
  public void execute(CropDB cropdb) {
    initialize(cropdb, oldName);

    CropMap<CropId, Document> newMap = cropStore.openMap(newName, CropId.class, Document.class);
    try (CollectionOperations newOperations =
        new CollectionOperations(newName, newMap, cropdb.getConfig(), null)) {

      for (Pair<CropId, Document> entry : cropMap.entries()) {
        newMap.put(entry.getFirst(), entry.getSecond());
      }

      try (IndexManager indexManager = new IndexManager(oldName, cropdb.getConfig())) {
        Collection<IndexDescriptor> indexEntries = indexManager.getIndexDescriptors();
        for (IndexDescriptor indexDescriptor : indexEntries) {
          Fields field = indexDescriptor.getIndexFields();
          String indexType = indexDescriptor.getIndexType();
          newOperations.createIndex(field, indexType);
        }
      }
    }

    operations.dropCollection();
  }
}
