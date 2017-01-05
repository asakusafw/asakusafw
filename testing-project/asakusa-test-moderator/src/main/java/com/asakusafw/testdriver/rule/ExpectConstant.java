/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

/**
 * Provides constant as expected value to a succeeding predicate.
 * @param <T> type of constant
 * @since 0.2.0
 */
public class ExpectConstant<T> implements ValuePredicate<T> {

    private final T constant;

    private final ValuePredicate<T> successor;

    /**
     * Creates a new instance.
     * @param constant expected constant (nullable)
     * @param successor expecting constant
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExpectConstant(T constant, ValuePredicate<T> successor) {
        if (successor == null) {
            throw new IllegalArgumentException("successor must not be null"); //$NON-NLS-1$
        }
        this.constant = constant;
        this.successor = successor;
    }

    @Override
    public boolean accepts(T expected, T actual) {
        return successor.accepts(constant, actual);
    }

    @Override
    public String describeExpected(T expected, T actual) {
        return successor.describeExpected(constant, actual);
    }
}
