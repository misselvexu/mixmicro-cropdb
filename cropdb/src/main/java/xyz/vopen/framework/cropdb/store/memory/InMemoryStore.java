package xyz.vopen.framework.cropdb.store.memory;

import xyz.vopen.framework.cropdb.index.BoundingBox;
import xyz.vopen.framework.cropdb.store.AbstractCropStore;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropRTree;
import xyz.vopen.framework.cropdb.store.events.StoreEventListener;
import xyz.vopen.framework.cropdb.store.events.StoreEvents;
import xyz.vopen.framework.cropdb.common.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The crop in-memory store.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public final class InMemoryStore extends AbstractCropStore<InMemoryConfig> {
    private final Map<String, CropMap<?, ?>> cropMapRegistry;
    private final Map<String, CropRTree<?, ?>> cropRTreeMapRegistry;
    private volatile boolean closed = false;

    /**
     * Instantiates a new {@link InMemoryStore}.
     */
    public InMemoryStore() {
        super();
        this.cropMapRegistry = new ConcurrentHashMap<>();
        this.cropRTreeMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void openOrCreate() {
        initEventBus();
        alert(StoreEvents.Opened);
    }

    @Override
    public boolean isClosed() {
        return closed;
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
        closed = true;

        for (CropMap<?, ?> map : cropMapRegistry.values()) {
            map.close();
        }

        for (CropRTree<?, ?> rTree : cropRTreeMapRegistry.values()) {
            rTree.close();
        }

        cropMapRegistry.clear();
        cropRTreeMapRegistry.clear();
        alert(StoreEvents.Closed);
        eventBus.close();
    }

    @Override
    public boolean hasMap(String mapName) {
        return cropMapRegistry.containsKey(mapName) || cropRTreeMapRegistry.containsKey(mapName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key, Value> CropMap<Key, Value> openMap(String mapName, Class<?> keyType, Class<?> valueType) {
        if (cropMapRegistry.containsKey(mapName)) {
            return (InMemoryMap<Key, Value>) cropMapRegistry.get(mapName);
        }

        CropMap<Key, Value> cropMap = new InMemoryMap<>(mapName, this);
        cropMapRegistry.put(mapName, cropMap);

        return cropMap;
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
        if (cropMapRegistry.containsKey(mapName)) {
            cropMapRegistry.get(mapName).clear();
            cropMapRegistry.remove(mapName);
            getCatalog().remove(mapName);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key extends BoundingBox, Value> CropRTree<Key, Value> openRTree(String rTreeName,
                                                                            Class<?> keyType,
                                                                            Class<?> valueType) {
        if (cropRTreeMapRegistry.containsKey(rTreeName)) {
            return (InMemoryRTree<Key, Value>) cropRTreeMapRegistry.get(rTreeName);
        }

        CropRTree<Key, Value> rTree = new InMemoryRTree<>();
        cropRTreeMapRegistry.put(rTreeName, rTree);

        return rTree;
    }

    @Override
    public String getStoreVersion() {
        return "InMemory/" + Constants.CROPDB_VERSION;
    }

    private void initEventBus() {
        if (getStoreConfig().eventListeners() != null) {
            for (StoreEventListener eventListener : getStoreConfig().eventListeners()) {
                eventBus.register(eventListener);
            }
        }
    }
}
