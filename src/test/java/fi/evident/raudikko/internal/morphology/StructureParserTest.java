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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructureParserTest {

    @Test
    void structures() {
        assertEquals("=qj", parseStructure("[La][Xr]qj[X][Xp]mA[X]ma[Sn][Ny]", 2));
        assertEquals("=jj-=ppppp", parseStructure("[La][Xr]jj[X][Xp]EU[X]eu-[Bc][Ln][Xp]kieli[X]kiel[Sn][Ny]i", 8));
        assertEquals("=ipppppp-", parseStructure("[Lp][Xp]Pohjois[X][Xr]i[X]pohjois[Bc]-", 8));
    }

    @Test
    void trailingDash() {
        assertEquals("=ipppppp-", parseStructure("[Lp][Xp]Pohjois[X][Xr]i[X]pohjois[Bc]-", 8));
        assertEquals("-=pppppppp-", parseStructure("-[Bc][Lt][Xp]säveltää[X]sävell[Ln][Xj]ys[X]y[Sn][Ny]s-[Bh][Bc]", 10));
    }

    @Test
    void capitalization() {
        assertEquals("=ipppppp-", parseStructure("[Lp]lounais[De]-", 8));
        assertEquals("=ppppp-", parseStructure("[Lp][Xp]keski[X]keski[Bc]-", 6));
    }

    private static @NotNull String parseStructure(@NotNull String s, int len) {
        return StructureParser.parseStructure(SymbolBuffer.parse(s), len).toString();
    }
}
