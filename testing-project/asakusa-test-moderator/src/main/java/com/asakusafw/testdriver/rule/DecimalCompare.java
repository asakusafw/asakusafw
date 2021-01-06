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

import java.math.BigDecimal;
import java.text.MessageFormat;

/**
 * Accepts iff satisfies {@code actual-value <compare-operator> expected-value}.
 * @since 0.7.0
 */
public class DecimalCompare implements ValuePredicate<BigDecimal> {

    private final CompareOperator operator;

    /**
     * Creates a new instance.
     * @param operator the comparison operator
     * @throws IllegalArgumentException if operator is {@code null}
     */
    public DecimalCompare(CompareOperator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        this.operator = operator;
    }

    @Override
    public boolean accepts(BigDecimal expected, BigDecimal actual) {
        return operator.satisfies(actual, expected);
    }

    @Override
    public String describeExpected(BigDecimal expected, BigDecimal actual) {
        if (expected == null) {
            return "(error)"; //$NON-NLS-1$
        }
        return MessageFormat.format(
                "{0} {1}", //$NON-NLS-1$
                operator.getSymbol(),
                Util.format(expected));
    }
}
