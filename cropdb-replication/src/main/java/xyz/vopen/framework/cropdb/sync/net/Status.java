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

package xyz.vopen.framework.cropdb.sync.net;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a> */
public class Status {
  public static final int CONNECTED = 1;
  public static final int CONNECTING = 0;
  public static final int RECONNECT = 2;
  public static final int DISCONNECTED = -1;

  interface CODE {
    int NORMAL_CLOSE = 1000;
    int ABNORMAL_CLOSE = 1001;
  }
}
