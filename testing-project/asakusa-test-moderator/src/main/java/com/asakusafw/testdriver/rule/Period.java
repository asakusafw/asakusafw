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
 * Accepts iff actual calendar is between begin and end.
 * @since 0.2.0
 */
public class Period implements ValuePredicate<Calendar> {

    private final Calendar begin;

    private final Calendar end;

    /**
     * Creates a new instance.
     * @param begin inclusive beginning time, or {@code null} if don't care
     * @param end inclusive ending time, or {@code null} if don't care
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Period(Calendar begin, Calendar end) {
        this.begin = begin == null ? null : (Calendar) begin.clone();
        this.end = end == null ? null : (Calendar) end.clone();
        normalize(this.begin);
        normalize(this.end);
    }

    private void normalize(Calendar calendar) {
        assert calendar != null;
        fillZeroIfUnset(calendar, Calendar.HOUR_OF_DAY);
        fillZeroIfUnset(calendar, Calendar.MINUTE);
        fillZeroIfUnset(calendar, Calendar.SECOND);
        fillZeroIfUnset(calendar, Calendar.MILLISECOND);
    }

    private void fillZeroIfUnset(Calendar calendar, int field) {
        assert calendar != null;
        if (calendar.isSet(field) == false) {
            calendar.set(field, 0);
        }
    }

    @Override
    public boolean accepts(Calendar expected, Calendar actual) {
        if (actual == null) {
            throw new IllegalArgumentException();
        }
        if (begin != null && begin.compareTo(actual) > 0) {
            return false;
        }
        if (begin != null && actual.compareTo(end) > 0) {
            return false;
        }
        return true;
    }

    @Override
    public String describeExpected(Calendar expected, Calendar actual) {
        return MessageFormat.format(
                "{0} ~ {1}", //$NON-NLS-1$
                begin == null ? "..." : Util.format(begin), //$NON-NLS-1$
                end == null ? "..." : Util.format(end)); //$NON-NLS-1$
    }
}
