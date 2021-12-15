package xyz.vopen.framework.cropdb.store.memory;

import lombok.AccessLevel;
import lombok.Setter;
import xyz.vopen.framework.cropdb.common.module.CropPlugin;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.store.StoreModule;
import xyz.vopen.framework.cropdb.common.util.Iterables;

import java.util.Set;

/**
 * The in-memory store module for crop.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class InMemoryStoreModule implements StoreModule {

    @Setter(AccessLevel.PACKAGE)
    private InMemoryConfig storeConfig;

    /**
     * Instantiates a new {@link InMemoryStoreModule}.
     */
    public InMemoryStoreModule() {
        this.storeConfig = new InMemoryConfig();
    }

    /**
     * Creates an {@link InMemoryModuleBuilder} to configure the in-memory store.
     *
     * @return the in memory module builder
     */
    public static InMemoryModuleBuilder withConfig() {
        return new InMemoryModuleBuilder();
    }

    @Override
    public CropStore<?> getStore() {
        InMemoryStore store = new InMemoryStore();
        store.setStoreConfig(storeConfig);
        return store;
    }

    @Override
    public Set<CropPlugin> plugins() {
        return Iterables.setOf(getStore());
    }
}
