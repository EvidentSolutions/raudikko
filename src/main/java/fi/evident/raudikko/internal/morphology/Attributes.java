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

import fi.evident.raudikko.internal.fst.Symbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

final class Attributes {

    private Attributes() { }

    private static final @NotNull Map<String, String> attributeTexts = new HashMap<>();

    static @Nullable String resolveAttribute(@NotNull Symbol symbol) {
        return Attributes.attributeTexts.get(symbol.toString());
    }

    static {

        attributeTexts.put("[Ln]", "nimisana");
        attributeTexts.put("[Ll]", "laatusana");
        attributeTexts.put("[Lnl]", "nimisana_laatusana");
        attributeTexts.put("[Lh]", "huudahdussana");
        attributeTexts.put("[Lee]", "etunimi");
        attributeTexts.put("[Les]", "sukunimi");
        attributeTexts.put("[Lep]", "paikannimi");
        attributeTexts.put("[Lem]", "nimi");
        attributeTexts.put("[Lt]", "teonsana");
        attributeTexts.put("[La]", "lyhenne");
        attributeTexts.put("[Ls]", "seikkasana");
        attributeTexts.put("[Lu]", "lukusana");
        attributeTexts.put("[Lur]", "lukusana");
        attributeTexts.put("[Lr]", "asemosana");
        attributeTexts.put("[Lc]", "sidesana");
        attributeTexts.put("[Ld]", "suhdesana");
        attributeTexts.put("[Lk]", "kieltosana");
        attributeTexts.put("[Lp]", "etuliite");

        attributeTexts.put("[Cc]", "comparative");
        attributeTexts.put("[Cs]", "superlative");

        attributeTexts.put("[Sn]", "nimento");
        attributeTexts.put("[Sg]", "omanto");
        attributeTexts.put("[Sp]", "osanto");
        attributeTexts.put("[Ses]", "olento");
        attributeTexts.put("[Str]", "tulento");
        attributeTexts.put("[Sine]", "sisaolento");
        attributeTexts.put("[Sela]", "sisaeronto");
        attributeTexts.put("[Sill]", "sisatulento");
        attributeTexts.put("[Sade]", "ulkoolento");
        attributeTexts.put("[Sabl]", "ulkoeronto");
        attributeTexts.put("[Sall]", "ulkotulento");
        attributeTexts.put("[Sab]", "vajanto");
        attributeTexts.put("[Sko]", "seuranto");
        attributeTexts.put("[Sin]", "keinonto");
        attributeTexts.put("[Ssti]", "kerrontosti");
        attributeTexts.put("[Sak]", "kohdanto");

        attributeTexts.put("[Tn1]", "A-infinitive");
        attributeTexts.put("[Tn2]", "E-infinitive");
        attributeTexts.put("[Tn3]", "MA-infinitive");
        attributeTexts.put("[Tn4]", "MINEN-infinitive");
        attributeTexts.put("[Tn5]", "MAINEN-infinitive");
        attributeTexts.put("[Tt]", "indicative");
        attributeTexts.put("[Te]", "conditional");
        attributeTexts.put("[Tk]", "imperative");
        attributeTexts.put("[Tm]", "potential");

        attributeTexts.put("[Ny]", "singular");
        attributeTexts.put("[Nm]", "plural");

        attributeTexts.put("[P1]", "1");
        attributeTexts.put("[P2]", "2");
        attributeTexts.put("[P3]", "3");
        attributeTexts.put("[P4]", "4");

        attributeTexts.put("[Ap]", "present_simple");
        attributeTexts.put("[Ai]", "past_imperfective");

        attributeTexts.put("[Fkin]", "kin");
        attributeTexts.put("[Fkaan]", "kaan");

        attributeTexts.put("[O1y]", "1s");
        attributeTexts.put("[O2y]", "2s");
        attributeTexts.put("[O1m]", "1p");
        attributeTexts.put("[O2m]", "2p");
        attributeTexts.put("[O3]", "3");

        attributeTexts.put("[Et]", "true");
        attributeTexts.put("[Ef]", "false");
        attributeTexts.put("[Eb]", "both");

        attributeTexts.put("[Rv]", "present_active");
        attributeTexts.put("[Ra]", "present_passive");
        attributeTexts.put("[Ru]", "past_active");
        attributeTexts.put("[Rt]", "past_passive");
        attributeTexts.put("[Rm]", "agent");
        attributeTexts.put("[Re]", "negation");
    }
}
