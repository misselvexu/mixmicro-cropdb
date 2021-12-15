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

package xyz.vopen.framework.cropdb.sync;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.collection.meta.Attributes;
import xyz.vopen.framework.cropdb.common.util.StringUtils;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.sync.crdt.LastWriteWinMap;
import xyz.vopen.framework.cropdb.common.Constants;

import java.util.UUID;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
interface ReplicationOperation {
    CropCollection getCollection();

    default Attributes getAttributes() {
        Attributes attributes = getCollection().getAttributes();
        if (attributes == null) {
            attributes = new Attributes();
            saveAttributes(attributes);
        }
        return attributes;
    }

    default void saveAttributes(Attributes attributes) {
        getCollection().setAttributes(attributes);
    }

    default Long getLastSyncTime() {
        Attributes attributes = getAttributes();
        String syncTimeStr = attributes.get(Attributes.LAST_SYNCED);
        if (StringUtils.isNullOrEmpty(syncTimeStr)) {
            return Long.MIN_VALUE;
        } else {
            return Long.parseLong(syncTimeStr);
        }
    }

    default LastWriteWinMap createReplicatedDataType() {
        Attributes attributes = getAttributes();
        String tombstoneName = getTombstoneName(attributes);
        saveAttributes(attributes);

        CropStore<?> store = getCollection().getStore();
        CropMap<CropId, Long> tombstone = store.openMap(tombstoneName, CropId.class, Long.class);
        return new LastWriteWinMap(getCollection(), tombstone);
    }

    default String getTombstoneName(Attributes attributes) {
        String tombstoneName = attributes.get(Attributes.TOMBSTONE);
        if (StringUtils.isNullOrEmpty(tombstoneName)) {
            tombstoneName = getCollection().getName()
                + Constants.INTERNAL_NAME_SEPARATOR + Attributes.TOMBSTONE
                + Constants.INTERNAL_NAME_SEPARATOR + UUID.randomUUID();
            attributes.set(Attributes.TOMBSTONE, tombstoneName);
        }
        return tombstoneName;
    }

    default void saveLastSyncTime(Long lastSyncTime) {
        Attributes attributes = getAttributes();
        attributes.set(Attributes.LAST_SYNCED, Long.toString(lastSyncTime));
        saveAttributes(attributes);
    }
}
