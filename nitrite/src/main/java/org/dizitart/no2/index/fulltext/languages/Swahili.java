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
 * Swahili stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Swahili implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
            "akasema",
            "alikuwa",
            "alisema",
            "baada",
            "basi",
            "bila",
            "cha",
            "chini",
            "hadi",
            "hapo",
            "hata",
            "hivyo",
            "hiyo",
            "huku",
            "huo",
            "ili",
            "ilikuwa",
            "juu",
            "kama",
            "karibu",
            "katika",
            "kila",
            "kima",
            "kisha",
            "kubwa",
            "kutoka",
            "kuwa",
            "kwa",
            "kwamba",
            "kwenda",
            "kwenye",
            "la",
            "lakini",
            "mara",
            "mdogo",
            "mimi",
            "mkubwa",
            "mmoja",
            "moja",
            "muda",
            "mwenye",
            "na",
            "naye",
            "ndani",
            "ng",
            "ni",
            "nini",
            "nonkungu",
            "pamoja",
            "pia",
            "sana",
            "sasa",
            "sauti",
            "tafadhali",
            "tena",
            "tu",
            "vile",
            "wa",
            "wakati",
            "wake",
            "walikuwa",
            "wao",
            "watu",
            "wengine",
            "wote",
            "ya",
            "yake",
            "yangu",
            "yao",
            "yeye",
            "yule",
            "za",
            "zaidi",
            "zake"
        ));
    }
}
