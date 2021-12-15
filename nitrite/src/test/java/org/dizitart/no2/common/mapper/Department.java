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

package org.dizitart.no2.common.mapper;

import lombok.Data;
import lombok.ToString;
import org.dizitart.no2.collection.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@Data
@ToString
public class Department implements Mappable {
    private String name;
    private List<MappableEmployee> employeeList;


    @Override
    public Document write(NitriteMapper mapper) {
        List<Document> docList = new ArrayList<>();
        if (employeeList != null && !employeeList.isEmpty()) {
            employeeList.stream().map(employee -> mapper.convert(employee, Document.class))
                .forEach(docList::add);
        }

        return Document.createDocument().put("name", name)
            .put("employeeList", docList);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(NitriteMapper mapper, Document document) {
        employeeList = new ArrayList<>();
        List<Document> documentList = (List<Document>) document.get("employeeList", ArrayList.class);
        if (documentList != null && !documentList.isEmpty()) {
            documentList.stream().map(doc -> mapper.convert(doc, MappableEmployee.class))
                .forEach(employeeList::add);
        }
        name = document.get("name", String.class);
    }
}
