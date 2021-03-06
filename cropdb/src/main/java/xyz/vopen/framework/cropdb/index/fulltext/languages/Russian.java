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
 * Russian stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Russian implements Language {
  @Override
  public Set<String> stopWords() {
    return new HashSet<>(
        Arrays.asList(
            "c",
            "а",
            "алло",
            "без",
            "белый",
            "близко",
            "более",
            "больше",
            "большой",
            "будем",
            "будет",
            "будете",
            "будешь",
            "будто",
            "буду",
            "будут",
            "будь",
            "бы",
            "бывает",
            "бывь",
            "был",
            "была",
            "были",
            "было",
            "быть",
            "в",
            "важная",
            "важное",
            "важные",
            "важный",
            "вам",
            "вами",
            "вас",
            "ваш",
            "ваша",
            "ваше",
            "ваши",
            "вверх",
            "вдали",
            "вдруг",
            "ведь",
            "везде",
            "вернуться",
            "весь",
            "вечер",
            "взгляд",
            "взять",
            "вид",
            "видел",
            "видеть",
            "вместе",
            "вне",
            "вниз",
            "внизу",
            "во",
            "вода",
            "война",
            "вокруг",
            "вон",
            "вообще",
            "вопрос",
            "восемнадцатый",
            "восемнадцать",
            "восемь",
            "восьмой",
            "вот",
            "впрочем",
            "времени",
            "время",
            "все",
            "все еще",
            "всегда",
            "всего",
            "всем",
            "всеми",
            "всему",
            "всех",
            "всею",
            "всю",
            "всюду",
            "вся",
            "всё",
            "второй",
            "вы",
            "выйти",
            "г",
            "где",
            "главный",
            "глаз",
            "говорил",
            "говорит",
            "говорить",
            "год",
            "года",
            "году",
            "голова",
            "голос",
            "город",
            "да",
            "давать",
            "давно",
            "даже",
            "далекий",
            "далеко",
            "дальше",
            "даром",
            "дать",
            "два",
            "двадцатый",
            "двадцать",
            "две",
            "двенадцатый",
            "двенадцать",
            "дверь",
            "двух",
            "девятнадцатый",
            "девятнадцать",
            "девятый",
            "девять",
            "действительно",
            "дел",
            "делал",
            "делать",
            "делаю",
            "дело",
            "день",
            "деньги",
            "десятый",
            "десять",
            "для",
            "до",
            "довольно",
            "долго",
            "должен",
            "должно",
            "должный",
            "дом",
            "дорога",
            "друг",
            "другая",
            "другие",
            "других",
            "друго",
            "другое",
            "другой",
            "думать",
            "душа",
            "е",
            "его",
            "ее",
            "ей",
            "ему",
            "если",
            "есть",
            "еще",
            "ещё",
            "ею",
            "её",
            "ж",
            "ждать",
            "же",
            "жена",
            "женщина",
            "жизнь",
            "жить",
            "за",
            "занят",
            "занята",
            "занято",
            "заняты",
            "затем",
            "зато",
            "зачем",
            "здесь",
            "земля",
            "знать",
            "значит",
            "значить",
            "и",
            "иди",
            "идти",
            "из",
            "или",
            "им",
            "имеет",
            "имел",
            "именно",
            "иметь",
            "ими",
            "имя",
            "иногда",
            "их",
            "к",
            "каждая",
            "каждое",
            "каждые",
            "каждый",
            "кажется",
            "казаться",
            "как",
            "какая",
            "какой",
            "кем",
            "книга",
            "когда",
            "кого",
            "ком",
            "комната",
            "кому",
            "конец",
            "конечно",
            "которая",
            "которого",
            "которой",
            "которые",
            "который",
            "которых",
            "кроме",
            "кругом",
            "кто",
            "куда",
            "лежать",
            "лет",
            "ли",
            "лицо",
            "лишь",
            "лучше",
            "любить",
            "люди",
            "м",
            "маленький",
            "мало",
            "мать",
            "машина",
            "между",
            "меля",
            "менее",
            "меньше",
            "меня",
            "место",
            "миллионов",
            "мимо",
            "минута",
            "мир",
            "мира",
            "мне",
            "много",
            "многочисленная",
            "многочисленное",
            "многочисленные",
            "многочисленный",
            "мной",
            "мною",
            "мог",
            "могу",
            "могут",
            "мож",
            "может",
            "может быть",
            "можно",
            "можхо",
            "мои",
            "мой",
            "мор",
            "москва",
            "мочь",
            "моя",
            "моё",
            "мы",
            "на",
            "наверху",
            "над",
            "надо",
            "назад",
            "наиболее",
            "найти",
            "наконец",
            "нам",
            "нами",
            "народ",
            "нас",
            "начала",
            "начать",
            "наш",
            "наша",
            "наше",
            "наши",
            "не",
            "него",
            "недавно",
            "недалеко",
            "нее",
            "ней",
            "некоторый",
            "нельзя",
            "нем",
            "немного",
            "нему",
            "непрерывно",
            "нередко",
            "несколько",
            "нет",
            "нею",
            "неё",
            "ни",
            "нибудь",
            "ниже",
            "низко",
            "никакой",
            "никогда",
            "никто",
            "никуда",
            "ним",
            "ними",
            "них",
            "ничего",
            "ничто",
            "но",
            "новый",
            "нога",
            "ночь",
            "ну",
            "нужно",
            "нужный",
            "нх",
            "о",
            "об",
            "оба",
            "обычно",
            "один",
            "одиннадцатый",
            "одиннадцать",
            "однажды",
            "однако",
            "одного",
            "одной",
            "оказаться",
            "окно",
            "около",
            "он",
            "она",
            "они",
            "оно",
            "опять",
            "особенно",
            "остаться",
            "от",
            "ответить",
            "отец",
            "откуда",
            "отовсюду",
            "отсюда",
            "очень",
            "первый",
            "перед",
            "писать",
            "плечо",
            "по",
            "под",
            "подойди",
            "подумать",
            "пожалуйста",
            "позже",
            "пойти",
            "пока",
            "пол",
            "получить",
            "помнить",
            "понимать",
            "понять",
            "пор",
            "пора",
            "после",
            "последний",
            "посмотреть",
            "посреди",
            "потом",
            "потому",
            "почему",
            "почти",
            "правда",
            "прекрасно",
            "при",
            "про",
            "просто",
            "против",
            "процентов",
            "путь",
            "пятнадцатый",
            "пятнадцать",
            "пятый",
            "пять",
            "работа",
            "работать",
            "раз",
            "разве",
            "рано",
            "раньше",
            "ребенок",
            "решить",
            "россия",
            "рука",
            "русский",
            "ряд",
            "рядом",
            "с",
            "с кем",
            "сам",
            "сама",
            "сами",
            "самим",
            "самими",
            "самих",
            "само",
            "самого",
            "самой",
            "самом",
            "самому",
            "саму",
            "самый",
            "свет",
            "свое",
            "своего",
            "своей",
            "свои",
            "своих",
            "свой",
            "свою",
            "сделать",
            "сеаой",
            "себе",
            "себя",
            "сегодня",
            "седьмой",
            "сейчас",
            "семнадцатый",
            "семнадцать",
            "семь",
            "сидеть",
            "сила",
            "сих",
            "сказал",
            "сказала",
            "сказать",
            "сколько",
            "слишком",
            "слово",
            "случай",
            "смотреть",
            "сначала",
            "снова",
            "со",
            "собой",
            "собою",
            "советский",
            "совсем",
            "спасибо",
            "спросить",
            "сразу",
            "стал",
            "старый",
            "стать",
            "стол",
            "сторона",
            "стоять",
            "страна",
            "суть",
            "считать",
            "т",
            "та",
            "так",
            "такая",
            "также",
            "таки",
            "такие",
            "такое",
            "такой",
            "там",
            "твои",
            "твой",
            "твоя",
            "твоё",
            "те",
            "тебе",
            "тебя",
            "тем",
            "теми",
            "теперь",
            "тех",
            "то",
            "тобой",
            "тобою",
            "товарищ",
            "тогда",
            "того",
            "тоже",
            "только",
            "том",
            "тому",
            "тот",
            "тою",
            "третий",
            "три",
            "тринадцатый",
            "тринадцать",
            "ту",
            "туда",
            "тут",
            "ты",
            "тысяч",
            "у",
            "увидеть",
            "уж",
            "уже",
            "улица",
            "уметь",
            "утро",
            "хороший",
            "хорошо",
            "хотел бы",
            "хотеть",
            "хоть",
            "хотя",
            "хочешь",
            "час",
            "часто",
            "часть",
            "чаще",
            "чего",
            "человек",
            "чем",
            "чему",
            "через",
            "четвертый",
            "четыре",
            "четырнадцатый",
            "четырнадцать",
            "что",
            "чтоб",
            "чтобы",
            "чуть",
            "шестнадцатый",
            "шестнадцать",
            "шестой",
            "шесть",
            "эта",
            "эти",
            "этим",
            "этими",
            "этих",
            "это",
            "этого",
            "этой",
            "этом",
            "этому",
            "этот",
            "эту",
            "я",
            "являюсь"));
  }
}
