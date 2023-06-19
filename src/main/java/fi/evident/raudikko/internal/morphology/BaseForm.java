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
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

import static fi.evident.raudikko.analysis.WordClass.*;
import static fi.evident.raudikko.internal.utils.StringUtils.*;

final class BaseForm {

    private BaseForm() {
    }

    static @Nullable String parseBaseform(@NotNull SymbolBuffer tokenizer, @NotNull Structure structure) {
        StringBuilder baseform = new StringBuilder(tokenizer.getTotalLength());
        @Nullable String latestBaseForm = null;
        int latestXpStartInBaseform = 0;
        int hyphensInLatestXp = 0;
        boolean allowDe = true;
        boolean isInsideDe = false;
        boolean classTagSeen = false;

        Iterator<Structure.StructureSymbol> structureIterator = structure.nonMorphemes();

        tokenizer.moveToStart();
        while (tokenizer.nextToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag != null) {
                if (tag.isBaseFormTag()) {
                    latestXpStartInBaseform = baseform.length();

                    latestBaseForm = withoutChar(tokenizer.readXTagContents(), '=');
                    hyphensInLatestXp = countOccurrences(latestBaseForm, '-');

                } else if (tag.isXParameter()) {
                    tokenizer.skipXTag();

                } else if (tag.matches(Tags.de)) {
                    isInsideDe = allowDe;

                } else if (tag.isClassTag()) {

                    // We will try completely different rules here and get back if it does not work out
                    if (!classTagSeen && tag.matches(NUMERAL)) {
                        String numeralBaseform = parseNumeralBaseform(tokenizer.copy());
                        if (numeralBaseform != null) {
                            baseform.append(numeralBaseform);
                            return baseform.toString();
                        }
                    }

                    classTagSeen = true;
                    isInsideDe = false;
                    allowDe = tag.matches(ADJECTIVE) || tag.matches(NOUN_ADJECTIVE);
                }
            } else {
                SymbolBuffer.CurrentToken token = tokenizer.currentToken;

                for (int i = 0, len = token.length(); i < len; i++) {
                    char nextChar = token.charAt(i);
                    if (nextChar == '-') {
                        if (hyphensInLatestXp > 0)
                            hyphensInLatestXp--;
                        else {
                            // Compound place name such as "Isolla-Britannialla" needs to have "Isolla" replaced with "Iso".
                            // However, "-is" is never replaced with "-nen" ("Pohjois-Suomella").
                            if (isInsideDe && latestBaseForm != null && !token.matchesAt(i - 2, "is-") && tokenizer.containsTagAfterCurrent(TOPONYM)) {
                                baseform.setLength(latestXpStartInBaseform);
                                baseform.append(capitalize(latestBaseForm));
                            }
                            latestBaseForm = null;
                        }
                        isInsideDe = false;
                    }

                    baseform.append(structureIterator.hasNext() ? structureIterator.next().convert(nextChar) : nextChar);
                }
            }
        }

        if (latestBaseForm != null) {
            baseform.setLength(latestXpStartInBaseform);
            baseform.append(latestBaseForm);
        }

        return baseform.length() == 0 ? null : baseform.toString();
    }

    private static @Nullable String parseNumeralBaseform(@NotNull SymbolBuffer tokenizer) {
        boolean isInDigitSequence = false;
        boolean xpPassed = false;
        StringBuilder baseform = new StringBuilder();

        boolean first = true;
        while (tokenizer.nextToken()) {
            if (first && (tokenizer.currentToken.startsWithDigit() || tokenizer.currentToken.startsWithChar('-')))
                isInDigitSequence = true;

            first = false;

            Symbol tag = tokenizer.getCurrentTag();
            if (tag != null) {
                if (isInDigitSequence) {
                    isInDigitSequence = false;
                    xpPassed = true;
                }

                if (tag.isBaseFormTag()) {
                    baseform.append(tokenizer.readXTagContents());
                    xpPassed = true;

                } else if (tag.isXParameter()) {
                    tokenizer.skipXTag();

                } else if (tag.matches(Tags.bc)) {
                    if (tokenizer.isAtLastToken())
                        return null; // incomplete numeral is really a prefix
                    xpPassed = false;

                } else if (tag.matches(NOUN) || tag.matches(ADJECTIVE) || tag.matches(NOUN_ADJECTIVE)) {
                    return null; // give up and return to standard algorithm
                }
            } else if (isInDigitSequence || !xpPassed) {
                baseform.append(tokenizer.currentToken);

            } else {
                SymbolBuffer.CurrentToken token = tokenizer.currentToken;
                for (int i = 0, len = token.length(); i < len; i++)
                    if (token.charAt(i) == '-' && i + 1 < len)
                        baseform.append(token.charAt(++i));
            }
        }
        return baseform.toString();
    }
}
