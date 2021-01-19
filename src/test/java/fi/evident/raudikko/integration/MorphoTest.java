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

import fi.evident.raudikko.Analysis;
import fi.evident.raudikko.Analyzer;
import fi.evident.raudikko.Morphology;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class MorphoTest {

    private final @NotNull Morphology morphology = Morphology.loadBundled();
    private final @NotNull Analyzer analyzer = morphology.newAnalyzer();

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void morphoTest(@NotNull MorphoTest.WordTest data) {
        List<Analysis> analyses = analyzer.analyze(data.word);

        if (data.expected != null)
            assertEquals(data.expected, analyses.size());

        for (Map<String, String> expectedAnalysis : data.analyses) {

            // These are not supported by Raudikko
            expectedAnalysis.remove("WORDIDS");
            expectedAnalysis.remove("WORDBASES");

            boolean anyMatch = false;
            for (Analysis analysis : analyses) {
                Map<String, String> map = analysis.toVoikkoFormat();
                map.keySet().retainAll(expectedAnalysis.keySet());
                if (map.equals(expectedAnalysis))
                    anyMatch = true;
            }

            if (!anyMatch)
                fail("Expected " + expectedAnalysis + ", but got " + analyses);
        }

        if (!data.forbiddenKeys.isEmpty() || !data.forbiddenValues.isEmpty())
            for (Analysis analysis : analyses) {
                Map<String, String> map = analysis.toVoikkoFormat();

                for (String forbidden : data.forbiddenKeys)
                    if (map.containsKey(forbidden))
                        fail("Analysis contains forbidden key " + forbidden);

                for (String forbidden : data.forbiddenValues)
                    if (map.containsValue(forbidden))
                        fail("Analysis contains forbidden value " + forbidden);
            }
    }

    private static @NotNull Stream<WordTest> testData() throws IOException {
        try (InputStream in = MorphoTest.class.getResourceAsStream("/morpho-test.txt")) {
            assertNotNull(in, "could not find morphology test data");

            List<String> lines = new BufferedReader(new InputStreamReader(in, UTF_8)).lines()
                    .filter(it -> !it.isEmpty() && !it.startsWith("#"))
                    .collect(Collectors.toList());

            WordTest current = null;

            List<WordTest> result = new ArrayList<>();

            Pattern keyValuePattern = Pattern.compile("(\\w+)=(.+)");

            for (String line : lines) {
                line = normalizeLine(line);

                if (line.startsWith("word:")) {
                    current = new WordTest(line.substring("word:".length()));
                    result.add(current);

                } else if (current == null) {
                    fail();
                } else if (line.startsWith("expected:")) {
                    current.expected = parseInt(line.substring("expected:".length()));

                } else if (line.startsWith("!=")) {
                    current.forbiddenValues.add(line.substring("!=".length()));

                } else if (line.startsWith("!")) {
                    current.forbiddenKeys.add(line.substring("!".length()));

                } else if (line.startsWith("analysis")) {
                    current.analyses.add(new HashMap<>());

                } else {
                    Matcher m = keyValuePattern.matcher(line);
                    if (m.matches()) {
                        if (current.analyses.isEmpty()) continue; // TODO

                        Map<String, String> currentAnalysis = current.analyses.get(current.analyses.size() - 1);
                        currentAnalysis.put(m.group(1), m.group(2));
                    } else {
                        fail("unknown line: '" + line + "'");
                    }
                }
            }

            return result.stream();
        }
    }

    private static @NotNull String normalizeLine(@NotNull String line) {
        int comment = line.indexOf('#');
        if (comment != -1)
            line = line.substring(0, comment);
        return line.trim();
    }

    private static final class WordTest {
        public final @NotNull String word;
        public @Nullable Integer expected;
        public final @NotNull Set<String> forbiddenKeys = new HashSet<>();
        public final @NotNull Set<String> forbiddenValues = new HashSet<>();
        public final @NotNull List<Map<String, String>> analyses = new ArrayList<>();

        WordTest(@NotNull String word) {
            this.word = word;
        }

        @Override
        public String toString() {
            return (expected != null && expected == 0) ? '!' + word : word;
        }
    }
}
