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

import java.text.MessageFormat;
import java.util.Calendar;

/**
 * Accepts iff satisfies {@code actual-value <compare-operator> expected-value}.
 * @since 0.7.0
 */
public class CalendarCompare implements ValuePredicate<Calendar> {

    private final CompareOperator operator;

    /**
     * Creates a new instance.
     * @param operator the comparison operator
     * @throws IllegalArgumentException if operator is {@code null}
     */
    public CalendarCompare(CompareOperator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        this.operator = operator;
    }

    @Override
    public boolean accepts(Calendar expected, Calendar actual) {
        return operator.satisfies(normalize(actual), normalize(expected));
    }

    @Override
    public String describeExpected(Calendar expected, Calendar actual) {
        if (expected == null) {
            return "(error)"; //$NON-NLS-1$
        }
        return MessageFormat.format(
                "{0} {1}", //$NON-NLS-1$
                operator.getSymbol(),
                Util.format(expected));
    }

    private Calendar normalize(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        Calendar c = (Calendar) calendar.clone();
        fillZeroIfUnset(c, Calendar.HOUR_OF_DAY);
        fillZeroIfUnset(c, Calendar.MINUTE);
        fillZeroIfUnset(c, Calendar.SECOND);
        fillZeroIfUnset(c, Calendar.MILLISECOND);
        return c;
    }

    private void fillZeroIfUnset(Calendar calendar, int field) {
        assert calendar != null;
        if (calendar.isSet(field) == false) {
            calendar.set(field, 0);
        }
    }
}
