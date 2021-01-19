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

import static fi.evident.raudikko.internal.utils.StringUtils.endsWithChar;

final class BasicAttributes {

    static void parseBasicAttributes(@NotNull Analysis analysis, @NotNull SymbolBuffer tokenizer) {
        boolean convertNimiLaatusanaToLaatusana = false;
        boolean bcPassed = false;

        tokenizer.moveToEnd();

        while (tokenizer.previousToken()) {
            Symbol tag = tokenizer.getCurrentTag();
            if (tag == null) continue;

            char tagClass = tag.toString().charAt(1);
            switch (tagClass) {
                case 'L':
                    if (analysis.getWordClass() == null) {
                        if (tag.matches(Tags.lnl)) {
                            String comp = analysis.getComparison();
                            analysis.setWordClass(convertNimiLaatusanaToLaatusana || Objects.equals(comp, "comparative") || Objects.equals(comp, "superlative") || tokenizer.firstTokenIs(Tags.lu) ? "laatusana" : "nimisana_laatusana");
                        } else
                            analysis.setWordClass(Attributes.CLASS.get(tag.toString()));
                    }
                    break;
                case 'N':
                    if (analysis.getNumber() == null && (analysis.getWordClass() == null || !analysis.getWordClass().equals("etuliite") && !analysis.getWordClass().equals("seikkasana")))
                        analysis.setNumber(Attributes.NUMBER.get(tag.toString()));
                    break;
                case 'P':
                    if (analysis.getPerson() == null)
                        analysis.setPerson(Attributes.PERSON.get(tag.toString()));
                    break;
                case 'S':
                    if (analysis.getWordClass() == null || !analysis.getWordClass().equals("etuliite") && !analysis.getWordClass().equals("seikkasana")) {
                        if (analysis.getSijamuoto() == null)
                            analysis.setSijamuoto(Attributes.SIJAMUOTO.get(tag.toString()));
                        if (tag.matches(Tags.ssti))
                            convertNimiLaatusanaToLaatusana = true;
                    }
                    break;
                case 'T':
                    if (analysis.getMood() == null && analysis.getWordClass() == null)
                        analysis.setMood(Attributes.MOOD.get(tag.toString()));
                    break;
                case 'A':
                    if (analysis.getTense() == null)
                        analysis.setTense(Attributes.TENSE.get(tag.toString()));
                    break;
                case 'F':
                    if (tag.matches(Tags.fko))
                        analysis.setKysymysliite(true);
                    else if (analysis.getFocus() == null)
                        analysis.setFocus(Attributes.FOCUS.get(tag.toString()));
                    break;
                case 'O':
                    if (analysis.getPossessive() == null)
                        analysis.setPossessive(Attributes.POSSESSIVE.get(tag.toString()));
                    break;
                case 'C':
                    if (analysis.getWordClass() == null && analysis.getComparison() == null)
                        analysis.setComparison(Attributes.COMPARISON.get(tag.toString()));
                    break;
                case 'E':
                    if (analysis.getNegative() == null)
                        analysis.setNegative(Attributes.NEGATIVE.get(tag.toString()));
                    break;
                case 'R':
                    // TODO: Checking the end for [Ln] is done to handle -tUAnne ("kuunneltuanne"). This is for compatibility
                    // with Malaga implementation. See VISK § 543 (temporaalirakenne) for correct analysis.
                    if (analysis.getParticiple() == null && !bcPassed)
                        if (analysis.getWordClass() == null || analysis.getWordClass().equals("laatusana") || tokenizer.lastTokenIsTag(Tags.ln))
                            analysis.setParticiple(Attributes.PARTICIPLE.get(tag.toString()));
                    break;
                case 'I':
                    addInfoFlag(tag, analysis, tokenizer);
                    break;
                case 'B':
                    if (tag.matches(Tags.bc) && analysis.getWordClass() == null) {
                        // is preceded by "-" or "-[Bh]"?
                        boolean match = false;
                        if (tokenizer.previousToken()) {
                            if (endsWithChar(tokenizer.currentToken, '-'))
                                match = true;
                            else if (tokenizer.matchesTag(Tags.bh) && tokenizer.previousToken()) {
                                match = endsWithChar(tokenizer.currentToken, '-');
                                tokenizer.nextToken();
                            }

                            tokenizer.nextToken();
                        }

                        if (match) {
                            analysis.setWordClass("etuliite");
                            bcPassed = true;
                        }
                    }
                    break;
            }
        }

        postProcess(analysis);
    }

    private static void postProcess(@NotNull Analysis analysis) {
        if (analysis.getNegative() != null && ((analysis.getWordClass() != null && !analysis.getWordClass().equals("teonsana")) || (Objects.equals(analysis.getMood(), "MINEN-infinitive") || Objects.equals(analysis.getMood(), "E-infinitive") || Objects.equals(analysis.getMood(), "MA-infinitive"))))
            analysis.setNegative(null);

        if (analysis.getParticiple() != null && analysis.getParticiple().equals("past_passive"))
            analysis.setWordClass("laatusana");

        if (analysis.getNumber() != null && Objects.equals(analysis.getSijamuoto(), "kerrontosti"))
            analysis.setNumber(null);

        if (analysis.getComparison() == null) {
            if (Objects.equals(analysis.getWordClass(), "laatusana") || Objects.equals(analysis.getWordClass(), "nimisana_laatusana"))
                analysis.setComparison("positive");

        } else if (Objects.equals(analysis.getWordClass(), "nimisana"))
            analysis.setComparison(null);
    }

    private static void addInfoFlag(@NotNull Symbol tag, @NotNull Analysis analysis, @NotNull SymbolBuffer tokenizer) {
        if (tag.matches(Tags.ivj)) {
            if (!tokenizer.fullContents().startsWith("-")) // TODO don't call fullContents
                analysis.setMalagaVapaaJalkiosa(true);
        } else if (tag.matches(Tags.ica)) {
            String className = analysis.getWordClass();
            if (!tokenizer.containsTagAfterCurrent(Tags.bc) && !tokenizer.containsTagAfterCurrent(Tags.ll) && (className == null || className.startsWith("nimisana")))
                analysis.setPossibleGeographicalName(true);
        } else {
            String className = analysis.getWordClass();
            String mood = analysis.getMood();

            if ((mood == null || (!mood.equals("E-infinitive") && !mood.equals("MINEN-infinitive") && !mood.equals("MA-infinitive"))) &&
                    (className == null || className.equals("teonsana"))) {

                if (tag.matches(Tags.ira))
                    analysis.setRequireFollowingVerb("A-infinitive");
                else if (tag.matches(Tags.irm))
                    analysis.setRequireFollowingVerb("MA-infinitive");
            }
        }
    }
}
