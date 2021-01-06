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
package com.asakusafw.testdriver.rule;

import java.text.MessageFormat;

/**
 * Logical-not.
 * @param <T> type of value
 * @since 0.2.0
 */
public class Not<T> implements ValuePredicate<T> {

    private final ValuePredicate<T> factor;

    /**
     * Creates a new instance.
     * @param factor the child predicate to be negated
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Not(ValuePredicate<T> factor) {
        if (factor == null) {
            throw new IllegalArgumentException("factor must not be null"); //$NON-NLS-1$
        }
        this.factor = factor;
    }

    @Override
    public boolean accepts(T expected, T actual) {
        return factor.accepts(expected, actual) == false;
    }

    @Override
    public String describeExpected(T expected, T actual) {
        String factorExpected = factor.describeExpected(expected, actual);
        if (factorExpected == null) {
            return null;
        }
        return MessageFormat.format(
                Messages.getString("Not.message"), //$NON-NLS-1$
                factorExpected);
    }
}
