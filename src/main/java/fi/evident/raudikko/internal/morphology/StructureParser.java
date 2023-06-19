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

import fi.evident.raudikko.analysis.Structure;
import fi.evident.raudikko.internal.fst.Symbol;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.raudikko.analysis.Structure.StructureSymbol.*;
import static fi.evident.raudikko.analysis.WordClass.*;
import static java.lang.Math.max;

final class StructureParser {

    private StructureParser() {
    }

    public static @NotNull Structure parseStructure(@NotNull SymbolBuffer tokenizer, int wordLength) {
        StructureBuilder structure = new StructureBuilder(wordLength * 2);
        structure.add(MORPHEME_START);

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
                    if (tokenizer.getCurrentOffset() == 1)
                        structure.add(MORPHEME_START);

                    if (charsSeen > charsFromDefault) {
                        defaultTitleCase = createDefaultStructure(structure, charsSeen - charsFromDefault, defaultTitleCase, isAbbr);
                        charsMissing = decreaseCharsMissing(charsMissing, charsSeen, charsFromDefault);
                    }

                    // TODO: Why is 'tokenizer.getCurrentOffset() + 5' necessary? Make the meaning clearer.
                    if (tokenizer.getCurrentOffset() != 1 && tokenizer.getCurrentOffset() + 5 < tokenizer.getTotalLength())
                        structure.ensureEndsWithNewMorpheme();

                    charsSeen = 0;
                    charsFromDefault = 0;

                } else if (tag.startsWith(Tags.PREFIX_X) && !tag.matches(Tags.x)) {
                    if (tag.matches(Tags.xr)) {
                        defaultTitleCase = false;

                        String currentToken = tokenizer.readXTagContents();

                        for (int i = 0, len = currentToken.length(); i < len && charsMissing != 0; i++) {
                            Structure.StructureSymbol c = Structure.StructureSymbol.forCode(currentToken.charAt(i));
                            structure.add(c);
                            if (c != MORPHEME_START) {
                                charsFromDefault++;
                                if (c != HYPHEN)
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
                        isAbbr = tag.matches(ABBREVIATION) || tag.matches(NUMERAL_ROMAN) || (tag.matches(NUMERAL) && tokenizer.nextTokenStartsWithDigit());
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
                                structure.add(HYPHEN);
                                charsSeen = 0;
                                charsFromDefault = 0;
                            } else if (!tokenizer.isAtFirstToken() || i != 0) {
                                if (charsSeen == charsFromDefault)
                                    structure.add(HYPHEN);
                                else
                                    charsSeen++;
                            }
                            if (charsMissing != 0)
                                charsMissing--;
                            if (structure.size() == 1)
                                structure.replaceStartWithHyphen();
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
                            structure.add(COLON);
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

        return structure.build();
    }

    private static int decreaseCharsMissing(int charsMissing, int charsSeen, int charsFromDefault) {
        return max(0, charsMissing - (charsSeen - charsFromDefault));
    }

    private static boolean createDefaultStructure(@NotNull StructureBuilder result, int charsMissing, boolean titleCase, boolean abbr) {
        if (charsMissing == 0)
            return titleCase;

        if (titleCase) {
            result.add(abbr ? UPPERCASE_NO_HYPHENATION : UPPERCASE);
            charsMissing--;
        }

        result.add(abbr ? LOWERCASE_NO_HYPHENATION : LOWERCASE, charsMissing);

        return false;
    }

    private static void capitalizeStructure(@NotNull StructureBuilder structure, @NotNull SymbolBuffer tokenizer) {
        boolean isDe = false;
        int hyphenCount = 0;

        tokenizer.moveToStart();

        while (tokenizer.nextToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag != null) {
                if (tag.matches(Tags.dg)) {
                    int hyphensInStructure = 0;

                    for (int i = 0, len = structure.size(); i < len; i++)
                        if (structure.get(i) == UPPERCASE) {
                            if (hyphensInStructure == hyphenCount)
                                structure.set(i, LOWERCASE);

                        } else if (structure.get(i) == HYPHEN)
                            hyphensInStructure++;

                } else if (tag.matches(Tags.de))
                    isDe = true;
                else if (tag.matches(NOUN))
                    isDe = false;

            } else {
                CharSequence token = tokenizer.currentToken;
                for (int i = 0, len = token.length(); i < len; i++) {
                    char c = token.charAt(i);
                    if (c == '-') {
                        hyphenCount++;

                        if (isDe) {
                            boolean hasPlace = tokenizer.containsTagAfterCurrent(TOPONYM);
                            if (hasPlace)
                                tokenizer.skipUntil(TOPONYM);

                            if (hasPlace || tokenizer.isAtLastToken())
                                for (int k = 0; k < structure.size(); k++) {
                                    Structure.StructureSymbol sym = structure.get(k);
                                    if (sym == UPPERCASE)
                                        return;
                                    else if (sym == LOWERCASE) {
                                        structure.set(k, UPPERCASE);
                                        return;
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    private static final class StructureBuilder {
        private final @NotNull List<Structure.StructureSymbol> symbols;

        StructureBuilder(int capacity) {
            this.symbols = new ArrayList<>(capacity);
        }

        public void add(@NotNull Structure.StructureSymbol symbol) {
            symbols.add(symbol);
        }

        public void add(@NotNull Structure.StructureSymbol symbol, int count) {
            for (int i = 0; i < count; i++)
                add(symbol);
        }

        public void ensureEndsWithNewMorpheme() {
            if (symbols.get(symbols.size() - 1) != MORPHEME_START)
                add(MORPHEME_START);
        }

        public int size() {
            return symbols.size();
        }

        public void replaceStartWithHyphen() {
            symbols.set(0, HYPHEN);
        }

        public @NotNull Structure build() {
            return new Structure(symbols);
        }

        public @NotNull Structure.StructureSymbol get(int i) {
            return symbols.get(i);
        }

        public void set(int i, @NotNull Structure.StructureSymbol symbol) {
            symbols.set(i, symbol);
        }
    }
}
