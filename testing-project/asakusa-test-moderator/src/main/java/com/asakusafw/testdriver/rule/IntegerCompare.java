/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
 * Accepts iff satisfies {@code actual-value <compare-operator> expected-value}.
 * @since 0.7.0
 */
public class IntegerCompare implements ValuePredicate<Number> {

    private final CompareOperator operator;

    /**
     * Creates a new instance.
     * @param operator the comparison operator
     * @throws IllegalArgumentException if operator is {@code null}
     */
    public IntegerCompare(CompareOperator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        this.operator = operator;
    }

    @Override
    public boolean accepts(Number expected, Number actual) {
        if (expected == null || actual == null) {
            throw new IllegalArgumentException();
        }
        long e = expected.longValue();
        long a = actual.longValue();
        switch (operator) {
        case LESS_THAN:
            return a < e;
        case GREATER_THAN:
            return a > e;
        case LESS_THAN_OR_EQUAL:
            return a <= e;
        case GREATER_THAN_OR_EQUAL:
            return a >= e;
        default:
            throw new AssertionError(operator);
        }
    }

    @Override
    public String describeExpected(Number expected, Number actual) {
        if (expected == null) {
            return "(error)"; //$NON-NLS-1$
        }
        return MessageFormat.format(
                "{0} {1}", //$NON-NLS-1$
                operator.getSymbol(),
                Util.format(expected));
    }
}
