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

import fi.evident.raudikko.internal.utils.CharMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static java.lang.Character.toLowerCase;

public final class UnweightedTransducer {

    private final @NotNull CharMap<Symbol> charToSymbol = new CharMap<>();
    private final @NotNull State rootState;
    public final int flagDiacriticFeatureCount;

    public UnweightedTransducer(@NotNull List<Symbol> symbols,
                                @NotNull State rootState,
                                int flagDiacriticFeatureCount) {
        this.rootState = rootState;
        this.flagDiacriticFeatureCount = flagDiacriticFeatureCount;

        for (Symbol symbol : symbols)
            if (symbol.isChar())
                charToSymbol.put(symbol.charValue(), symbol);
    }

    public void transduce(@NotNull CharSequence input,
                          @NotNull List<Symbol> inputBuffer,
                          short[] flags,
                          @NotNull Symbol[] outputStack,
                          @NotNull Consumer<Integer> callback) {

        if (!prepareInput(inputBuffer, input))
            return;

        enterState(rootState, 0, inputBuffer, flags, outputStack, callback, 0);
    }

    private boolean prepareInput(@NotNull List<Symbol> inputSymbols, @NotNull CharSequence input) {
        inputSymbols.clear();

        for (int i = 0, len = input.length(); i < len; i++) {
            Symbol symbol = charToSymbol.get(toLowerCase(input.charAt(i)));
            if (symbol == null)
                return false;

            inputSymbols.add(symbol);
        }

        return true;
    }

    private void enterState(@NotNull State st,
                            int inputPos,
                            @NotNull List<Symbol> input,
                            short[] flags,
                            @NotNull Symbol[] output,
                            @NotNull Consumer<Integer> callback,
                            int depth) {
        if (depth >= output.length) return;

        if (st.isFinal() && inputPos == input.size()) {
            callback.accept(depth);
            return;
        }

        for (DiacriticTransition transition : st.diacriticTransitions) {
            Diacritic diacritic = transition.in;

            if (diacritic == Diacritic.EPSILON) {
                output[depth] = transition.symOut;
                enterState(transition.target, inputPos, input, flags, output, callback, depth + 1);

            } else {
                short oldValue = flags[diacritic.feature];
                if (flagDiacriticCheck(flags, diacritic, oldValue)) {
                    output[depth] = transition.symOut;
                    enterState(transition.target, inputPos, input, flags, output, callback, depth + 1);
                    flags[diacritic.feature] = oldValue;
                }
            }
        }

        if (inputPos < input.size()) {
            char ch  = input.get(inputPos).charValue();

            CharTransition[] transitions = st.charTransitions;
            for (int i = st.firstCharacterTransitionFor(ch); i < transitions.length; i++) {
                CharTransition transition = transitions[i];
                if (ch != transition.in) break;

                output[depth] = transition.symOut;
                enterState(transition.target, inputPos + 1, input, flags, output, callback, depth + 1);
            }
        }
    }

    private boolean flagDiacriticCheck(short[] flags, @NotNull Diacritic ofv, short value) {
        switch (ofv.op) {
            case P:
                flags[ofv.feature] = ofv.value;
                return true;
            case C:
                flags[ofv.feature] = Diacritic.Neutral;
                return true;
            case U: {
                if (value != Diacritic.Neutral)
                    return value == ofv.value;
                else
                    flags[ofv.feature] = ofv.value;
                return true;
            }
            case R: {
                boolean ok = (ofv.value != Diacritic.Any || value != Diacritic.Neutral) && (ofv.value == Diacritic.Any || value == ofv.value);
                if (ok)
                    flags[ofv.feature] = value;
                return ok;
            }
            case D: {
                boolean ok = (ofv.value != Diacritic.Any || value == Diacritic.Neutral) && value != ofv.value;
                if (ok)
                    flags[ofv.feature] = value;
                return ok;
            }
        }

        throw new UnsupportedOperationException("unknown operation: " + ofv.feature);
    }
}
