package xyz.vopen.framework.cropdb.store.memory;

import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.common.tuples.Pair;
import xyz.vopen.framework.cropdb.common.util.Comparables;
import xyz.vopen.framework.cropdb.store.CropMap;
import xyz.vopen.framework.cropdb.store.CropStore;
import xyz.vopen.framework.cropdb.common.util.ValidationUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The in-memory {@link CropMap}.
 *
 * @param <Key> the type parameter
 * @param <Value> the type parameter
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class InMemoryMap<Key, Value> implements CropMap<Key, Value> {
  private final NavigableMap<Key, Value> backingMap;
  private final CropStore<?> cropStore;
  private final String mapName;
  private final AtomicBoolean droppedFlag;
  private final AtomicBoolean closedFlag;

  /**
   * Instantiates a new {@link InMemoryMap}.
   *
   * @param mapName the map name
   * @param cropStore the crop store
   */
  public InMemoryMap(String mapName, CropStore<?> cropStore) {
    this.mapName = mapName;
    this.cropStore = cropStore;
    this.backingMap =
        new ConcurrentSkipListMap<>(
            (o1, o2) -> Comparables.compare((Comparable<?>) o1, (Comparable<?>) o2));

    this.closedFlag = new AtomicBoolean(false);
    this.droppedFlag = new AtomicBoolean(false);
  }

  @Override
  public boolean containsKey(Key key) {
    return backingMap.containsKey(key);
  }

  @Override
  public Value get(Key key) {
    return backingMap.get(key);
  }

  @Override
  public CropStore<?> getStore() {
    return cropStore;
  }

  @Override
  public void clear() {
    backingMap.clear();
    updateLastModifiedTime();
  }

  @Override
  public String getName() {
    return mapName;
  }

  @Override
  public RecordStream<Value> values() {
    return RecordStream.fromIterable(backingMap.values());
  }

  @Override
  public Value remove(Key key) {
    Value value = backingMap.remove(key);
    updateLastModifiedTime();
    return value;
  }

  @Override
  public RecordStream<Key> keys() {
    return RecordStream.fromIterable(backingMap.keySet());
  }

  @Override
  public void put(Key key, Value value) {
    ValidationUtils.notNull(value, "value cannot be null");
    backingMap.put(key, value);
    updateLastModifiedTime();
  }

  @Override
  public long size() {
    return backingMap.size();
  }

  @Override
  public Value putIfAbsent(Key key, Value value) {
    ValidationUtils.notNull(value, "value cannot be null");

    Value v = get(key);
    if (v == null) {
      put(key, value);
    }
    updateLastModifiedTime();
    return v;
  }

  @Override
  public RecordStream<Pair<Key, Value>> entries() {
    return getStream(backingMap);
  }

  @Override
  public RecordStream<Pair<Key, Value>> reversedEntries() {
    return getStream(backingMap.descendingMap());
  }

  @Override
  public Key higherKey(Key key) {
    if (key == null) {
      return null;
    }
    return backingMap.higherKey(key);
  }

  @Override
  public Key ceilingKey(Key key) {
    if (key == null) {
      return null;
    }
    return backingMap.ceilingKey(key);
  }

  @Override
  public Key lowerKey(Key key) {
    if (key == null) {
      return null;
    }
    return backingMap.lowerKey(key);
  }

  @Override
  public Key floorKey(Key key) {
    if (key == null) {
      return null;
    }
    return backingMap.floorKey(key);
  }

  @Override
  public boolean isEmpty() {
    return backingMap.isEmpty();
  }

  @Override
  public void drop() {
    if (!droppedFlag.get()) {
      droppedFlag.compareAndSet(false, true);
      clear();
      getStore().removeMap(mapName);
    }
  }

  @Override
  public void close() {
    if (!closedFlag.get() && !droppedFlag.get()) {
      closedFlag.compareAndSet(false, true);
    }
  }

  private RecordStream<Pair<Key, Value>> getStream(NavigableMap<Key, Value> primaryMap) {
    return RecordStream.fromIterable(
        () ->
            new Iterator<Pair<Key, Value>>() {
              private final Iterator<Map.Entry<Key, Value>> entryIterator =
                  primaryMap.entrySet().iterator();

              @Override
              public boolean hasNext() {
                return entryIterator.hasNext();
              }

              @Override
              public Pair<Key, Value> next() {
                Map.Entry<Key, Value> entry = entryIterator.next();
                return new Pair<>(entry.getKey(), entry.getValue());
              }
            });
  }
}
