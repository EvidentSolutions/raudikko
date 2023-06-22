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

import org.junit.jupiter.api.Test;

import static fi.evident.raudikko.internal.utils.StringUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void testMatchesAt() {
        assertTrue(matchesAt("foo", 0, "foo"));
        assertTrue(matchesAt("foobar", 0, "foo"));
        assertFalse(matchesAt("foobar", 1, "foo"));

        assertTrue(matchesAt("foobar", 1, "oo"));
        assertTrue(matchesAt("foobar", 1, "oob"));
        assertTrue(matchesAt("foobar", 1, "oobar"));
        assertFalse(matchesAt("foobar", 1, "foobarbaz"));

        assertFalse(matchesAt("foobar", -1, "ofoo"));
    }

    @Test
    void testWithoutChar() {
        assertEquals("helloworld", withoutChar("hello world", ' '));
        assertEquals("hello world", withoutChar("hello world", 'x'));
    }

    @Test
    void testCountOccurrences() {
        assertEquals(3, countOccurrences("hello world", 'l'));
        assertEquals(0, countOccurrences("hello world", 'x'));
    }

    @Test
    void testEndsWithChar() {
        assertTrue(endsWithChar("hello", 'o'));
        assertFalse(endsWithChar("hello", 'l'));
    }

    @Test
    void testStartsWithChar() {
        assertTrue(startsWithChar("hello", 'h'));
        assertFalse(startsWithChar("hello", 'e'));
    }

    @Test
    void testCapitalizeIfLower() {
        assertEquals("Abc", capitalizeIfLower("abc"));
        assertEquals("aBc", capitalizeIfLower("aBc"));
    }

    @Test
    void testIsAllUpper() {
        assertTrue(isAllUpper("HELLO"));
        assertFalse(isAllUpper("Hello"));
    }

    @Test
    void testIsAllLower() {
        assertTrue(isAllLower("hello"));
        assertFalse(isAllLower("Hello"));
    }

    @Test
    void testDecapitalize() {
        assertEquals("hello", decapitalize("Hello"));
        assertEquals("hello", decapitalize("hello"));
    }

    @Test
    void testRemoveRange() {
        assertEquals("hello world", removeRange("hello cruel world", 6, 12));
        assertEquals("hello world", removeRange("hello world", 6, 6));
    }

    @Test
    void testContains() {
        assertTrue(contains("hello world", 'h'));
        assertFalse(contains("hello world", 'x'));
    }

    @Test
    void testIndexOf() {
        assertEquals(0, indexOf("hello world", 'h'));
        assertEquals(0, indexOf("hello world", 'h', 0));
        assertEquals(-1, indexOf("hello world", 'h', 1));
        assertEquals(-1, indexOf("hello world", 'h', 1));

        assertEquals(4, indexOf("hello world", 'o'));
        assertEquals(4, indexOf("hello world", 'o', 4));
        assertEquals(7, indexOf("hello world", 'o', 5));
        assertEquals(7, indexOf("hello world", 'o', 6));
        assertEquals(7, indexOf("hello world", 'o', 7));
        assertEquals(-1, indexOf("hello world", 'o', 8));

        assertEquals(-1, indexOf("hello world", 'x'));
        assertEquals(-1, indexOf("hello world", 'x', 4));
    }

    @Test
    void testSwap() {
        assertEquals("hlelow", swap("hellow", 1, 2));
        assertEquals("hellow", swap("hellow", 2, 2));
    }

    @Test
    void testReplaceTwoChars() {
        assertEquals("xx", replaceTwoChars("ab", 0, 'x'));
        assertEquals("helloxxorld", replaceTwoChars("hello world", 5, 'x'));
    }

    @Test
    void testContainsInSubstring() {
        assertTrue(containsInSubstring("hello world", 0, 5, 'e'));

        assertFalse(containsInSubstring("hello world", 0, 5, 'w'));
        assertFalse(containsInSubstring("hello world", 1, 5, 'h'));
    }

    @Test
    void testContainsAdjacentCharacter() {
        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 0, 'h'));
        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 1, 'h'));
        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 1, 'e'));
        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 2, 'e'));
        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 2, 'l'));
        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 3, 'l'));
        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 4, 'l'));
        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 4, 'o'));
        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 5, 'o'));

        assertFalse(containsAdjacentCharacterIgnoringCase("hello", 0, 'e'));
        assertFalse(containsAdjacentCharacterIgnoringCase("hello", 1, 'o'));
        assertFalse(containsAdjacentCharacterIgnoringCase("hello", 1, 'x'));
        assertFalse(containsAdjacentCharacterIgnoringCase("hello", 5, 'x'));

        assertTrue(containsAdjacentCharacterIgnoringCase("hello", 1, 'E'));
    }

    @Test
    void testReplaceCharAt() {
        assertEquals("hallo", replaceCharAt("hello", 1, 'a'));
        assertEquals("hello", replaceCharAt("hello", 1, 'e'));
    }

    @Test
    void testCharIndices() {
        assertArrayEquals(new int[]{0}, charIndices("foo", 'f').toArray());
        assertArrayEquals(new int[]{1,2}, charIndices("foo", 'o').toArray());
        assertArrayEquals(new int[]{2, 3, 9}, charIndices("hello world", 'l').toArray());
        assertArrayEquals(new int[]{}, charIndices("hello world", 'x').toArray());
    }
}
