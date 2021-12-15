/*
 * Copyright (c) 2021-2022. CropDB author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.vopen.framework.cropdb.common.module;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.exceptions.PluginException;
import xyz.vopen.framework.cropdb.common.mapper.MappableMapper;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;
import xyz.vopen.framework.cropdb.index.*;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.store.memory.InMemoryStoreModule;

import java.util.HashMap;
import java.util.Map;

/**
 * The crop database plugin manager. It loads the crop plugins
 * before opening the database.
 *
 * @see CropModule
 * @see CropPlugin
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0
 */
@Slf4j
@Getter
public class PluginManager implements AutoCloseable {
    private final Map<String, CropIndexer> indexerMap;
    private final CropConfig cropConfig;
    private CropMapper cropMapper;
    private CropStore<?> cropStore;

    /**
     * Instantiates a new {@link PluginManager}.
     *
     * @param cropConfig the crop config
     */
    public PluginManager(CropConfig cropConfig) {
        this.indexerMap = new HashMap<>();
        this.cropConfig = cropConfig;
    }

    /**
     * Loads a {@link CropModule} instance.
     *
     * @param module the module
     */
    public void loadModule(CropModule module) {
        if (module != null && module.plugins() != null) {
            for (CropPlugin plugin : module.plugins()) {
                loadPlugin(plugin);
            }
        }
    }

    /**
     * Find and loads all crop plugins configured.
     */
    public void findAndLoadPlugins() {
        try {
            loadInternalPlugins();
        } catch (Exception e) {
            log.error("Error while loading internal plugins", e);
            throw new PluginException("error while loading internal plugins", e);
        }
    }

    /**
     * Initializes all plugins instances.
     */
    public void initializePlugins() {
        if (cropStore != null) {
            initializePlugin(cropStore);
        } else {
            log.error("No storage engine found. Please ensure that a storage module has been loaded properly");
            throw new CropIOException("no storage engine found");
        }

        if (cropMapper != null) {
            initializePlugin(cropMapper);
        }

        if (!indexerMap.isEmpty()) {
            for (CropIndexer cropIndexer : indexerMap.values()) {
                initializePlugin(cropIndexer);
            }
        }
    }

    @Override
    public void close() {
        for (CropIndexer cropIndexer : indexerMap.values()) {
            cropIndexer.close();
        }

        if (cropMapper != null) {
            cropMapper.close();
        }

        if (cropStore != null) {
            cropStore.close();
        }
    }

    private void loadPlugin(CropPlugin plugin) {
        populatePlugins(plugin);
    }

    private void initializePlugin(CropPlugin plugin) {
        plugin.initialize(cropConfig);
    }

    private void populatePlugins(CropPlugin plugin) {
        if (plugin != null) {
            if (plugin instanceof CropIndexer) {
                loadIndexer((CropIndexer) plugin);
            } else if (plugin instanceof CropMapper) {
                loadCropMapper((CropMapper) plugin);
            } else if (plugin instanceof CropStore) {
                loadCropStore((CropStore<?>) plugin);
            } else {
                plugin.close();
                throw new PluginException("invalid plugin loaded " + plugin);
            }
        }
    }

    private void loadCropStore(CropStore<?> cropStore) {
        if (this.cropStore != null) {
            cropStore.close();
            throw new PluginException("multiple CropStore found");
        }
        this.cropStore = cropStore;
    }

    private void loadCropMapper(CropMapper cropMapper) {
        if (this.cropMapper != null) {
            cropMapper.close();
            throw new PluginException("multiple CropMapper found");
        }
        this.cropMapper = cropMapper;
    }

    private synchronized void loadIndexer(CropIndexer cropIndexer) {
        if (indexerMap.containsKey(cropIndexer.getIndexType())) {
            cropIndexer.close();
            throw new PluginException("multiple Indexer found for type "
                + cropIndexer.getIndexType());
        }
        this.indexerMap.put(cropIndexer.getIndexType(), cropIndexer);
    }

    protected void loadInternalPlugins() {
        if (!indexerMap.containsKey(IndexType.UNIQUE)) {
            log.debug("Loading default unique indexer");
            CropPlugin plugin = new UniqueIndexer();
            loadPlugin(plugin);
        }

        if (!indexerMap.containsKey(IndexType.NON_UNIQUE)) {
            log.debug("Loading default non-unique indexer");
            CropPlugin plugin = new NonUniqueIndexer();
            loadPlugin(plugin);
        }

        if (!indexerMap.containsKey(IndexType.FULL_TEXT)) {
            log.debug("Loading crop text indexer");
            CropPlugin plugin = new CropTextIndexer();
            loadPlugin(plugin);
        }

        if (cropMapper == null) {
            log.debug("Loading mappable mapper");
            CropPlugin plugin = new MappableMapper();
            loadPlugin(plugin);
        }

        if (cropStore == null) {
            loadModule(new InMemoryStoreModule());
            log.warn("No persistent storage module found, creating an in-memory database");
        }
    }
}
