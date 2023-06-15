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

import static fi.evident.raudikko.internal.utils.StringUtils.replaceCharAt;

/*
  This attribute describes morpheme boundaries, character case and
  hyphenation restrictions for the word. The following characters
  are used in the values of this attribute:

  = Start of a new morpheme. This must also be present at the start
    of a word.

  - Hyphen. Word can be split in text processors after this character
    without inserting an extra hyphen. If the hyphen is at morpheme
    boundary, the boundary symbol = must be placed after the hyphen.

  p Letter that is written in lower case in the standard form.

  q Letter that is written in lower case in the standard form.
    Hyphenation is forbidden before this letter.

  i Letter that is written in upper case in the standard form.

  j Letter that is written in upper case in the standard form.
    Hyphenation is forbidden before this letter.

  Examples:
   Word: Matti-niminen -> STRUCTURE: =ipppp-=ppppppp
   Word: DNA-näyte ->     STRUCTURE: =jjj-=ppppp
   Word: autokauppa ->    STRUCTURE: =pppp=pppppp
 */
public final class Structure {

    private final @NotNull String structure;

    public Structure(@NotNull String structure) {
        if (structure.isEmpty()) throw new IllegalArgumentException("empty structure");
        this.structure = structure;
    }

    @Override
    public String toString() {
        return structure;
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

    public @NotNull Structure capitalized() {
        return new Structure(structure.length() > 1 ? replaceCharAt(structure, 1, 'i') : structure);
    }

    public @NotNull StructureIterator structureIterator() {
        return new StructureIterator(structure);
    }

    public static final class StructureIterator {

        private final @NotNull String structure;
        private int i = 0;

        StructureIterator(@NotNull String structure) {
            this.structure = structure;
            skip();
        }

        public boolean nextOutputCharUpperCased() {
            if (i < structure.length()) {
                char c = structure.charAt(i++);
                skip();
                return c == 'i' || c == 'j';
            } else {
                return false;
            }
        }

        private void skip() {
            while (i < structure.length() && structure.charAt(i) == '=')
                i++;
        }
    }
}
