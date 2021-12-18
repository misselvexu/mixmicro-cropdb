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
 * Hindi stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Hindi implements Language {
  @Override
  public Set<String> stopWords() {
    return new HashSet<>(
        Arrays.asList(
            "अंदर", "अत", "अदि", "अप", "अपना", "अपनि", "अपनी", "अपने", "अभि", "अभी", "आदि", "आप",
            "इंहिं", "इंहें", "इंहों", "इतयादि", "इत्यादि", "इन", "इनका", "इन्हीं", "इन्हें",
            "इन्हों", "इस", "इसका", "इसकि", "इसकी", "इसके", "इसमें", "इसि", "इसी", "इसे", "उंहिं",
            "उंहें", "उंहों", "उन", "उनका", "उनकि", "उनकी", "उनके", "उनको", "उन्हीं", "उन्हें",
            "उन्हों", "उस", "उसके", "उसि", "उसी", "उसे", "एक", "एवं", "एस", "एसे", "ऐसे", "ओर",
            "और", "कइ", "कई", "कर", "करता", "करते", "करना", "करने", "करें", "कहते", "कहा", "का",
            "काफि", "काफ़ी", "कि", "किंहें", "किंहों", "कितना", "किन्हें", "किन्हों", "किया", "किर",
            "किस", "किसि", "किसी", "किसे", "की", "कुछ", "कुल", "के", "को", "कोइ", "कोई", "कोन",
            "कोनसा", "कौन", "कौनसा", "गया", "घर", "जब", "जहाँ", "जहां", "जा", "जिंहें", "जिंहों",
            "जितना", "जिधर", "जिन", "जिन्हें", "जिन्हों", "जिस", "जिसे", "जीधर", "जेसा", "जेसे",
            "जैसा", "जैसे", "जो", "तक", "तब", "तरह", "तिंहें", "तिंहों", "तिन", "तिन्हें",
            "तिन्हों", "तिस", "तिसे", "तो", "था", "थि", "थी", "थे", "दबारा", "दवारा", "दिया",
            "दुसरा", "दुसरे", "दूसरे", "दो", "द्वारा", "न", "नहिं", "नहीं", "ना", "निचे", "निहायत",
            "नीचे", "ने", "पर", "पहले", "पुरा", "पूरा", "पे", "फिर", "बनि", "बनी", "बहि", "बही",
            "बहुत", "बाद", "बाला", "बिलकुल", "भि", "भितर", "भी", "भीतर", "मगर", "मानो", "मे", "में",
            "यदि", "यह", "यहाँ", "यहां", "यहि", "यही", "या", "यिह", "ये", "रखें", "रवासा", "रहा",
            "रहे", "ऱ्वासा", "लिए", "लिये", "लेकिन", "व", "वगेरह", "वरग", "वर्ग", "वह", "वहाँ",
            "वहां", "वहिं", "वहीं", "वाले", "वुह", "वे", "वग़ैरह", "संग", "सकता", "सकते", "सबसे",
            "सभि", "सभी", "साथ", "साबुत", "साभ", "सारा", "से", "सो", "हि", "ही", "हुअ", "हुआ",
            "हुइ", "हुई", "हुए", "हे", "हें", "है", "हैं", "हो", "होता", "होति", "होती", "होते",
            "होना", "होने"));
  }
}
