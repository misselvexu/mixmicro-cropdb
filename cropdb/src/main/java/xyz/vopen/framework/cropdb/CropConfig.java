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

package xyz.vopen.framework.cropdb;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.common.Constants;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.index.CropIndexer;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;
import xyz.vopen.framework.cropdb.migration.Migration;
import xyz.vopen.framework.cropdb.common.module.CropModule;
import xyz.vopen.framework.cropdb.common.module.CropPlugin;
import xyz.vopen.framework.cropdb.common.module.PluginManager;
import xyz.vopen.framework.cropdb.store.CropStore;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class to configure {@link CropDB} database.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 4.0.0
 */
@Slf4j
@ToString
public class CropConfig implements AutoCloseable {
  /** Indicates if this {@link CropConfig} is already configured. */
  protected boolean configured = false;

  /** Returns the {@link PluginManager} instance. */
  @Getter(AccessLevel.PACKAGE)
  protected final PluginManager pluginManager;

  @Getter private static String fieldSeparator = ".";

  @Getter private final Map<Integer, TreeMap<Integer, Migration>> migrations;

  @Getter private Integer schemaVersion = Constants.INITIAL_SCHEMA_VERSION;

  /** Instantiates a new {@link CropConfig}. */
  public CropConfig() {
    this.pluginManager = new PluginManager(this);
    this.migrations = new HashMap<>();
  }

  /**
   * Sets the embedded field separator character. Default value is `.`
   *
   * @param separator the separator
   */
  public void fieldSeparator(String separator) {
    if (configured) {
      throw new InvalidOperationException(
          "cannot change the separator after database" + " initialization");
    }
    CropConfig.fieldSeparator = separator;
  }

  /**
   * Loads {@link CropPlugin} instances defined in the {@link CropModule}.
   *
   * @param module the {@link CropModule} instances.
   * @return the {@link CropConfig} instance.
   */
  public CropConfig loadModule(CropModule module) {
    if (configured) {
      throw new InvalidOperationException("cannot load module after database" + " initialization");
    }
    pluginManager.loadModule(module);
    return this;
  }

  /**
   * Adds schema migration instructions.
   *
   * @param migration the migration
   * @return the crop config
   */
  @SuppressWarnings("Java8MapApi")
  public CropConfig addMigration(Migration migration) {
    if (configured) {
      throw new InvalidOperationException(
          "cannot add migration steps after database" + " initialization");
    }

    if (migration != null) {
      final int start = migration.getStartVersion();
      final int end = migration.getEndVersion();
      TreeMap<Integer, Migration> targetMap = migrations.get(start);
      if (targetMap == null) {
        targetMap = new TreeMap<>();
        migrations.put(start, targetMap);
      }
      Migration existing = targetMap.get(end);
      if (existing != null) {
        log.warn("Overriding migration " + existing + " with " + migration);
      }
      targetMap.put(end, migration);
    }
    return this;
  }

  /**
   * Sets the current schema version.
   *
   * @param version the version
   * @return the crop config
   */
  public CropConfig schemaVersion(Integer version) {
    if (configured) {
      throw new InvalidOperationException(
          "cannot add schema version info after database" + " initialization");
    }
    this.schemaVersion = version;
    return this;
  }

  /**
   * Auto configures crop database with default configuration values and default built-in plugins.
   */
  public void autoConfigure() {
    if (configured) {
      throw new InvalidOperationException(
          "cannot execute autoconfigure after database" + " initialization");
    }
    pluginManager.findAndLoadPlugins();
  }

  /**
   * Finds an {@link CropIndexer} by indexType.
   *
   * @param indexType the type of {@link CropIndexer} to find.
   * @return the {@link CropIndexer}
   */
  public CropIndexer findIndexer(String indexType) {
    CropIndexer cropIndexer = pluginManager.getIndexerMap().get(indexType);
    if (cropIndexer != null) {
      cropIndexer.initialize(this);
      return cropIndexer;
    } else {
      throw new IndexingException("no indexer found for index type " + indexType);
    }
  }

  /**
   * Gets the {@link CropMapper} instance.
   *
   * @return the {@link CropMapper}
   */
  public CropMapper cropMapper() {
    return pluginManager.getCropMapper();
  }

  /**
   * Gets {@link CropStore} instance.
   *
   * @return the {@link CropStore}
   */
  public CropStore<?> getCropStore() {
    return pluginManager.getCropStore();
  }

  @Override
  public void close() {
    if (pluginManager != null) {
      pluginManager.close();
    }
  }

  /** Initializes this {@link CropConfig} instance. */
  protected void initialize() {
    this.configured = true;
    this.pluginManager.initializePlugins();
  }
}
