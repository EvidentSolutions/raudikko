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

package fi.evident.raudikko;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of analyzing a word.
 *
 * Note that some of the properties are named in English and some are in Finnish.
 * This is for naming compatibility with Voikko.
 */
public final class Analysis implements Cloneable {

    private @Nullable String baseForm;
    private @Nullable String wordClass;
    private @Nullable String sijamuoto;
    private @Nullable String comparison;
    private @Nullable String focus;
    private @Nullable String fstOutput;
    private @Nullable String structure;
    private @Nullable String number;
    private @Nullable String negative;
    private @Nullable String mood;
    private @Nullable String participle;
    private @Nullable String person;
    private @Nullable String possessive;
    private @Nullable String tense;
    private boolean kysymysliite = false;
    private boolean malagaVapaaJalkiosa = false;
    private boolean possibleGeographicalName = false;
    private @Nullable String requireFollowingVerb;
    private @Nullable List<String> baseFormParts;

    public @Nullable String getBaseForm() {
        return baseForm;
    }

    public void setBaseForm(@Nullable String baseForm) {
        this.baseForm = baseForm;
    }

    public @Nullable String getWordClass() {
        return wordClass;
    }

    public void setWordClass(@Nullable String wordClass) {
        this.wordClass = wordClass;
    }

    public @Nullable String getSijamuoto() {
        return sijamuoto;
    }

    public void setSijamuoto(@Nullable String sijamuoto) {
        this.sijamuoto = sijamuoto;
    }

    public @Nullable String getNumber() {
        return number;
    }

    public void setNumber(@Nullable String number) {
        this.number = number;
    }

    public @Nullable String getComparison() {
        return comparison;
    }

    public void setComparison(@Nullable String comparison) {
        this.comparison = comparison;
    }

    public @Nullable String getFocus() {
        return focus;
    }

    public void setFocus(@Nullable String focus) {
        this.focus = focus;
    }

    public @Nullable String getFstOutput() {
        return fstOutput;
    }

    public void setFstOutput(@Nullable String fstOutput) {
        this.fstOutput = fstOutput;
    }

    public @Nullable String getStructure() {
        return structure;
    }

    public void setStructure(@Nullable String structure) {
        this.structure = structure;
    }

    public @Nullable String getMood() {
        return mood;
    }

    public void setMood(@Nullable String mood) {
        this.mood = mood;
    }

    public @Nullable String getParticiple() {
        return participle;
    }

    public void setParticiple(@Nullable String participle) {
        this.participle = participle;
    }

    public @Nullable String getNegative() {
        return negative;
    }

    public void setNegative(@Nullable String negative) {
        this.negative = negative;
    }

    public boolean isKysymysliite() {
        return kysymysliite;
    }

    public void setKysymysliite(boolean kysymysliite) {
        this.kysymysliite = kysymysliite;
    }

    public @Nullable String getPerson() {
        return person;
    }

    public void setPerson(@Nullable String person) {
        this.person = person;
    }

    public @Nullable String getPossessive() {
        return possessive;
    }

    public void setPossessive(@Nullable String possessive) {
        this.possessive = possessive;
    }

    public @Nullable String getTense() {
        return tense;
    }

    public void setTense(@Nullable String tense) {
        this.tense = tense;
    }

    public boolean isMalagaVapaaJalkiosa() {
        return malagaVapaaJalkiosa;
    }

    public void setMalagaVapaaJalkiosa(boolean malagaVapaaJalkiosa) {
        this.malagaVapaaJalkiosa = malagaVapaaJalkiosa;
    }

    public boolean isPossibleGeographicalName() {
        return possibleGeographicalName;
    }

    public void setPossibleGeographicalName(boolean possibleGeographicalName) {
        this.possibleGeographicalName = possibleGeographicalName;
    }

    public @Nullable String getRequireFollowingVerb() {
        return requireFollowingVerb;
    }

    public void setRequireFollowingVerb(@Nullable String requireFollowingVerb) {
        this.requireFollowingVerb = requireFollowingVerb;
    }

    public void setBaseFormParts(@Nullable List<String> baseFormParts) {
        this.baseFormParts = baseFormParts;
    }

    public @Nullable List<String> getBaseFormParts() {
        return baseFormParts;
    }

    @Override
    public @NotNull Analysis clone() {
        try {
            return (Analysis) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Converts the results of the analysis in same kind of dictionary that Voikko returns.
     */
    public @NotNull Map<String, String> toVoikkoFormat() {
        Map<String, String> map = new HashMap<>();

        putIfNotNull(map, "BASEFORM", baseForm);
        putIfNotNull(map, "CLASS", wordClass);
        putIfNotNull(map, "SIJAMUOTO", sijamuoto);
        putIfNotNull(map, "COMPARISON", comparison);
        putIfNotNull(map, "FOCUS", focus);
        putIfNotNull(map, "FSTOUTPUT", fstOutput);
        putIfNotNull(map, "STRUCTURE", structure);
        putIfNotNull(map, "NUMBER", number);
        putIfNotNull(map, "NEGATIVE", negative);
        putIfNotNull(map, "MOOD", mood);
        putIfNotNull(map, "PARTICIPLE", participle);
        putIfNotNull(map, "PERSON", person);
        putIfNotNull(map, "POSSESSIVE", possessive);
        putIfNotNull(map, "TENSE", tense);
        putIfNotNull(map,"REQUIRE_FOLLOWING_VERB", requireFollowingVerb);
        putIfTrue(map, "KYSYMYSLIITE", kysymysliite);
        putIfTrue(map, "MALAGA_VAPAA_JALKIOSA", malagaVapaaJalkiosa);
        putIfTrue(map, "POSSIBLE_GEOGRAPHICAL_NAME", possibleGeographicalName);

        return map;
    }

    private static void putIfNotNull(@NotNull Map<String, String> map, @NotNull String key, @Nullable String value) {
        if (value != null)
            map.put(key, value);
    }

    private static void putIfTrue(@NotNull Map<String, String> map, @NotNull String key, boolean value) {
        if (value)
            map.put(key, "true");
    }

    @Override
    public String toString() {
        return "{" +
                "baseForm='" + baseForm + '\'' +
                ", wordClass='" + wordClass + '\'' +
                ", sijamuoto='" + sijamuoto + '\'' +
                ", comparison='" + comparison + '\'' +
                ", focus='" + focus + '\'' +
                ", fstOutput='" + fstOutput + '\'' +
                ", structure='" + structure + '\'' +
                ", number='" + number + '\'' +
                ", negative='" + negative + '\'' +
                ", mood='" + mood + '\'' +
                ", participle='" + participle + '\'' +
                ", person='" + person + '\'' +
                ", possessive='" + possessive + '\'' +
                ", tense='" + tense + '\'' +
                ", kysymysliite=" + kysymysliite +
                ", malagaVapaaJalkiosa=" + malagaVapaaJalkiosa +
                ", possibleGeographicalName=" + possibleGeographicalName +
                ", requireFollowingVerb='" + requireFollowingVerb + '\'' +
                ", baseFormParts='" + baseFormParts + '\'' +
                '}';
    }
}
