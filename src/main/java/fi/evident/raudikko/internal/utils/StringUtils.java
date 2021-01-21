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

public final class StringUtils {

    private StringUtils() {
    }

    public static @NotNull String replaceCharAt(@NotNull String s, int i, char c) {
        char[] chars = s.toCharArray();
        chars[i] = c;
        return new String(chars);
    }

    public static @NotNull String withoutChar(@NotNull CharSequence s, char removed) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            if (c != removed)
                sb.append(c);
        }

        return sb.toString();
    }

    public static int countOccurrences(@NotNull CharSequence s, char c) {
        int count = 0;
        for (int i = 0, len = s.length(); i < len; i++)
            if (s.charAt(i) == c)
                count++;
        return count;
    }

    public static boolean endsWithChar(@NotNull CharSequence s, char c) {
        int len = s.length();
        return len != 0 && s.charAt(len - 1) == c;
    }

    public static boolean startsWithChar(@NotNull CharSequence s, char c) {
        int len = s.length();
        return len != 0 && s.charAt(0) == c;
    }

    public static @NotNull String capitalize(@NotNull String s) {
        if (s.isEmpty()) return s;

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static boolean matchesAt(@NotNull CharSequence haystack, int offset, @NotNull CharSequence needle) {
        if (offset < 0 || offset + needle.length() > haystack.length())
            return false;

        for (int i = 0; i < needle.length(); i++)
            if (haystack.charAt(i + offset) != needle.charAt(i))
                return false;

        return true;
    }
}
