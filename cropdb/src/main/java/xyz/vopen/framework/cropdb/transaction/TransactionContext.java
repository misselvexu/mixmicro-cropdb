package xyz.vopen.framework.cropdb.transaction;

import lombok.Data;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.store.CropMap;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Data
class TransactionContext implements AutoCloseable {
  private String collectionName;
  private Queue<JournalEntry> journal;
  private CropMap<CropId, Document> cropMap;
  private TransactionConfig config;
  private AtomicBoolean active;

  public TransactionContext() {
    active = new AtomicBoolean(true);
  }

  @Override
  public void close() throws Exception {
    journal.clear();
    cropMap.clear();
    cropMap.close();
    active.compareAndSet(true, false);
  }
}
