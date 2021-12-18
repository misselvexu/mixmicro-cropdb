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

package xyz.vopen.framework.cropdb.collection;

import lombok.EqualsAndHashCode;
import xyz.vopen.framework.cropdb.exceptions.InvalidIdException;
import xyz.vopen.framework.cropdb.common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * An unique identifier across the Crop database. Each document in a crop collection is associated
 * with a {@link CropId}.
 *
 * <p>During insertion if an unique object is supplied in the '_id' field of the document, then the
 * value of the '_id' field will be used to create a new {@link CropId}. If that is not supplied,
 * then crop will auto generate one and supply it in the '_id' field of the document.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @see CropCollection#getById(CropId)
 * @since 1.0
 */
@EqualsAndHashCode
public final class CropId implements Comparable<CropId>, Serializable {
  private static final long serialVersionUID = 1477462375L;
  private static final transient SnowflakeIdGenerator generator = new SnowflakeIdGenerator();

  private String idValue;

  private CropId() {
    this.idValue = Long.toString(generator.getId());
  }

  private CropId(String value) {
    this.idValue = value;
  }

  /**
   * Gets a new auto-generated {@link CropId}.
   *
   * @return a new auto-generated {@link CropId}.
   */
  public static CropId newId() {
    return new CropId();
  }

  /**
   * Creates a {@link CropId} from a long value.
   *
   * @param value the value
   * @return the {@link CropId}
   */
  public static CropId createId(String value) {
    validId(value);
    return new CropId(value);
  }

  public static boolean validId(Object value) {
    if (value == null) {
      throw new InvalidIdException("id cannot be null");
    }
    try {
      Long.parseLong(value.toString());
      return true;
    } catch (Exception e) {
      throw new InvalidIdException("id must be a string representation of 64bit decimal number");
    }
  }

  @Override
  public int compareTo(CropId other) {
    if (other.idValue == null) {
      throw new InvalidIdException("cannot compare with null id");
    }

    return Long.compare(Long.parseLong(idValue), Long.parseLong(other.idValue));
  }

  @Override
  public String toString() {
    if (idValue != null) {
      return Constants.ID_PREFIX + idValue + Constants.ID_SUFFIX;
    }
    return "";
  }

  /**
   * Gets the underlying id object.
   *
   * @return the underlying id object.
   */
  public String getIdValue() {
    return idValue;
  }

  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.writeUTF(idValue);
  }

  private void readObject(ObjectInputStream stream) throws IOException {
    idValue = stream.readUTF();
  }
}
