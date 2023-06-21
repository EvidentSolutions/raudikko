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

import fi.evident.raudikko.internal.fst.UnweightedTransducer;
import fi.evident.raudikko.internal.fst.UnweightedVfstLoader;
import fi.evident.raudikko.internal.morphology.FinnishVfstAnalyzer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Represents morphology rules. This class is immutable and can be shared between threads.
 */
public final class Morphology {

    private final @NotNull UnweightedTransducer transducer;

    private Morphology(@NotNull UnweightedTransducer transducer) {
        this.transducer = transducer;
    }

    /**
     * Loads the morphology rules bundles with the library.
     */
    public static @NotNull Morphology loadBundled() {
        InputStream stream = Morphology.class.getResourceAsStream("/morpho/5/mor-morpho/mor.vfst");
        if (stream == null)
            throw new IllegalStateException("Failed to find bundled morphology");

        try {
            try {
                return new Morphology(UnweightedVfstLoader.load(stream));
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load bundled morphology", e);
        }
    }

    /**
     * Create a new {@link Analyzer} for this morphology.
     * <p>
     * The analyzer is a mutable object that can be used repeatedly, but may not be
     * shared between threads.
     */
    public @NotNull Analyzer newAnalyzer() {
        return newAnalyzer(new AnalyzerConfiguration());
    }

    /**
     * Create a new {@link Analyzer} for this morphology.
     * <p>
     * The analyzer is a mutable object that can be used repeatedly, but may not be
     * shared between threads.
     */
    public @NotNull Analyzer newAnalyzer(@NotNull AnalyzerConfiguration configuration) {
        return new FinnishVfstAnalyzer(transducer, configuration);
    }
}
