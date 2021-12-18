package xyz.vopen.framework.cropdb.rocksdb;

import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.common.UnknownType;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.exceptions.CropException;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.index.BoundingBox;
import xyz.vopen.framework.cropdb.store.AbstractCropStore;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropRTree;
import xyz.vopen.framework.cropdb.store.events.StoreEventListener;
import xyz.vopen.framework.cropdb.store.events.StoreEvents;
import org.rocksdb.RocksDB;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RocksDBStore extends AbstractCropStore<RocksDBConfig> {
  private final AtomicBoolean closed;
  private final Map<String, CropMap<?, ?>> cropMapRegistry;
  private RocksDBReference reference;

  public RocksDBStore() {
    super();
    cropMapRegistry = new ConcurrentHashMap<>();
    closed = new AtomicBoolean(true);
  }

  @Override
  public void openOrCreate() {
    try {
      if (closed.get()) {
        this.reference = RocksDBStoreUtils.openOrCreate(getStoreConfig());
        closed.compareAndSet(true, false);
        initEventBus();
        alert(StoreEvents.Opened);
      }
    } catch (CropException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error while opening database", e);
      throw new CropIOException("failed to open database", e);
    }
  }

  @Override
  public boolean isClosed() {
    return closed.get();
  }

  @Override
  public boolean hasUnsavedChanges() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void commit() {
    alert(StoreEvents.Commit);
  }

  @Override
  public void close() {
    try {
      if (!closed.get()) {
        // close crop maps
        for (CropMap<?, ?> cropMap : cropMapRegistry.values()) {
          cropMap.close();
        }

        reference.close();
        closed.compareAndSet(false, true);
      }

      alert(StoreEvents.Closed);
      eventBus.close();
    } catch (Exception e) {
      log.error("Error while closing the database", e);
      throw new CropIOException("failed to close database", e);
    }
  }

  @Override
  public boolean hasMap(String mapName) {
    return reference.getColumnFamilyHandleRegistry().containsKey(mapName);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <Key, Value> CropMap<Key, Value> openMap(
      String mapName, Class<?> keyType, Class<?> valueType) {
    if (cropMapRegistry.containsKey(mapName)) {
      RocksDBMap<Key, Value> cropMap = (RocksDBMap<Key, Value>) cropMapRegistry.get(mapName);
      if (UnknownType.class.equals(cropMap.getKeyType())) {
        cropMap.setKeyType(keyType);
      }

      if (UnknownType.class.equals(cropMap.getValueType())) {
        cropMap.setValueType(valueType);
      }

      return cropMap;
    } else {
      CropMap<Key, Value> cropMap =
          new RocksDBMap<>(mapName, this, this.reference, keyType, valueType);
      cropMapRegistry.put(mapName, cropMap);
      return cropMap;
    }
  }

  @Override
  public void closeMap(String mapName) {
    if (!StringUtils.isNullOrEmpty(mapName)) {
      cropMapRegistry.remove(mapName);
    }
  }

  @Override
  public void removeMap(String mapName) {
    reference.dropColumnFamily(mapName);
    getCatalog().remove(mapName);
    cropMapRegistry.remove(mapName);
  }

  @Override
  public <Key extends BoundingBox, Value> CropRTree<Key, Value> openRTree(
      String rTreeName, Class<?> keyType, Class<?> valueType) {
    throw new InvalidOperationException("rtree not supported on rocksdb store");
  }

  @Override
  public void closeRTree(String rTreeName) {
    throw new InvalidOperationException("rtree not supported on rocksdb store");
  }

  @Override
  public void removeRTree(String mapName) {
    throw new InvalidOperationException("rtree not supported on rocksdb store");
  }

  @Override
  public String getStoreVersion() {
    return "RocksDB/" + getRocksDbVersion();
  }

  private void initEventBus() {
    if (getStoreConfig().eventListeners() != null) {
      for (StoreEventListener eventListener : getStoreConfig().eventListeners()) {
        eventBus.register(eventListener);
      }
    }
  }

  private static String getRocksDbVersion() {
    RocksDB.Version version = RocksDB.rocksdbVersion();
    return version.toString();
  }
}
