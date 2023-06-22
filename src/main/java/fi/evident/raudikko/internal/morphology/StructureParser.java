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
import fi.evident.raudikko.analysis.Structure.StructureSymbol;
import fi.evident.raudikko.internal.fst.Symbol;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.raudikko.analysis.Structure.StructureSymbol.*;
import static fi.evident.raudikko.analysis.WordClass.*;

final class StructureParser {

    private StructureParser() {
    }

    public static @NotNull Structure parseStructure(@NotNull SymbolBuffer tokenizer, int wordLength) {
        StructureBuilder structure = new StructureBuilder(wordLength * 2);

        int charsMissing = wordLength;
        int charsSeen = 0;
        int charsFromDefault = 0;
        boolean defaultTitleCase = false;
        boolean isAbbr = false;

        tokenizer.moveToStart();

        while (tokenizer.nextToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag != null) {
                if (tag.matches(Tags.bc) || tag.matches(Tags.bm)) {
                    if (tokenizer.getCurrentOffset() == 1)
                        structure.add(MORPHEME_START);

                    if (charsSeen > charsFromDefault) {
                        defaultTitleCase = createDefaultStructure(structure, charsSeen - charsFromDefault, defaultTitleCase, isAbbr);
                        charsMissing -= charsSeen - charsFromDefault;
                    }

                    // TODO: Why is 'tokenizer.getCurrentOffset() + 5' necessary? Make the meaning clearer.
                    if (tokenizer.getCurrentOffset() != 1 && tokenizer.getCurrentOffset() + 5 < tokenizer.getTotalLength())
                        structure.ensureEndsWithNewMorpheme();

                    charsSeen = 0;
                    charsFromDefault = 0;

                } else if (tag.matches(Tags.xr)) {
                    defaultTitleCase = false;

                    List<StructureSymbol> structureSymbols = tokenizer.readStructure();

                    for (StructureSymbol c : structureSymbols) {
                        if (charsMissing == 0)
                            break;

                        structure.add(c);
                        if (c != MORPHEME_START) {
                            charsFromDefault++;
                            if (c != HYPHEN)
                                charsMissing--;
                        }
                    }

                } else if (tag.isXParameter()) {
                    tokenizer.skipXTag();

                } else if (tag.isNameTag()) {
                    defaultTitleCase = true;
                    isAbbr = false;

                } else if (tag.isClassTag()) {
                    isAbbr = tag.matches(ABBREVIATION) || tag.matches(NUMERAL_ROMAN) || (tag.matches(NUMERAL) && tokenizer.nextTokenStartsWithDigit());
                }
            } else {
                SymbolBuffer.CurrentToken currentToken = tokenizer.currentToken;
                for (int i = 0, len = currentToken.length(); i < len; i++) {
                    char c = currentToken.charAt(i);
                    switch (c) {
                        case '-' -> {
                            if (charsSeen > charsFromDefault) {
                                defaultTitleCase =
                                    createDefaultStructure(structure,
                                        charsSeen - charsFromDefault,
                                        defaultTitleCase,
                                        isAbbr
                                    );
                                charsMissing -= charsSeen - charsFromDefault;
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
                        }
                        case ':' -> {
                            if (isAbbr) {
                                if (charsSeen > charsFromDefault) {
                                    defaultTitleCase = createDefaultStructure(structure, charsSeen - charsFromDefault, defaultTitleCase, true);
                                    charsMissing -= charsSeen - charsFromDefault;
                                    charsSeen = 0;
                                    charsFromDefault = 0;
                                }
                                isAbbr = false;
                            }
                            structure.add(COLON);
                            if (charsMissing != 0)
                                charsMissing--;
                        }
                        default -> charsSeen++;
                    }
                }
            }
        }

        createDefaultStructure(structure, charsMissing, defaultTitleCase, isAbbr);
        capitalizeStructure(structure, tokenizer);

        return structure.build();
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
        int totalHyphens = 0;

        tokenizer.moveToStart();

        while (tokenizer.nextToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag != null) {
                if (tag.matches(Tags.dg))
                    structure.changeToLowerCaseAtHyphenIndex(totalHyphens);
                else if (tag.matches(Tags.de))
                    isDe = true;
                else if (tag.matches(NOUN))
                    isDe = false;

            } else {
                int hyphens = tokenizer.currentToken.count('-');

                if (hyphens > 0 && isDe) {
                    if (tokenizer.containsTagAfterCurrent(TOPONYM) || tokenizer.isAtLastToken()) {
                        structure.capitalize();
                        return;
                    }
                }

                totalHyphens += hyphens;
            }
        }
    }

    private static final class StructureBuilder {

        /**
         * The symbols gathered so far.
         * <p>
         * Note that the constructor always adds a MORPHEME_START and other methods only add or replace symbols.
         * Therefore, the list can never be empty and the following code relies on that.
         */
        private final @NotNull List<StructureSymbol> symbols;

        StructureBuilder(int capacity) {
            this.symbols = new ArrayList<>(capacity);
            this.symbols.add(MORPHEME_START);
        }

        public void add(@NotNull StructureSymbol symbol) {
            symbols.add(symbol);
        }

        public void add(@NotNull StructureSymbol symbol, int count) {
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

        public void set(int i, @NotNull StructureSymbol symbol) {
            symbols.set(i, symbol);
        }

        /**
         * Ensure that the first letter token is upper-case.
         */
        private void capitalize() {
            // TODO: The code does not handle UPPERCASE_NO_HYPHENATION and LOWERCASE_NO_HYPHENATION, which is somewhat
            //       suspicious. They probably can't occur in the call-path, but it's still a bit sketchy.
            for (int i = 0; i < symbols.size(); i++) {
                StructureSymbol sym = symbols.get(i);
                if (sym == UPPERCASE)
                    break;
                else if (sym == LOWERCASE) {
                    symbols.set(i, UPPERCASE);
                    break;
                }
            }
        }

        private void changeToLowerCaseAtHyphenIndex(int hyphenIndex) {
            int seenHyphens = 0;

            for (int i = 0, len = size(); i < len; i++) {
                StructureSymbol current = symbols.get(i);
                if (current == UPPERCASE) {
                    if (seenHyphens == hyphenIndex)
                        set(i, LOWERCASE);

                } else if (current == HYPHEN)
                    seenHyphens++;
            }
        }
    }
}
