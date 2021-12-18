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

import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.common.Constants;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.store.StoreMetaData;
import xyz.vopen.framework.cropdb.transaction.Session;

import java.util.Map;
import java.util.Set;

/**
 * An in-memory, single-file based embedded nosql persistent document store. The store can contains
 * multiple named document collections.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
public interface CropDB extends AutoCloseable {

  /**
   * Returns an instance of a {@link CropDBBuilder}.
   *
   * @return the crop builder
   */
  static CropDBBuilder builder() {
    return new CropDBBuilder();
  }

  /**
   * Commits the unsaved changes. For file based store, it saves the changes to disk if there are
   * any unsaved changes.
   *
   * <p>No need to call it after every change, if auto-commit is not disabled while opening the db.
   * However, it may still be called to flush all changes to disk.
   */
  void commit();

  /**
   * Opens a named collection from the store. If the collections does not exist it will be created
   * automatically and returned. If a collection is already opened, it is returned as is. Returned
   * collection is thread-safe for concurrent use.
   *
   * <p>The name cannot contain below reserved strings:
   *
   * <ul>
   *   <li>{@link Constants#INTERNAL_NAME_SEPARATOR}
   *   <li>{@link Constants#USER_MAP}
   *   <li>{@link Constants#INDEX_META_PREFIX}
   *   <li>{@link Constants#INDEX_PREFIX}
   *   <li>{@link Constants#OBJECT_STORE_NAME_SEPARATOR}
   * </ul>
   *
   * @param name the name of the collection
   * @return the collection
   * @see CropCollection
   */
  CropCollection getCollection(String name);

  /**
   * Opens a type-safe object repository from the store. If the repository does not exist it will be
   * created automatically and returned. If a repository is already opened, it is returned as is.
   *
   * <p>The returned repository is thread-safe for concurrent use.
   *
   * @param <T> the type parameter
   * @param type the type of the object
   * @return the repository containing objects of type {@link T}.
   * @see ObjectRepository
   */
  <T> ObjectRepository<T> getRepository(Class<T> type);

  /**
   * Opens a type-safe object repository with a key identifier from the store. If the repository
   * does not exist it will be created automatically and returned. If a repository is already
   * opened, it is returned as is.
   *
   * <p>The returned repository is thread-safe for concurrent use.
   *
   * @param <T> the type parameter.
   * @param type the type of the object.
   * @param key the key, which will be appended to the repositories name.
   * @return the repository containing objects of type {@link T}.
   * @see ObjectRepository
   */
  <T> ObjectRepository<T> getRepository(Class<T> type, String key);

  /**
   * Destroys a {@link CropCollection} without opening it first.
   *
   * @param name the name of the collection
   */
  void destroyCollection(String name);

  /**
   * Destroys an {@link ObjectRepository} without opening it first.
   *
   * @param <T> the type parameter
   * @param type the type
   */
  <T> void destroyRepository(Class<T> type);

  /**
   * Destroys an keyed-{@link ObjectRepository} without opening it first.
   *
   * @param <T> the type parameter
   * @param type the type
   * @param key the key
   */
  <T> void destroyRepository(Class<T> type, String key);

  /**
   * Gets the set of all {@link CropCollection}s' names saved in the store.
   *
   * @return the set of all collections' names.
   */
  Set<String> listCollectionNames();

  /**
   * Gets the set of all fully qualified class names corresponding to all {@link ObjectRepository}s
   * in the store.
   *
   * @return the set of all registered classes' names.
   */
  Set<String> listRepositories();

  /**
   * Gets the map of all key to the fully qualified class names corresponding to all keyed-{@link
   * ObjectRepository}s in the store.
   *
   * @return the set of all registered classes' names.
   */
  Map<String, Set<String>> listKeyedRepository();

  /**
   * Checks whether the store has any unsaved changes.
   *
   * @return <code>true</code> if there are unsaved changes; otherwise <code>false</code>.
   */
  boolean hasUnsavedChanges();

  /**
   * Checks whether the store is closed.
   *
   * @return <code>true</code> if closed; otherwise <code>false</code>.
   */
  boolean isClosed();

  /**
   * Gets the {@link CropConfig} instance to configure the database.
   *
   * @return the {@link CropConfig} instance to configure the database.
   */
  CropConfig getConfig();

  /**
   * Gets the {@link CropStore} instance powering the database.
   *
   * @return the {@link CropStore} instance of the database.
   */
  CropStore<?> getStore();

  /**
   * Gets database meta data.
   *
   * @return the database meta data
   */
  StoreMetaData getDatabaseMetaData();

  /**
   * Creates a {@link Session} for transaction.
   *
   * @return the session
   */
  Session createSession();

  /** Closes the database. */
  void close();

  /**
   * Checks whether a particular {@link CropCollection} exists in the store.
   *
   * @param name the name of the collection.
   * @return <code>true</code> if the collection exists; otherwise <code>false</code>.
   */
  default boolean hasCollection(String name) {
    checkOpened();
    return listCollectionNames().contains(name);
  }

  /**
   * Checks whether a particular {@link ObjectRepository} exists in the store.
   *
   * @param <T> the type parameter
   * @param type the type of the object
   * @return <code>true</code> if the repository exists; otherwise <code>false</code>.
   */
  default <T> boolean hasRepository(Class<T> type) {
    checkOpened();
    return listRepositories().contains(type.getName());
  }

  /**
   * Checks whether a particular keyed-{@link ObjectRepository} exists in the store.
   *
   * @param <T> the type parameter.
   * @param type the type of the object.
   * @param key the key, which will be appended to the repositories name.
   * @return <code>true</code> if the repository exists; otherwise <code>false</code>.
   */
  default <T> boolean hasRepository(Class<T> type, String key) {
    checkOpened();
    return listKeyedRepository().containsKey(key)
        && listKeyedRepository().get(key).contains(type.getName());
  }

  /**
   * Validate the collection name.
   *
   * @param name the name
   */
  default void validateCollectionName(String name) {
    ValidationUtils.notNull(name, "name cannot be null");
    ValidationUtils.notEmpty(name, "name cannot be empty");

    for (String reservedName : Constants.RESERVED_NAMES) {
      if (name.contains(reservedName)) {
        throw new ValidationException("name cannot contain " + reservedName);
      }
    }
  }

  /** Checks if the store is opened. */
  default void checkOpened() {
    if (getStore() == null || getStore().isClosed()) {
      throw new CropIOException("store is closed");
    }
  }
}
