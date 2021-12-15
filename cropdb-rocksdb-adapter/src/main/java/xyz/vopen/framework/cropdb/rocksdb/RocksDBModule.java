package xyz.vopen.framework.cropdb.rocksdb;

import lombok.AccessLevel;
import lombok.Setter;
import xyz.vopen.framework.cropdb.common.module.CropPlugin;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.store.StoreModule;
import xyz.vopen.framework.cropdb.common.util.Iterables;

import java.util.Set;

public class RocksDBModule implements StoreModule {
    @Setter(AccessLevel.PACKAGE)
    private RocksDBConfig storeConfig;

    public RocksDBModule(String path) {
        this.storeConfig = new RocksDBConfig();
        this.storeConfig.filePath(path);
    }

    @Override
    public Set<CropPlugin> plugins() {
        return Iterables.setOf(getStore());
    }

    public static RocksDBModuleBuilder withConfig() {
        return new RocksDBModuleBuilder();
    }

    public CropStore<?> getStore() {
        RocksDBStore store = new RocksDBStore();
        store.setStoreConfig(storeConfig);
        return store;
    }
}
