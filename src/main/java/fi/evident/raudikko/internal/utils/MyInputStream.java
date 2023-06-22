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

package fi.evident.raudikko.internal.utils;

import org.jetbrains.annotations.NotNull;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class MyInputStream {

    @NotNull
    private final BufferedInputStream stream;

    private int position = 0;

    public MyInputStream(@NotNull InputStream stream) {
        this.stream = (stream instanceof BufferedInputStream s) ? s : new BufferedInputStream(stream);
    }

    public int readByte() throws IOException {
        int b = stream.read();
        if (b == -1)
            throw new EOFException();

        position++;
        return b;
    }

    public short readShort() throws IOException {
        int b1 = readByte();
        int b2 = readByte();

        return (short) ((b2 << 8 | b1));
    }

    public int readInt() throws IOException {
        int b1 = readByte();
        int b2 = readByte();
        int b3 = readByte();
        int b4 = readByte();

        return ((b4 << 24) | (b3 << 16) | (b2 << 8) | b1);
    }

    public int readInt24() throws IOException {
        int b1 = readByte();
        int b2 = readByte();
        int b3 = readByte();

        return ((b3 << 16) | (b2 << 8) | b1);
    }

    @NotNull
    public String readUtf8String() throws IOException {
        ByteArrayOutputStream strBytes = new ByteArrayOutputStream();
        strBytes.reset();

        while (true) {
            int b = readByte();
            if (b == 0) break;
            strBytes.write(b);
        }

        return strBytes.toString(UTF_8);
    }

    public boolean hasMore() throws IOException {
        stream.mark(1);
        boolean hasMore = stream.read() != -1;
        stream.reset();
        return hasMore;
    }

    public void skipNBytes(int n) throws IOException {
        for (int i = 0; i < n; i++)
            if (stream.read() == -1)
                throw new EOFException();
        position += n;
    }

    public int getPosition() {
        return position;
    }
}
