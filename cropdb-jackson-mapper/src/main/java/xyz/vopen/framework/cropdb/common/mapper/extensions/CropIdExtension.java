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

package xyz.vopen.framework.cropdb.common.mapper.extensions;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.mapper.JacksonExtension;
import xyz.vopen.framework.cropdb.common.util.Iterables;

import java.util.List;

/**
 * Class that registers capability of serializing {@code CropId} with the Jackson core.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0.0
 */
public class CropIdExtension implements JacksonExtension {

  @Override
  public List<Class<?>> getSupportedTypes() {
    return Iterables.listOf(CropId.class);
  }

  @Override
  public Module getModule() {
    return new SimpleModule() {
      @Override
      public void setupModule(SetupContext context) {
        addSerializer(CropId.class, new CropIdSerializer());
        addDeserializer(CropId.class, new CropIdDeserializer());
        super.setupModule(context);
      }
    };
  }
}
