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

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Core predicates.
 * @since 0.2.0
 */
public final class Predicates {

    /**
     * Returns a predicate that accepts iff actual value is equalt to the specified value.
     * @param value target
     * @return the created predicate
     */
    public static ValuePredicate<Object> equalTo(Object value) {
        return new ExpectConstant<>(value, new Equals());
    }

    /**
     * Returns {@link Not} predicate.
     * @param <T> type of predicate
     * @param predicate the predicate to negate
     * @return the created predicate
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <T> ValuePredicate<T> not(ValuePredicate<T> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("predicate must not be null"); //$NON-NLS-1$
        }
        return new Not<>(predicate);
    }

    /**
     * Returns {@link Equals} predicate.
     * @return the created predicate
     */
    public static ValuePredicate<Object> equals() {
        return new Equals();
    }

    /**
     * Returns {@link IsNull} predicate.
     * @return the created predicate
     */
    public static ValuePredicate<Object> isNull() {
        return new IsNull();
    }

    /**
     * Returns {@link FloatRange} predicate.
     * @param lower lower bounds
     * @param upper upper bounds
     * @return the created predicate
     */
    public static ValuePredicate<Number> floatRange(double lower, double upper) {
        return new FloatRange(lower, upper);
    }

    /**
     * Returns {@link IntegerRange} predicate.
     * @param lower lower bounds
     * @param upper upper bounds
     * @return the created predicate
     */
    public static ValuePredicate<Number> integerRange(long lower, long upper) {
        return new IntegerRange(lower, upper);
    }

    /**
     * Returns {@link FloatRange} predicate.
     * @param lower lower bounds
     * @param upper upper bounds
     * @return the created predicate
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ValuePredicate<BigDecimal> decimalRange(BigDecimal lower, BigDecimal upper) {
        if (lower == null) {
            throw new IllegalArgumentException("lower must not be null"); //$NON-NLS-1$
        }
        if (upper == null) {
            throw new IllegalArgumentException("upper must not be null"); //$NON-NLS-1$
        }
        return new DecimalRange(lower, upper);
    }

    /**
     * Returns {@link CalendarRange} predicate with date scaled.
     * @param lower lower bounds
     * @param upper upper bounds
     * @return the created predicate
     */
    public static ValuePredicate<Calendar> dateRange(int lower, int upper) {
        return new CalendarRange(lower, upper, Calendar.DATE);
    }

    /**
     * Returns {@link CalendarRange} predicate with second scaled.
     * @param lower lower bounds
     * @param upper upper bounds
     * @return the created predicate
     */
    public static ValuePredicate<Calendar> timeRange(int lower, int upper) {
        return new CalendarRange(lower, upper, Calendar.SECOND);
    }

    /**
     * Returns {@link Period} predicate.
     * @param begin start time
     * @param end finish time
     * @return the created predicate
     */
    public static ValuePredicate<Calendar> period(Calendar begin, Calendar end) {
        return new Period(begin, end);
    }

    /**
     * Returns {@link ContainsString} predicate.
     * @return the created predicate
     */
    public static ValuePredicate<String> containsString() {
        return new ContainsString();
    }

    private Predicates() {
        return;
    }
}
