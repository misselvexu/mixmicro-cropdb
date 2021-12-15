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

package xyz.vopen.framework.cropdb.collection.meta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents metadata attributes of a collection.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
@EqualsAndHashCode
public class Attributes implements Serializable {
    /**
     * The constant CREATED_TIME.
     */
    public static final String CREATED_TIME = "createdTime";
    /**
     * The constant LAST_MODIFIED_TIME.
     */
    public static final String LAST_MODIFIED_TIME = "lastModifiedTime";
    /**
     * The constant OWNER.
     */
    public static final String OWNER = "owner";
    /**
     * The constant UNIQUE_ID.
     */
    public static final String UNIQUE_ID = "uuid";
    /**
     * The constant LAST_SYNCED.
     */
    public static final String LAST_SYNCED = "lastSynced";
    /**
     * The constant SYNC_LOCK.
     */
    public static final String SYNC_LOCK = "syncLock";
    /**
     * The constant EXPIRY_WAIT.
     */
    public static final String EXPIRY_WAIT = "expiryWait";
    /**
     * The constant TOMBSTONE.
     */
    public static final String TOMBSTONE = "tombstone";
    /**
     * The constant REPLICA.
     */
    public static final String REPLICA = "replica";
    private static final long serialVersionUID = 1481284930L;

    @Getter @Setter
    private Map<String, String> attributes;

    /**
     * Instantiates a new Attributes.
     */
    public Attributes() {
        attributes = new ConcurrentHashMap<>();
        set(CREATED_TIME, Long.toString(System.currentTimeMillis()));
        set(UNIQUE_ID, java.util.UUID.randomUUID().toString());
    }

    /**
     * Instantiates a new Attributes.
     *
     * @param collection the collection
     */
    public Attributes(String collection) {
        attributes = new ConcurrentHashMap<>();
        set(OWNER, collection);
        set(CREATED_TIME, Long.toString(System.currentTimeMillis()));
        set(UNIQUE_ID, java.util.UUID.randomUUID().toString());
    }

    /**
     * Set attributes.
     *
     * @param key   the key
     * @param value the value
     * @return the attributes
     */
    public Attributes set(String key, String value) {
        attributes.put(LAST_MODIFIED_TIME, Long.toString(System.currentTimeMillis()));
        attributes.put(key, value);
        return this;
    }

    /**
     * Get string value of an attribute.
     *
     * @param key the key
     * @return the string
     */
    public String get(String key) {
        return attributes.get(key);
    }

    /**
     * Check whether a key exists in the attributes.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean hasKey(String key) {
        return attributes.containsKey(key);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(attributes);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        attributes = (Map<String, String>) stream.readObject();
    }
}
