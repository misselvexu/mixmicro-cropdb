package xyz.vopen.framework.cropdb.store;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.common.event.CropEventBus;
import xyz.vopen.framework.cropdb.store.events.EventInfo;
import xyz.vopen.framework.cropdb.store.events.StoreEventBus;
import xyz.vopen.framework.cropdb.store.events.StoreEventListener;
import xyz.vopen.framework.cropdb.store.events.StoreEvents;

import java.util.Map;
import java.util.Set;

/**
 * An abstract {@link CropStore} implementation.
 *
 * @param <Config> the type parameter
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Slf4j
public abstract class AbstractCropStore<Config extends StoreConfig>
    implements CropStore<Config> {

    @Getter @Setter
    private Config storeConfig;

    /**
     * The {@link CropEventBus} for the database.
     */
    protected final CropEventBus<EventInfo, StoreEventListener> eventBus;

    /**
     * The {@link CropConfig} for this store.
     */
    protected CropConfig cropConfig;

    private StoreCatalog storeCatalog;

    /**
     * Instantiates a new {@link AbstractCropStore}.
     */
    protected AbstractCropStore() {
        eventBus = new StoreEventBus();
    }

    /**
     * Alerts about an {@link StoreEvents} to all subscribed {@link StoreEventListener}s.
     *
     * @param eventType the event type
     */
    protected void alert(StoreEvents eventType) {
        EventInfo event = new EventInfo(eventType, cropConfig);
        eventBus.post(event);
    }

    @Override
    public Set<String> getCollectionNames() {
        return getCatalog().getCollectionNames();
    }

    @Override
    public Set<String> getRepositoryRegistry() {
        return getCatalog().getRepositoryNames();
    }

    @Override
    public Map<String, Set<String>> getKeyedRepositoryRegistry() {
        return getCatalog().getKeyedRepositoryNames();
    }

    @Override
    public void beforeClose() {
        alert(StoreEvents.Closing);
    }

    @Override
    public void removeRTree(String mapName) {
        this.removeMap(mapName);
    }

    @Override
    public void subscribe(StoreEventListener listener) {
        eventBus.register(listener);
    }

    @Override
    public void unsubscribe(StoreEventListener listener) {
        eventBus.deregister(listener);
    }

    @Override
    public void initialize(CropConfig cropConfig) {
        this.cropConfig = cropConfig;
    }

    @Override
    public StoreCatalog getCatalog() {
        if (storeCatalog == null) {
            this.storeCatalog = new StoreCatalog(this);
        }
        return storeCatalog;
    }
}
