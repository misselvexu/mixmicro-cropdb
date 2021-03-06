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

package xyz.vopen.framework.cropdb.index.fulltext.languages;

import xyz.vopen.framework.cropdb.index.fulltext.Language;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Croatian stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Croatian implements Language {
  @Override
  public Set<String> stopWords() {
    return new HashSet<>(
        Arrays.asList(
            "a", "ako", "ali", "bi", "bih", "bila", "bili", "bilo", "bio", "bismo", "biste", "biti",
            "bumo", "da", "do", "duž", "ga", "hoće", "hoćemo", "hoćete", "hoćeš", "hoću", "i",
            "iako", "ih", "ili", "iz", "ja", "je", "jedna", "jedne", "jedno", "jer", "jesam",
            "jesi", "jesmo", "jest", "jeste", "jesu", "jim", "joj", "još", "ju", "kada", "kako",
            "kao", "koja", "koje", "koji", "kojima", "koju", "kroz", "li", "me", "mene", "meni",
            "mi", "mimo", "moj", "moja", "moje", "mu", "na", "nad", "nakon", "nam", "nama", "nas",
            "naš", "naša", "naše", "našeg", "ne", "nego", "neka", "neki", "nekog", "neku", "nema",
            "netko", "neće", "nećemo", "nećete", "nećeš", "neću", "nešto", "ni", "nije", "nikoga",
            "nikoje", "nikoju", "nisam", "nisi", "nismo", "niste", "nisu", "njega", "njegov",
            "njegova", "njegovo", "njemu", "njezin", "njezina", "njezino", "njih", "njihov",
            "njihova", "njihovo", "njim", "njima", "njoj", "nju", "no", "o", "od", "odmah", "on",
            "ona", "oni", "ono", "ova", "pa", "pak", "po", "pod", "pored", "prije", "s", "sa",
            "sam", "samo", "se", "sebe", "sebi", "si", "smo", "ste", "su", "sve", "svi", "svog",
            "svoj", "svoja", "svoje", "svom", "ta", "tada", "taj", "tako", "te", "tebe", "tebi",
            "ti", "to", "toj", "tome", "tu", "tvoj", "tvoja", "tvoje", "u", "uz", "vam", "vama",
            "vas", "vaš", "vaša", "vaše", "već", "vi", "vrlo", "za", "zar", "će", "ćemo", "ćete",
            "ćeš", "ću", "što"));
  }
}
