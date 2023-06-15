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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.puimula.libvoikko.Voikko;

import java.io.File;
import java.net.URL;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static fi.evident.raudikko.analysis.WordClass.NOUN;
import static fi.evident.raudikko.integration.TestUtils.locateProjectRoot;
import static fi.evident.raudikko.test.ResourceUtils.readLines;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class VoikkoComparisonTest {

    private Voikko voikko;
    private Analyzer analyzer;
    private Morphology morphology;
    private List<String> referenceWords;

    @BeforeAll
    void setupVoikko() {
        String voikkoPath = System.getenv("VOIKKO_LIBRARY_PATH");
        assumeTrue(voikkoPath != null, "VOIKKO_LIBRARY_PATH not defined, skipping voikko tests");
        Voikko.addLibraryPath(voikkoPath);

        URL morphologyUrl = getClass().getResource("/morpho/5/mor-morpho/mor.vfst");
        assertNotNull(morphologyUrl);

        voikko = new Voikko("fi-x-morpho", new File(locateProjectRoot(), "src/main/resources/morpho").toString());
    }

    @BeforeAll
    void setupRaudikko() {
        morphology = Morphology.loadBundled();
        analyzer = morphology.newAnalyzer();
    }

    @BeforeAll
    void loadReferenceWords() throws Exception {
        referenceWords = readLines("rautatie-unhyphenated.txt");
    }

    @AfterAll
    void releaseVoikko() {
        if (voikko != null)
            voikko.terminate();
    }

    @Test
    void voikkoSmokeTest() {
        List<org.puimula.libvoikko.Analysis> analyses = voikko.analyze("vatsaneläkeruokaa");

        assertEquals(1, analyses.size());

        org.puimula.libvoikko.Analysis analysis = analyses.get(0);
        assertEquals("vatsaneläkeruoka", analysis.get("BASEFORM"));
        assertEquals(NOUN.getLegacyCode(), analysis.get("CLASS"));
        assertEquals("[Ln][Xp]vatsa[X]vats[Sg][Ny]an[Bh][Bc][Ln][Xp]eläke[X]eläke[Sn][Ny][Bh][Bc][Ln][Xp]ruoka[X]ruok[Sp][Ny]aa", analysis.get("FSTOUTPUT"));
    }

    @Test
    void failures() {
        int failures = 0;
        for (String word : referenceWords)
            if (analyzer.analyze(word).isEmpty())
                failures++;

        System.out.printf("failures %d/%d (%.2f %%)\n", failures, referenceWords.size(), (failures * 100.0) / referenceWords.size());
        assertEquals(439, failures);
    }

    @Test
    void compareReferenceText() {
        for (String word : referenceWords) {
            List<Map<String, ?>> voikkoResult = voikko.analyze(word).stream().map(VoikkoComparisonTest::withoutUnsupportedAttributes).collect(toList());
            List<Map<String, ?>> raudikkoResult = analyzer.analyze(word).stream().map(Analysis::toVoikkoFormat).collect(toList());
            assertEquals(voikkoResult, raudikkoResult);
        }
    }

    @Test
    void times() throws Exception {
        int loops = 1;
        List<String> words = new RepeatedList<>(referenceWords, loops);

        long voikkoStart = System.currentTimeMillis();
        for (String word : words)
            voikko.analyze(word);
        long voikkoMillis = System.currentTimeMillis() - voikkoStart;
        System.out.printf("voikko:            %5d ms (%d words/s)\n", voikkoMillis, (words.size() * 1000L) / voikkoMillis);

        long raudikkoStart = System.currentTimeMillis();
        for (String word : words)
            analyzer.analyze(word);
        long raudikkoMillis = System.currentTimeMillis() - raudikkoStart;
        System.out.printf("raudikko:          %5d ms (%d words/s)\n", raudikkoMillis, (words.size() * 1000L) / raudikkoMillis);

        for (int threadCount : Arrays.asList(2, 4, 8, 16)) {
            long concurrentStart = System.currentTimeMillis();
            AtomicInteger next = new AtomicInteger(0);
            AtomicInteger analyzed = new AtomicInteger(0);

            Thread[] threads = new Thread[threadCount];
            for (int t = 0; t < threads.length; t++) {
                threads[t] = new Thread(() -> {
                    Analyzer myAnalyzer = morphology.newAnalyzer();
                    while (true) {
                        int index = next.getAndIncrement();
                        if (index >= words.size()) break;

                        myAnalyzer.analyze(words.get(index));
                        analyzed.incrementAndGet();
                    }
                });
                threads[t].start();
            }

            for (Thread t : threads)
                t.join();

            assertEquals(words.size(), analyzed.get());

            long concurrentMillis = System.currentTimeMillis() - concurrentStart;

            double concurrentSpeedUp = ((double) raudikkoMillis) / (concurrentMillis * threadCount);
            System.out.printf("%2d threads:        %5d ms (%d words/s, speedup/thread: %f)\n", threadCount, concurrentMillis, (words.size() * 1000L) / concurrentMillis, concurrentSpeedUp);
        }
    }

    private static @NotNull Map<String, ?> withoutUnsupportedAttributes(@NotNull Map<String, ?> analysis) {
        analysis.remove("WORDIDS");
        analysis.remove("WORDBASES");
        return analysis;
    }

    private static final class RepeatedList<T> extends AbstractList<T> {
        private final @NotNull List<T> list;
        private final int repeats;

        public RepeatedList(@NotNull List<T> items, int repeats) {
            this.list = items;
            this.repeats = repeats;
        }

        @Override
        public T get(int index) {
            return list.get(index % list.size());
        }

        @Override
        public int size() {
            return repeats * list.size();
        }
    }
}
