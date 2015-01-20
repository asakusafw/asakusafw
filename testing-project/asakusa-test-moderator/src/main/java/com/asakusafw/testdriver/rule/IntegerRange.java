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
 * Accepts iff actual integer is in [ expected + lower-bound, expected + upper-bound ].
 * @since 0.2.0
 */
public class IntegerRange implements ValuePredicate<Number> {

    private final long lowerBound;

    private final long upperBound;

    /**
     * Creates a new instance.
     * @param lowerBound lower bound offset from expected value
     * @param upperBound upper bound offset from expected value
     */
    public IntegerRange(long lowerBound, long upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean accepts(Number expected, Number actual) {
        if (expected == null || actual == null) {
            throw new IllegalArgumentException();
        }
        // TODO avoid overflow
        long e = expected.longValue();
        long a = actual.longValue();
        return (e + lowerBound <= a && a <= e + upperBound);
    }

    @Override
    public String describeExpected(Number expected, Number actual) {
        if (expected == null) {
            return "(error)";
        }
        return MessageFormat.format(
                "{0} ~ {1}",
                Util.format(expected.longValue() + lowerBound),
                Util.format(expected.longValue() + upperBound));
    }
}
