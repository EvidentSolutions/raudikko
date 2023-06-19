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

package fi.evident.raudikko.analysis;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static fi.evident.raudikko.analysis.Structure.StructureSymbol.MORPHEME_START;
import static fi.evident.raudikko.internal.utils.CollectionUtils.count;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

/**
 * This class describes morpheme boundaries, character case and hyphenation restrictions for the word.
 *
 * <table>
 *     <caption>Examples</caption>
 *     <tr>
 *         <td>Matti-niminen</td>
 *         <td>=ipppp-=ppppppp</td>
 *     </tr>
 *     <tr>
 *         <td>DNA-näyte</td>
 *         <td>=jjj-=ppppp</td>
 *     </tr>
 *     <tr>
 *         <td>autokauppa</td>
 *         <td>=pppp=pppppp</td>
 *     </tr>
 * </table>
 */
public final class Structure {

    private final @NotNull List<StructureSymbol> structure;

    public Structure(@NotNull List<StructureSymbol> structure) {
        if (structure.isEmpty()) throw new IllegalArgumentException("empty structure");

        this.structure = structure;
    }

    @Override
    public String toString() {
        return structure.stream().map(it -> String.valueOf(it.code)).collect(Collectors.joining(""));
    }

    public int getMorphemeCount() {
        return count(structure, MORPHEME_START);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return structure.equals(((Structure) o).structure);
    }

    @Override
    public int hashCode() {
        return structure.hashCode();
    }

    /**
     * Returns a structure that is otherwise identical to this one, but starts with a capital letter.
     */
    public @NotNull Structure capitalized() {
        if (structure.size() <= 1 || structure.get(1) == StructureSymbol.UPPERCASE)
            return this;

        List<StructureSymbol> copy = new ArrayList<>(structure);
        copy.set(1, StructureSymbol.UPPERCASE);
        return new Structure(copy);
    }

    /**
     * Returns al the token symbols of this structure (i.e. excludes morpheme start).
     */
    public @NotNull Iterator<StructureSymbol> nonMorphemes() {
        return structure.stream().filter(it -> it != MORPHEME_START).iterator();
    }

    /**
     * Converts given word to follow this structure.
     */
    public @NotNull String apply(@NotNull CharSequence word) {
        Iterator<StructureSymbol> it = nonMorphemes();

        StringBuilder sb = new StringBuilder(word.length());

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            sb.append(it.hasNext() ? it.next().convert(c) : c);
        }

        return sb.toString();
    }

    public enum StructureSymbol {

        /**
         * Start of a new morpheme. This must also be present at the start of a word.
         */
        MORPHEME_START('='),

        /**
         * Letter that is written in upper case in the standard form.
         */
        UPPERCASE('i'),

        /**
         * Letter that is written in upper case in the standard form. Hyphenation is forbidden before this letter.
         */
        UPPERCASE_NO_HYPHENATION('j'),

        /**
         * Letter that is written in lower case in the standard form.
         */
        LOWERCASE('p'),

        /**
         * Letter that is written in lower case in the standard form. Hyphenation is forbidden before this letter.
         */
        LOWERCASE_NO_HYPHENATION('q'),

        /**
         * Hyphen. Word can be split in text processors after this character without inserting an extra hyphen.
         * If the hyphen is at morpheme boundary, the boundary symbol = must be placed after the hyphen.
         */
        HYPHEN('-'),

        /** Colon in the word. */
        COLON(':');

        final char code;

        StructureSymbol(char code) {
            this.code = code;
        }

        public static @NotNull StructureSymbol forCode(char c) {
            for (StructureSymbol symbol : values())
                if (symbol.code == c)
                    return symbol;

            throw new IllegalArgumentException("unknown structure-code" + c);
        }

        public char convert(char c) {
            return isUpperCase() ? toUpperCase(c)
                : isLowerCase() ? toLowerCase(c)
                : c;
        }

        public boolean agrees(char ch) {
            switch (this) {
                case MORPHEME_START:
                    return true;
                case UPPERCASE:
                case UPPERCASE_NO_HYPHENATION:
                    return !Character.isLowerCase(ch);
                case LOWERCASE:
                case LOWERCASE_NO_HYPHENATION:
                    return !Character.isUpperCase(ch);
                case HYPHEN:
                    return ch == '-';
                case COLON:
                    return ch == ':';
                default:
                    throw new IllegalStateException("Unexpected type: " + this);
            }
        }

        public boolean isLowerCase() {
            return this == LOWERCASE || this == LOWERCASE_NO_HYPHENATION;
        }

        public boolean isUpperCase() {
            return this == UPPERCASE || this == UPPERCASE_NO_HYPHENATION;
        }
    }
}
