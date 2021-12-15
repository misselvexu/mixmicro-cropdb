package xyz.vopen.framework.cropdb.transaction;

import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.store.memory.InMemoryMap;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static xyz.vopen.framework.cropdb.common.util.ObjectUtils.deepCopy;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@SuppressWarnings("SortedCollectionWithNonComparableKeys")
class TransactionalMap<K, V> implements CropMap<K, V> {
    private final CropMap<K, V> primary;
    private final CropMap<K, V> backingMap;
    private final String mapName;
    private final CropStore<?> store;
    private final Set<K> tombstones;
    private final AtomicBoolean droppedFlag;
    private final AtomicBoolean closedFlag;

    private boolean cleared = false;

    public TransactionalMap(String mapName, CropMap<K, V> primary, CropStore<?> store) {
        this.mapName = mapName;
        this.primary = primary != null ? primary : new InMemoryMap<>(mapName, store);
        this.store = store;
        this.backingMap = new InMemoryMap<>(mapName, store);
        this.tombstones = new HashSet<>();
        this.closedFlag = new AtomicBoolean(false);
        this.droppedFlag = new AtomicBoolean(false);
    }

    @Override
    public boolean containsKey(K k) {
        if (cleared) return false;

        if (backingMap.containsKey(k)) {
            return true;
        }

        if (tombstones.contains(k)) {
            return false;
        }

        return primary.containsKey(k);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K k) {
        if (tombstones.contains(k) || cleared) {
            return null;
        }

        V result = backingMap.get(k);
        if (result == null) {
            result = primary.get(k);
            if (result instanceof CopyOnWriteArrayList) {
                // create a deep copy of the list so that it does not effect the original one
                List<?> list = deepCopy((CopyOnWriteArrayList<?>) result);
                backingMap.put(k, (V) list);
                result = (V) list;
            }
        }

        return result;
    }

    @Override
    public CropStore<?> getStore() {
        return store;
    }

    @Override
    public void clear() {
        backingMap.clear();
        cleared = true;
    }

    @Override
    public String getName() {
        return mapName;
    }

    @Override
    public RecordStream<V> values() {
        if (cleared) {
            return RecordStream.empty();
        }

        return RecordStream.fromIterable(() -> new Iterator<V>() {
            private final Iterator<Pair<K, V>> entryIterator = entries().iterator();
            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public V next() {
                return entryIterator.next().getSecond();
            }
        });
    }

    @Override
    public V remove(K k) {
        V item = null;
        if (cleared || tombstones.contains(k)) {
            return null;
        }

        if (backingMap.containsKey(k)) {
            item = backingMap.remove(k);
        } else if (primary.containsKey(k)) {
            item = primary.get(k);
        }
        tombstones.add(k);
        return item;
    }

    @Override
    public RecordStream<K> keys() {
        if (cleared) {
            return RecordStream.empty();
        }

        return RecordStream.fromCombined(RecordStream.except(primary.keys(), tombstones),
            backingMap.keys());
    }

    @Override
    public void put(K k, V v) {
        cleared = false;
        tombstones.remove(k);
        backingMap.put(k, v);
    }

    @Override
    public long size() {
        if (cleared) {
            return 0;
        }
        return backingMap.size();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        cleared = false;
        V v = get(key);
        if (v == null) {
            put(key, value);
        }

        return v;
    }

    @Override
    public RecordStream<Pair<K, V>> entries() {
        return getStream(primary.entries(), backingMap.entries());
    }

    @Override
    public RecordStream<Pair<K, V>> reversedEntries() {
        return getStream(primary.reversedEntries(), backingMap.reversedEntries());
    }

    @Override
    public K higherKey(K k) {
        if (cleared) {
            return null;
        }

        K primaryKey = primary.higherKey(k);
        K backingKey = backingMap.higherKey(k);

        if (primaryKey == null) {
            return backingKey;
        }

        if (backingKey == null) {
            return primaryKey;
        }

        NavigableSet<K> keySet = new TreeSet<>();
        keySet.add(backingKey);
        keySet.add(primaryKey);

        return keySet.higher(k);
    }

    @Override
    public K ceilingKey(K k) {
        if (cleared) {
            return null;
        }

        K primaryKey = primary.ceilingKey(k);
        K backingKey = backingMap.ceilingKey(k);

        if (primaryKey == null) {
            return backingKey;
        }

        if (backingKey == null) {
            return primaryKey;
        }

        NavigableSet<K> keySet = new TreeSet<>();
        keySet.add(backingKey);
        keySet.add(primaryKey);

        return keySet.ceiling(k);
    }

    @Override
    public K lowerKey(K k) {
        if (cleared) {
            return null;
        }

        K primaryKey = primary.lowerKey(k);
        K backingKey = backingMap.lowerKey(k);

        if (primaryKey == null) {
            return backingKey;
        }

        if (backingKey == null) {
            return primaryKey;
        }

        NavigableSet<K> keySet = new TreeSet<>();
        keySet.add(backingKey);
        keySet.add(primaryKey);

        return keySet.lower(k);
    }

    @Override
    public K floorKey(K k) {
        if (cleared) {
            return null;
        }

        K primaryKey = primary.floorKey(k);
        K backingKey = backingMap.floorKey(k);

        if (primaryKey == null) {
            return backingKey;
        }

        if (backingKey == null) {
            return primaryKey;
        }

        NavigableSet<K> keySet = new TreeSet<>();
        keySet.add(backingKey);
        keySet.add(primaryKey);

        return keySet.floor(k);
    }

    @Override
    public boolean isEmpty() {
        if (cleared) {
            return true;
        }

        boolean result = primary.isEmpty();
        if (result) {
            return backingMap.isEmpty();
        }
        return false;
    }

    @Override
    public void drop() {
        if (!droppedFlag.get()) {
            droppedFlag.compareAndSet(false, true);
            closedFlag.compareAndSet(false, true);

            cleared = true;
            backingMap.clear();
            tombstones.clear();
        }
    }

    @Override
    public void close() {
        if (!closedFlag.get() && !droppedFlag.get()) {
            closedFlag.compareAndSet(false, true);
            backingMap.clear();
            tombstones.clear();
        }
    }

    private RecordStream<Pair<K, V>> getStream(RecordStream<Pair<K, V>> primaryStream,
                                               RecordStream<Pair<K, V>> backingStream) {
        if (cleared) {
            return RecordStream.empty();
        }

        return () -> new Iterator<Pair<K, V>>() {
            private final Iterator<Pair<K, V>> primaryIterator = primaryStream.iterator();
            private final Iterator<Pair<K, V>> iterator = backingStream.iterator();
            private Pair<K, V> nextPair;
            private boolean nextPairSet = false;

            @Override
            public boolean hasNext() {
                return nextPairSet || setNextId();
            }

            @Override
            public Pair<K, V> next() {
                if (!nextPairSet && !setNextId()) {
                    throw new NoSuchElementException();
                }
                nextPairSet = false;
                return nextPair;
            }

            private boolean setNextId() {
                if (iterator.hasNext()) {
                    nextPair = iterator.next();
                    nextPairSet = true;
                    return true;
                }

                while (primaryIterator.hasNext()) {
                    final Pair<K, V> pair = primaryIterator.next();
                    if (!tombstones.contains(pair.getFirst())) {
                        nextPair = pair;
                        nextPairSet = true;
                        return true;
                    }
                }

                return false;
            }
        };
    }
}
