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

import fi.evident.raudikko.analysis.WordClass;
import fi.evident.raudikko.internal.fst.Symbol;
import org.jetbrains.annotations.NotNull;

import static java.lang.Character.isDigit;
import static java.lang.Character.toLowerCase;

final class Validator {

    private static final @NotNull String VOWELS = "aeiouyäö";

    // TODO: instead of separate validation step, we could just bail out from parsing if analysis is invalid
    public static boolean isValidAnalysis(@NotNull SymbolBuffer tokenizer) {
        char beforeLastChar = '\0';
        char lastChar = '\0';
        boolean boundaryPassed = false;
        boolean hyphenPresent = false;
        boolean hyphenUnconditionallyAllowed = false;
        boolean hyphenUnconditionallyAllowedJustSet = false;
        boolean hyphenRequired = false;
        boolean requiredHyphenMissing = false;
        boolean startsWithProperNoun = false;
        boolean endsWithNonIcaNoun = false;

        tokenizer.moveToStart();
        while (tokenizer.nextToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag != null) {
                if (tag.matches(Tags.isf)) {
                    hyphenUnconditionallyAllowed = true;
                    hyphenUnconditionallyAllowedJustSet = true;

                } else if (tag.matches(Tags.icu)) {
                    boundaryPassed = false;
                    hyphenUnconditionallyAllowed = true;
                    hyphenRequired = true;

                } else if (tag.matches(Tags.ica)) {
                    requiredHyphenMissing = false;
                    endsWithNonIcaNoun = false;

                } else if (tag.isNameTag()) {
                    startsWithProperNoun = true; // TODO starts?
                    endsWithNonIcaNoun = false;

                } else if (tag.matches(WordClass.NOUN) || tag.matches(WordClass.NOUN_ADJECTIVE)) {
                    endsWithNonIcaNoun = true;

                } else if (tag.matches(Tags.dg)) {
                    startsWithProperNoun = false;

                } else if (tag.isXParameter()) {
                    tokenizer.skipXTag();

                } else if (tag.matches(Tags.bh)) {
                    boundaryPassed = true;
                    hyphenPresent = false;

                    if (requiredHyphenMissing)
                        return false;

                    if (hyphenRequired)
                        requiredHyphenMissing = true;
                }
            } else {
                for (int i = 0, len = tokenizer.currentToken.length(); i < len; i++) {
                    char current = tokenizer.currentToken.charAt(i);
                    if (current == '-') {
                        startsWithProperNoun = false;
                        endsWithNonIcaNoun = false;

                        if (i == len - 1 && tokenizer.nextTokenIsTag(Tags.bh)) {
                            tokenizer.nextToken();
                            boundaryPassed = true;
                            hyphenPresent = true;
                        }
                    } else {
                        if (boundaryPassed) {
                            if (lastChar == '\0' || (beforeLastChar == 'i' && lastChar == 's'))
                                hyphenUnconditionallyAllowed = true;

                            if (hyphenPresent)
                                hyphenRequired = false;

                            if (!hyphenUnconditionallyAllowed || !hyphenPresent) {
                                lastChar = toLowerCase(lastChar);

                                boolean needHyphen = (lastChar == toLowerCase(current) && isVowel(lastChar)) || isDigit(lastChar);

                                if (needHyphen != hyphenPresent)
                                    return false;
                            }

                            boundaryPassed = false;

                            if (hyphenUnconditionallyAllowedJustSet)
                                hyphenUnconditionallyAllowedJustSet = false;
                            else
                                hyphenUnconditionallyAllowed = false;
                        }
                        beforeLastChar = lastChar;
                        lastChar = current;
                    }
                }
            }
        }

        return !requiredHyphenMissing && (!startsWithProperNoun || !endsWithNonIcaNoun);
    }

    private static boolean isVowel(char ch) {
        return VOWELS.indexOf(ch) != -1;
    }
}
