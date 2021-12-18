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
 * Breton stop words
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 2.1.0
 */
public class Breton implements Language {
  @Override
  public Set<String> stopWords() {
    return new HashSet<>(
        Arrays.asList(
            "'blam",
            "'d",
            "'m",
            "'r",
            "'ta",
            "'vat",
            "'z",
            "'zo",
            "a",
            "a:",
            "aba",
            "abalamour",
            "abaoe",
            "ac'hane",
            "ac'hanoc'h",
            "ac'hanomp",
            "ac'hanon",
            "ac'hanout",
            "adal",
            "adalek",
            "adarre",
            "ae",
            "aec'h",
            "aed",
            "aemp",
            "aen",
            "aent",
            "aes",
            "afe",
            "afec'h",
            "afed",
            "afemp",
            "afen",
            "afent",
            "afes",
            "ag",
            "ah",
            "aimp",
            "aint",
            "aio",
            "aiou",
            "aje",
            "ajec'h",
            "ajed",
            "ajemp",
            "ajen",
            "ajent",
            "ajes",
            "al",
            "alato",
            "alies",
            "aliesañ",
            "alkent",
            "all",
            "allas",
            "allo",
            "allô",
            "am",
            "amañ",
            "amzer",
            "an",
            "anezhañ",
            "anezhe",
            "anezhi",
            "anezho",
            "anvet",
            "aon",
            "aotren",
            "ar",
            "arall",
            "araok",
            "araoki",
            "araozañ",
            "araozo",
            "araozoc'h",
            "araozomp",
            "araozon",
            "araozor",
            "araozout",
            "arbenn",
            "arre",
            "atalek",
            "atav",
            "az",
            "azalek",
            "azirazañ",
            "azirazi",
            "azirazo",
            "azirazoc'h",
            "azirazomp",
            "azirazon",
            "azirazor",
            "azirazout",
            "b:",
            "ba",
            "ba'l",
            "ba'n",
            "ba'r",
            "bad",
            "bah",
            "bal",
            "ban",
            "bar",
            "bastañ",
            "befe",
            "bell",
            "benaos",
            "benn",
            "bennag",
            "bennak",
            "bennozh",
            "bep",
            "bepred",
            "berr",
            "berzh",
            "bet",
            "betek",
            "betra",
            "bev",
            "bevet",
            "bez",
            "bezañ",
            "beze",
            "bezent",
            "bezet",
            "bezh",
            "bezit",
            "bezomp",
            "bihan",
            "bije",
            "biou",
            "biskoazh",
            "blam",
            "bo",
            "boa",
            "bominapl",
            "boudoudom",
            "bouez",
            "boull",
            "boum",
            "bout",
            "bras",
            "brasañ",
            "brav",
            "bravo",
            "bremañ",
            "bres",
            "brokenn",
            "bronn",
            "brrr",
            "brutal",
            "buhezek",
            "c'h:",
            "c'haout",
            "c'he",
            "c'hem",
            "c'herz",
            "c'heñver",
            "c'hichen",
            "c'hiz",
            "c'hoazh",
            "c'horre",
            "c'houde",
            "c'houst",
            "c'hreiz",
            "c'hwec'h",
            "c'hwec'hvet",
            "c'hwezek",
            "c'hwi",
            "ch:",
            "chaous",
            "chik",
            "chit",
            "chom",
            "chut",
            "d'",
            "d'al",
            "d'an",
            "d'ar",
            "d'az",
            "d'e",
            "d'he",
            "d'ho",
            "d'hol",
            "d'hon",
            "d'hor",
            "d'o",
            "d'ober",
            "d'ul",
            "d'un",
            "d'ur",
            "d:",
            "da",
            "dak",
            "daka",
            "dal",
            "dalbezh",
            "dalc'hmat",
            "dalit",
            "damdost",
            "damheñvel",
            "damm",
            "dan",
            "danvez",
            "dao",
            "daol",
            "daonet",
            "daou",
            "daoust",
            "daouzek",
            "daouzekvet",
            "darn",
            "dastrewiñ",
            "dav",
            "davedoc'h",
            "davedomp",
            "davedon",
            "davedor",
            "davedout",
            "davet",
            "davetañ",
            "davete",
            "daveti",
            "daveto",
            "defe",
            "dehou",
            "dek",
            "dekvet",
            "den",
            "deoc'h",
            "deomp",
            "deor",
            "derc'hel",
            "deus",
            "dez",
            "deze",
            "dezhañ",
            "dezhe",
            "dezhi",
            "dezho",
            "di",
            "diabarzh",
            "diagent",
            "diar",
            "diaraok",
            "diavaez",
            "dibaoe",
            "dibaot",
            "dibar",
            "dic'halañ",
            "didiac'h",
            "dienn",
            "difer",
            "diganeoc'h",
            "diganeomp",
            "diganeor",
            "diganimp",
            "diganin",
            "diganit",
            "digant",
            "digantañ",
            "digante",
            "diganti",
            "diganto",
            "digemmesk",
            "diget",
            "digor",
            "digoret",
            "dija",
            "dije",
            "dimp",
            "din",
            "dinaou",
            "dindan",
            "dindanañ",
            "dindani",
            "dindano",
            "dindanoc'h",
            "dindanomp",
            "dindanon",
            "dindanor",
            "dindanout",
            "dioutañ",
            "dioute",
            "diouti",
            "diouto",
            "diouzh",
            "diouzhin",
            "diouzhit",
            "diouzhoc'h",
            "diouzhomp",
            "diouzhor",
            "dirak",
            "dirazañ",
            "dirazi",
            "dirazo",
            "dirazoc'h",
            "dirazomp",
            "dirazon",
            "dirazor",
            "dirazout",
            "disheñvel",
            "dispar",
            "distank",
            "dister",
            "disterañ",
            "disterig",
            "distro",
            "dit",
            "divaez",
            "diwar",
            "diwezhat",
            "diwezhañ",
            "do",
            "doa",
            "doare",
            "dont",
            "dost",
            "doue",
            "douetus",
            "douez",
            "doug",
            "draou",
            "draoñ",
            "dre",
            "drede",
            "dreist",
            "dreistañ",
            "dreisti",
            "dreisto",
            "dreistoc'h",
            "dreistomp",
            "dreiston",
            "dreistor",
            "dreistout",
            "drek",
            "dreñv",
            "dring",
            "dro",
            "du",
            "e",
            "e:",
            "eas",
            "ebet",
            "ec'h",
            "edo",
            "edoc'h",
            "edod",
            "edomp",
            "edon",
            "edont",
            "edos",
            "eer",
            "eeun",
            "efed",
            "egedoc'h",
            "egedomp",
            "egedon",
            "egedor",
            "egedout",
            "eget",
            "egetañ",
            "egete",
            "egeti",
            "egeto",
            "eh",
            "eil",
            "eilvet",
            "eizh",
            "eizhvet",
            "ejoc'h",
            "ejod",
            "ejomp",
            "ejont",
            "ejout",
            "el",
            "em",
            "emaint",
            "emaoc'h",
            "emaomp",
            "emaon",
            "emaout",
            "emañ",
            "eme",
            "emeur",
            "emezañ",
            "emezi",
            "emezo",
            "emezoc'h",
            "emezomp",
            "emezon",
            "emezout",
            "emporzhiañ",
            "en",
            "end",
            "endan",
            "endra",
            "enep",
            "ennañ",
            "enni",
            "enno",
            "ennoc'h",
            "ennomp",
            "ennon",
            "ennor",
            "ennout",
            "enta",
            "eo",
            "eomp",
            "eont",
            "eor",
            "eot",
            "er",
            "erbet",
            "erfin",
            "esa",
            "esae",
            "espar",
            "estlamm",
            "estrañj",
            "eta",
            "etre",
            "etreoc'h",
            "etrezo",
            "etrezoc'h",
            "etrezomp",
            "etrezor",
            "euh",
            "eur",
            "eus",
            "evel",
            "evelato",
            "eveldoc'h",
            "eveldomp",
            "eveldon",
            "eveldor",
            "eveldout",
            "evelkent",
            "eveltañ",
            "evelte",
            "evelti",
            "evelto",
            "evidoc'h",
            "evidomp",
            "evidon",
            "evidor",
            "evidout",
            "evit",
            "evitañ",
            "evite",
            "eviti",
            "evito",
            "ez",
            "eñ",
            "f:",
            "fac'h",
            "fall",
            "fed",
            "feiz",
            "fenn",
            "fezh",
            "fin",
            "finsalvet",
            "foei",
            "fouilhezañ",
            "g:",
            "gallout",
            "ganeoc'h",
            "ganeomp",
            "ganin",
            "ganit",
            "gant",
            "gantañ",
            "ganti",
            "ganto",
            "gaout",
            "gast",
            "gein",
            "gellout",
            "genndost",
            "gentañ",
            "ger",
            "gerz",
            "get",
            "geñver",
            "gichen",
            "gin",
            "giz",
            "glan",
            "gloev",
            "goll",
            "gorre",
            "goude",
            "gouez",
            "gouezit",
            "gouezomp",
            "goulz",
            "gounnar",
            "gour",
            "goust",
            "gouze",
            "gouzout",
            "gra",
            "grak",
            "grec'h",
            "greiz",
            "grenn",
            "greomp",
            "grit",
            "groñs",
            "gutez",
            "gwall",
            "gwashoc'h",
            "gwazh",
            "gwech",
            "gwechall",
            "gwechoù",
            "gwell",
            "gwezh",
            "gwezhall",
            "gwezharall",
            "gwezhoù",
            "gwig",
            "gwirionez",
            "gwitibunan",
            "gêr",
            "h:",
            "ha",
            "hag",
            "han",
            "hanter",
            "hanterc'hantad",
            "hanterkantved",
            "harz",
            "hañ",
            "hañval",
            "he",
            "hebioù",
            "hec'h",
            "hei",
            "hein",
            "hem",
            "hemañ",
            "hen",
            "hend",
            "henhont",
            "henn",
            "hennezh",
            "hent",
            "hep",
            "hervez",
            "hervezañ",
            "hervezi",
            "hervezo",
            "hervezoc'h",
            "hervezomp",
            "hervezon",
            "hervezor",
            "hervezout",
            "heul",
            "heuliañ",
            "hevelep",
            "heverk",
            "heñvel",
            "heñvelat",
            "heñvelañ",
            "heñveliñ",
            "heñveloc'h",
            "heñvelout",
            "hi",
            "hilh",
            "hini",
            "hirie",
            "hirio",
            "hiziv",
            "hiziviken",
            "ho",
            "hoaliñ",
            "hoc'h",
            "hogen",
            "hogos",
            "hogozik",
            "hol",
            "holl",
            "holà",
            "homañ",
            "hon",
            "honhont",
            "honnezh",
            "hont",
            "hop",
            "hopala",
            "hor",
            "hou",
            "houp",
            "hudu",
            "hue",
            "hui",
            "hum",
            "hurrah",
            "i",
            "i:",
            "in",
            "int",
            "is",
            "ispisial",
            "isurzhiet",
            "it",
            "ivez",
            "izelañ",
            "j:",
            "just",
            "k:",
            "kae",
            "kaer",
            "kalon",
            "kalz",
            "kant",
            "kaout",
            "kar",
            "kazi",
            "keid",
            "kein",
            "keit",
            "kel",
            "kellies",
            "keloù",
            "kement",
            "ken",
            "kenkent",
            "kenkoulz",
            "kenment",
            "kent",
            "kentañ",
            "kentizh",
            "kentoc'h",
            "kentre",
            "ker",
            "kerkent",
            "kerz",
            "kerzh",
            "ket",
            "keta",
            "keñver",
            "keñverel",
            "keñverius",
            "kichen",
            "kichenik",
            "kit",
            "kiz",
            "klak",
            "klek",
            "klik",
            "komprenet",
            "komz",
            "kont",
            "korf",
            "korre",
            "koulskoude",
            "koulz",
            "koust",
            "krak",
            "krampouezh",
            "krec'h",
            "kreiz",
            "kuit",
            "kwir",
            "l:",
            "la",
            "laez",
            "laoskel",
            "laouen",
            "lavar",
            "lavaret",
            "lavarout",
            "lec'h",
            "lein",
            "leizh",
            "lerc'h",
            "leun",
            "leuskel",
            "lew",
            "lies",
            "liesañ",
            "lod",
            "lusk",
            "lâr",
            "lârout",
            "m:",
            "ma",
            "ma'z",
            "mac'h",
            "mac'hat",
            "mac'hañ",
            "mac'hoc'h",
            "mad",
            "maez",
            "maksimal",
            "mann",
            "mar",
            "mard",
            "marg",
            "marzh",
            "mat",
            "mañ",
            "me",
            "memes",
            "memestra",
            "merkapl",
            "mersi",
            "mes",
            "mesk",
            "met",
            "meur",
            "mil",
            "minimal",
            "moan",
            "moaniaat",
            "mod",
            "mont",
            "mout",
            "mui",
            "muiañ",
            "muioc'h",
            "n",
            "n'",
            "n:",
            "na",
            "nag",
            "naontek",
            "naturel",
            "nav",
            "navet",
            "ne",
            "nebeudig",
            "nebeut",
            "nebeutañ",
            "nebeutoc'h",
            "neketa",
            "nemedoc'h",
            "nemedomp",
            "nemedon",
            "nemedor",
            "nemedout",
            "nemet",
            "nemetañ",
            "nemete",
            "nemeti",
            "nemeto",
            "nemeur",
            "neoac'h",
            "nepell",
            "nerzh",
            "nes",
            "neseser",
            "netra",
            "neubeudoù",
            "neuhe",
            "neuze",
            "nevez",
            "newazh",
            "nez",
            "ni",
            "nikun",
            "niverus",
            "nul",
            "o",
            "o:",
            "oa",
            "oac'h",
            "oad",
            "oamp",
            "oan",
            "oant",
            "oar",
            "oas",
            "ober",
            "oc'h",
            "oc'ho",
            "oc'hola",
            "oc'hpenn",
            "oh",
            "ohe",
            "ollé",
            "olole",
            "olé",
            "omp",
            "on",
            "ordin",
            "ordinal",
            "ouejoc'h",
            "ouejod",
            "ouejomp",
            "ouejont",
            "ouejout",
            "ouek",
            "ouezas",
            "ouezi",
            "ouezimp",
            "ouezin",
            "ouezint",
            "ouezis",
            "ouezo",
            "ouezoc'h",
            "ouezor",
            "ouf",
            "oufe",
            "oufec'h",
            "oufed",
            "oufemp",
            "oufen",
            "oufent",
            "oufes",
            "ouie",
            "ouiec'h",
            "ouied",
            "ouiemp",
            "ouien",
            "ouient",
            "ouies",
            "ouije",
            "ouijec'h",
            "ouijed",
            "ouijemp",
            "ouijen",
            "ouijent",
            "ouijes",
            "out",
            "outañ",
            "outi",
            "outo",
            "ouzer",
            "ouzh",
            "ouzhin",
            "ouzhit",
            "ouzhoc'h",
            "ouzhomp",
            "ouzhor",
            "ouzhpenn",
            "ouzhpennik",
            "ouzoc'h",
            "ouzomp",
            "ouzon",
            "ouzont",
            "ouzout",
            "p'",
            "p:",
            "pa",
            "pad",
            "padal",
            "paf",
            "pan",
            "panevedeoc'h",
            "panevedo",
            "panevedomp",
            "panevedon",
            "panevedout",
            "panevet",
            "panevetañ",
            "paneveti",
            "pas",
            "paseet",
            "pe",
            "peadra",
            "peder",
            "pedervet",
            "pedervetvet",
            "pefe",
            "pegeit",
            "pegement",
            "pegen",
            "pegiz",
            "pegoulz",
            "pehini",
            "pelec'h",
            "pell",
            "pemod",
            "pemp",
            "pempved",
            "pemzek",
            "penaos",
            "penn",
            "peogwir",
            "peotramant",
            "pep",
            "perak",
            "perc'hennañ",
            "pergen",
            "permetiñ",
            "peseurt",
            "pet",
            "petiaoul",
            "petoare",
            "petra",
            "peur",
            "peurgetket",
            "peurheñvel",
            "peurliesañ",
            "peurvuiañ",
            "peus",
            "peustost",
            "peuz",
            "pevar",
            "pevare",
            "pevarevet",
            "pevarzek",
            "pez",
            "peze",
            "pezh",
            "pff",
            "pfft",
            "pfut",
            "picher",
            "pif",
            "pife",
            "pign",
            "pije",
            "pikol",
            "pitiaoul",
            "piv",
            "plaouf",
            "plok",
            "plouf",
            "po",
            "poa",
            "poelladus",
            "pof",
            "pok",
            "posupl",
            "pouah",
            "pourc'henn",
            "prest",
            "prestik",
            "prim",
            "prin",
            "provostapl",
            "pst",
            "pu",
            "pur",
            "r:",
            "ra",
            "rae",
            "raec'h",
            "raed",
            "raemp",
            "raen",
            "raent",
            "raes",
            "rafe",
            "rafec'h",
            "rafed",
            "rafemp",
            "rafen",
            "rafent",
            "rafes",
            "rag",
            "raimp",
            "raint",
            "raio",
            "raje",
            "rajec'h",
            "rajed",
            "rajemp",
            "rajen",
            "rajent",
            "rajes",
            "rak",
            "ral",
            "ran",
            "rankout",
            "raok",
            "razh",
            "re",
            "reas",
            "reer",
            "regennoù",
            "reiñ",
            "rejoc'h",
            "rejod",
            "rejomp",
            "rejont",
            "rejout",
            "rener",
            "rentañ",
            "reoc'h",
            "reomp",
            "reont",
            "reor",
            "reot",
            "resis",
            "ret",
            "reve",
            "rez",
            "ri",
            "rik",
            "rin",
            "ris",
            "rit",
            "rouez",
            "s:",
            "sac'h",
            "sant",
            "sav",
            "sañset",
            "se",
            "sed",
            "seitek",
            "seizh",
            "seizhvet",
            "sell",
            "sellit",
            "ser",
            "setu",
            "seul",
            "seurt",
            "siwazh",
            "skignañ",
            "skoaz",
            "skouer",
            "sort",
            "souden",
            "souvitañ",
            "soñj",
            "speriañ",
            "spririñ",
            "stad",
            "stlabezañ",
            "stop",
            "stranañ",
            "strewiñ",
            "strishaat",
            "stumm",
            "sujed",
            "surtoud",
            "t:",
            "ta",
            "taer",
            "tailh",
            "tak",
            "tal",
            "talvoudegezh",
            "tamm",
            "tanav",
            "taol",
            "te",
            "techet",
            "teir",
            "teirvet",
            "telt",
            "teltenn",
            "teus",
            "teut",
            "teuteu",
            "ti",
            "tik",
            "toa",
            "tok",
            "tost",
            "tostig",
            "toud",
            "touesk",
            "touez",
            "toull",
            "tra",
            "trantenn",
            "traoñ",
            "trawalc'h",
            "tre",
            "trede",
            "tregont",
            "tremenet",
            "tri",
            "trivet",
            "triwec'h",
            "trizek",
            "tro",
            "trugarez",
            "trumm",
            "tsoin",
            "tsouin",
            "tu",
            "tud",
            "u:",
            "ugent",
            "uhel",
            "uhelañ",
            "ul",
            "un",
            "unan",
            "unanez",
            "unanig",
            "unnek",
            "unnekvet",
            "ur",
            "urzh",
            "us",
            "v:",
            "va",
            "vale",
            "van",
            "vare",
            "vat",
            "vefe",
            "vefec'h",
            "vefed",
            "vefemp",
            "vefen",
            "vefent",
            "vefes",
            "vesk",
            "vete",
            "vez",
            "vezan",
            "vezañ",
            "veze",
            "vezec'h",
            "vezed",
            "vezemp",
            "vezen",
            "vezent",
            "vezer",
            "vezes",
            "vezez",
            "vezit",
            "vezomp",
            "vezont",
            "vi",
            "vihan",
            "vihanañ",
            "vije",
            "vijec'h",
            "vijed",
            "vijemp",
            "vijen",
            "vijent",
            "vijes",
            "viken",
            "vimp",
            "vin",
            "vint",
            "vior",
            "viot",
            "virviken",
            "viskoazh",
            "vlan",
            "vlaou",
            "vo",
            "vod",
            "voe",
            "voec'h",
            "voed",
            "voemp",
            "voen",
            "voent",
            "voes",
            "vont",
            "vostapl",
            "vrac'h",
            "vrasañ",
            "vremañ",
            "w:",
            "walc'h",
            "war",
            "warnañ",
            "warni",
            "warno",
            "warnoc'h",
            "warnomp",
            "warnon",
            "warnor",
            "warnout",
            "wazh",
            "wech",
            "wechoù",
            "well",
            "y:",
            "you",
            "youadenn",
            "youc'hadenn",
            "youc'hou",
            "z:",
            "za",
            "zan",
            "zaw",
            "zeu",
            "zi",
            "ziar",
            "zigarez",
            "ziget",
            "zindan",
            "zioc'h",
            "ziouzh",
            "zirak",
            "zivout",
            "ziwar",
            "ziwezhañ",
            "zo",
            "zoken",
            "zokenoc'h",
            "zouesk",
            "zouez",
            "zro",
            "zu"));
  }
}
