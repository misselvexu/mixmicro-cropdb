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
 * Yoruba stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Yoruba implements Language {
  @Override
  public Set<String> stopWords() {
    return new HashSet<>(
        Arrays.asList(
            "a",
            "an",
            "bá",
            "bí",
            "bẹ̀rẹ̀",
            "fún",
            "fẹ́",
            "gbogbo",
            "inú",
            "jù",
            "jẹ",
            "jẹ́",
            "kan",
            "kì",
            "kí",
            "kò",
            "láti",
            "lè",
            "lọ",
            "mi",
            "mo",
            "máa",
            "mọ̀",
            "ni",
            "náà",
            "ní",
            "nígbà",
            "nítorí",
            "nǹkan",
            "o",
            "padà",
            "pé",
            "púpọ̀",
            "pẹ̀lú",
            "rẹ̀",
            "sì",
            "sí",
            "sínú",
            "ṣ",
            "ti",
            "tí",
            "wà",
            "wá",
            "wọn",
            "wọ́n",
            "yìí",
            "àti",
            "àwọn",
            "é",
            "í",
            "òun",
            "ó",
            "ń",
            "ńlá",
            "ṣe",
            "ṣé",
            "ṣùgbọ́n",
            "ẹmọ́",
            "ọjọ́",
            "ọ̀pọ̀lọpọ̀"));
  }
}
