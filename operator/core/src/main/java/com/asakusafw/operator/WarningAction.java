/**
 * Copyright 2011-2021 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.operator;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Action type of warnings.
 * @since 0.9.0
 */
public enum WarningAction {

    /**
     * Ignores warning.
     */
    IGNORE,

    /**
     * Reports on warn.
     */
    REPORT,

    /**
     * Fails on warn.
     */
    FAIL,
    ;

    /**
     * Returns the default value of {@link WarningAction}.
     * @return the default value
     */
    public static WarningAction getDefault() {
        return REPORT;
    }

    /**
     * Returns a constant of the given symbol.
     * @param symbol the symbol
     * @return the related constant of the symbol, or empty if the symbol is wrong
     */
    public static Optional<WarningAction> fromSymbol(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(symbol.trim().toUpperCase(Locale.ENGLISH)));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }
}
