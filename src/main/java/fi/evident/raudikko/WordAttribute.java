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

package fi.evident.raudikko;

import fi.evident.raudikko.internal.fst.Symbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("NonAsciiCharacters")
public enum WordAttribute {

    // Sanaluokat
    Ln,  // nimisana
    Lee, // erisnimi - etunimi
    Les, // erisnimi - sukunimi
    Lep, // erisnimi - paikannimi
    Lem, // erisnimi - muu erisnimi
    Ll,  // laatusana
    Lnl, // nimi_laatusana
    Lt,  // teonsana
    Lh,  // huudahdussana
    Lp,  // etuliite
    La,  // lyhenne
    Ls,  // seikkasana
    Lc,  // sidesana
    Lu,  // lukusana
    Lur, // lukusana (roomalainen)
    Lr,  // asemosana
    Ld,  // suhdesana
    Lk,  // kieltosana

    // Vokaalisointu
    Va,
    Vä,
    Vää,

    // Sijamuodot
    Sn,   // nominatiivi
    Sg,   // genetiivi
    Sp,   // partitiivi
    Ses,  // essiivi
    Str,  // translatiivi
    Sine, // inessiivi
    Sela, // elatiivi
    Sill, // illatiivi
    Sade, // adessiivi
    Sabl, // ablatiivi
    Sall, // allatiivi
    Sab,  // abessiivi
    Sko,  // komitatiivi
    Sin,  // instruktiivi
    Ssti, // kerronto_sti
    Sak , // akkusatiivi

    // Luvut
    Ny, // yksikkö
    Nm, // monikko

    // Omistusliitteet
    O1y, // yksikön 1.
    O2y, // yksikön 2.
    O1m, // monikon 1.
    O2m, // monikon 2.
    O3,  // yksikön/monikon 3.

    // Vertailumuodot
    Cc, // voittoaste
    Cs, // yliaste

    // Liitepartikkelit
    Fkin, // -kin
    Fkaan, // -kaan
    Fko, // kysymysliite ko/kö

    // Tapaluokat
    Tn1, // 1. nimitapa
    Tn2, // 2. nimitapa
    Tn3, // 3. nimitapa
    Tn4, // 4. nimitapa
    Tn5, // 5. nimitapa
    Tt, // tositapa
    Te, // ehtotapa
    Tk, // käskytapa
    Tm, // mahtotapa

    // Laatutavat
    Rv, // -vA
    Ra, // -tAvA
    Ru, // -Ut
    Rt, // -ttU
    Rm, // -mA
    Re, // -mAtOn

    // Tekijät
    P1, // 1. persoona
    P2, // 2. persoona
    P3, // 3. persoona
    P4, // 4. persoona

    // Aikamuodot
    Ap, // kestämä (preesens)
    Ai, // kertoma (imperfekti)

    // Kieltomuodot (NEGATIVE-attribuutti)
    Et, // true
    Ef, // false
    Eb, // both

    // Johdoksiin liittyvät liput
    Dg, // erisnimestä johdetaan yleisnimi
    De, // erisnimen etuliitteenä kirjoitettava isolla alkukirjaimella

    // Lisätietoliput
    Ips, // paikannimi taipuu sisäpaikallissijoissa
    Ipu, // paikannimi taipuu ulkopaikallissijoissa
    Isf, // sitaattilaina
    Icu, // voidaan jatkaa yhdyssanana vain yhdysmerkin kanssa
    Ica, // sallitaan yhdysmerkin puuttuminen [Icu]-lipusta huolimatta lippua edeltävällä rajalla (muttei myöhemmin)
    Ivj, // vapaasti yhdistyvä yhdyssanan jälkiosa
    Ion, // organisaation nimenä voidaan kirjoittaa isolla alkukirjaimella
    Ira, // Verbiketju: vaatii A-infinitiivin
    Irm, // Verbiketju: vaatii MA-infinitiivin

    // Rajamerkit
    Bc, // arvaamalla tuotetun yhdyssanan osien välinen raja
    Bh, // yhdysmerkki vokaaliehdolla
    Bm; // tunnettu morfeemiraja

    public static @Nullable WordAttribute fromTag(@NotNull Symbol tag) {
        String rawTag = tag.toString();

        if (rawTag.charAt(0) != '[' || rawTag.charAt(rawTag.length() - 1) != ']')
            return null;

        try {
            return WordAttribute.valueOf(rawTag.substring(1, rawTag.length() - 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
