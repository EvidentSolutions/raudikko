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

import static fi.evident.raudikko.internal.utils.StringUtils.endsWithChar;

/*
 STRUCTURE
 =========
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
final class Structure {
    private Structure() {
    }

    public static @NotNull String parseStructure(@NotNull SymbolBuffer tokenizer, int wordLength) {
        StringBuilder structure = new StringBuilder(wordLength * 2);
        structure.append('=');

        int charsMissing = wordLength;
        int charsSeen = 0;
        int charsFromDefault = 0;
        boolean defaultTitleCase = false;
        boolean isAbbr = false;

        tokenizer.moveToStart();

        while (tokenizer.nextToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag != null) {
                if (tag.startsWith(Tags.PREFIX_B) && !tag.matches(Tags.bh)) {
                    if (tokenizer.getStart() == 1)
                        structure.append('=');

                    if (charsSeen > charsFromDefault) {
                        defaultTitleCase = createDefaultStructure(structure, charsSeen - charsFromDefault, defaultTitleCase, isAbbr);
                        charsMissing = decreaseCharsMissing(charsMissing, charsSeen, charsFromDefault);
                    }

                    // TODO: Why is 'tokenizer.getStart() + 5' necessary? Make the meaning clearer.
                    if (tokenizer.getStart() != 1 && tokenizer.getStart() + 5 < tokenizer.getTotalLength() && structure.length() != 0 && !endsWithChar(structure, '='))
                        structure.append('=');

                    charsSeen = 0;
                    charsFromDefault = 0;

                } else if (tag.startsWith(Tags.PREFIX_X) && !tag.matches(Tags.x)) {
                    if (tag.matches(Tags.xr)) {
                        defaultTitleCase = false;

                        String currentToken = tokenizer.readXTagContents();

                        for (int i = 0, len = currentToken.length(); i < len && charsMissing != 0; i++) {
                            char c = currentToken.charAt(i);
                            structure.append(c);
                            if (c != '=') {
                                charsFromDefault++;
                                if (c != '-')
                                    charsMissing--;
                            }
                        }

                    } else {
                        tokenizer.skipXTag();
                    }
                } else if (tag.startsWith(Tags.PREFIX_L)) {
                    if (tag.startsWith(Tags.PREFIX_LE)) {
                        defaultTitleCase = true;
                        isAbbr = false;
                    } else {
                        isAbbr = tag.matches(Tags.la) || tag.matches(Tags.lur) || (tag.matches(Tags.lu) && tokenizer.nextTokenStartsWithDigit());
                    }
                }
            } else {
                CharSequence currentToken = tokenizer.currentToken;
                for (int i = 0, len = currentToken.length(); i < len; i++) {
                    char c = currentToken.charAt(i);
                    switch (c) {
                        case '-':
                            if (charsSeen > charsFromDefault) {
                                defaultTitleCase =
                                        createDefaultStructure(structure,
                                                charsSeen - charsFromDefault,
                                                defaultTitleCase,
                                                isAbbr
                                        );
                                charsMissing = decreaseCharsMissing(charsMissing, charsSeen, charsFromDefault);
                                structure.append('-');
                                charsSeen = 0;
                                charsFromDefault = 0;
                            } else if (!tokenizer.isAtFirstToken() || i != 0) {
                                if (charsSeen == charsFromDefault)
                                    structure.append('-');
                                else
                                    charsSeen++;
                            }
                            if (charsMissing != 0)
                                charsMissing--;
                            if (structure.length() == 1)
                                structure.setCharAt(0, '-');
                            break;
                        case ':':
                            if (isAbbr) {
                                if (charsSeen > charsFromDefault) {
                                    defaultTitleCase = createDefaultStructure(structure, charsSeen - charsFromDefault, defaultTitleCase, isAbbr);
                                    charsMissing = decreaseCharsMissing(charsMissing, charsSeen, charsFromDefault);
                                    charsSeen = 0;
                                    charsFromDefault = 0;
                                }
                                isAbbr = false;
                            }
                            structure.append(':');
                            if (charsMissing != 0)
                                charsMissing--;
                            break;
                        default:
                            charsSeen++;
                            break;
                    }
                }
            }
        }

        createDefaultStructure(structure, charsMissing, defaultTitleCase, isAbbr);
        capitalizeStructure(structure, tokenizer);

        return structure.toString();
    }

    private static int decreaseCharsMissing(int charsMissing, int charsSeen, int charsFromDefault) {
        if (charsSeen - charsFromDefault <= charsMissing) {
            return charsMissing - (charsSeen - charsFromDefault);
        } else {
            // lexicon error: something wrong with fstOutput
            assert false;
            return charsMissing;
        }
    }

    private static boolean createDefaultStructure(@NotNull StringBuilder sb, int charsMissing, boolean defaultTitleCase, boolean abbr) {
        boolean titleCase = defaultTitleCase;

        for (int i = 0; i < charsMissing; i++) {
            if (titleCase) {
                sb.append(abbr ? 'j' : 'i');
                titleCase = false;
            } else {
                sb.append(abbr ? 'q' : 'p');
            }
        }
        return titleCase;
    }

    private static void capitalizeStructure(@NotNull StringBuilder structure, @NotNull SymbolBuffer tokenizer) {
        boolean isDe = false;
        int hyphenCount = 0;

        tokenizer.moveToStart();

        while (tokenizer.nextToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag != null) {
                if (tag.matches(Tags.dg)) {
                    int hyphensInStructure = 0;

                    for (int i = 0, len = structure.length(); i < len; i++)
                        if (structure.charAt(i) == 'i') {
                            if (hyphensInStructure == hyphenCount)
                                structure.setCharAt(i, 'p');

                        } else if (structure.charAt(i) == '-')
                            hyphensInStructure++;

                } else if (tag.matches(Tags.de))
                    isDe = true;
                else if (tag.matches(Tags.ln))
                    isDe = false;

            } else {
                CharSequence token = tokenizer.currentToken;
                for (int i = 0, len = token.length(); i < len; i++) {
                    char c = token.charAt(i);
                    if (c == '-') {
                        hyphenCount++;

                        if (isDe) {
                            boolean hasLep = tokenizer.containsTagAfterCurrent(Tags.lep);
                            if (hasLep)
                                tokenizer.skipUntilTag(Tags.lep);

                            if (hasLep || tokenizer.isAtLastToken())
                                for (int k = 0; k < structure.length(); k++)
                                    if (structure.charAt(k) == 'i' || structure.charAt(k) == 'p') {
                                        structure.setCharAt(k, 'i');
                                        return;
                                    }
                        }
                    }
                }
            }
        }
    }
}
