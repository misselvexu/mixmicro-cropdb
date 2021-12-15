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

package xyz.vopen.framework.cropdb.mvstore.compat.v3;

import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.meta.Attributes;
import xyz.vopen.framework.cropdb.common.DBNull;
import xyz.vopen.framework.cropdb.common.Fields;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;
import xyz.vopen.framework.cropdb.exceptions.ValidationException;
import xyz.vopen.framework.cropdb.index.DBValue;
import xyz.vopen.framework.cropdb.index.IndexDescriptor;
import xyz.vopen.framework.cropdb.index.IndexMeta;
import xyz.vopen.framework.cropdb.store.UserCredential;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import xyz.vopen.framework.cropdb.common.Constants;
import xyz.vopen.framework.cropdb.common.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An utility class to migrate the.
 *
 * @since 4.0.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class MigrationUtil {

    /**
     * Migrate an old 3.x compatible store to new 4.x compatible store.
     *
     * @param newStore the new store
     * @param oldStore the old store
     */
    @SuppressWarnings({"rawtypes"})
    public static void migrate(MVStore newStore, MVStore oldStore) {
        try {
            validateOldStore(oldStore);

            Set<String> mapNames = oldStore.getMapNames();
            for (String mapName : mapNames) {
                MVMap oldMap = oldStore.openMap(mapName, new MVMapBuilder<>());
                MVMap newMap = newStore.openMap(mapName);
                copyData(oldMap, newMap);
            }

            oldStore.commit();
            newStore.commit();
        } catch (Throwable t) {
            throw new CropIOException("migration of old data has failed", t);
        } finally {
            oldStore.close();
            newStore.close();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyData(MVMap oldMap, MVMap newMap) {
        if (oldMap != null) {
            Set<Map.Entry> entrySet = oldMap.entrySet();
            for (Map.Entry entry : entrySet) {
                Object key = entry.getKey();
                Object newKey = entry.getKey();

                if (key instanceof Compat.CropId) {
                    newKey = cropId((Compat.CropId) key);
                } else if (oldMap.getName().contains(Constants.INDEX_PREFIX)) {
                    // index map, wrap with DBValue
                    newKey = newKey == null ? DBNull.getInstance() : new DBValue((Comparable<?>) newKey);
                }

                Object newValue = migrateValue(entry.getValue());
                newMap.put(newKey, newValue);
            }
        }
    }

    private static Object migrateValue(Object value) {
        if (value != null) {
            if (value instanceof Compat.UserCredential) {
                // old user credentials
                return credential((Compat.UserCredential) value);
            } else if (value instanceof Compat.CropId) {
                // old crop id
                return cropId((Compat.CropId) value);
            } else if (value instanceof Compat.Index) {
                // old index entry
                return indexEntry((Compat.Index) value);
            } else if (value instanceof Compat.IndexMeta) {
                // old index meta data
                return indexMeta((Compat.IndexMeta) value);
            } else if (value instanceof Compat.Document) {
                // old document
                return document((Compat.Document) value);
            } else if (value instanceof Compat.Attributes) {
                // old attribute
                return attributes((Compat.Attributes) value);
            } else if (value instanceof ConcurrentSkipListSet) {
                // old index crop id list
                return arrayList((ConcurrentSkipListSet<?>) value);
            } else if (value instanceof Iterable) {
                return iterable((Iterable<?>) value);
            } else if (value.getClass().isArray()) {
                return array(ObjectUtils.convertToObjectArray(value));
            }
            return value;
        }
        return null;
    }

    private static Object[] array(Object[] array) {
        Object[] newArray = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = migrateValue(array[i]);
        }
        return newArray;
    }

    private static Iterable<?> iterable(Iterable<?> value) {
        Collection<Object> collection = null;
        if (value instanceof List) {
            collection = new ArrayList<>();
        } else if (value instanceof Set) {
            collection = new HashSet<>();
        }

        if (collection != null) {
            for (Object object : value) {
                Object newValue = migrateValue(object);
                collection.add(newValue);
            }
        }

        return collection;
    }

    private static CopyOnWriteArrayList<?> arrayList(ConcurrentSkipListSet<?> value) {
        CopyOnWriteArrayList<Object> newList = new CopyOnWriteArrayList<>();
        for (Object object : value) {
            Object newValue = migrateValue(object);
            newList.add(newValue);
        }
        return newList;
    }

    private static Attributes attributes(Compat.Attributes value) {
        Attributes attributes = new Attributes();
        attributes.set(Attributes.CREATED_TIME, Long.toString(value.getCreatedTime()));
        attributes.set(Attributes.LAST_MODIFIED_TIME, Long.toString(value.getLastModifiedTime()));
        attributes.set(Attributes.LAST_SYNCED, Long.toString(value.getLastSynced()));
        attributes.set(Attributes.SYNC_LOCK, Long.toString(value.getSyncLock()));
        attributes.set(Attributes.EXPIRY_WAIT, Long.toString(value.getExpiryWait()));
        if (value.getCollection() != null) {
            attributes.set(Attributes.OWNER, value.getCollection());
        }

        if (value.getUuid() != null) {
            attributes.set(Attributes.UNIQUE_ID, value.getUuid());
        }
        return attributes;
    }

    private static Document document(Compat.Document value) {
        Document document = Document.createDocument();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            Object val = entry.getValue();
            Object migratedVal = migrateValue(val);
            document.put(entry.getKey(), migratedVal);
        }
        return document;
    }

    private static IndexMeta indexMeta(Compat.IndexMeta value) {
        Compat.Index index = value.getIndex();
        IndexDescriptor indexDescriptor = indexEntry(index);

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.setIndexDescriptor(indexDescriptor);
        indexMeta.setIndexMap(value.getIndexMap());
        indexMeta.setIsDirty(value.getIsDirty());

        return indexMeta;
    }

    private static IndexDescriptor indexEntry(Compat.Index value) {
        String indexType = value.getIndexType().name();
        return new IndexDescriptor(indexType, Fields.withNames(value.getField()), value.getCollectionName());
    }

    private static CropId cropId(Compat.CropId value) {
        return CropId.createId(Long.toString(value.getIdValue()));
    }

    private static UserCredential credential(Compat.UserCredential value) {
        UserCredential userCredential = new UserCredential();
        userCredential.setPasswordHash(value.getPasswordHash());
        userCredential.setPasswordSalt(value.getPasswordSalt());
        return userCredential;
    }

    private static void validateOldStore(MVStore store) {
        if (store.hasMap(Constants.STORE_INFO)) {
            throw new ValidationException("database file is corrupted");
        }
    }
}
