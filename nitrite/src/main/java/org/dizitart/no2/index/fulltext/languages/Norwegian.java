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
 * Norwegian stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Norwegian implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
            "alle",
            "andre",
            "arbeid",
            "at",
            "av",
            "bare",
            "begge",
            "ble",
            "blei",
            "bli",
            "blir",
            "blitt",
            "bort",
            "bra",
            "bruke",
            "både",
            "båe",
            "da",
            "de",
            "deg",
            "dei",
            "deim",
            "deira",
            "deires",
            "dem",
            "den",
            "denne",
            "der",
            "dere",
            "deres",
            "det",
            "dette",
            "di",
            "din",
            "disse",
            "ditt",
            "du",
            "dykk",
            "dykkar",
            "då",
            "eg",
            "ein",
            "eit",
            "eitt",
            "eller",
            "elles",
            "en",
            "ene",
            "eneste",
            "enhver",
            "enn",
            "er",
            "et",
            "ett",
            "etter",
            "folk",
            "for",
            "fordi",
            "forsûke",
            "fra",
            "få",
            "før",
            "fûr",
            "fûrst",
            "gjorde",
            "gjûre",
            "god",
            "gå",
            "ha",
            "hadde",
            "han",
            "hans",
            "har",
            "hennar",
            "henne",
            "hennes",
            "her",
            "hjå",
            "ho",
            "hoe",
            "honom",
            "hoss",
            "hossen",
            "hun",
            "hva",
            "hvem",
            "hver",
            "hvilke",
            "hvilken",
            "hvis",
            "hvor",
            "hvordan",
            "hvorfor",
            "i",
            "ikke",
            "ikkje",
            "ingen",
            "ingi",
            "inkje",
            "inn",
            "innen",
            "inni",
            "ja",
            "jeg",
            "kan",
            "kom",
            "korleis",
            "korso",
            "kun",
            "kunne",
            "kva",
            "kvar",
            "kvarhelst",
            "kven",
            "kvi",
            "kvifor",
            "lage",
            "lang",
            "lik",
            "like",
            "makt",
            "man",
            "mange",
            "me",
            "med",
            "medan",
            "meg",
            "meget",
            "mellom",
            "men",
            "mens",
            "mer",
            "mest",
            "mi",
            "min",
            "mine",
            "mitt",
            "mot",
            "mye",
            "mykje",
            "må",
            "måte",
            "navn",
            "ned",
            "nei",
            "no",
            "noe",
            "noen",
            "noka",
            "noko",
            "nokon",
            "nokor",
            "nokre",
            "ny",
            "nå",
            "når",
            "og",
            "også",
            "om",
            "opp",
            "oss",
            "over",
            "part",
            "punkt",
            "på",
            "rett",
            "riktig",
            "samme",
            "sant",
            "seg",
            "selv",
            "si",
            "sia",
            "sidan",
            "siden",
            "sin",
            "sine",
            "sist",
            "sitt",
            "sjøl",
            "skal",
            "skulle",
            "slik",
            "slutt",
            "so",
            "som",
            "somme",
            "somt",
            "start",
            "stille",
            "så",
            "sånn",
            "tid",
            "til",
            "tilbake",
            "tilstand",
            "um",
            "under",
            "upp",
            "ut",
            "uten",
            "var",
            "vart",
            "varte",
            "ved",
            "verdi",
            "vere",
            "verte",
            "vi",
            "vil",
            "ville",
            "vite",
            "vore",
            "vors",
            "vort",
            "vår",
            "være",
            "vært",
            "vöre",
            "vört",
            "å"
        ));
    }
}
