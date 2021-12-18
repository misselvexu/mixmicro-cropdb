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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * @since 4.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class NearFilter extends WithinFilter {
  NearFilter(String field, Coordinate point, Double distance) {
    super(field, createCircle(point, distance));
  }

  NearFilter(String field, Point point, Double distance) {
    super(field, createCircle(point.getCoordinate(), distance));
  }

  private static Geometry createCircle(Coordinate center, double radius) {
    GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
    shapeFactory.setNumPoints(64);
    shapeFactory.setCentre(center);
    shapeFactory.setSize(radius * 2);
    return shapeFactory.createCircle();
  }

  @Override
  public String toString() {
    return "(" + getField() + " nears " + getValue() + ")";
  }
}
