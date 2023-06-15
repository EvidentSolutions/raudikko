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
import fi.evident.raudikko.internal.fst.Symbol;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static fi.evident.raudikko.internal.morphology.Attributes.*;

final class BasicAttributes {

    static void parseBasicAttributes(@NotNull Analysis analysis, @NotNull SymbolBuffer tokenizer) {
        boolean convertNimiLaatusanaToLaatusana = false;
        boolean bcPassed = false;

        tokenizer.moveToEnd();

        while (tokenizer.previousToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag == null) continue;

            switch (tag.toString().charAt(1)) {
                case 'L':
                    if (analysis.getWordClass() == null) {
                        if (tag.matches(Tags.lnl)) {
                            String comp = analysis.getComparison();
                            analysis.setWordClass(convertNimiLaatusanaToLaatusana || Objects.equals(comp, COMPARATIVE) || Objects.equals(comp, SUPERLATIVE) || tokenizer.firstTokenIs(Tags.lu) ? LAATUSANA : NIMISANA_LAATUSANA);
                        } else
                            analysis.setWordClass(resolveAttribute(tag));
                    }
                    break;
                case 'N':
                    if (analysis.getNumber() == null && (analysis.getWordClass() == null || !analysis.getWordClass().equals(ETULIITE) && !analysis.getWordClass().equals(SEIKKASANA)))
                        analysis.setNumber(resolveAttribute(tag));
                    break;
                case 'P':
                    if (analysis.getPerson() == null)
                        analysis.setPerson(resolveAttribute(tag));
                    break;
                case 'S':
                    if (analysis.getWordClass() == null || !analysis.getWordClass().equals(ETULIITE) && !analysis.getWordClass().equals(SEIKKASANA)) {
                        if (analysis.getSijamuoto() == null)
                            analysis.setSijamuoto(resolveAttribute(tag));
                        if (tag.matches(Tags.ssti))
                            convertNimiLaatusanaToLaatusana = true;
                    }
                    break;
                case 'T':
                    if (analysis.getMood() == null && analysis.getWordClass() == null)
                        analysis.setMood(resolveAttribute(tag));
                    break;
                case 'A':
                    if (analysis.getTense() == null)
                        analysis.setTense(resolveAttribute(tag));
                    break;
                case 'F':
                    if (tag.matches(Tags.fko))
                        analysis.setKysymysliite(true);
                    else if (analysis.getFocus() == null)
                        analysis.setFocus(resolveAttribute(tag));
                    break;
                case 'O':
                    if (analysis.getPossessive() == null)
                        analysis.setPossessive(resolveAttribute(tag));
                    break;
                case 'C':
                    if (analysis.getWordClass() == null && analysis.getComparison() == null)
                        analysis.setComparison(resolveAttribute(tag));
                    break;
                case 'E':
                    if (analysis.getNegative() == null)
                        analysis.setNegative(resolveAttribute(tag));
                    break;
                case 'R':
                    // TODO: Checking the end for [Ln] is done to handle -tUAnne ("kuunneltuanne"). This is for compatibility
                    // with Malaga implementation. See VISK § 543 (temporaalirakenne) for correct analysis.
                    if (analysis.getParticiple() == null && !bcPassed)
                        if (analysis.getWordClass() == null || analysis.getWordClass().equals(LAATUSANA) || tokenizer.lastTokenIsTag(Tags.ln))
                            analysis.setParticiple(resolveAttribute(tag));
                    break;
                case 'I':
                    addInfoFlag(tag, analysis, tokenizer);
                    break;
                case 'B':
                    if (tag.matches(Tags.bc) && analysis.getWordClass() == null) {
                        // is preceded by "-" or "-[Bh]"?
                        boolean match = tokenizer.relativeTokenEndsWithChar(-1, '-')
                            || (tokenizer.previousTokenIsTag(Tags.bh) && tokenizer.relativeTokenEndsWithChar(-2, '-'));

                        if (match) {
                            analysis.setWordClass(ETULIITE);
                            bcPassed = true;
                        }
                    }
                    break;
            }
        }

        postProcess(analysis);
    }

    private static void postProcess(@NotNull Analysis analysis) {
        if (analysis.getNegative() != null && ((analysis.getWordClass() != null && !analysis.getWordClass().equals(TEONSANA)) || (Objects.equals(analysis.getMood(), MINEN_INFINITIVE) || Objects.equals(analysis.getMood(), E_INFINITIVE) || Objects.equals(analysis.getMood(), MA_INFINITIVE))))
            analysis.setNegative(null);

        if (Objects.equals(analysis.getParticiple(), PAST_PASSIVE))
            analysis.setWordClass(LAATUSANA);

        if (analysis.getNumber() != null && Objects.equals(analysis.getSijamuoto(), KERRONTOSTI))
            analysis.setNumber(null);

        if (analysis.getComparison() == null) {
            if (Objects.equals(analysis.getWordClass(), LAATUSANA) || Objects.equals(analysis.getWordClass(), NIMISANA_LAATUSANA))
                analysis.setComparison(POSITIVE);

        } else if (Objects.equals(analysis.getWordClass(), NIMISANA))
            analysis.setComparison(null);
    }

    private static void addInfoFlag(@NotNull Symbol tag, @NotNull Analysis analysis, @NotNull SymbolBuffer tokenizer) {
        if (tag.matches(Tags.ivj)) {
            if (!tokenizer.firstTokenStartsWith('-'))
                analysis.setMalagaVapaaJalkiosa(true);
        } else if (tag.matches(Tags.ica)) {
            String className = analysis.getWordClass();
            if (!tokenizer.containsTagAfterCurrent(Tags.bc) && !tokenizer.containsTagAfterCurrent(Tags.ll) && (className == null || className.equals(NIMISANA) || className.equals(NIMISANA_LAATUSANA)))
                analysis.setPossibleGeographicalName(true);
        } else {
            String className = analysis.getWordClass();
            String mood = analysis.getMood();

            if ((mood == null || (!mood.equals(E_INFINITIVE) && !mood.equals(MINEN_INFINITIVE) && !mood.equals(MA_INFINITIVE))) && (className == null || className.equals(TEONSANA))) {
                if (tag.matches(Tags.ira))
                    analysis.setRequireFollowingVerb(A_INFINITIVE);
                else if (tag.matches(Tags.irm))
                    analysis.setRequireFollowingVerb(MA_INFINITIVE);
            }
        }
    }
}
