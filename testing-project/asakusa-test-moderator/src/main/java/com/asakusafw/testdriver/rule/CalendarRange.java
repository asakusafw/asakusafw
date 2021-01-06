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
import java.util.Calendar;

/**
 * Accepts iff actual calendar is in [ expected + lower-bound, expected + upper-bound ].
 * @since 0.2.0
 */
public class CalendarRange implements ValuePredicate<Calendar> {

    private final int lowerBound;

    private final int upperBound;

    private final int scale;

    /**
     * Creates a new instance.
     * @param lowerBound lower bound offset from expected value in the specified scale
     * @param upperBound upper bound offset from expected value in the specified scale
     * @param scale the calendar field of offset scale (ex. Calendar.DATE, or Calendar.SECOND)
     */
    public CalendarRange(int lowerBound, int upperBound, int scale) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.scale = scale;
    }

    @Override
    public boolean accepts(Calendar expected, Calendar actual) {
        if (expected == null || actual == null) {
            throw new IllegalArgumentException();
        }
        return add(expected, lowerBound).compareTo(actual) <= 0
            && actual.compareTo(add(expected, upperBound)) <= 0;
    }

    @Override
    public String describeExpected(Calendar expected, Calendar actual) {
        if (expected == null) {
            return "(error)"; //$NON-NLS-1$
        }
        return MessageFormat.format(
                "{0} ~ {1}", //$NON-NLS-1$
                Util.format(add(expected, upperBound)),
                Util.format(add(expected, lowerBound)));
    }

    private Calendar add(Calendar c, int offset) {
        Calendar copy = (Calendar) c.clone();
        copy.add(scale, offset);
        return copy;
    }
}
