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
import java.util.List;

public final class StrongMorphemeCompoundWordPart implements WordPart {

    private final @NotNull List<SingleWordPart> parts;
    private final @NotNull String baseForm;

    public StrongMorphemeCompoundWordPart(@NotNull List<SingleWordPart> parts, @NotNull String baseForm) {
        this.parts = Collections.unmodifiableList(parts);
        this.baseForm = baseForm;
    }

    public @NotNull List<SingleWordPart> getParts() {
        return parts;
    }

    public @NotNull String getBaseForm() {
        return baseForm;
    }

    @Override
    public @NotNull List<String> getBaseForms() {
        return Collections.singletonList(baseForm);
    }

    @Override
    public boolean isInBaseForm() {
        return parts.get(parts.size() - 1).isInBaseForm();
    }

    @Override
    public boolean isProperNoun() {
        return parts.get(0).isProperNoun();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (SingleWordPart part : parts)
            result.append(part.toString());

        return result.toString();
    }
}
