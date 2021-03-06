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

package xyz.vopen.framework.cropdb.spatial;

import lombok.Data;
import xyz.vopen.framework.cropdb.index.BoundingBox;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @since 4.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@Data
class CropBoundingBox implements BoundingBox {
  private float minX;
  private float maxX;
  private float minY;
  private float maxY;

  public CropBoundingBox(Geometry geometry) {
    Envelope env = geometry.getEnvelopeInternal();
    this.minX = (float) env.getMinX();
    this.maxX = (float) env.getMaxX();
    this.minY = (float) env.getMinY();
    this.maxY = (float) env.getMaxY();
  }

  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.writeFloat(minX);
    stream.writeFloat(maxX);
    stream.writeFloat(minY);
    stream.writeFloat(maxY);
  }

  private void readObject(ObjectInputStream stream) throws IOException {
    this.minX = stream.readFloat();
    this.maxX = stream.readFloat();
    this.minY = stream.readFloat();
    this.maxY = stream.readFloat();
  }
}
