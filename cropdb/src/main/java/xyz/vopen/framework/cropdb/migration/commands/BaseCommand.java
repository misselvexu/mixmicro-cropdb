package xyz.vopen.framework.cropdb.migration.commands;

import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.operation.CollectionOperations;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;

/**
 * Represents a base command for database migration. It initializes different components necessary
 * to execute the migration steps
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
abstract class BaseCommand implements Command {
  /** The crop store. */
  protected CropStore<?> cropStore;

  /** The crop map. */
  protected CropMap<CropId, Document> cropMap;

  /** The collection operations. */
  protected CollectionOperations operations;

  @Override
  public void close() {
    if (operations != null) {
      operations.close();
    }
  }

  /**
   * Initializes the database for migration.
   *
   * @param cropdb the crop
   * @param collectionName the collection name
   */
  void initialize(CropDB cropdb, String collectionName) {
    cropStore = cropdb.getStore();

    cropMap = cropStore.openMap(collectionName, CropId.class, Document.class);
    operations = new CollectionOperations(collectionName, cropMap, cropdb.getConfig(), null);
  }
}
