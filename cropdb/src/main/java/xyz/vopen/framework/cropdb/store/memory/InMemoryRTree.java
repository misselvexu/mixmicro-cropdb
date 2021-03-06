package xyz.vopen.framework.cropdb.store.memory;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.RecordStream;
import xyz.vopen.framework.cropdb.index.BoundingBox;
import xyz.vopen.framework.cropdb.store.CropRTree;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The in-memory {@link CropRTree}.
 *
 * @param <Key> the type parameter
 * @param <Value> the type parameter
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public class InMemoryRTree<Key extends BoundingBox, Value> implements CropRTree<Key, Value> {
  private final Map<SpatialKey, Key> backingMap;

  /** Instantiates a new {@link InMemoryRTree}. */
  public InMemoryRTree() {
    this.backingMap = new ConcurrentHashMap<>();
  }

  @Override
  public void add(Key key, CropId cropId) {
    if (cropId != null && cropId.getIdValue() != null) {
      SpatialKey spatialKey = getKey(key, Long.parseLong(cropId.getIdValue()));
      backingMap.put(spatialKey, key);
    }
  }

  @Override
  public void remove(Key key, CropId cropId) {
    if (cropId != null && cropId.getIdValue() != null) {
      SpatialKey spatialKey = getKey(key, Long.parseLong(cropId.getIdValue()));
      backingMap.remove(spatialKey);
    }
  }

  @Override
  public RecordStream<CropId> findIntersectingKeys(Key key) {
    SpatialKey spatialKey = getKey(key, 0L);
    Set<CropId> set = new HashSet<>();

    for (SpatialKey sk : backingMap.keySet()) {
      if (isOverlap(sk, spatialKey)) {
        set.add(CropId.createId(Long.toString(sk.getId())));
      }
    }

    return RecordStream.fromIterable(set);
  }

  @Override
  public RecordStream<CropId> findContainedKeys(Key key) {
    SpatialKey spatialKey = getKey(key, 0L);
    Set<CropId> set = new HashSet<>();

    for (SpatialKey sk : backingMap.keySet()) {
      if (isInside(sk, spatialKey)) {
        set.add(CropId.createId(Long.toString(sk.getId())));
      }
    }

    return RecordStream.fromIterable(set);
  }

  private boolean isOverlap(SpatialKey a, SpatialKey b) {
    if (a.isNull() || b.isNull()) {
      return false;
    }
    for (int i = 0; i < 2; i++) {
      if (a.max(i) < b.min(i) || a.min(i) > b.max(i)) {
        return false;
      }
    }
    return true;
  }

  private boolean isInside(SpatialKey a, SpatialKey b) {
    if (a.isNull() || b.isNull()) {
      return false;
    }
    for (int i = 0; i < 2; i++) {
      if (a.min(i) <= b.min(i) || a.max(i) >= b.max(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public long size() {
    return backingMap.size();
  }

  private SpatialKey getKey(Key key, long id) {
    return new SpatialKey(id, key.getMinX(), key.getMaxX(), key.getMinY(), key.getMaxY());
  }

  @Override
  public void close() {}

  @Override
  public void clear() {
    backingMap.clear();
  }

  @Override
  public void drop() {
    backingMap.clear();
  }

  /** The type Spatial key. */
  static class SpatialKey {

    private final long id;
    private final float[] minMax;

    /**
     * Instantiates a new Spatial key.
     *
     * @param id the id
     * @param minMax the min max
     */
    public SpatialKey(long id, float... minMax) {
      this.id = id;
      this.minMax = minMax;
    }

    /**
     * Min float.
     *
     * @param dim the dim
     * @return the float
     */
    public float min(int dim) {
      return minMax[dim + dim];
    }

    /**
     * Sets min.
     *
     * @param dim the dim
     * @param x the x
     */
    public void setMin(int dim, float x) {
      minMax[dim + dim] = x;
    }

    /**
     * Max float.
     *
     * @param dim the dim
     * @return the float
     */
    public float max(int dim) {
      return minMax[dim + dim + 1];
    }

    /**
     * Sets max.
     *
     * @param dim the dim
     * @param x the x
     */
    public void setMax(int dim, float x) {
      minMax[dim + dim + 1] = x;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
      return id;
    }

    /**
     * Is null boolean.
     *
     * @return the boolean
     */
    public boolean isNull() {
      return minMax.length == 0;
    }

    @Override
    public int hashCode() {
      return (int) ((id >>> 32) ^ id);
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      } else if (!(other instanceof SpatialKey)) {
        return false;
      }
      SpatialKey o = (SpatialKey) other;
      if (id != o.id) {
        return false;
      }
      return equalsIgnoringId(o);
    }

    /**
     * Equals ignoring id boolean.
     *
     * @param o the o
     * @return the boolean
     */
    public boolean equalsIgnoringId(SpatialKey o) {
      return Arrays.equals(minMax, o.minMax);
    }
  }
}
