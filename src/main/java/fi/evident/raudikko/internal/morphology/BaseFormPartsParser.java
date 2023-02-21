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
import java.util.LinkedHashSet;
import java.util.List;

import static fi.evident.raudikko.internal.utils.StringUtils.withoutChar;

final class BaseFormPartsParser {

    private final @NotNull List<String> wordsInBoundaries = new ArrayList<>();
    private final @NotNull LinkedHashSet<String> baseFormParts = new LinkedHashSet<>();
    private final @NotNull List<String> compoundWordParts = new ArrayList<>();

    private @NotNull Word currentWord = new Word();
    private boolean skipAddingCurrentWord = false;

    public @NotNull List<String> parse(@NotNull SymbolBuffer tokenizer) {
        while (tokenizer.nextToken())
            atToken(tokenizer.getCurrentTag(), tokenizer);

        atBoundary(true);

        ArrayList<String> result = new ArrayList<>(baseFormParts.size() + compoundWordParts.size());
        result.addAll(baseFormParts);
        result.addAll(compoundWordParts);
        return result;
    }

    private void atToken(@Nullable Symbol tag, @NotNull SymbolBuffer tokenizer) {
        if (tag == null) {
            currentWord.append(withoutChar(tokenizer.currentToken, '-'));
        } else {
            if (tag.matches(Tags.xp)) {
                String part = withoutChar(tokenizer.readXTagContents(), '=');

                if (!part.isEmpty())
                    baseFormParts.add(part);

                if (currentWord.properNoun)
                    skipAddingCurrentWord = true;

            } else if (tag.matches(Tags.xj)) {
                baseFormParts.add(currentWord.toString() + withoutChar(tokenizer.readXTagContents(), '='));
                skipAddingCurrentWord = true;
            }

            else if (tag.matches(Tags.xr) || tag.matches(Tags.xs))
                tokenizer.readXTagContents();

            else if (tag.startsWith(Tags.PREFIX_LE))
                currentWord.properNoun = true;

            else if (tag.matches(Tags.sn))
                currentWord.nominative = true;

            else if (tag.matches(Tags.ny))
                currentWord.singular = true;

            else if (tag.startsWith(Tags.PREFIX_FK))
                currentWord.clitic = true;

            else if (tag.startsWith(Tags.PREFIX_O))
                currentWord.possessiveSuffix = true;

            else if (tag.startsWith(Tags.PREFIX_C))
                currentWord.comparative = true;

            else if (tag.isBoundary())
                atBoundary(false);
        }
    }

    private void atBoundary(boolean last) {
        if (currentWord.isNotEmpty()) {
            if (currentWord.isInBaseForm()) {
                if (!skipAddingCurrentWord) {
                    baseFormParts.add(currentWord.toString());
                }

                for (int i = last ? 1 : 0; i < wordsInBoundaries.size(); i++) {
                    List<String> subBoundaries = wordsInBoundaries.subList(i, wordsInBoundaries.size());
                    StringBuilder compoundWordBuilder = new StringBuilder();

                    for (int j = 0; j < subBoundaries.size(); j++) {
                        String prevSubWord = j > 0 ? subBoundaries.get(j - 1) : null;
                        String subWord = subBoundaries.get(j);

                        if (prevSubWord != null && prevSubWord.charAt(prevSubWord.length() - 1) == subWord.charAt(0))
                            compoundWordBuilder.append("-");

                        compoundWordBuilder.append(subWord);
                    }

                    if (compoundWordBuilder.charAt(compoundWordBuilder.length() - 1) == currentWord.firstChar())
                        compoundWordBuilder.append("-");

                    compoundWordBuilder.append(currentWord);
                    compoundWordParts.add(compoundWordBuilder.toString());
                }
            }

            wordsInBoundaries.add(currentWord.toString());
        }

        currentWord = new Word();
        skipAddingCurrentWord = false;
    }

    private final static class Word {

        boolean nominative = false;
        boolean singular = false;
        boolean clitic = false;
        boolean possessiveSuffix = false;
        boolean comparative = false;
        boolean properNoun = false;

        private final StringBuilder word = new StringBuilder();

        public void append(@NotNull String s) {
            word.append(s);
        }

        public boolean isNotEmpty() {
            return word.length() > 0;
        }

        public boolean isInBaseForm() {
            return nominative && singular && !clitic && !possessiveSuffix && !comparative;
        }

        @Override
        public String toString() {
            return word.toString();
        }

        public char firstChar() {
            return word.charAt(0);
        }
    }
}
