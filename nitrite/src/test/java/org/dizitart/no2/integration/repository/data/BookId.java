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

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Embedded;

import static org.dizitart.no2.collection.Document.createDocument;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@Data
public class BookId implements Mappable {
    @Embedded(order = 0)
    private String isbn;

    @Embedded(order = 1, fieldName = "book_name")
    private String name;

    private String author;

    @Override
    public Document write(NitriteMapper mapper) {
        return createDocument("isbn", isbn)
            .put("book_name", name)
            .put("author", author);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        isbn = document.get("isbn", String.class);
        name = document.get("book_name", String.class);
        author = document.get("author", String.class);
    }
}
