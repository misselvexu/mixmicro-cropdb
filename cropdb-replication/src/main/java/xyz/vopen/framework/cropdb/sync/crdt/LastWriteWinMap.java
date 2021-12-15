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

package xyz.vopen.framework.cropdb.sync.crdt;

import lombok.Data;
import xyz.vopen.framework.cropdb.collection.*;
import xyz.vopen.framework.cropdb.common.Constants;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.filters.FluentFilter;
import xyz.vopen.framework.cropdb.store.CropMap;

import java.util.Map;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
@Data
public class LastWriteWinMap {
    private CropCollection collection;
    private CropMap<CropId, Long> tombstones;

    public LastWriteWinMap(CropCollection collection, CropMap<CropId, Long> tombstones) {
        this.collection = collection;
        this.tombstones = tombstones;
    }

    public void merge(LastWriteWinState snapshot) {
        if (snapshot.getChanges() != null) {
            for (Document entry : snapshot.getChanges()) {
                put(entry);
            }
        }

        if (snapshot.getTombstones() != null) {
            for (Map.Entry<String, Long> entry : snapshot.getTombstones().entrySet()) {
                remove(CropId.createId(entry.getKey()), entry.getValue());
            }
        }
    }

    public LastWriteWinState getChangesSince(Long since, int offset, int size) {
        LastWriteWinState state = new LastWriteWinState();

        DocumentCursor cursor = collection.find(FluentFilter.where(Constants.DOC_MODIFIED).gte(since), FindOptions.skipBy(offset).limit(size));
        state.getChanges().addAll(cursor.toSet());

        if (offset == 0) {
            // don't repeat for other offsets
            for (Pair<CropId, Long> entry : tombstones.entries()) {
                Long timestamp = entry.getSecond();
                if (timestamp >= since) {
                    state.getTombstones().put(entry.getFirst().getIdValue(), entry.getSecond());
                }
            }
        }

        return state;
    }

    private void put(Document value) {
        if (value != null) {
            CropId key = value.getId();

            Document entry = collection.getById(key);
            if (entry == null) {
                if (tombstones.containsKey(key)) {
                    Long tombstoneTime = tombstones.get(key);
                    Long docModifiedTime = value.getLastModifiedSinceEpoch();

                    if (docModifiedTime >= tombstoneTime) {
                        value.put(Constants.DOC_SOURCE, Constants.REPLICATOR);
                        collection.insert(value);
                        tombstones.remove(key);
                    }
                } else {
                    value.put(Constants.DOC_SOURCE, Constants.REPLICATOR);
                    collection.insert(value);
                }
            } else {
                Long oldTime = entry.getLastModifiedSinceEpoch();
                Long newTime = value.getLastModifiedSinceEpoch();

                if (newTime > oldTime) {
                    entry.put(Constants.DOC_SOURCE, Constants.REPLICATOR);
                    collection.remove(entry);

                    value.put(Constants.DOC_SOURCE, Constants.REPLICATOR);
                    collection.insert(value);
                }
            }
        }
    }

    private void remove(CropId key, long timestamp) {
        Document entry = collection.getById(key);
        if (entry != null) {
            entry.put(Constants.DOC_SOURCE, Constants.REPLICATOR);
            collection.remove(entry);
            tombstones.put(key, timestamp);
        }
    }
}
