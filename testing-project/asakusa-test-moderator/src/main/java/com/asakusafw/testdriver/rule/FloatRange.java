/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * Accepts iff actual float is in [ expected + lower-bound, expected + upper-bound ].
 * @since 0.2.0
 */
public class FloatRange implements ValuePredicate<Number> {

    private final double lowerBound;

    private final double upperBound;

    /**
     * Creates a new instance.
     * @param lowerBound lower bound offset from expected value
     * @param upperBound upper bound offset from expected value
     */
    public FloatRange(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean accepts(Number expected, Number actual) {
        if (expected == null || actual == null) {
            throw new IllegalArgumentException();
        }
        double e = expected.doubleValue();
        double a = actual.doubleValue();
        return (e + lowerBound <= a && a <= e + upperBound);
    }

    @Override
    public String describeExpected(Number expected, Number actual) {
        if (expected == null) {
            return "(error)"; //$NON-NLS-1$
        }
        return MessageFormat.format(
                "{0} ~ {1}", //$NON-NLS-1$
                Util.format(expected.doubleValue() + lowerBound),
                Util.format(expected.doubleValue() + upperBound));
    }
}
