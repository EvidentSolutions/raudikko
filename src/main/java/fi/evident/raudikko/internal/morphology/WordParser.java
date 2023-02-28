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

import fi.evident.raudikko.WordAttribute;
import fi.evident.raudikko.Word;
import fi.evident.raudikko.WordPart;
import fi.evident.raudikko.internal.fst.Symbol;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static fi.evident.raudikko.internal.utils.StringUtils.withoutChar;
import static java.util.Objects.requireNonNull;

final class WordParser {

    private final WordBuilder word = new WordBuilder();

    @NotNull Word parseWord(@NotNull SymbolBuffer tokenizer) {
        tokenizer.moveToStart();
        word.reset();

        while (tokenizer.nextToken()) {
            Symbol tag = tokenizer.getCurrentTag();

            if (tag == null) {
                word.append(tokenizer.currentToken.toString());
            } else {
                if (tag.matches(Tags.xp))
                    word.addBaseForm(withoutChar(tokenizer.readXTagContents(), '='));

                else if (tag.matches(Tags.xj))
                    word.addBaseForm(word.getCurrentPart().word + withoutChar(tokenizer.readXTagContents(), '='));

                else if (tag.matches(Tags.xr) || tag.matches(Tags.xs))
                    tokenizer.readXTagContents(); // not used

                else if (tag.isBoundary()) {
                    word.setAttribute(requireNonNull(WordAttribute.fromTag(tag)));

                    while (tokenizer.nextTokenIsBoundary()) {
                        tokenizer.nextToken();
                        word.setAttribute(requireNonNull(WordAttribute.fromTag(requireNonNull(tokenizer.getCurrentTag()))));
                    }

                    word.startWordPart();
                } else {
                    WordAttribute attribute = WordAttribute.fromTag(tag);

                    if (attribute != null)
                        word.setAttribute(attribute);
                }
            }
        }
        return word.toWord();
    }

    private static final class WordBuilder {

        private @NotNull final WordPartBuilder currentPart = new WordPartBuilder();
        private @NotNull final List<WordPart> wordParts = new ArrayList<>();

        private void append(@NotNull CharSequence s) {
            currentPart.append(s);
        }

        private @NotNull WordPartBuilder getCurrentPart() {
            return currentPart;
        }

        private void addBaseForm(@NotNull String baseForm) {
            currentPart.addBaseForm(baseForm);
        }

        private void setAttribute(WordAttribute attribute) {
            currentPart.addAttribute(attribute);
        }

        private void startWordPart() {
            endWordPart();
            currentPart.reset();
        }

        private void endWordPart() {
            if (currentPart.isNotEmpty())
                wordParts.add(currentPart.toWordPart());
        }

        private @NotNull Word toWord() {
            endWordPart();
            return new Word(new ArrayList<>(wordParts));
        }

        private void reset() {
            currentPart.reset();
            wordParts.clear();
        }
    }

    private static final class WordPartBuilder {

        private final @NotNull StringBuilder word = new StringBuilder();
        private final @NotNull List<String> baseForms = new ArrayList<>();
        private final @NotNull EnumSet<WordAttribute> attributes = EnumSet.noneOf(WordAttribute.class);

        private void append(@NotNull CharSequence s) {
            this.word.append(s);
        }

        private void addBaseForm(@NotNull String baseForm) {
            if (!baseForm.isEmpty())
                this.baseForms.add(baseForm);
        }

        private boolean isNotEmpty() {
            return !withoutChar(word, '-').isEmpty();
        }

        private void addAttribute(@NotNull WordAttribute attribute) {
            attributes.add(attribute);
        }

        private void reset() {
            word.delete(0, word.length());
            baseForms.clear();
            attributes.clear();
        }

        private @NotNull WordPart toWordPart() {
            return new WordPart(word.toString(), new ArrayList<>(baseForms), EnumSet.copyOf(attributes));
        }
    }
}
