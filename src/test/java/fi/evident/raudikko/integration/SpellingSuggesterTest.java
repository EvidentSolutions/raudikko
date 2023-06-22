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

package fi.evident.raudikko.integration;

import fi.evident.raudikko.Morphology;
import fi.evident.raudikko.SpellingSuggester;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static fi.evident.raudikko.test.ResourceUtils.readLines;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class SpellingSuggesterTest {

    private static final @NotNull Pattern TEST_PATTERN = Pattern.compile("(.+):\\[(.*)]");
    private SpellingSuggester suggester;

    @BeforeAll
    void setup() {
        suggester = Morphology.loadBundled().newSpellingSuggester();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void testSuggestions(@NotNull String line) {
        var ignored = line.startsWith("#");
        if (ignored)
            line = line.substring(1);

        var m = TEST_PATTERN.matcher(line);
        if (!m.matches())
            fail("Invalid line '" + line + "'");

        var word = m.group(1);
        var suggestions = suggester.provideSpellingSuggestions(word);
        var expected = Arrays.stream(m.group(2).split(";")).filter(s -> !s.isEmpty()).toList();

        if (!ignored) {
            assertEquals(expected, suggestions, "word: " + word);
        } else {
            if (expected.equals(suggestions))
                fail("PASSED test for ignored word " + word);
            else
                throw new TestAbortedException("ignored word " + word);
        }
    }

    private static @NotNull List<String> testData() throws IOException {
        return readLines("typing-error-suggester-test.txt");
    }
}
