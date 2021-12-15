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

package xyz.vopen.framework.cropdb.store;

import xyz.vopen.framework.cropdb.collection.meta.Attributes;
import xyz.vopen.framework.cropdb.collection.meta.MetadataAware;
import xyz.vopen.framework.cropdb.common.Constants;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.util.StringUtils;

/**
 * Represents a Crop key-value pair map. Every piece of
 * data in a Crop database is stored in {@link CropMap}.
 *
 * @param <Key>   the type of key
 * @param <Value> the type of value
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 * @since 1.0
 */
public interface CropMap<Key, Value> extends MetadataAware, AutoCloseable {
    /**
     * Determines if the map contains a mapping for the
     * specified key.
     *
     * @param key key whose presence in this map is to be tested
     * @return `true` if this map contains a mapping for the specified key.
     */
    boolean containsKey(Key key);

    /**
     * Gets the value mapped with the specified key or <code>null</code> otherwise.
     *
     * @param key the key
     * @return the value, or null if the key not found.
     */
    Value get(Key key);

    /**
     * Removes all entries in the map.
     */
    void clear();

    /**
     * Closes this {@link CropMap}.
     * */
    void close();

    /**
     * Gets a {@link RecordStream} view of the values contained in
     * this map.
     *
     * @return the collection view of all values in this map.
     */
    Iterable<Value> values();

    /**
     * Gets a {@link RecordStream} view of the keys contained in this map.
     *
     * @return a set view of the keys contained in this map.
     */
    Iterable<Key> keys();

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key the key whose mapping is to be removed from this map.
     * @return the value that has been removed.
     */
    Value remove(Key key);

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key   key with which the specified value is to be associated (may not be null).
     * @param value value to be associated with the specified key (may not be null).
     */
    void put(Key key, Value value);

    /**
     * Get the number of entries, as a integer. Integer.MAX_VALUE is returned if
     * there are more than this entries.
     *
     * @return the number of entries, as an integer.
     */
    long size();

    /**
     * Add a key-value pair if it does not yet exist.
     *
     * @param key   the key (may not be null)
     * @param value the new value
     * @return the old value if the key existed, or `null` otherwise.
     */
    Value putIfAbsent(Key key, Value value);

    /**
     * Get the smallest key that is larger than the given key, or null if no
     * such key exists.
     *
     * @param key the key
     * @return the result.
     */
    Key higherKey(Key key);

    /**
     * Get the smallest key that is larger or equal to this key.
     *
     * @param key the key
     * @return the result.
     */
    Key ceilingKey(Key key);

    /**
     * Get the largest key that is smaller than the given key, or null if no
     * such key exists.
     *
     * @param key the key
     * @return the result.
     */
    Key lowerKey(Key key);

    /**
     * Get the largest key that is smaller or equal to this key.
     *
     * @param key the key
     * @return the result.
     */
    Key floorKey(Key key);

    /**
     * Indicates whether the map is empty.
     *
     * @return `true` if the map is empty; `false` otherwise.
     */
    boolean isEmpty();

    /**
     * Gets the parent {@link CropStore} where this map is stored.
     *
     * @return the store where this map is stored.
     */
    CropStore<?> getStore();

    /**
     * Gets name of this map.
     *
     * @return the name of this map.
     */
    String getName();

    /**
     * Gets a {@link RecordStream} view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     */
    RecordStream<Pair<Key, Value>> entries();

    /**
     * Gets a reversed {@link RecordStream} view of the mappings contained in this map.
     *
     * @return the record stream
     */
    RecordStream<Pair<Key, Value>> reversedEntries();

    /**
     * Deletes the map from the store.
     */
    void drop();

    /**
     * Gets the attributes of this map.
     * */
    default Attributes getAttributes() {
        CropMap<String, Attributes> metaMap = getStore().openMap(Constants.META_MAP_NAME, String.class, Attributes.class);
        if (metaMap != null && !getName().contentEquals(Constants.META_MAP_NAME)) {
            return metaMap.get(getName());
        }
        return null;
    }

    /**
     * Sets the attributes for this map.
     * */
    default void setAttributes(Attributes attributes) {
        CropMap<String, Attributes> metaMap = getStore().openMap(Constants.META_MAP_NAME, String.class, Attributes.class);
        if (metaMap != null && !getName().contentEquals(Constants.META_MAP_NAME)) {
            metaMap.put(getName(), attributes);
        }
    }

    /**
     * Update last modified time of the map.
     */
    default void updateLastModifiedTime() {
        if (StringUtils.isNullOrEmpty(getName())
            || Constants.META_MAP_NAME.equals(getName())) return;

        CropMap<String, Attributes> metaMap = getStore().openMap(Constants.META_MAP_NAME, String.class, Attributes.class);
        if (metaMap != null) {
            Attributes attributes = metaMap.get(getName());
            if (attributes == null) {
                attributes = new Attributes(getName());
                metaMap.put(getName(), attributes);
            }
            attributes.set(Attributes.LAST_MODIFIED_TIME, Long.toString(System.currentTimeMillis()));
        }
    }
}
