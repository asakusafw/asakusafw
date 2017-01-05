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

import java.math.BigDecimal;
import java.text.MessageFormat;

/**
 * Accepts iff actual decimal is in [ expected + lower-bound, expected + upper-bound ].
 * @since 0.2.0
 */
public class DecimalRange implements ValuePredicate<BigDecimal> {

    private final BigDecimal lowerBound;

    private final BigDecimal upperBound;

    /**
     * Creates a new instance.
     * @param lowerBound lower bound offset from expected value
     * @param upperBound upper bound offset from expected value
     */
    public DecimalRange(BigDecimal lowerBound, BigDecimal upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean accepts(BigDecimal expected, BigDecimal actual) {
        if (expected == null || actual == null) {
            throw new IllegalArgumentException();
        }
        return expected.add(lowerBound).compareTo(actual) <= 0
            && actual.compareTo(expected.add(upperBound)) <= 0;
    }

    @Override
    public String describeExpected(BigDecimal expected, BigDecimal actual) {
        if (expected == null) {
            return "(error)"; //$NON-NLS-1$
        }
        return MessageFormat.format(
                "{0} ~ {1}", //$NON-NLS-1$
                Util.format(expected.add(lowerBound)),
                Util.format(expected.add(upperBound)));
    }
}
