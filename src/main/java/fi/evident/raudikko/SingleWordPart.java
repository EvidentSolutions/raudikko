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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class SingleWordPart implements WordPart {

    private final @NotNull String word;
    private final @NotNull List<String> baseForms;
    private final @NotNull Set<WordAttribute> attributes;

    public SingleWordPart(@NotNull String word,
                          @NotNull List<String> baseForms,
                          @NotNull EnumSet<WordAttribute> attributes) {
        this.word = word;
        this.baseForms = Collections.unmodifiableList(baseForms);
        this.attributes = Collections.unmodifiableSet(attributes);
    }

    @Override
    public @NotNull List<String> getBaseForms() {
        return baseForms;
    }

    public boolean isNominative() {
        return hasAttribute(WordAttribute.Sn);
    }

    public boolean isSingular() {
        return hasAttribute(WordAttribute.Ny);
    }

    public boolean isClitic() {
        return hasAnyAttribute(WordAttribute.Fko, WordAttribute.Fkin, WordAttribute.Fkaan);
    }

    public boolean isPossessiveSuffix() {
        return hasAnyAttribute(WordAttribute.O3, WordAttribute.O2y, WordAttribute.O2m, WordAttribute.O1y, WordAttribute.O1m);
    }

    public boolean isComparative() {
        return hasAnyAttribute(WordAttribute.Cc, WordAttribute.Cs);
    }

    @Override
    public boolean isProperNoun() {
        return hasAnyAttribute(WordAttribute.Lee, WordAttribute.Les, WordAttribute.Lep, WordAttribute.Lem);
    }

    public boolean hasAttribute(@NotNull WordAttribute attribute) {
        return attributes.contains(attribute);
    }

    public boolean hasAnyAttribute(@NotNull WordAttribute... attributes) {
        for (WordAttribute flag : attributes)
            if (hasAttribute(flag))
                return true;

        return false;
    }

    @Override
    public boolean isInBaseForm() {
        return isNominative() && isSingular() && !isClitic() && !isPossessiveSuffix() && !isComparative();
    }

    @Override
    public String toString() {
        return word;
    }
}
