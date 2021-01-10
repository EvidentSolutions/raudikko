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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SymbolBufferTest {

    @Test
    void tokenizeVfstOutput() {
        SymbolBuffer buffer = SymbolBuffer.parse("[Lee][Xp]Outi[X]out[Sn][Ny]i-[Bc][Lee][Xp]Marjukka[X]marjukk[Sn][Ny]a");
        List<String> expectedTokens = Arrays.asList("[Lee]", "[Xp]", "Outi", "[X]", "out", "[Sn]", "[Ny]", "i-", "[Bc]", "[Lee]", "[Xp]", "Marjukka", "[X]", "marjukk", "[Sn]", "[Ny]", "a");

        ArrayList<String> tokens = new ArrayList<>();
        while (buffer.nextToken())
            tokens.add(buffer.currentToken.toString());

        assertEquals(expectedTokens, tokens);

        ArrayList<String> reverseTokens = new ArrayList<>();
        buffer.moveToEnd();
        while (buffer.previousToken())
            reverseTokens.add(buffer.currentToken.toString());

        assertEquals(reverse(expectedTokens), reverseTokens);
    }

    private static <T> @NotNull List<T> reverse(@NotNull List<T> list) {
        ArrayList<T> copy = new ArrayList<>(list);
        Collections.reverse(copy);
        return copy;
    }
}
