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

import fi.evident.raudikko.Analysis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static fi.evident.raudikko.internal.morphology.BaseForm.parseBaseform;
import static fi.evident.raudikko.internal.utils.StringUtils.replaceCharAt;
import static fi.evident.raudikko.internal.utils.StringUtils.startsWithChar;

final class Organization {

    private Organization() {
    }

    static @Nullable Analysis organizationNameAnalysis(@NotNull Analysis analysis,
                                                       @NotNull SymbolBuffer tokenizer,
                                                       @NotNull String oldStructure) {

        if (Objects.equals(analysis.getWordClass(), "nimisana") && isOrganizationName(tokenizer)) {
            String  structure = oldStructure.length() > 1 ? replaceCharAt(oldStructure, 1, 'i') : oldStructure;

            Analysis newAnalysis = analysis.clone();
            newAnalysis.setWordClass("nimi");
            newAnalysis.setStructure(structure);
            newAnalysis.setBaseForm(parseBaseform(tokenizer, structure));
            return newAnalysis;
        } else {
            return null;
        }
    }

    private static boolean isOrganizationName(@NotNull SymbolBuffer buffer) {
        buffer.moveToStart();

        if (!buffer.nextToken() || startsWithChar(buffer.currentToken, '-') || buffer.matchesTag(Tags.la))
            return false;

        buffer.moveToEnd();

        while (buffer.previousToken()) {
            if (buffer.matchesTag(Tags.bc))
                return false;

            if (buffer.matchesTag(Tags.ion))
                while (buffer.previousToken())
                    if (buffer.matchesTag(Tags.bc))
                        return true;
        }

        return false;
    }
}
