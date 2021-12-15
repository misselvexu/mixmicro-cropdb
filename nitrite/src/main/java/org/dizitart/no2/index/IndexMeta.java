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

package org.dizitart.no2.index;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents index metadata.
 *
 * @since 1.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@Data
@NoArgsConstructor
public class IndexMeta implements Serializable {
    private static final long serialVersionUID = 1576690663L;

    private IndexDescriptor indexDescriptor;
    private String indexMap;
    private AtomicBoolean isDirty;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(indexDescriptor);
        stream.writeUTF(indexMap);
        stream.writeObject(isDirty);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        indexDescriptor = (IndexDescriptor) stream.readObject();
        indexMap = stream.readUTF();
        isDirty = (AtomicBoolean) stream.readObject();
    }
}
