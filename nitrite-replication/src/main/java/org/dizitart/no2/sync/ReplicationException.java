/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.sync;

import org.dizitart.no2.exceptions.NitriteException;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
public class ReplicationException extends NitriteException {
    private final boolean fatal;

    public ReplicationException(String errorMessage, boolean fatal) {
        super(errorMessage);
        this.fatal = fatal;
    }

    public ReplicationException(String errorMessage, Throwable cause, boolean fatal) {
        super(errorMessage, cause);
        this.fatal = fatal;
    }

    public boolean isFatal() {
        return fatal;
    }
}
