package xyz.vopen.framework.cropdb.transaction;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.common.concurrent.LockService;
import xyz.vopen.framework.cropdb.common.module.CropModule;
import xyz.vopen.framework.cropdb.exceptions.TransactionException;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import static xyz.vopen.framework.cropdb.common.util.ObjectUtils.findRepositoryName;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Slf4j
class CropTransaction implements Transaction {
  private final CropDB cropdb;
  private final LockService lockService;

  private TransactionStore<?> transactionStore;
  private TransactionConfig transactionConfig;
  private Map<String, TransactionContext> contextMap;
  private Map<String, CropCollection> collectionRegistry;
  private Map<String, ObjectRepository<?>> repositoryRegistry;
  private Map<String, Stack<UndoEntry>> undoRegistry;

  @Getter private String id;

  private State state;

  public CropTransaction(CropDB cropdb, LockService lockService) {
    this.cropdb = cropdb;
    this.lockService = lockService;
    prepare();
  }

  @Override
  public synchronized CropCollection getCollection(String name) {
    checkState();

    if (collectionRegistry.containsKey(name)) {
      return collectionRegistry.get(name);
    }

    CropCollection primary;
    if (cropdb.hasCollection(name)) {
      primary = cropdb.getCollection(name);
    } else {
      throw new TransactionException("collection " + name + " does not exists");
    }

    CropMap<CropId, Document> txMap = transactionStore.openMap(name, CropId.class, Document.class);

    TransactionContext context = new TransactionContext();
    context.setCollectionName(name);
    context.setCropMap(txMap);
    context.setJournal(new LinkedList<>());
    context.setConfig(transactionConfig);

    CropCollection txCollection = new DefaultTransactionalCollection(primary, context, cropdb);
    collectionRegistry.put(name, txCollection);
    contextMap.put(name, context);
    return txCollection;
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized <T> ObjectRepository<T> getRepository(Class<T> type) {
    checkState();

    String name = findRepositoryName(type, null);
    if (repositoryRegistry.containsKey(name)) {
      return (ObjectRepository<T>) repositoryRegistry.get(name);
    }

    ObjectRepository<T> primary;
    if (cropdb.hasRepository(type)) {
      primary = cropdb.getRepository(type);
    } else {
      throw new TransactionException("repository of type " + type.getName() + " does not exists");
    }

    CropMap<CropId, Document> txMap = transactionStore.openMap(name, CropId.class, Document.class);

    TransactionContext context = new TransactionContext();
    context.setCollectionName(name);
    context.setCropMap(txMap);
    context.setJournal(new LinkedList<>());
    context.setConfig(transactionConfig);

    CropCollection primaryCollection = primary.getDocumentCollection();
    CropCollection backingCollection =
        new DefaultTransactionalCollection(primaryCollection, context, cropdb);
    ObjectRepository<T> txRepository =
        new DefaultTransactionalRepository<>(type, primary, backingCollection, transactionConfig);

    repositoryRegistry.put(name, txRepository);
    contextMap.put(name, context);
    return txRepository;
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized <T> ObjectRepository<T> getRepository(Class<T> type, String key) {
    checkState();

    String name = findRepositoryName(type, key);
    if (repositoryRegistry.containsKey(name)) {
      return (ObjectRepository<T>) repositoryRegistry.get(name);
    }

    ObjectRepository<T> primary;
    if (cropdb.hasRepository(type, key)) {
      primary = cropdb.getRepository(type, key);
    } else {
      throw new TransactionException(
          "repository of type " + type.getName() + " and key " + key + " does not exists");
    }

    CropMap<CropId, Document> txMap = transactionStore.openMap(name, CropId.class, Document.class);

    TransactionContext context = new TransactionContext();
    context.setCollectionName(name);
    context.setCropMap(txMap);
    context.setJournal(new LinkedList<>());
    context.setConfig(transactionConfig);

    CropCollection primaryCollection = primary.getDocumentCollection();
    CropCollection backingCollection =
        new DefaultTransactionalCollection(primaryCollection, context, cropdb);
    ObjectRepository<T> txRepository =
        new DefaultTransactionalRepository<>(type, primary, backingCollection, transactionConfig);
    repositoryRegistry.put(name, txRepository);
    contextMap.put(name, context);
    return txRepository;
  }

  @Override
  public synchronized void commit() {
    checkState();
    this.state = State.PartiallyCommitted;

    for (Map.Entry<String, TransactionContext> contextEntry : contextMap.entrySet()) {
      String collectionName = contextEntry.getKey();
      TransactionContext transactionContext = contextEntry.getValue();

      Stack<UndoEntry> undoLog =
          undoRegistry.containsKey(collectionName)
              ? undoRegistry.get(collectionName)
              : new Stack<>();

      // put collection level lock
      Lock lock = lockService.getWriteLock(collectionName);
      try {
        lock.lock();
        Queue<JournalEntry> commitLog = transactionContext.getJournal();
        int length = commitLog.size();
        for (int i = 0; i < length; i++) {
          JournalEntry entry = commitLog.poll();
          if (entry != null) {
            Command commitCommand = entry.getCommit();
            if (commitCommand != null) {
              try {
                commitCommand.execute();
              } finally {
                UndoEntry undoEntry = new UndoEntry();
                undoEntry.setCollectionName(collectionName);
                undoEntry.setRollback(entry.getRollback());
                undoLog.push(undoEntry);
              }
            }
          }
        }
      } catch (TransactionException te) {
        state = State.Failed;
        log.error("Error while committing transaction", te);
        throw te;
      } catch (Exception e) {
        state = State.Failed;
        log.error("Error while committing transaction", e);
        throw new TransactionException("failed to commit transaction", e);
      } finally {
        undoRegistry.put(collectionName, undoLog);
        transactionContext.getActive().set(false);
        lock.unlock();
      }
    }

    state = State.Committed;
    close();
  }

  @Override
  public synchronized void rollback() {
    this.state = State.Aborted;

    for (Map.Entry<String, Stack<UndoEntry>> entry : undoRegistry.entrySet()) {
      String collectionName = entry.getKey();
      Stack<UndoEntry> undoLog = entry.getValue();

      // put collection level lock
      Lock writeLock = lockService.getWriteLock(collectionName);
      try {
        writeLock.lock();

        int size = undoLog.size();
        for (int i = 0; i < size; i++) {
          UndoEntry undoEntry = undoLog.pop();
          if (undoEntry != null) {
            Command rollbackCommand = undoEntry.getRollback();
            rollbackCommand.execute();
          }
        }
      } finally {
        writeLock.unlock();
      }
    }
    close();
  }

  @Override
  public synchronized void close() {
    try {
      state = State.Closed;
      for (TransactionContext context : contextMap.values()) {
        context.getActive().set(false);
      }

      this.contextMap.clear();
      this.collectionRegistry.clear();
      this.repositoryRegistry.clear();
      this.undoRegistry.clear();
      this.transactionStore.close();
      this.transactionConfig.close();
    } catch (Exception e) {
      throw new TransactionException("transaction failed to close", e);
    }
  }

  @Override
  public synchronized State getState() {
    return state;
  }

  private void prepare() {
    this.contextMap = new ConcurrentHashMap<>();
    this.collectionRegistry = new ConcurrentHashMap<>();
    this.repositoryRegistry = new ConcurrentHashMap<>();
    this.undoRegistry = new ConcurrentHashMap<>();

    this.id = UUID.randomUUID().toString();

    CropStore<?> cropStore = cropdb.getStore();
    CropConfig cropConfig = cropdb.getConfig();
    this.transactionConfig = new TransactionConfig(cropConfig);
    this.transactionConfig.loadModule(CropModule.module(new TransactionStore<>(cropStore)));

    this.transactionConfig.autoConfigure();
    this.transactionConfig.initialize();
    this.transactionStore = (TransactionStore<?>) this.transactionConfig.getCropStore();
    this.state = State.Active;
  }

  private void checkState() {
    if (state != State.Active) {
      throw new TransactionException("transaction is not active");
    }
  }
}
