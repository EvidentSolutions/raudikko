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
import fi.evident.raudikko.analysis.*;
import fi.evident.raudikko.internal.fst.Symbol;
import org.jetbrains.annotations.NotNull;

import static fi.evident.raudikko.analysis.Comparison.COMPARATIVE;
import static fi.evident.raudikko.analysis.Comparison.SUPERLATIVE;
import static fi.evident.raudikko.analysis.Locative.INSTRUCTIVE_STI;
import static fi.evident.raudikko.analysis.Mood.INFINITIVE1;
import static fi.evident.raudikko.analysis.Mood.INFINITIVE3;
import static fi.evident.raudikko.analysis.Participle.PAST_PASSIVE;
import static fi.evident.raudikko.analysis.WordClass.*;

final class BasicAttributes {

    private static final @NotNull TaggedValueLookupTable<WordClass> wordClasses = new TaggedValueLookupTable<>(WordClass.class);
    private static final @NotNull TaggedValueLookupTable<Comparison> comparisons = new TaggedValueLookupTable<>(Comparison.class);
    private static final @NotNull TaggedValueLookupTable<Locative> locatives = new TaggedValueLookupTable<>(Locative.class);
    private static final @NotNull TaggedValueLookupTable<Mood> moods = new TaggedValueLookupTable<>(Mood.class);
    private static final @NotNull TaggedValueLookupTable<GrammaticalNumber> grammaticalNumbers = new TaggedValueLookupTable<>(GrammaticalNumber.class);
    private static final @NotNull TaggedValueLookupTable<Negative> negatives = new TaggedValueLookupTable<>(Negative.class);
    private static final @NotNull TaggedValueLookupTable<Participle> participles = new TaggedValueLookupTable<>(Participle.class);
    private static final @NotNull TaggedValueLookupTable<Person> persons = new TaggedValueLookupTable<>(Person.class);
    private static final @NotNull TaggedValueLookupTable<Tense> tenses = new TaggedValueLookupTable<>(Tense.class);
    private static final @NotNull TaggedValueLookupTable<FocusParticle> focusParticles = new TaggedValueLookupTable<>(FocusParticle.class);
    private static final @NotNull TaggedValueLookupTable<Possessive> possessives = new TaggedValueLookupTable<>(Possessive.class);

    static void parseBasicAttributes(@NotNull Analysis analysis, @NotNull SymbolBuffer tokenizer) {
        boolean convertNimiLaatusanaToLaatusana = false;
        boolean bcPassed = false;

        tokenizer.moveToEnd();

        while (tokenizer.previousToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag == null) continue;

            switch (tag.toString().charAt(1)) {
                case 'L' -> {
                    if (analysis.getWordClass() == null) {
                        if (tag.matches(NOUN_ADJECTIVE))
                            analysis.setWordClass(convertNimiLaatusanaToLaatusana || analysis.getComparison() == COMPARATIVE || analysis.getComparison() == SUPERLATIVE || tokenizer.firstTokenIs(NUMERAL) ? ADJECTIVE : NOUN_ADJECTIVE);
                        else
                            analysis.setWordClass(wordClasses.get(tag));
                    }
                }
                case 'N' -> {
                    if (analysis.getNumber() == null && analysis.getWordClass() != PREFIX && analysis.getWordClass() != ADVERB)
                        analysis.setNumber(grammaticalNumbers.get(tag));
                }
                case 'P' -> {
                    if (analysis.getPerson() == null)
                        analysis.setPerson(persons.get(tag));
                }
                case 'S' -> {
                    if (analysis.getWordClass() != PREFIX && analysis.getWordClass() != ADVERB) {
                        if (analysis.getLocative() == null)
                            analysis.setLocative(locatives.get(tag));
                        if (tag.matches(INSTRUCTIVE_STI))
                            convertNimiLaatusanaToLaatusana = true;
                    }
                }
                case 'T' -> {
                    if (analysis.getMood() == null && analysis.getWordClass() == null)
                        analysis.setMood(moods.get(tag));
                }
                case 'A' -> {
                    if (analysis.getTense() == null)
                        analysis.setTense(tenses.get(tag));
                }
                case 'F' -> {
                    if (tag.matches(Tags.fko))
                        analysis.setInterrogative(true);
                    else if (analysis.getFocus() == null)
                        analysis.setFocus(focusParticles.get(tag));
                }
                case 'O' -> {
                    if (analysis.getPossessive() == null)
                        analysis.setPossessive(possessives.get(tag));
                }
                case 'C' -> {
                    if (analysis.getWordClass() == null && analysis.getComparison() == null)
                        analysis.setComparison(comparisons.get(tag));
                }
                case 'E' -> {
                    if (analysis.getNegative() == null)
                        analysis.setNegative(negatives.get(tag));
                }
                case 'R' -> {
                    // TODO: Checking the end for [Ln] is done to handle -tUAnne ("kuunneltuanne"). This is for compatibility
                    // with Malaga implementation. See VISK § 543 (temporaalirakenne) for correct analysis.
                    if (analysis.getParticiple() == null && !bcPassed)
                        if (analysis.getWordClass() == null || analysis.getWordClass() == ADJECTIVE || tokenizer.lastTokenIs(NOUN))
                            analysis.setParticiple(participles.get(tag));
                }
                case 'I' -> addInfoFlag(tag, analysis, tokenizer);
                case 'B' -> {
                    if (tag.matches(Tags.bc) && analysis.getWordClass() == null) {
                        // is preceded by "-" or "-[Bh]"?
                        boolean match = tokenizer.relativeTokenEndsWithChar(-1, '-')
                            || (tokenizer.previousTokenIsTag(Tags.bh) && tokenizer.relativeTokenEndsWithChar(-2, '-'));

                        if (match) {
                            analysis.setWordClass(PREFIX);
                            bcPassed = true;
                        }
                    }
                }
            }
        }

        postProcess(analysis);
    }

    private static void postProcess(@NotNull Analysis analysis) {
        Mood mood = analysis.getMood();

        if (analysis.getNegative() != null && ((analysis.getWordClass() != null && analysis.getWordClass() != VERB) || (mood != null && mood.isSecondThirdOrFourthInfinitive())))
            analysis.setNegative(null);

        if (analysis.getParticiple() == PAST_PASSIVE)
            analysis.setWordClass(ADJECTIVE);

        if (analysis.getNumber() != null && analysis.getLocative() == INSTRUCTIVE_STI)
            analysis.setNumber(null);

        if (analysis.getComparison() == null) {
            if (analysis.getWordClass() == ADJECTIVE || analysis.getWordClass() == NOUN_ADJECTIVE)
                analysis.setComparison(Comparison.POSITIVE);

        } else if (analysis.getWordClass() == NOUN)
            analysis.setComparison(null);
    }

    private static void addInfoFlag(@NotNull Symbol tag, @NotNull Analysis analysis, @NotNull SymbolBuffer tokenizer) {
        if (tag.matches(Tags.ivj)) {
            if (!tokenizer.firstTokenStartsWith('-'))
                analysis.setMalagaVapaaJalkiosa(true);
        } else if (tag.matches(Tags.ica)) {
            WordClass wordClass = analysis.getWordClass();
            if (!tokenizer.containsTagAfterCurrent(Tags.bc) && !tokenizer.containsTagAfterCurrent(ADJECTIVE) && (wordClass == null || wordClass == NOUN || wordClass == NOUN_ADJECTIVE))
                analysis.setPossibleGeographicalName(true);
        } else {
            WordClass wordClass = analysis.getWordClass();
            Mood mood = analysis.getMood();

            if ((mood == null || !mood.isSecondThirdOrFourthInfinitive()) && (wordClass == null || wordClass == VERB)) {
                if (tag.matches(Tags.ira))
                    analysis.setRequireFollowingVerb(INFINITIVE1);
                else if (tag.matches(Tags.irm))
                    analysis.setRequireFollowingVerb(INFINITIVE3);
            }
        }
    }
}
