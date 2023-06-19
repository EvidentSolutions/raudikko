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

import fi.evident.raudikko.Analyzer;
import fi.evident.raudikko.AnalyzerConfiguration;
import fi.evident.raudikko.Morphology;
import fi.evident.raudikko.SpellingSuggester;
import fi.evident.raudikko.internal.suggestions.Suggestion.SimpleSuggestion;
import fi.evident.raudikko.internal.suggestions.Suggestion.SplitSuggestion;
import fi.evident.raudikko.internal.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static fi.evident.raudikko.internal.suggestions.Replacements.*;
import static fi.evident.raudikko.internal.utils.StringUtils.isAllUpper;
import static java.lang.Character.isUpperCase;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;

public final class DefaultSpellingSuggester implements SpellingSuggester {

    private final @NotNull SpellChecker spellChecker;

    /**
     * How many suggestions are returned to user
     */
    private static final int MAX_SUGGESTIONS_RETURNED = 5;

    /**
     * How many variations are generated for words?
     */
    private static final int MAX_VARIATIONS = 800;

    /**
     * Generate more suggestions than required so that sorting gets to pick the best ones
     */
    private static final int MAX_SUGGESTIONS_GENERATED = 3 * MAX_SUGGESTIONS_RETURNED;

    private static final int MAX_WORD_SIZE = 255;
    private static final @NotNull String COMMON_LETTERS = "aitesn";
    private static final @NotNull String UNCOMMON_LETTERS = "ulkoämrvpyhjdögfbcw:xzqå'.";

    private static final @NotNull List<Function<? super String, ? extends Stream<? extends Suggestion>>> primaryGenerators = List.of(
        simple(Stream::of),
        simple(SuggestionGenerators::removeSoftHyphens)
    );

    private static final @NotNull List<Function<? super String, ? extends Stream<? extends Suggestion>>> secondaryGenerators = List.of(
        simple(SuggestionGenerators::vowelChange),
        simple(SuggestionGenerators::replace, REPLACEMENTS_1_FULL),
        simple(SuggestionGenerators::delete),
        simple(SuggestionGenerators::insertHyphen),
        simple(SuggestionGenerators::duplicateCharacters),
        SuggestionGenerators::splitWord,
        simple(SuggestionGenerators::replaceTwo, REPLACEMENTS_1),
        simple(SuggestionGenerators::replace, REPLACEMENTS_2_FULL),
        simple(SuggestionGenerators::insertion, COMMON_LETTERS),
        simple(SuggestionGenerators::swap),
        simple(SuggestionGenerators::replace, REPLACEMENTS_3_FULL),
        simple(SuggestionGenerators::insertion, UNCOMMON_LETTERS),
        simple(SuggestionGenerators::replace, REPLACEMENTS_4_FULL),
        simple(SuggestionGenerators::replaceTwo, REPLACEMENTS_2),
        simple(SuggestionGenerators::replaceTwo, REPLACEMENTS_3),
        simple(SuggestionGenerators::replaceTwo, REPLACEMENTS_4),
        simple(SuggestionGenerators::deleteTwo),
        simple(SuggestionGenerators::replace, REPLACEMENTS_5_FULL)
    );

    public DefaultSpellingSuggester(@NotNull Morphology morphology) {
        this.spellChecker = new SpellChecker(newAnalyzer(morphology));
    }

    @Override
    public @NotNull List<String> provideSpellingSuggestions(@NotNull String word) {
        if (word.length() <= 1 || word.length() > MAX_WORD_SIZE)
            return emptyList();

        var capitalizer = capitalizer(word);

        var results1 = generateSuggestions(word, primaryGenerators);
        var results2 = generateSuggestions(word, secondaryGenerators);

        return Stream.concat(results1, results2)
            .map(capitalizer.compose(WordWithPriority::word))
            .distinct()
            .limit(MAX_SUGGESTIONS_RETURNED)
            .toList();
    }

    private @NotNull Stream<WordWithPriority> generateSuggestions(
        @NotNull String word,
        @NotNull List<Function<? super String, ? extends Stream<? extends Suggestion>>> generators
    ) {
        AtomicInteger count = new AtomicInteger(0); // atomicity not really needed, just box for counter

        return generators.stream()
            .flatMap(g -> g.apply(word))
            .distinct()
            .limit(MAX_VARIATIONS)
            .flatMap(s -> Stream.ofNullable(processSuggestion(s, spellChecker)))
            .limit(MAX_SUGGESTIONS_GENERATED)
            .map(s -> new WordWithPriority(s.word(), s.priority() * (count.getAndIncrement() + 5)))
            .sorted(comparing(WordWithPriority::priority));
    }

    private static @Nullable WordWithPriority processSuggestion(@NotNull Suggestion suggestion, @NotNull SpellChecker spellChecker) {
        if (suggestion instanceof SimpleSuggestion s) {
            return spellChecker.spellCheck(s.word());

        } else if (suggestion instanceof SplitSuggestion s) {
            var s1 = spellChecker.spellCheck(s.word1());
            if (s1 == null)
                return null;

            var s2 = spellChecker.spellCheck(s.word2());
            if (s2 == null)
                return null;

            return new WordWithPriority(s1.word() + " " + s2.word(), (s1.priority() + s2.priority()) * s.priorityMultiplier());
        } else {
            throw new IllegalStateException("unexpected suggestion: " + suggestion);
        }
    }

    private static @NotNull UnaryOperator<String> capitalizer(@NotNull String word) {
        if (isAllUpper(word))
            return String::toUpperCase;
        else if (isUpperCase(word.charAt(0)))
            return StringUtils::capitalizeIfLower;
        else
            return UnaryOperator.identity();
    }

    private static @NotNull Analyzer newAnalyzer(@NotNull Morphology morphology) {
        var config = new AnalyzerConfiguration();

        config.setIncludeWord(true);
        config.setIncludeStructure(true);
        config.setIncludeBasicAttributes(true);
        config.setIncludeOrganizationNameAnalysis(true);

        config.setIncludeBaseForm(false);
        config.setIncludeBaseFormParts(false);
        config.setIncludeFstOutput(false);

        return morphology.newAnalyzer(config);
    }

    private static @NotNull Function<String, Stream<SimpleSuggestion>> simple(@NotNull Function<String, Stream<String>> f) {
        return w -> f.apply(w).map(SimpleSuggestion::new);
    }

    private static @NotNull <T> Function<String, Stream<SimpleSuggestion>> simple(@NotNull BiFunction<String, T, Stream<String>> f, T param) {
        return w -> f.apply(w, param).map(SimpleSuggestion::new);
    }
}
