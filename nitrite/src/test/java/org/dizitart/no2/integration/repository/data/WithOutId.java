/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.integration.repository.data;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>.
 */
@Getter
@Setter
public class WithOutId implements Comparable<WithOutId>, Mappable {
    private String name;
    private long number;

    @Override
    public int compareTo(WithOutId o) {
        return Long.compare(number, o.number);
    }

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument()
            .put("name", name)
            .put("number", number);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        name = document.get("name", String.class);
        number = document.get("number", Long.class);
    }
}
