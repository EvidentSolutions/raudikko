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

import fi.evident.raudikko.Word;
import fi.evident.raudikko.WordPart;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static fi.evident.raudikko.internal.utils.StringUtils.removeLeadingAndTrailing;

final class BaseFormParts {

    private BaseFormParts() {
    }

    static @NotNull List<String> parseBaseFormParts(@NotNull SymbolBuffer tokenizer, @NotNull WordParser wordParser) {
        Word word = wordParser.parseWord(tokenizer);

        List<String> result = new ArrayList<>();
        List<WordPart> wordParts = word.getWordParts();

        for (WordPart wordPart : wordParts) {
            result.addAll(wordPart.getBaseForms());

            if (wordPart.isInBaseForm() && !wordPart.isProperNoun()) {
                String wp = removeLeadingAndTrailing(wordPart.toString(), '-');

                if (!result.contains(wp))
                    result.add(wp);
            }
        }

        for (int i = 0; i < wordParts.size(); i++) {
            boolean isLast = i == wordParts.size() - 1;

            if (wordParts.get(i).isInBaseForm())
                for (int j = isLast ? 1 : 0; j < i; j++)
                    result.add(joinSubCompoundWord(wordParts.subList(j, i + 1)));
        }

        return result;
    }

    private static @NotNull String joinSubCompoundWord(@NotNull List<WordPart> subList) {
        StringBuilder s = new StringBuilder();

        for (Iterator<WordPart> subWordIterator = subList.iterator(); subWordIterator.hasNext();) {
            WordPart subWord = subWordIterator.next();
            boolean isLastSubWord = !subWordIterator.hasNext();

            if (isLastSubWord)
                s.append(removeLeadingAndTrailing(subWord.toString(), '-'));
            else
                s.append(subWord.toString());
        }

        return s.toString();
    }
}
