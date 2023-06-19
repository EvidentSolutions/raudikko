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

// https://github.com/voikko/corevoikko/blob/master/voikko-fi/vvfst/root.lexc
public final class Tags {

    // Boundaries
    public static final String PREFIX_B = "[B";
    static final @NotNull String bc = "[Bc]";
    static final @NotNull String bh = "[Bh]";
    static final @NotNull String bm = "[Bm]";

    // Comparative
    static final @NotNull String de = "[De]";
    static final @NotNull String dg = "[Dg]";

    // Clitic
    static final @NotNull String fko = "[Fko]";

    static final @NotNull String isf = "[Isf]";
    static final @NotNull String icu = "[Icu]";
    static final @NotNull String ica = "[Ica]";
    static final @NotNull String ion = "[Ion]";
    static final @NotNull String ivj = "[Ivj]";
    static final @NotNull String ira = "[Ira]";
    static final @NotNull String irm = "[Irm]";

    // Classes
    public static final String PREFIX_L = "[L";
    public static final String PREFIX_LE = "[Le";

    // Parameters
    public static final String PREFIX_X = "[X";
    public static final @NotNull String xp = "[Xp]"; // perusmuoto
    public static final @NotNull String xj = "[Xj]"; // johtimen perusmuoto
    static final @NotNull String xr = "[Xr]"; // rakenne
    static final @NotNull String xs = "[Xs]"; // sourceid
    public static final @NotNull String x = "[X]"; // end marker
}
