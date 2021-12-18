/*
 * Copyright (c) 2019-2020. Crop author or authors.
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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.common.concurrent.ThreadPoolManager;
import xyz.vopen.framework.cropdb.exceptions.CropSecurityException;
import xyz.vopen.framework.cropdb.migration.Migration;
import xyz.vopen.framework.cropdb.common.module.CropModule;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;

/**
 * A builder utility to create a {@link CropDB} database instance.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @see CropDB
 * @since 1.0
 */
@Slf4j
public class CropDBBuilder {
  @Getter private final CropConfig cropConfig;

  /** Instantiates a new {@link CropDBBuilder}. */
  CropDBBuilder() {
    this.cropConfig = new CropConfig();
  }

  /**
   * Sets the embedded field separator character. Default value is `.`
   *
   * @param separator the separator
   * @return the {@link CropDBBuilder} instance.
   */
  public CropDBBuilder fieldSeparator(String separator) {
    this.cropConfig.fieldSeparator(separator);
    return this;
  }

  /**
   * Loads {@link CropModule} instance.
   *
   * @param module the {@link CropModule} instance.
   * @return the {@link CropDBBuilder} instance.
   */
  public CropDBBuilder loadModule(CropModule module) {
    this.cropConfig.loadModule(module);
    return this;
  }

  /**
   * Adds instructions to perform during schema migration.
   *
   * @param migrations the migrations
   * @return the crop builder
   */
  public CropDBBuilder addMigrations(Migration... migrations) {
    for (Migration migration : migrations) {
      this.cropConfig.addMigration(migration);
    }
    return this;
  }

  /**
   * Sets the current schema version.
   *
   * @param version the version
   * @return the crop builder
   */
  public CropDBBuilder schemaVersion(Integer version) {
    this.cropConfig.schemaVersion(version);
    return this;
  }

  /**
   * Opens or creates a new crop database backed by mvstore. If it is an in-memory store, then it
   * will create a new one. If it is a file based store, and if the file does not exists, then it
   * will create a new file store and open; otherwise it will open the existing file store.
   *
   * <p>NOTE: If the database is corrupted somehow then at the time of opening, it will try to
   * repair it using the last known good version. If still it fails to recover, then it will throw a
   * {@link CropIOException}.
   *
   * @return the crop database instance.
   * @throws CropIOException if unable to create a new in-memory database.
   * @throws CropIOException if the database is corrupt and recovery fails.
   * @throws IllegalArgumentException if the directory does not exist.
   */
  public CropDB openOrCreate() {
    this.cropConfig.autoConfigure();
    return new CropDBDatabase(cropConfig);
  }

  /**
   * Opens or creates a new crop database backed by mvstore. If it is an in-memory store, then it
   * will create a new one. If it is a file based store, and if the file does not exists, then it
   * will create a new file store and open; otherwise it will open the existing file store.
   *
   * <p>While creating a new database, it will use the specified user credentials. While opening an
   * existing database, it will use the specified credentials to open it.
   *
   * <p>NOTE: If the database is corrupted somehow then at the time of opening, it will try to
   * repair it using the last known good version. If still it fails to recover, then it will throw a
   * {@link CropIOException}.
   *
   * @param username the username
   * @param password the password
   * @return the crop database instance.
   * @throws CropSecurityException if the user credentials are wrong or one of them is empty string.
   * @throws CropIOException if unable to create a new in-memory database.
   * @throws CropIOException if the database is corrupt and recovery fails.
   * @throws CropIOException if the directory does not exist.
   */
  public CropDB openOrCreate(String username, String password) {
    this.cropConfig.autoConfigure();
    Runtime.getRuntime().addShutdownHook(new Thread(ThreadPoolManager::shutdownThreadPools));
    return new CropDBDatabase(username, password, cropConfig);
  }
}
