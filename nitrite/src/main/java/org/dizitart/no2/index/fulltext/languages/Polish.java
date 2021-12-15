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
 * Polish stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Polish implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
            "a",
            "aby",
            "ach",
            "acz",
            "aczkolwiek",
            "aj",
            "albo",
            "ale",
            "ależ",
            "ani",
            "aż",
            "bardziej",
            "bardzo",
            "bez",
            "bo",
            "bowiem",
            "by",
            "byli",
            "bym",
            "bynajmniej",
            "byä‡",
            "być",
            "był",
            "była",
            "było",
            "były",
            "będzie",
            "będą",
            "cali",
            "cała",
            "cały",
            "chce",
            "choć",
            "ci",
            "ciebie",
            "ciä™",
            "cię",
            "co",
            "cokolwiek",
            "coraz",
            "coś",
            "czasami",
            "czasem",
            "czemu",
            "czy",
            "czyli",
            "często",
            "daleko",
            "dla",
            "dlaczego",
            "dlatego",
            "do",
            "dobrze",
            "dokä…d",
            "dokąd",
            "doå›ä‡",
            "dość",
            "dr",
            "duå¼o",
            "dużo",
            "dwa",
            "dwaj",
            "dwie",
            "dwoje",
            "dzisiaj",
            "dziå›",
            "dziś",
            "gdy",
            "gdyby",
            "gdyż",
            "gdzie",
            "gdziekolwiek",
            "gdzieś",
            "go",
            "godz",
            "hab",
            "i",
            "ich",
            "ii",
            "iii",
            "ile",
            "im",
            "inna",
            "inne",
            "inny",
            "innych",
            "inż",
            "iv",
            "ix",
            "iż",
            "ja",
            "jak",
            "jakaś",
            "jakby",
            "jaki",
            "jakichś",
            "jakie",
            "jakiś",
            "jakiż",
            "jakkolwiek",
            "jako",
            "jakoś",
            "je",
            "jeden",
            "jedna",
            "jednak",
            "jednakże",
            "jedno",
            "jednym",
            "jedynie",
            "jego",
            "jej",
            "jemu",
            "jest",
            "jestem",
            "jeszcze",
            "jeå¼eli",
            "jeå›li",
            "jeśli",
            "jeżeli",
            "juå¼",
            "już",
            "jä…",
            "ją",
            "kaå¼dy",
            "każdy",
            "kiedy",
            "kierunku",
            "kilka",
            "kilku",
            "kimś",
            "kto",
            "ktokolwiek",
            "ktoś",
            "która",
            "które",
            "którego",
            "której",
            "który",
            "których",
            "którym",
            "którzy",
            "ku",
            "lat",
            "lecz",
            "lub",
            "ma",
            "majä…",
            "mają",
            "mam",
            "mamy",
            "mało",
            "mgr",
            "mi",
            "miał",
            "mimo",
            "między",
            "mnie",
            "mnä…",
            "mną",
            "mogą",
            "moi",
            "moim",
            "moja",
            "moje",
            "moå¼e",
            "może",
            "możliwe",
            "można",
            "mu",
            "musi",
            "my",
            "mã³j",
            "mój",
            "na",
            "nad",
            "nam",
            "nami",
            "nas",
            "nasi",
            "nasz",
            "nasza",
            "nasze",
            "naszego",
            "naszych",
            "natomiast",
            "natychmiast",
            "nawet",
            "nic",
            "nich",
            "nie",
            "niech",
            "niego",
            "niej",
            "niemu",
            "nigdy",
            "nim",
            "nimi",
            "niä…",
            "niå¼",
            "nią",
            "niż",
            "no",
            "nowe",
            "np",
            "nr",
            "o",
            "o.o.",
            "obok",
            "od",
            "ok",
            "okoå‚o",
            "około",
            "on",
            "ona",
            "one",
            "oni",
            "ono",
            "oraz",
            "oto",
            "owszem",
            "pan",
            "pana",
            "pani",
            "pl",
            "po",
            "pod",
            "podczas",
            "pomimo",
            "ponad",
            "poniewaå¼",
            "ponieważ",
            "powinien",
            "powinna",
            "powinni",
            "powinno",
            "poza",
            "prawie",
            "prof",
            "przecież",
            "przed",
            "przede",
            "przedtem",
            "przez",
            "przy",
            "raz",
            "razie",
            "roku",
            "również",
            "sam",
            "sama",
            "siä™",
            "się",
            "skä…d",
            "skąd",
            "sobie",
            "sobą",
            "sposób",
            "swoje",
            "sä…",
            "są",
            "ta",
            "tak",
            "taka",
            "taki",
            "takich",
            "takie",
            "także",
            "tam",
            "te",
            "tego",
            "tej",
            "tel",
            "temu",
            "ten",
            "teraz",
            "też",
            "to",
            "tobie",
            "tobä…",
            "tobą",
            "toteż",
            "totobą",
            "trzeba",
            "tu",
            "tutaj",
            "twoi",
            "twoim",
            "twoja",
            "twoje",
            "twym",
            "twã³j",
            "twój",
            "ty",
            "tych",
            "tylko",
            "tym",
            "tys",
            "tzw",
            "tę",
            "u",
            "ul",
            "vi",
            "vii",
            "viii",
            "vol",
            "w",
            "wam",
            "wami",
            "was",
            "wasi",
            "wasz",
            "wasza",
            "wasze",
            "we",
            "według",
            "wie",
            "wiele",
            "wielu",
            "wiä™c",
            "więc",
            "więcej",
            "wszyscy",
            "wszystkich",
            "wszystkie",
            "wszystkim",
            "wszystko",
            "wtedy",
            "www",
            "wy",
            "właśnie",
            "wśród",
            "xi",
            "xii",
            "xiii",
            "xiv",
            "xv",
            "z",
            "za",
            "zapewne",
            "zawsze",
            "zaś",
            "ze",
            "zeznowu",
            "znowu",
            "znów",
            "został",
            "zł",
            "å¼aden",
            "å¼e",
            "żaden",
            "żadna",
            "żadne",
            "żadnych",
            "że",
            "żeby"
        ));
    }
}
