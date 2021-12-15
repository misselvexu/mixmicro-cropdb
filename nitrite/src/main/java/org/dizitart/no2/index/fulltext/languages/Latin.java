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

package org.dizitart.no2.index.fulltext.languages;

import org.dizitart.no2.index.fulltext.Language;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Latin stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Latin implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
            "a",
            "ab",
            "ac",
            "ad",
            "at",
            "atque",
            "aut",
            "autem",
            "cum",
            "de",
            "dum",
            "e",
            "erant",
            "erat",
            "est",
            "et",
            "etiam",
            "ex",
            "haec",
            "hic",
            "hoc",
            "in",
            "ita",
            "me",
            "nec",
            "neque",
            "non",
            "per",
            "qua",
            "quae",
            "quam",
            "qui",
            "quibus",
            "quidem",
            "quo",
            "quod",
            "re",
            "rebus",
            "rem",
            "res",
            "sed",
            "si",
            "sic",
            "sunt",
            "tamen",
            "tandem",
            "te",
            "ut",
            "vel"
        ));
    }
}
