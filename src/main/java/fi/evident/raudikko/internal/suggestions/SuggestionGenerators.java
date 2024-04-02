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

import fi.evident.raudikko.internal.suggestions.Suggestion.SplitSuggestion;
import fi.evident.raudikko.internal.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.evident.raudikko.internal.utils.CharUtils.*;
import static fi.evident.raudikko.internal.utils.StringUtils.*;
import static java.lang.Math.min;

final class SuggestionGenerators {

    /**
     * Generates suggestions by deleting each character from the input word.
     * A character is not deleted if it is the same as the previous character.
     */
    static @NotNull Stream<String> delete(@NotNull String word) {
        return IntStream.range(0, word.length())
            .filter(i -> i == 0 || !equalsIgnoreCase(word.charAt(i), word.charAt(i - 1)))
            .mapToObj(i -> removeRange(word, i, i + 1));
    }

    /**
     * Generates suggestions by removing duplicate pairs of characters.
     */
    static @NotNull Stream<String> deleteTwo(@NotNull String word) {
        if (word.length() < 6)
            return Stream.empty();

        return IntStream.range(0, word.length() - 3)
            .filter(i -> word.regionMatches(i, word, i + 2, 2))
            .mapToObj(i -> removeRange(word, i, i + 2));
    }

    /**
     * Generates a variant without soft hyphens.
     */
    static @NotNull Stream<String> removeSoftHyphens(@NotNull String word) {
        var withoutHyphen = word.replace("\u00AD", "");

        return withoutHyphen.equals(word) ? Stream.empty() : Stream.of(withoutHyphen);
    }

    /**
     * Generates suggestions by trying to apply each of the given replacements to each of the possible characters.
     */
    static @NotNull Stream<String> replace(@NotNull String word, @NotNull Replacements replacements) {
        var result = Stream.<String>builder();

        for (var i = 0; i < word.length(); i++)
            for (var to : replacements.forCharacter(word.charAt(i)))
                result.add(replaceCharAt(word, i, to));

        return result.build();
    }

    /**
     * Generates suggestions where two a pair of same characters are replaced by pair of
     * other characters using given replacement mappings.
     */
    static @NotNull Stream<String> replaceTwo(@NotNull String word, @NotNull Replacements replacements) {
        var s = word.toLowerCase();
        var result = Stream.<String>builder();

        for (int i = 1; i < s.length(); i++) {
            var ch = s.charAt(i);
            if (ch == s.charAt(i - 1)) {
                for (char to : replacements.forCharacter(ch))
                    result.add(replaceTwoChars(s, i - 1, to));
                i++;
            }
        }
        return result.build();
    }

    /**
     * Generates suggestions by inserting given characters into the string.
     * Will not insert a character next to an existing instance of it.
     */
    static @NotNull Stream<String> insertion(@NotNull String word, @NotNull String insertedChars) {
        return insertedChars.chars().mapToObj(c -> insertions(word, (char) c)).flatMap(s -> s);
    }

    private static @NotNull Stream<String> insertions(@NotNull String word, char insertionChar) {
        return IntStream.rangeClosed(0, word.length())
            .filter(i -> !containsAdjacentCharacterIgnoringCase(word, i, insertionChar))
            .mapToObj(i -> word.substring(0, i) + insertionChar + word.substring(i));
    }

    /**
     * Generates suggestions by inserting a hyphen into various places.
     * Hyphen is never inserted near an existing hyphen or near beginning or end.
     */
    @NotNull
    static Stream<String> insertHyphen(@NotNull String word) {
        return IntStream.range(2, word.length() - 1)
            .filter(i -> !containsInSubstring(word, i - 2, i + 2, '-'))
            .mapToObj(i -> word.substring(0, i) + '-' + word.substring(i));
    }

    /**
     * Generates suggestions by duplicating existing characters in a word.
     */
    @NotNull
    static Stream<String> duplicateCharacters(@NotNull String word) {
        return IntStream.range(0, word.length())
            .filter(i -> {
                char c = word.charAt(i);
                return (i == 0 || word.charAt(i - 1) != c)
                    && (i + 1 >= word.length() || word.charAt(i + 1) != c)
                    && c != '-' && c != '\'';
            })
            .mapToObj(i -> word.substring(0, i) + word.charAt(i) + word.substring(i));
    }

    /**
     * Generates suggestions by swapping characters with nearby characters.
     */
    static @NotNull Stream<String> swap(@NotNull String word) {
        var maxDistance = (word.length() <= 8) ? word.length() : (50 / word.length());
        if (maxDistance == 0)
            return Stream.empty();

        return IntStream.range(0, word.length())
            .mapToObj(i -> swapOne(word, maxDistance, i))
            .flatMap(s -> s);
    }

    private static @NotNull Stream<String> swapOne(@NotNull String word, int maxDistance, int i) {
        return IntStream.range(i + 1, min(i + maxDistance + 1, word.length()))
            .filter(j -> !equalsIgnoreCase(word.charAt(i), word.charAt(j)) && !isFrontOrBackVowel(word.charAt(i)))
            .mapToObj(j -> StringUtils.swap(word, i, j));
    }

    /**
     * Generates suggestions by converting front-vowels to back-vowels and vice versa.
     */
    static @NotNull Stream<String> vowelChange(@NotNull String word) {
        var frontOrBackVowelIndices = new ArrayList<Integer>(word.length());
        for (int i = 0, len = word.length(); i < len; i++)
            if (isFrontOrBackVowel(word.charAt(i)))
                frontOrBackVowelIndices.add(i);

        if (frontOrBackVowelIndices.size() == 0 || frontOrBackVowelIndices.size() > 7)
            return Stream.empty();

        return IntStream.range(1, 1 << frontOrBackVowelIndices.size()).mapToObj(mask -> {
            var chars = word.toCharArray();

            for (int j = 0; j < frontOrBackVowelIndices.size(); j++) {
                int i = frontOrBackVowelIndices.get(j);

                if ((mask & (1 << j)) != 0)
                    chars[i] = convertVowelBetweenFrontAndBack(chars[i]);
            }

            return new String(chars);
        });
    }

    /**
     * Returns suggestions by trying to split the word at various points.
     */
    static @NotNull Stream<SplitSuggestion> splitWord(@NotNull String word) {
        var lower = word.toLowerCase();
        return IntStream.range(2, lower.length() - 2)
            .mapToObj(i -> splitWordAt(lower, lower.length() - i))
            .flatMap(Stream::ofNullable);
    }

    private static @Nullable SplitSuggestion splitWordAt(@NotNull String word, int i) {
        // Don't split if there's a nearby hyphen
        if (word.charAt(i - 2) == '-' || word.charAt(i - 1) == '-' || word.charAt(i + 1) == '-')
            return null;

        var hyphen = word.charAt(i) == '-';
        var word1 = word.substring(0, i);
        var word2 = word.substring(i + (hyphen ? 1 : 0));
        var priorityMultiplier = hyphen ? 6 : 1;

        return new SplitSuggestion(word1, word2, priorityMultiplier);
    }
}
