package xyz.vopen.framework.cropdb.store.memory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.vopen.framework.cropdb.store.StoreConfig;
import xyz.vopen.framework.cropdb.store.events.StoreEventListener;

import java.util.HashSet;
import java.util.Set;

/**
 * The in-memory crop store config.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Accessors(fluent = true)
public class InMemoryConfig implements StoreConfig {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Set<StoreEventListener> eventListeners;

    /**
     * Instantiates a new {@link InMemoryConfig}.
     */
    InMemoryConfig() {
        this.eventListeners = new HashSet<>();
    }

    @Override
    public final String filePath() {
        return null;
    }

    @Override
    public Boolean isReadOnly() {
        return false;
    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
    }
}
