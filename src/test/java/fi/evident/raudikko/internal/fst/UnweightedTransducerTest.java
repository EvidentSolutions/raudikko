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

package fi.evident.raudikko.internal.fst;

import fi.evident.raudikko.internal.morphology.SymbolBuffer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class UnweightedTransducerTest {

    private UnweightedTransducer transducer;

    @BeforeAll
    public void setup() throws Exception {
        try (InputStream stream = UnweightedTransducer.class.getResourceAsStream("/morpho/5/mor-morpho/mor.vfst")) {
            if (stream == null) throw new FileNotFoundException("could not load morphology");

            transducer = UnweightedVfstLoader.load(stream);
        }
    }

    private @NotNull List<String> transduce(@NotNull String word) {
        Symbol[] output = new Symbol[2000];
        SymbolBuffer buffer = new SymbolBuffer(2000);
        ArrayList<Symbol> inputBuffer = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();
        short[] flags = new short[transducer.flagDiacriticFeatureCount];

        transducer.transduce(word, inputBuffer, flags, output, depth -> {
            buffer.reset(output, depth);
            result.add(buffer.fullContents());
        });

        return result;
    }

    private void assertSingle(@NotNull String word, @NotNull String expected) {
        List<String> result = transduce(word);
        if (result.size() != 1)
            fail("expected single result, got " + result.size() + ": " + result);

        assertEquals(expected, result.get(0));
    }

    private void assertMatches(@NotNull String word, @NotNull Set<String> expected) {
        assertEquals(expected, new HashSet<>(transduce(word)));
    }

    @Test
    void baseForm() {
        assertSingle("kissoille", "[Ln][Xp]kissa[X]kisso[Sall][Nm]ille");
    }

    @Test
    void baseFormForCompoundWord1() {
        assertSingle("vatsaneläkeruokaa", "[Ln][Xp]vatsa[X]vats[Sg][Ny]an[Bh][Bc][Ln][Xp]eläke[X]eläke[Sn][Ny][Bh][Bc][Ln][Xp]ruoka[X]ruok[Sp][Ny]aa");
    }

    @Test
    void baseFormForCompoundWord2() {
        assertSingle("hevosrakenteinen", "[Ln][Xp]hevonen[X]hevos[Bh][Bc][Ll][Xp]rakenteinen[X]rakentei[Sn][Ny]nen");
    }


    @Test
    void baseFormForNounDerivedFromVerb() {
        assertSingle("hyppijässä", "[Lt][Xp]hyppiä[X]hyppi[Ln][Xj]jä[X][Sine][Ny]jässä");
    }

    @Test
    void baseFormForNumeral() {
        assertSingle("kahdellakymmenelläseitsemällä", "[Lu][Xp]kaksi[X]ka[Sade][Ny]hdella[Bc][Lu][Xp]kymmentä[X][Sade][Ny]kymmenellä[Bc][Lu][Xp]seitsemän[X]seitsem[Sade][Ny]ällä");
    }

    @Test
    void baseFormForOrdinal() {
        assertMatches("kahdennellakymmenennellä", setOf("[Lu][Xp]kahdes[X]kahde[Sade][Ny]nnella[Bc][Lu][Bc][Xp]kymmenes[X][Sade][Ny]kymmenennellä", "[Lu][Xp]kahdes[X]kahde[Sade][Ny]nnella[Bc][Xp]kymmenes[X][Sade][Ny]kymmenennellä"));
        assertMatches("kolmannellasadannella", setOf("[Lu][Xp]kolmas[X]kolma[Sade][Ny]nnella[Bc][Lu][Bc][Xp]sadas[X][Sade][Ny]sadannella", "[Lu][Xp]kolmas[X]kolma[Sade][Ny]nnella[Bc][Xp]sadas[X][Sade][Ny]sadannella"));
        assertMatches("kolmannellamiljoonannella", setOf("[Lu][Xp]kolmas[X]kolma[Sade][Ny]nnella[Bc][Lu][Bc][Xp]miljoonas[X][Sade][Ny]miljoonannella", "[Lu][Xp]kolmas[X]kolma[Sade][Ny]nnella[Bc][Xp]miljoonas[X][Sade][Ny]miljoonannella"));
    }

    @Test
    void baseFormForWordHavingNoInflections() {
        assertSingle("kohti", "[Ld][Xp]kohti[X]kohti");
    }

    @Test
    void baseFormForCompoundProperNoun() {
        assertSingle("Outi-Marjukka", "[Lee][Xp]Outi[X]out[Sn][Ny]i-[Bc][Lee][Xp]Marjukka[X]marjukk[Sn][Ny]a");
    }

    @Test
    void baseFormForCapitalizedWord() {
        assertSingle("kissa", "[Ln][Xp]kissa[X]kiss[Sn][Ny]a");
    }

    @SafeVarargs
    private static <T> Set<T> setOf(T... args) {
        return new HashSet<>(Arrays.asList(args));
    }
}
