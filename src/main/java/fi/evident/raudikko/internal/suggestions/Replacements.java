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

package fi.evident.raudikko.internal.suggestions;

import fi.evident.raudikko.internal.utils.CharMap;
import fi.evident.raudikko.internal.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.toUpperCase;

final class Replacements {

    static final @NotNull Replacements REPLACEMENTS_1 = parse(".,asiuiotrtdersšsanmuilkklkgoiäömnrertvbpbpoythjjhjkdtdsdföägfghgkfgfdbpbncvcswewvxczžzxqaåoåpåäåöaeiktyea");
    static final @NotNull Replacements REPLACEMENTS_2 = parse("1q2q2w3w3e4e4r5r5t6t6y7y7u8u8i9i9o0o0p+pie");
    static final @NotNull Replacements REPLACEMENTS_3 = parse("essdnhujlökjopäpmkrdvgplyhhujideölgtfvbvckwaxszaqkåaaåeéaâkcscijxz");
    static final @NotNull Replacements REPLACEMENTS_4 = parse("qwqswqwswdedefrfrgtftgthygyjuhukilokolpöpäsesxdrbgfefrftfcgygbgvhyhnhbhgjujmjnkikokmlolpöpöåäåzsxdcdcfcxvfbhnjnbmjewpåaqswszdwdcdxvcawazsq");
    static final @NotNull Replacements REPLACEMENTS_5 = parse("aooaoutlsraieääeuvvuoddokqpvvpqeeqaddarsetteryyrtuutyiiyuoippioåhvvhhmmh");
    static final @NotNull Replacements REPLACEMENTS_1_FULL = REPLACEMENTS_1.extendWithMatchingUpperCaseReplacements();
    static final @NotNull Replacements REPLACEMENTS_2_FULL = REPLACEMENTS_2.extendWithMatchingUpperCaseReplacements();
    static final @NotNull Replacements REPLACEMENTS_3_FULL = REPLACEMENTS_3.extendWithMatchingUpperCaseReplacements();
    static final @NotNull Replacements REPLACEMENTS_4_FULL = REPLACEMENTS_4.extendWithMatchingUpperCaseReplacements();
    static final @NotNull Replacements REPLACEMENTS_5_FULL = REPLACEMENTS_5.extendWithMatchingUpperCaseReplacements();

    private final @NotNull CharMap<char[]> replacementMapping;
    private static final char @NotNull[] EMPTY_MAPPING = new char[0];

    private Replacements(@NotNull CharMap<char[]> replacementMapping) {
        this.replacementMapping = replacementMapping;
    }

    public char @NotNull [] forCharacter(char from) {
        return replacementMapping.getOrDefault(from, EMPTY_MAPPING);
    }

    private @NotNull Replacements extendWithMatchingUpperCaseReplacements() {
        var newMapping = replacementMapping.copy();
        for (char ch : newMapping.keys()) {
            var values = newMapping.get(ch);
            if (values != null && isLowerCase(ch))
                newMapping.put(toUpperCase(ch), CollectionUtils.toUpperCase(values));
        }

        return new Replacements(newMapping);
    }

    private static @NotNull Replacements parse(@NotNull String input) {
        if (input.length() % 2 != 0) throw new IllegalArgumentException("invalid replacement string " + input);

        var mapping = new HashMap<Character, List<Character>>();
        for (int i = 0; i < input.length(); i += 2) {
            char from = input.charAt(i);
            char to = input.charAt(i + 1);

            var targets = mapping.computeIfAbsent(from, k -> new ArrayList<>());
            targets.add(to);
        }

        var result = new CharMap<char[]>();
        mapping.forEach((key, value) -> result.put(key, CollectionUtils.toCharArray(value)));
        return new Replacements(result);
    }
}
