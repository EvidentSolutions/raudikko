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

package fi.evident.raudikko.internal.fst;

import fi.evident.raudikko.analysis.AnalysisClass;
import fi.evident.raudikko.internal.morphology.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed class Symbol permits Diacritic {

    private final @NotNull String s;

    static final @NotNull Symbol FINAL = new Symbol("<final>");

    public Symbol(@NotNull String s) {
        this.s = s;
    }

    public boolean isEpsilon() {
        return this == Diacritic.EPSILON;
    }

    public @Nullable Diacritic getDiacritic() {
        return this instanceof Diacritic d ? d : null;
    }

    public boolean isFinal() {
        return this == FINAL;
    }

    public boolean isChar() {
        return s.length() == 1;
    }

    public boolean isDiacritic() {
        return this instanceof Diacritic;
    }

    public @NotNull Symbol toOutputSymbol() {
        return isDiacritic() ? Diacritic.EPSILON : this;
    }

    public char charValue() {
        assert s.length() == 1 : "not a char '" + s + "'";
        return s.charAt(0);
    }

    @Override
    public String toString() {
        return s;
    }

    public boolean matches(@NotNull String s) {
        return this.s.equals(s);
    }

    public boolean matches(@NotNull AnalysisClass c) {
        String tag = c.getMorphologyTag();
        return s.length() == tag.length() + 2 && s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']' && s.indexOf(tag) == 1;
    }

    public boolean isNameTag() {
        return startsWith(Tags.PREFIX_LE);
    }

    public boolean isXParameter() {
        return startsWith(Tags.PREFIX_X);
    }

    public boolean isClassTag() {
        return startsWith(Tags.PREFIX_L);
    }

    public boolean isBaseFormTag() {
        return matches(Tags.xp) || matches(Tags.xj);
    }

    public boolean isBoundary() {
        return startsWith(Tags.PREFIX_B);
    }

    private boolean startsWith(@NotNull String s) {
        return this.s.startsWith(s);
    }
}

final class Diacritic extends Symbol {

    public final @NotNull TransducerOperation op;
    public final short feature;
    public final short value;
    public static final short Neutral = 0;
    public static final short Any = 1;
    public static final @NotNull Diacritic EPSILON = new Diacritic("", TransducerOperation.C, (short) 0, (short) 0);

    private Diacritic(@NotNull String s, @NotNull TransducerOperation op, short feature, short value) {
        super(s);
        this.op = op;
        this.feature = feature;
        this.value = value;
    }

    static @NotNull Diacritic parse(@NotNull String symbol,
                                    @NotNull SymbolMap features,
                                    @NotNull SymbolMap values) {

        if (symbol.length() < 4)
            throw new IllegalArgumentException("Malformed flag diacritic: '" + symbol + "'.");

        String featureAndValue = symbol.substring(3, symbol.length() - 1);
        int valueStart = featureAndValue.indexOf('.');
        String feature = (valueStart != -1) ? featureAndValue.substring(0, valueStart) : featureAndValue;
        String value = (valueStart != -1) ? featureAndValue.substring(valueStart + 1) : "@";

        return new Diacritic(symbol, TransducerOperation.forCode(symbol.charAt(1)), features.getCode(feature), values.getCode(value));
    }
}
