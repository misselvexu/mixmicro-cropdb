package xyz.vopen.framework.cropdb.transaction;

import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.index.BoundingBox;
import xyz.vopen.framework.cropdb.store.*;
import xyz.vopen.framework.cropdb.store.events.StoreEventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
class TransactionStore<T extends StoreConfig> extends AbstractCropStore<T> {
    private final CropStore<T> primaryStore;
    private final Map<String, CropMap<?, ?>> mapRegistry;
    private final Map<String, CropRTree<?, ?>> rTreeRegistry;

    public TransactionStore(CropStore<T> store) {
        this.primaryStore = store;
        this.mapRegistry = new ConcurrentHashMap<>();
        this.rTreeRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void openOrCreate() {
        // nothing to do
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void commit() {
        throw new InvalidOperationException("call commit on transaction");
    }

    @Override
    public void close() {
        for (CropMap<?, ?> cropMap : mapRegistry.values()) {
            cropMap.close();
        }

        for (CropRTree<?, ?> rTree : rTreeRegistry.values()) {
            rTree.close();
        }

        mapRegistry.clear();
        rTreeRegistry.clear();
        eventBus.close();
    }

    @Override
    public boolean hasMap(String mapName) {
        boolean result = primaryStore.hasMap(mapName);
        if (!result) {
            result = mapRegistry.containsKey(mapName);
            if (!result) {
                return rTreeRegistry.containsKey(mapName);
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key, Value> CropMap<Key, Value> openMap(String mapName, Class<?> keyType, Class<?> valueType) {
        if (mapRegistry.containsKey(mapName)) {
            return (CropMap<Key, Value>) mapRegistry.get(mapName);
        }

        CropMap<Key, Value> primaryMap = null;
        if (primaryStore.hasMap(mapName)) {
            primaryMap = primaryStore.openMap(mapName, keyType, valueType);
        }

        TransactionalMap<Key, Value> transactionalMap = new TransactionalMap<>(mapName, primaryMap, this);
        mapRegistry.put(mapName, transactionalMap);
        return transactionalMap;
    }

    @Override
    public void closeMap(String mapName) {
        // nothing to close as it is volatile map, moreover,
        // removing it form registry means loosing the map
    }

    @Override
    public void closeRTree(String rTreeName) {
        // nothing to close as it is volatile map, moreover,
        // removing it form registry means loosing the map
    }

    @Override
    public void removeMap(String mapName) {
        mapRegistry.remove(mapName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key extends BoundingBox, Value> CropRTree<Key, Value> openRTree(String rTreeName,
                                                                            Class<?> keyType,
                                                                            Class<?> valueType) {
        if (rTreeRegistry.containsKey(rTreeName)) {
            return (CropRTree<Key, Value>) rTreeRegistry.get(rTreeName);
        }

        CropRTree<Key, Value> primaryMap = null;
        if (primaryStore.hasMap(rTreeName)) {
            primaryMap = primaryStore.openRTree(rTreeName, keyType, valueType);
        }

        TransactionalRTree<Key, Value> transactionalRtree = new TransactionalRTree<>(primaryMap);
        rTreeRegistry.put(rTreeName, transactionalRtree);
        return transactionalRtree;
    }

    @Override
    public void removeRTree(String rTreeName) {
        rTreeRegistry.remove(rTreeName);
    }

    @Override
    public void subscribe(StoreEventListener listener) {

    }

    @Override
    public void unsubscribe(StoreEventListener listener) {

    }

    @Override
    public String getStoreVersion() {
        return primaryStore.getStoreVersion();
    }

    @Override
    public T getStoreConfig() {
        return null;
    }
}
