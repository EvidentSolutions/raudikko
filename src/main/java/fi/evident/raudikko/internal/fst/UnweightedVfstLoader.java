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

import fi.evident.raudikko.internal.utils.MyInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Loads [UnweightedTransducer] from [VFST-file](https://github.com/voikko/corevoikko/wiki/vfst-fileformat).
 */
public final class UnweightedVfstLoader {

    private static final int HEADER_SIZE = 16;
    private static final int TRANSITION_ALIGNMENT = 8;

    @NotNull
    public static UnweightedTransducer load(@NotNull InputStream inputStream) throws IOException {
        MyInputStream stream = new MyInputStream(inputStream);
        stream.skipNBytes(HEADER_SIZE);

        SymbolMap features = new SymbolMap();
        SymbolMap values = new SymbolMap();

        // initialize these to 0 and 1 (Neutral and Any)
        values.getCode("");
        values.getCode("@");

        short symbolCount = stream.readShort();
        ArrayList<Symbol> symbols = new ArrayList<>(symbolCount);
        for (short i = 0; i < symbolCount; i++) {
            String s = stream.readUtf8String();

            if (i == 0)
                symbols.add(Diacritic.EPSILON);
            else if (s.charAt(0) == '@')
                symbols.add(Diacritic.parse(s, features, values));
            else
                symbols.add(new Symbol(s));
        }

        int partial = stream.getPosition() % TRANSITION_ALIGNMENT;
        if (partial != 0)
            stream.skipNBytes(TRANSITION_ALIGNMENT - partial);

        ArrayList<TransitionData> transitions = new ArrayList<>();
        while (stream.hasMore()) {
            short symIn = stream.readShort();
            short symOut = stream.readShort();
            int targetState = stream.readInt24();
            int moreTransitions = stream.readByte();

            assert symIn >= -1;
            assert symOut >= 0;

            boolean overflow = moreTransitions == 0xff;

            if (overflow) {
                moreTransitions = stream.readInt();
                int padding = stream.readInt();
                assert padding == 0;
            }

            Symbol in = (symIn == -1) ? Symbol.FINAL : symbols.get(symIn);
            Symbol out = symbols.get(symOut);
            transitions.add(new TransitionData(in, out, targetState, moreTransitions));

            if (overflow)
                transitions.add(null); // add null to keep indexes correct
        }

        Set<Integer> targets = new HashSet<>();
        targets.add(0);
        for (TransitionData transition : transitions)
            if (transition != null)
                targets.add(transition.targetState);

        Map<Integer, State> states = new HashMap<>();
        for (int i = 0; i < transitions.size(); i++)
            if (targets.contains(i)) {
                TransitionData tr = transitions.get(i);
                assert (!tr.symIn.isFinal() || tr.moreTransitions == 0);
                states.put(i, new State());
            }

        for (int i = 0; i < transitions.size(); i++) {
            if (!targets.contains(i)) continue;

            TransitionData head = transitions.get(i);
            State state = states.get(i);

            if (head.symIn.isFinal()) {
                state.diacriticTransitions = new DiacriticTransition[0];
                state.charTransitions = new CharTransition[0];
            } else {
                ArrayList<DiacriticTransition> diacriticTransitions = new ArrayList<>();
                ArrayList<CharTransition> characterTransitions = new ArrayList<>();

                int offset = head.hasOverflow() ? 1 : 0;
                for (int j = 0; j < head.moreTransitions + 1 + offset; j++) {
                    if (j == 1 && head.hasOverflow()) continue;
                    TransitionData data = transitions.get(i + j);

                    State targetState = states.get(data.targetState);
                    Diacritic diacritic = data.symIn.getDiacritic();
                    if (diacritic != null)
                        diacriticTransitions.add(new DiacriticTransition(diacritic, data.symOut, targetState));
                    else
                        characterTransitions.add(new CharTransition(data.symIn.charValue(), data.symOut, targetState));
                }

                state.diacriticTransitions = diacriticTransitions.toArray(new DiacriticTransition[0]);
                state.charTransitions = characterTransitions.toArray(new CharTransition[0]);
            }
        }

        return new UnweightedTransducer(symbols, states.get(0), features.size());
    }

    private static final class TransitionData {

        final @NotNull Symbol symIn;
        final @NotNull Symbol symOut;
        final int targetState;
        final int moreTransitions;

        TransitionData(@NotNull Symbol symIn, @NotNull Symbol symOut, int targetState, int moreTransitions) {
            this.symIn = symIn;
            this.symOut = symOut.toOutputSymbol();
            this.targetState = targetState;
            this.moreTransitions = moreTransitions;
        }

        boolean hasOverflow() {
            return moreTransitions >= 255;
        }
    }
}
