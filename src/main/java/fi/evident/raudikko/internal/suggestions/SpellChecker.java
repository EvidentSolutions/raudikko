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

package fi.evident.raudikko.internal.suggestions;

import fi.evident.raudikko.Analysis;
import fi.evident.raudikko.Analyzer;
import fi.evident.raudikko.analysis.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.Math.min;

/**
 * A spell-checker that uses Analyzer to handle the spell-checking.
 */
final class SpellChecker {

    private final @NotNull Analyzer analyzer;

    public SpellChecker(@NotNull Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public @Nullable WordWithPriority spellCheck(@NotNull String word) {
        var analyses = analyzer.analyze(word);
        WordWithPriority best = null;

        for (var analysis : analyses) {
            var result = createResult(word, analysis);
            if (result != null && (best == null || result.priority() < best.priority()))
                best = result;
        }

        return best;
    }

    private static @Nullable WordWithPriority createResult(@NotNull String word, @NotNull Analysis analysis) {
        var structure = analysis.getStructure();
        if (structure == null) return null;

        var cr = CapitalizationResult.resolve(word, structure);
        var priority = priorityFromWordClassAndInflection(analysis) * priorityFromStructure(structure) * cr.priority();

        return new WordWithPriority(cr == CapitalizationResult.OK ? word : structure.apply(word), priority);
    }

    private static int priorityFromStructure(@NotNull Structure structure) {
        return 1 << (3 * (min(structure.getMorphemeCount(), 5) - 1));
    }

    private static int priorityFromWordClassAndInflection(@NotNull Analysis analysis) {
        var wordClass = analysis.getWordClass();
        if (wordClass == null)
            return 4;

        return switch (wordClass) {
            case NOUN, ADJECTIVE, NOUN_ADJECTIVE, PRONOUN, FIRST_NAME, LAST_NAME, TOPONYM, PROPER_NOUN ->
                priorityFromNounInflection(analysis);
            default -> 4; // other word classes have no special handling yet
        };
    }

    private static int priorityFromNounInflection(@NotNull Analysis analysis) {
        var locative = analysis.getLocative();
        if (locative == null)
            return 4;

        return switch (locative) {
            case NOMINATIVE -> 2;
            case GENITIVE -> 3;
            case PARTITIVE -> 5;
            case INESIVE, ILLATIVE -> 8;
            case ELATIVE, ADESSIVE -> 12;
            case ALLATIVE, ESSIVE, TRANSLATIVE, INSTRUCTIVE -> 20;
            case ABLATIVE -> 30;
            case ABESSIVE, COMITATIVE -> 60;
            default -> 4;
        };
    }

    private enum CapitalizationResult {
        OK, FIRST_CAPITALIZED, CAPITALIZATION_ERROR;

        int priority() {
            return ordinal() + 1;
        }

        static @NotNull CapitalizationResult resolve(@NotNull String word, @NotNull Structure structure) {
            var result = OK;
            var it = structure.nonMorphemes();

            for (int i = 0; i < word.length(); i++) {
                if (!it.hasNext())
                    break;

                var expected = it.next();
                if (!expected.agrees(word.charAt(i))) {
                    if (i == 0 && expected.isUpperCase())
                        result = CapitalizationResult.FIRST_CAPITALIZED;
                    else
                        return CapitalizationResult.CAPITALIZATION_ERROR;
                }
            }

            return result;
        }
    }
}
