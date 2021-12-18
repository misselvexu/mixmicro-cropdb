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

package xyz.vopen.framework.cropdb.collection.operation;

import lombok.ToString;
import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.common.WriteResult;

import java.util.*;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
@ToString
class WriteResultImpl implements WriteResult {
  private List<CropId> cropIds;

  void setCropIds(List<CropId> cropIds) {
    this.cropIds = cropIds;
  }

  void addToList(CropId cropId) {
    if (cropIds == null) {
      cropIds = new ArrayList<>();
    }
    cropIds.add(cropId);
  }

  @Override
  public Iterator<CropId> iterator() {
    return cropIds == null ? Collections.emptyIterator() : cropIds.iterator();
  }
}
