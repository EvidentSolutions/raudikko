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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.raudikko.internal.utils.StringUtils.*;
import static java.lang.Character.isDigit;
import static java.lang.Character.toUpperCase;

final class BaseForm {

    private BaseForm() {
    }

    static @Nullable String parseBaseform(@NotNull SymbolBuffer tokenizer, @NotNull String structure) {
        StringBuilder baseform = new StringBuilder(tokenizer.getTotalLength());
        @Nullable String latestBaseForm = null;
        int latestXpStartInBaseform = 0;
        int hyphensInLatestXp = 0;
        boolean ignoreNextDe = false;
        boolean isDe = false;
        boolean classTagSeen = false;

        StructureIterator structureIterator = new StructureIterator(structure);

        tokenizer.moveToStart();
        while (tokenizer.nextToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag != null) {
                if (tag.isBaseFormTag()) {
                    latestXpStartInBaseform = baseform.length();

                    latestBaseForm = withoutChar(tokenizer.readXTagContents(), '=');
                    hyphensInLatestXp += countOccurrences(latestBaseForm, '-');

                } else if (tag.startsWith(Tags.PREFIX_X))
                    tokenizer.skipXTag();

                else if (tag.matches(Tags.de))
                    isDe = !ignoreNextDe;

                else if (!classTagSeen && tag.matches(Tags.lu)) {
                    classTagSeen = true;
                    // we will try completely different rules here and get back if it does not work out
                    String numeralBaseform = parseNumeralBaseform(tokenizer.copy());
                    if (numeralBaseform != null) {
                        baseform.append(numeralBaseform);
                        return baseform.toString();
                    }
                } else if (tag.isClassTag()) {
                    classTagSeen = true;
                    isDe = false;
                    ignoreNextDe = !tag.matches(Tags.ll) && !tag.matches(Tags.lnl);
                }
            } else {
                CharSequence token = tokenizer.currentToken;

                for (int i = 0, len = token.length(); i < len; i++) {
                    char nextChar = token.charAt(i);
                    if (nextChar == '-') {
                        if (hyphensInLatestXp > 0)
                            hyphensInLatestXp--;
                        else {
                            // Compound place name such as "Isolla-Britannialla" needs to have "Isolla" replaced with "Iso".
                            // However "-is" is never replaced with "-nen" ("Pohjois-Suomella").
                            if (isDe && latestBaseForm != null && !matchesAt(token, i - 2, "is-") && tokenizer.containsTagAfterCurrent(Tags.lep)) {
                                baseform.setLength(latestXpStartInBaseform);
                                baseform.append(capitalize(latestBaseForm));
                            }
                            latestBaseForm = null;
                        }
                        isDe = false;
                    }

                    baseform.append(structureIterator.nextOutputCharUpperCased() ? toUpperCase(nextChar) : nextChar);
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
            CharSequence token = tokenizer.currentToken;
            if (first && (isDigit(token.charAt(0)) || token.charAt(0) == '-'))
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

                } else if (tag.startsWith(Tags.PREFIX_X)) {
                    tokenizer.skipXTag();

                } else if (tag.matches(Tags.bc)) {
                    if (tokenizer.isAtLastToken())
                        return null; // incomplete numeral is really a prefix
                    xpPassed = false;

                } else if (tag.matches(Tags.ln) || tag.matches(Tags.ll) || tag.matches(Tags.lnl)) {
                    return null; // give up and return to standard algorithm
                }
            } else if (isInDigitSequence || !xpPassed) {
                baseform.append(tokenizer.currentToken);

            } else {
                for (int i = 0, len = token.length(); i < len; i++)
                    if (token.charAt(i) == '-' && i + 1 < len)
                        baseform.append(token.charAt(++i));
            }
        }
        return baseform.toString();
    }

    static @NotNull List<String> parseBaseFormParts(@NotNull SymbolBuffer tokenizer) {
        tokenizer.moveToStart();
        return new BaseFormPartsParser().parse(tokenizer);
    }

    private static final class StructureIterator {

        private final @NotNull String structure;
        private int i = 0;

        StructureIterator(@NotNull String structure) {
            this.structure = structure;
            skip();
        }

        boolean nextOutputCharUpperCased() {
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
