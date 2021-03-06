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
 * Kurdish stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Kurdish implements Language {
  @Override
  public Set<String> stopWords() {
    return new HashSet<>(
        Arrays.asList(
            "ئێمە",
            "ئێوە",
            "ئەم",
            "ئەو",
            "ئەوان",
            "ئەوەی",
            "بۆ",
            "بێ",
            "بێجگە",
            "بە",
            "بەبێ",
            "بەدەم",
            "بەردەم",
            "بەرلە",
            "بەرەوی",
            "بەرەوە",
            "بەلای",
            "بەپێی",
            "تۆ",
            "تێ",
            "جگە",
            "دوای",
            "دوو",
            "دە",
            "دەکات",
            "دەگەڵ",
            "سەر",
            "لێ",
            "لە",
            "لەبابەت",
            "لەباتی",
            "لەبارەی",
            "لەبرێتی",
            "لەبن",
            "لەبەر",
            "لەبەینی",
            "لەدەم",
            "لەرێ",
            "لەرێگا",
            "لەرەوی",
            "لەسەر",
            "لەلایەن",
            "لەناو",
            "لەنێو",
            "لەو",
            "لەپێناوی",
            "لەژێر",
            "لەگەڵ",
            "من",
            "ناو",
            "نێوان",
            "هەر",
            "هەروەها",
            "و",
            "وەک",
            "پاش",
            "پێ",
            "پێش",
            "چەند",
            "کرد",
            "کە",
            "ی"));
  }
}
