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

public final class Attributes {

    // word class
    public static final @NotNull String NIMISANA = "nimisana";
    public static final @NotNull String LAATUSANA = "laatusana";
    public static final @NotNull String NIMISANA_LAATUSANA = "nimisana_laatusana";
    public static final @NotNull String ETUNIMI = "etunimi";
    public static final @NotNull String SUKUNIMI = "sukunimi";
    public static final @NotNull String PAIKANNIMI = "paikannimi";
    public static final @NotNull String NIMI = "nimi";
    public static final @NotNull String ASEMOSANA = "asemosana";
    public static final @NotNull String LUKUSANA = "lukusana";
    public static final @NotNull String TEONSANA = "teonsana";
    public static final @NotNull String SEIKKASANA = "seikkasana";
    public static final @NotNull String ETULIITE = "etuliite";

    // comparison
    public static final @NotNull String COMPARATIVE = "comparative";
    public static final @NotNull String SUPERLATIVE = "superlative";
    public static final @NotNull String POSITIVE = "positive";

    // sijamuoto
    public static final @NotNull String NIMENTO = "nimento";
    public static final @NotNull String KERRONTOSTI = "kerrontosti";
    public static final @NotNull String OMANTO = "omanto";
    public static final @NotNull String OSANTO = "osanto";
    public static final @NotNull String OLENTO = "olento";
    public static final @NotNull String TULENTO = "tulento";
    public static final @NotNull String SISAOLENTO = "sisaolento";
    public static final @NotNull String SISAERONTO = "sisaeronto";
    public static final @NotNull String SISATULENTO = "sisatulento";
    public static final @NotNull String ULKOOLENTO = "ulkoolento";
    public static final @NotNull String ULKOERONTO = "ulkoeronto";
    public static final @NotNull String ULKOTULENTO = "ulkotulento";
    public static final @NotNull String VAJANTO = "vajanto";
    public static final @NotNull String SEURANTO = "seuranto";
    public static final @NotNull String KEINONTO = "keinonto";
    public static final @NotNull String KOHDANTO = "kohdanto";

    // mood
    public static final @NotNull String A_INFINITIVE = "A-infinitive";
    public static final @NotNull String E_INFINITIVE = "E-infinitive";
    public static final @NotNull String MA_INFINITIVE = "MA-infinitive";
    public static final @NotNull String MINEN_INFINITIVE = "MINEN-infinitive";
    public static final @NotNull String MAINEN_INFINITIVE = "MAINEN-infinitive";

    // participle
    public static final @NotNull String PAST_PASSIVE = "past_passive";

    private Attributes() { }

    private static final @NotNull Map<String, String> attributeTexts = new HashMap<>();

    static @Nullable String resolveAttribute(@NotNull Symbol symbol) {
        return Attributes.attributeTexts.get(symbol.toString());
    }

    static {

        attributeTexts.put(Tags.ln, NIMISANA);
        attributeTexts.put(Tags.ll, LAATUSANA);
        attributeTexts.put(Tags.lnl, NIMISANA_LAATUSANA);
        attributeTexts.put(Tags.lh, "huudahdussana");
        attributeTexts.put(Tags.lee, ETUNIMI);
        attributeTexts.put(Tags.les, SUKUNIMI);
        attributeTexts.put(Tags.lep, PAIKANNIMI);
        attributeTexts.put(Tags.lem, NIMI);
        attributeTexts.put(Tags.lt, TEONSANA);
        attributeTexts.put(Tags.la, "lyhenne");
        attributeTexts.put(Tags.ls, SEIKKASANA);
        attributeTexts.put(Tags.lu, LUKUSANA);
        attributeTexts.put(Tags.lur, LUKUSANA);
        attributeTexts.put(Tags.lr, ASEMOSANA);
        attributeTexts.put(Tags.lc, "sidesana");
        attributeTexts.put(Tags.ld, "suhdesana");
        attributeTexts.put(Tags.lk, "kieltosana");
        attributeTexts.put(Tags.lp, ETULIITE);

        attributeTexts.put("[Cc]", COMPARATIVE);
        attributeTexts.put("[Cs]", SUPERLATIVE);

        attributeTexts.put(Tags.sn, NIMENTO);
        attributeTexts.put("[Sg]", OMANTO);
        attributeTexts.put("[Sp]", OSANTO);
        attributeTexts.put("[Ses]", OLENTO);
        attributeTexts.put("[Str]", TULENTO);
        attributeTexts.put("[Sine]", SISAOLENTO);
        attributeTexts.put("[Sela]", SISAERONTO);
        attributeTexts.put("[Sill]", SISATULENTO);
        attributeTexts.put("[Sade]", ULKOOLENTO);
        attributeTexts.put("[Sabl]", ULKOERONTO);
        attributeTexts.put("[Sall]", ULKOTULENTO);
        attributeTexts.put("[Sab]", VAJANTO);
        attributeTexts.put("[Sko]", SEURANTO);
        attributeTexts.put("[Sin]", KEINONTO);
        attributeTexts.put("[Ssti]", KERRONTOSTI);
        attributeTexts.put("[Sak]", KOHDANTO);

        attributeTexts.put("[Tn1]", A_INFINITIVE);
        attributeTexts.put("[Tn2]", E_INFINITIVE);
        attributeTexts.put("[Tn3]", MA_INFINITIVE);
        attributeTexts.put("[Tn4]", MINEN_INFINITIVE);
        attributeTexts.put("[Tn5]", MAINEN_INFINITIVE);
        attributeTexts.put("[Tt]", "indicative");
        attributeTexts.put("[Te]", "conditional");
        attributeTexts.put("[Tk]", "imperative");
        attributeTexts.put("[Tm]", "potential");

        attributeTexts.put(Tags.ny, "singular");
        attributeTexts.put(Tags.nm, "plural");

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
        attributeTexts.put("[Rt]", PAST_PASSIVE);
        attributeTexts.put("[Rm]", "agent");
        attributeTexts.put("[Re]", "negation");
    }
}
