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

package xyz.vopen.framework.cropdb.exceptions;

import xyz.vopen.framework.cropdb.collection.CropId;
import xyz.vopen.framework.cropdb.collection.Document;

/**
 * Exception thrown when a {@link Document}
 * does not have any {@link CropId} associated
 * with it or it has invalid/incompatible {@link CropId}.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
public class InvalidIdException extends CropException {
    /**
     * Instantiates a new Invalid id exception.
     *
     * @param message the message
     */
    public InvalidIdException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Invalid id exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public InvalidIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
