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

import fi.evident.raudikko.internal.fst.Symbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;

public final class SymbolBuffer {

    private final @NotNull StringBuilder textBuffer;
    private final @Nullable Symbol[] tags;
    private int index;
    private final int[] startIndices;
    private int tokenCount;

    public SymbolBuffer(int bufferSize) {
        this(new StringBuilder(bufferSize), new Symbol[bufferSize], -1, new int[bufferSize / 2], 0);
    }

    private SymbolBuffer(@NotNull StringBuilder textBuffer, @Nullable Symbol[] tags, int index, int[] startIndices, int tokenCount) {
        this.textBuffer = textBuffer;
        this.tags = tags;
        this.index = index;
        this.startIndices = startIndices;
        this.tokenCount = tokenCount;
    }

    @TestOnly
    static @NotNull SymbolBuffer parse(@NotNull String cs) {
        ArrayList<Symbol> symbols = new ArrayList<>();

        int offset = 0;
        while (offset < cs.length()) {
            if (cs.charAt(offset) == '[') {
                int i = cs.indexOf("]", offset + 1);
                i = (i == -1) ? cs.length() : i + 1;
                symbols.add(new Symbol(cs.substring(offset, i)));
                offset = i;
            } else {
                symbols.add(new Symbol(cs.substring(offset, offset + 1)));
                offset++;
            }
        }

        SymbolBuffer buffer = new SymbolBuffer(2000);
        buffer.reset(symbols.toArray(new Symbol[0]), symbols.size());
        return buffer;
    }

    public void reset(@NotNull Symbol[] symbols, int length) {
        textBuffer.setLength(0);
        int index = 0;

        boolean previousChar = false;
        for (int i = 0; i < length; i++) {
            Symbol symbol = symbols[i];

            if (symbol.isChar()) {
                if (!previousChar) {
                    previousChar = true;
                    tags[index] = null;
                    startIndices[index] = textBuffer.length();
                    index++;
                }
                textBuffer.append(symbol);

            } else if (!symbol.isEpsilon()) {
                tags[index] = symbol;
                startIndices[index] = textBuffer.length();
                index++;
                textBuffer.append(symbol);
                previousChar = false;
            }
        }

        tokenCount = index;
        startIndices[index] = textBuffer.length();
    }

    public @NotNull String fullContents() {
        return textBuffer.toString();
    }

    void moveToStart() {
        index = -1;
    }

    void moveToEnd() {
        index = tokenCount;
    }

    public int getIndex() {
        return index;
    }

    private int getStart() {
        return startIndices[index];
    }

    private int getEnd() {
        return startIndices[index + 1];
    }

    int getTotalLength() {
        return textBuffer.length();
    }

    boolean isAtFirstToken() {
        return index == 0;
    }

    boolean isAtLastToken() {
        return index == tokenCount - 1;
    }

    boolean nextToken() {
        if (index + 1 < tokenCount) {
            index++;
            return true;

        } else {
            return false;
        }
    }

    boolean previousToken() {
        if (index > 0) {
            index--;
            return true;
        } else {
            return false;
        }
    }

    public boolean nextTokenIsTag(@NotNull String tag) {
        int nextIndex = index + 1;
        if (nextIndex < tokenCount) {
            Symbol nextTag = tags[nextIndex];
            return nextTag != null && nextTag.matches(tag);
        }
        return false;
    }

    public boolean firstTokenIs(@NotNull  String tag) {
        if (tokenCount == 0) return false;
        Symbol first = tags[0];
        return first != null && first.matches(tag);
    }

    public boolean lastTokenIsTag(@NotNull String tag) {
        if (tokenCount == 0) return false;
        Symbol last = tags[tokenCount-1];
        return last != null && last.matches(tag);
    }

    @NotNull String readXTagContents() {
        nextToken();
        if (matchesTag(Tags.x)) {
            return "";
        } else {
            String content = currentToken.toString();
            nextToken();
            assert(matchesTag(Tags.x)) : currentToken + " from " + fullContents();
            return content;
        }
    }

    void skipXTag() {
        while (nextToken())
            if (matchesTag(Tags.x))
                break;
    }

    /**
     * Returns a shallow copy of current state. Since the copy shares the analyzed
     * indices with the original, it won't survive a reindex.
     */
    @NotNull SymbolBuffer copy() {
        return new SymbolBuffer(textBuffer, tags, index, startIndices, tokenCount);
    }

    @Nullable Symbol getCurrentTag() {
        return tags[index];
    }

    boolean matchesTag(@NotNull String s) {
        Symbol tag = getCurrentTag();
        return tag != null && tag.matches(s);
    }

    boolean containsTagAfterCurrent(@NotNull String s) {
        for (int i = index + 1; i < tokenCount; i++) {
            Symbol tag = tags[i];
            if (tag != null && tag.matches(s))
                return true;
        }
        return false;
    }

    /**
     * A CharSequence representing the current token. Note that to avoid allocation, this
     * sequence is only valid while the token is active
     */
    final @NotNull CharSequence currentToken = new CharSequence() {
        @Override
        public int length() {
            return getEnd() - getStart();
        }

        @Override
        public char charAt(int index) {
            return textBuffer.charAt(getStart() + index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            int offset = getStart();
            return textBuffer.subSequence(offset + start, offset + end);
        }

        @Override
        public @NotNull String toString() {
            int len = length();
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++)
                sb.append(charAt(i));
            return sb.toString();
        }
    };
}
