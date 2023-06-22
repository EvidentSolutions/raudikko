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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fi.evident.raudikko.internal.suggestions.Replacements.REPLACEMENTS_1;
import static fi.evident.raudikko.internal.suggestions.Replacements.REPLACEMENTS_1_FULL;
import static fi.evident.raudikko.internal.suggestions.SuggestionGenerators.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
class SuggestionGeneratorsTest {

    @Nested
    class Delete {

        @Test
        void suggestions() {
            assertEquals(emptyList(), delete("").toList());
            assertEquals(asList("ello", "hllo", "helo", "hell"), delete("hello").toList());
        }
    }

    @Nested
    class DeleteTwo {

        @Test
        void wordsUnderLimit() {
            assertEquals(emptyList(), deleteTwo("").toList());
            assertEquals(emptyList(), deleteTwo("hello").toList());
            assertEquals(emptyList(), deleteTwo("aaaaa").toList());
        }

        @Test
        void noConsecutiveDuplicatePairs() {
            assertEquals(emptyList(), deleteTwo("abcdefgh").toList());
            assertEquals(emptyList(), deleteTwo("abcdeeefgh").toList());
        }

        @Test
        void consecutiveDuplicatePairs() {
            assertEquals(asList("abccdeeeef", "abccccdeef"), deleteTwo("abccccdeeeef").toList());
        }

        @Test
        void nonConsecutiveDuplicatePairs() {
            assertEquals(emptyList(), deleteTwo("aabbaa").toList());
        }
    }

    @Nested
    class RemoveSoftHyphens {

        @Test
        void noSoftHyphens() {
            assertEquals(emptyList(), removeSoftHyphens("").toList());
            assertEquals(emptyList(), removeSoftHyphens("foo").toList());
        }

        @Test
        void softHyphensAreRemoved() {
            assertEquals(singletonList("foobar"), removeSoftHyphens("foo\u00ADbar").toList());
            assertEquals(singletonList("foobarbaz"), removeSoftHyphens("foo\u00ADbar\u00ADbaz").toList());
        }
    }


    @Nested
    class Replace {

        @Test
        void suggestions() {
            assertEquals(List.of(
                "batsaneläkeruokaa", "vstsaneläkeruokaa", "vetsaneläkeruokaa", "varsaneläkeruokaa", "vadsaneläkeruokaa",
                "vaysaneläkeruokaa", "vatšaneläkeruokaa", "vataaneläkeruokaa", "vatssneläkeruokaa", "vatseneläkeruokaa",
                "vatsameläkeruokaa", "vatsanrläkeruokaa", "vatsanaläkeruokaa", "vatsanekäkeruokaa", "vatsanelökeruokaa",
                "vatsaneläleruokaa", "vatsanelägeruokaa", "vatsaneläkrruokaa", "vatsaneläkaruokaa", "vatsaneläkeeuokaa",
                "vatsaneläketuokaa", "vatsaneläkeriokaa", "vatsaneläkeruikaa", "vatsaneläkeruolaa", "vatsaneläkeruogaa",
                "vatsaneläkeruoksa", "vatsaneläkeruokea", "vatsaneläkeruokas", "vatsaneläkeruokae"
            ), replace("vatsaneläkeruokaa", REPLACEMENTS_1_FULL).toList());
        }

        @Test
        void upperCaseReplacements() {
            assertEquals(List.of("Goo", "Doo", "Fio", "Foi"), replace("Foo", REPLACEMENTS_1_FULL).toList());
        }
    }

    @Nested
    class ReplaceTwo {

        @Test
        void suggestions() {
            assertEquals(emptyList(), replaceTwo("", REPLACEMENTS_1).toList());
            assertEquals(emptyList(), replaceTwo("bar", REPLACEMENTS_1).toList());
            assertEquals(asList("fiibarbazquux", "foobarbazqiix"), replaceTwo("foobarbazquux", REPLACEMENTS_1).toList());
            assertEquals(asList("fiibarbazquux", "foobarbazqiix"), replaceTwo("foobarbazquux", REPLACEMENTS_1).toList());
        }

        @Test
        void multipleConsecutiveAreProcessedJustOnce() {
            assertEquals(asList("iioooo", "ooiioo", "ooooii"), replaceTwo("oooooo", REPLACEMENTS_1).toList());
        }
    }

    @Nested
    class Insertion {

        @Test
        void suggestions() {
            assertEquals(List.of(
                "1foobar", "f1oobar", "fo1obar", "foo1bar", "foob1ar", "fooba1r", "foobar1",
                "2foobar", "f2oobar", "fo2obar", "foo2bar", "foob2ar", "fooba2r", "foobar2",
                "3foobar", "f3oobar", "fo3obar", "foo3bar", "foob3ar", "fooba3r", "foobar3"
            ), insertion("foobar", "123").toList());
        }

        @Test
        void suggestionsWhenInsertionCharsAreInInput() {
            assertEquals(List.of(
                "1foo12bar", "f1oo12bar", "fo1o12bar", "foo121bar", "foo12b1ar", "foo12ba1r", "foo12bar1",
                "2foo12bar", "f2oo12bar", "fo2o12bar", "foo212bar", "foo12b2ar", "foo12ba2r", "foo12bar2",
                "3foo12bar", "f3oo12bar", "fo3o12bar", "foo312bar", "foo132bar", "foo123bar", "foo12b3ar", "foo12ba3r", "foo12bar3"
            ), insertion("foo12bar", "123").toList());
        }
    }

    @Nested
    class InsertHyphen {

        @Test
        void testGenerateWithEmptyString() {
            assertEquals(emptyList(), insertHyphen("").toList());
        }

        @Test
        void testGenerateWithStringWithoutHyphen() {
            assertEquals(List.of("ab-cdefgh", "abc-defgh", "abcd-efgh", "abcde-fgh", "abcdef-gh"), insertHyphen("abcdefgh").toList());
        }

        @Test
        void stringWithHyphen() {
            assertEquals(List.of("ab-cd-efgh", "abcd-ef-gh"), insertHyphen("abcd-efgh").toList());
        }

        @Test
        void testGenerateWithStringWithSpecialCharacters() {
            assertEquals(emptyList(), insertHyphen("a-b-'c").toList());
        }

    }

    @Nested
    class DuplicateCharacters {

        @Test
        void testGenerateWithEmptyString() {
            assertEquals(emptyList(), duplicateCharacters("").toList());
        }

        @Test
        void testGenerateWithStringWithoutHyphen() {
            assertEquals(List.of("aabcdefgh", "abbcdefgh", "abccdefgh", "abcddefgh", "abcdeefgh", "abcdeffgh", "abcdefggh", "abcdefghh"),
                duplicateCharacters("abcdefgh").toList());
        }

        @Test
        void stringWithHyphen() {
            assertEquals(List.of("aabcd-efgh", "abbcd-efgh", "abccd-efgh", "abcdd-efgh", "abcd-eefgh", "abcd-effgh", "abcd-efggh", "abcd-efghh"),
                duplicateCharacters("abcd-efgh").toList());
        }

        @Test
        void testGenerateWithStringWithSpecialCharacters() {
            assertEquals(List.of("aa-b-'c", "a-bb-'c", "a-b-'cc"), duplicateCharacters("a-b-'c").toList());
        }
    }

    @Nested
    class Swap {

        @Test
        void suggestions() {
            assertEquals(asList(
                "ofobarbaz", "oofbarbaz", "boofarbaz", "aoobfrbaz", "roobafbaz",
                "fooabrbaz", "foorabbaz", "fooaarbbz", "foozarbab", "foobabraz",
                "foobaabrz", "foobazbar", "foobarabz", "foobarzab"
            ), swap("foobarbaz").toList());
        }
    }

    @Nested
    class VowelChange {

        @Test
        void suggestions() {
            assertEquals(
                asList("hamähäkki", "hämahäkki", "hamahäkki", "hämähakki", "hamähakki", "hämahakki", "hamahakki"),
                vowelChange("hämähäkki").toList());

            assertEquals(
                asList(
                    "äamuyö", "aämuyö", "äämuyö", "aamyyö", "äamyyö", "aämyyö", "äämyyö", "aamuuö",
                    "äamuuö", "aämuuö", "äämuuö", "aamyuö", "äamyuö", "aämyuö", "äämyuö", "aamuyo",
                    "äamuyo", "aämuyo", "äämuyo", "aamyyo", "äamyyo", "aämyyo", "äämyyo", "aamuuo",
                    "äamuuo", "aämuuo", "äämuuo", "aamyuo", "äamyuo", "aämyuo", "äämyuo"),
                vowelChange("aamuyö").toList());

            assertEquals(asList("öy", "ou", "öu"), vowelChange("oy").toList());
        }
    }

    @Nested
    class SplitWord {

        @Test
        void suggestions() {
            assertEquals(asList("foobarb az", "foobar baz", "fooba rbaz", "foob arbaz", "foo barbaz"), splitWord("foobarbaz").map(s -> s.word1() + " " + s.word2()).toList());
        }
    }
}
