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

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

/**
 * A specialized version of {@link ObjectInputStream} for crop.
 *
 * @since 4.0.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
@Slf4j
class CropObjectInputStream extends ObjectInputStream {
  private static final Map<String, Class<?>> migrationMap = new HashMap<>();

  static {
    migrationMap.put(
        "xyz.vopen.framework.cropdb.Security$UserCredential", Compat.UserCredential.class);
    migrationMap.put("xyz.vopen.framework.cropdb.CropId", Compat.CropId.class);
    migrationMap.put("xyz.vopen.framework.cropdb.Index", Compat.Index.class);
    migrationMap.put("xyz.vopen.framework.cropdb.IndexType", Compat.IndexType.class);
    migrationMap.put(
        "xyz.vopen.framework.cropdb.internals.IndexMetaService$IndexMeta", Compat.IndexMeta.class);
    migrationMap.put("xyz.vopen.framework.cropdb.Document", Compat.Document.class);
    migrationMap.put("xyz.vopen.framework.cropdb.meta.Attributes", Compat.Attributes.class);
  }

  /**
   * Instantiates a new Crop object input stream.
   *
   * @param stream the stream
   * @throws IOException the io exception
   */
  public CropObjectInputStream(InputStream stream) throws IOException {
    super(stream);
  }

  @Override
  protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
    ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();

    for (final String oldName : migrationMap.keySet()) {
      if (resultClassDescriptor != null && resultClassDescriptor.getName().equals(oldName)) {
        Class<?> replacement = migrationMap.get(oldName);

        try {
          resultClassDescriptor = ObjectStreamClass.lookup(replacement);
        } catch (Exception e) {
          log.error("Error while replacing class name." + e.getMessage(), e);
        }
      }
    }

    return resultClassDescriptor;
  }
}
