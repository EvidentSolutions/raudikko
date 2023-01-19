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
import fi.evident.raudikko.AnalyzerConfiguration;
import fi.evident.raudikko.Morphology;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class BaseFormPartsTest {

    private final @NotNull Morphology morphology = Morphology.loadBundled();
    private final @NotNull Analyzer analyzer = morphology.newAnalyzer(createConfiguration());

    private static AnalyzerConfiguration createConfiguration() {
        AnalyzerConfiguration configuration = new AnalyzerConfiguration();
        configuration.setIncludeBaseFormParts(true);
        configuration.setIncludeStructure(false);
        configuration.setIncludeBaseForm(false);
        configuration.setIncludeFstOutput(false);
        configuration.setIncludeOrganizationNameAnalysis(false);
        configuration.setIncludeBasicAttributes(false);
        return configuration;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void baseFormPartsTest(@NotNull BaseFormPartsTest.WordTest data) {
        List<Analysis> analyze = analyzer.analyze(data.word);

        assertEquals(data.expectedBaseFormParts.size(), analyze.size());

        for (int i = 0; i < data.expectedBaseFormParts.size(); i++) {
            List<String> expected = data.expectedBaseFormParts.get(i);
            List<String> actual = analyze.get(i).getBaseFormParts();
            assertEquals(expected, actual);
        }
    }

    private static @NotNull Stream<BaseFormPartsTest.WordTest> testData() throws IOException {
        try (InputStream in = BaseFormPartsTest.class.getResourceAsStream("/base-form-parts-test.txt")) {
            assertNotNull(in, "could not find base form parts test data");

            List<String> lines = new BufferedReader(new InputStreamReader(in, UTF_8)).lines()
                    .map(String::trim)
                    .filter(it -> !it.isEmpty())
                    .collect(Collectors.toList());

            BaseFormPartsTest.WordTest current = null;

            List<BaseFormPartsTest.WordTest> result = new ArrayList<>();

            Pattern splitPattern = Pattern.compile(",");

            for (String line : lines) {
                if (line.startsWith("word:")) {
                    current = new BaseFormPartsTest.WordTest(line.substring("word:".length()));
                    result.add(current);
                } else if (current == null) {
                    fail();
                } else {
                    if (line.startsWith("[") && line.endsWith("]")) {
                        ArrayList<String> parts = new ArrayList<>();
                        for (String part : splitPattern.split(line.substring(1, line.length() - 1))) {
                            if (!part.isEmpty()) {
                                parts.add(part);
                            }
                        }
                        current.expectedBaseFormParts.add(parts);
                    } else {
                        fail("unknown line: '" + line + "'");
                    }
                }
            }

            return result.stream();
        }
    }

    private static final class WordTest {
        public final @NotNull String word;
        public final @NotNull List<List<String>> expectedBaseFormParts = new ArrayList<>();

        WordTest(@NotNull String word) {
            this.word = word;
        }

        @Override
        public String toString() {
            return word;
        }
    }
}
