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
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static fi.evident.raudikko.internal.morphology.BaseForm.parseBaseform;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseFormParserTest {

    @Test
    void baseForms() {
        assertEquals("kissa", parse("[Ln][Xp]kissa[X]kisso[Sall][Nm]ille", "ppppppppp"));
        assertEquals("vatsaneläkeruoka", parse("[Ln][Xp]vatsa[X]vats[Sg][Ny]an[Bh][Bc][Ln][Xp]eläke[X]eläke[Sn][Ny][Bh][Bc][Ln][Xp]ruoka[X]ruok[Sp][Ny]aa", "pppppp=ppppp=pppppp"));
        assertEquals("hevosrakenteinen", parse("[Ln][Xp]hevonen[X]hevos[Bh][Bc][Ll][Xp]rakenteinen[X]rakentei[Sn][Ny]nen", "ppppp=ppppppppppp"));
        assertEquals("hyppijä", parse("[Lt][Xp]hyppiä[X]hyppi[Ln][Xj]jä[X][Sine][Ny]jässä", "pppppppppp"));
    }

    @Test
    void numeralBaseForms() {
        assertEquals("kaksikymmentäseitsemän", parse("[Lu][Xp]kaksi[X]ka[Sade][Ny]hdella[Bc][Lu][Xp]kymmentä[X][Sade][Ny]kymmenellä[Bc][Lu][Xp]seitsemän[X]seitsem[Sade][Ny]ällä", "pppppppp=pppppppppp=ppppppppppp"));

        assertEquals("kahdeskymmenes", parse("[Lu][Xp]kahdes[X]kahde[Sade][Ny]nnella[Bc][Xp]kymmenes[X][Sade][Ny]kymmenennellä", "ppppppppppp=ppppppppppppp"));
        assertEquals("kolmassadas", parse("[Lu][Xp]kolmas[X]kolma[Sade][Ny]nnella[Bc][Xp]sadas[X][Sade][Ny]sadannella", "ppppppppppp=pppppppppp"));
        assertEquals("kolmasmiljoonas", parse("[Lu][Xp]kolmas[X]kolma[Sade][Ny]nnella[Bc][Lu][Bc][Xp]miljoonas[X][Sade][Ny]miljoonannella", "ppppppppppp=pppppppppppppp"));

        assertEquals("200", parse("[Lu]200[Sela][Ny]:sta", "=qqq:ppp"));
        assertEquals("-200", parse("-[Bc][Lu]200[Sela][Ny]:sta", "-=qqq:ppp"));
    }

    private static @Nullable String parse(@NotNull String fstOutput, @NotNull String structure) {
        return parseBaseform(SymbolBuffer.parse(fstOutput), structure);
    }
}
