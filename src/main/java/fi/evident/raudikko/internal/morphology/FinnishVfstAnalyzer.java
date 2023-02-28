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

import fi.evident.raudikko.Analysis;
import fi.evident.raudikko.Analyzer;
import fi.evident.raudikko.AnalyzerConfiguration;
import fi.evident.raudikko.internal.fst.Symbol;
import fi.evident.raudikko.internal.fst.UnweightedTransducer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.raudikko.internal.morphology.BaseForm.parseBaseform;
import static fi.evident.raudikko.internal.morphology.BaseFormParts.parseBaseFormParts;
import static fi.evident.raudikko.internal.morphology.BasicAttributes.parseBasicAttributes;
import static fi.evident.raudikko.internal.morphology.Organization.organizationNameAnalysis;
import static fi.evident.raudikko.internal.morphology.Structure.parseStructure;
import static fi.evident.raudikko.internal.morphology.Validator.isValidAnalysis;
import static java.util.Objects.requireNonNull;

public final class FinnishVfstAnalyzer implements Analyzer {

    private final @NotNull UnweightedTransducer transducer;
    private final @NotNull List<Symbol> inputBuffer = new ArrayList<>(2000);
    private final @NotNull Symbol[] output = new Symbol[2000];
    private final @NotNull SymbolBuffer buffer = new SymbolBuffer(2000);
    private final @NotNull AnalyzerConfiguration configuration;
    private final @NotNull WordParser wordParser = new WordParser();
    private final short[] flags;
    private static final int MAX_WORD_LENGTH = 255;

    public FinnishVfstAnalyzer(@NotNull UnweightedTransducer transducer, @NotNull AnalyzerConfiguration configuration) {
        this.transducer = transducer;
        this.flags = new short[transducer.flagDiacriticFeatureCount];
        this.configuration = configuration;
    }

    @Override
    public @NotNull List<Analysis> analyze(@NotNull CharSequence word, int maxResults) {
        ArrayList<Analysis> results = new ArrayList<>();

        if (word.length() > MAX_WORD_LENGTH)
            return results;

        transducer.transduce(word, inputBuffer, flags, output, depth -> {
            buffer.reset(output, depth);

            if (isValidAnalysis(buffer))
                createAnalysis(buffer, word.length(), results, configuration, wordParser);
        });

        return results;
    }

    @Override
    public @NotNull List<String> baseForms(@NotNull CharSequence word) {
        ArrayList<String> results = new ArrayList<>();

        if (word.length() > MAX_WORD_LENGTH)
            return results;

        transducer.transduce(word, inputBuffer, flags, output, depth -> {
            buffer.reset(output, depth);

            if (isValidAnalysis(buffer)) {
                String structure = parseStructure(buffer, word.length());

                String baseForm = parseBaseform(buffer, structure);
                if (baseForm != null && !results.contains(baseForm))
                    results.add(baseForm);
            }
        });

        return results;
    }

    private static void createAnalysis(@NotNull SymbolBuffer buffer,
                                       int wordLength,
                                       @NotNull List<Analysis> results,
                                       @NotNull AnalyzerConfiguration configuration,
                                       @NotNull WordParser wordParser) {
        Analysis analysis = new Analysis();

        boolean dependsOnStructure =
                configuration.isIncludeStructure()
                        || configuration.isIncludeBaseForm()
                        || configuration.isIncludeOrganizationNameAnalysis();

        String structure = dependsOnStructure ? parseStructure(buffer, wordLength) : null;

        if (configuration.isIncludeStructure())
            analysis.setStructure(requireNonNull(structure));

        if (configuration.isIncludeBaseForm())
            analysis.setBaseForm(parseBaseform(buffer, requireNonNull(structure)));

        if (configuration.isIncludeFstOutput())
            analysis.setFstOutput(buffer.fullContents());

        if (configuration.isIncludeBaseFormParts())
            analysis.setBaseFormParts(parseBaseFormParts(buffer, wordParser));

        if (configuration.isIncludeWord())
            analysis.setWord(wordParser.parseWord(buffer));

        if (configuration.isIncludeBasicAttributes())
            parseBasicAttributes(analysis, buffer);

        results.add(analysis);

        if (configuration.isIncludeOrganizationNameAnalysis()) {
            Analysis organizationNameAnalysis = organizationNameAnalysis(analysis, buffer, requireNonNull(structure));
            if (organizationNameAnalysis != null)
                results.add(organizationNameAnalysis);
        }
    }
}
