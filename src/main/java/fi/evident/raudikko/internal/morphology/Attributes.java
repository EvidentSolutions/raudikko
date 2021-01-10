/*
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Libvoikko: Library of natural language processing tools.
 * The Initial Developer of the Original Code is Harri Pitkänen <hatapitk@iki.fi>.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Raudikko, the Java port of the Initial Code is Copyright (C) 2020 by
 * Evident Solutions Oy. All Rights Reserved.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */

package fi.evident.raudikko.internal.morphology;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

final class Attributes {

    private Attributes() { }

    public static final @NotNull Map<String, String> CLASS = new HashMap<>();
    public static final @NotNull Map<String, String> COMPARISON = new HashMap<>();
    public static final @NotNull Map<String, String> SIJAMUOTO = new HashMap<>();
    public static final @NotNull Map<String, String> MOOD = new HashMap<>();
    public static final @NotNull Map<String, String> NUMBER = new HashMap<>();
    public static final @NotNull Map<String, String> PERSON = new HashMap<>();
    public static final @NotNull Map<String, String> TENSE = new HashMap<>();
    public static final @NotNull Map<String, String> FOCUS = new HashMap<>();
    public static final @NotNull Map<String, String> POSSESSIVE = new HashMap<>();
    public static final @NotNull Map<String, String> PARTICIPLE = new HashMap<>();
    public static final @NotNull Map<String, String> NEGATIVE = new HashMap<>();

    static {
        CLASS.put("[Ln]", "nimisana");
        CLASS.put("[Ll]", "laatusana");
        CLASS.put("[Lnl]", "nimisana_laatusana");
        CLASS.put("[Lh]", "huudahdussana");
        CLASS.put("[Lee]", "etunimi");
        CLASS.put("[Les]", "sukunimi");
        CLASS.put("[Lep]", "paikannimi");
        CLASS.put("[Lem]", "nimi");
        CLASS.put("[Lt]", "teonsana");
        CLASS.put("[La]", "lyhenne");
        CLASS.put("[Ls]", "seikkasana");
        CLASS.put("[Lu]", "lukusana");
        CLASS.put("[Lur]", "lukusana");
        CLASS.put("[Lr]", "asemosana");
        CLASS.put("[Lc]", "sidesana");
        CLASS.put("[Ld]", "suhdesana");
        CLASS.put("[Lk]", "kieltosana");
        CLASS.put("[Lp]", "etuliite");

        COMPARISON.put("[Cc]", "comparative");
        COMPARISON.put("[Cs]", "superlative");

        SIJAMUOTO.put("[Sn]", "nimento");
        SIJAMUOTO.put("[Sg]", "omanto");
        SIJAMUOTO.put("[Sp]", "osanto");
        SIJAMUOTO.put("[Ses]", "olento");
        SIJAMUOTO.put("[Str]", "tulento");
        SIJAMUOTO.put("[Sine]", "sisaolento");
        SIJAMUOTO.put("[Sela]", "sisaeronto");
        SIJAMUOTO.put("[Sill]", "sisatulento");
        SIJAMUOTO.put("[Sade]", "ulkoolento");
        SIJAMUOTO.put("[Sabl]", "ulkoeronto");
        SIJAMUOTO.put("[Sall]", "ulkotulento");
        SIJAMUOTO.put("[Sab]", "vajanto");
        SIJAMUOTO.put("[Sko]", "seuranto");
        SIJAMUOTO.put("[Sin]", "keinonto");
        SIJAMUOTO.put("[Ssti]", "kerrontosti");
        SIJAMUOTO.put("[Sak]", "kohdanto");

        MOOD.put("[Tn1]", "A-infinitive");
        MOOD.put("[Tn2]", "E-infinitive");
        MOOD.put("[Tn3]", "MA-infinitive");
        MOOD.put("[Tn4]", "MINEN-infinitive");
        MOOD.put("[Tn5]", "MAINEN-infinitive");
        MOOD.put("[Tt]", "indicative");
        MOOD.put("[Te]", "conditional");
        MOOD.put("[Tk]", "imperative");
        MOOD.put("[Tm]", "potential");

        NUMBER.put("[Ny]", "singular");
        NUMBER.put("[Nm]", "plural");

        PERSON.put("[P1]", "1");
        PERSON.put("[P2]", "2");
        PERSON.put("[P3]", "3");
        PERSON.put("[P4]", "4");

        TENSE.put("[Ap]", "present_simple");
        TENSE.put("[Ai]", "past_imperfective");

        FOCUS.put("[Fkin]", "kin");
        FOCUS.put("[Fkaan]", "kaan");

        POSSESSIVE.put("[O1y]", "1s");
        POSSESSIVE.put("[O2y]", "2s");
        POSSESSIVE.put("[O1m]", "1p");
        POSSESSIVE.put("[O2m]", "2p");
        POSSESSIVE.put("[O3]", "3");

        NEGATIVE.put("[Et]", "true");
        NEGATIVE.put("[Ef]", "false");
        NEGATIVE.put("[Eb]", "both");

        PARTICIPLE.put("[Rv]", "present_active");
        PARTICIPLE.put("[Ra]", "present_passive");
        PARTICIPLE.put("[Ru]", "past_active");
        PARTICIPLE.put("[Rt]", "past_passive");
        PARTICIPLE.put("[Rm]", "agent");
        PARTICIPLE.put("[Re]", "negation");
    }
}
