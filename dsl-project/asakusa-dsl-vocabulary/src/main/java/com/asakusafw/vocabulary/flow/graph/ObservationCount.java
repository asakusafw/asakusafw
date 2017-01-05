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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;

/**
 * Represents an operator constraint how many each operator can perform for individual data-sets.
 */
public enum ObservationCount implements FlowElementAttribute {

    /**
     * Don't care.
     */
    DONT_CARE(false, false) {
        @Override
        public ObservationCount and(ObservationCount other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
            }
            return other;
        }
    },

    /**
     * The operator must perform at most once for the data-set.
     */
    AT_MOST_ONCE(true, false) {
        @Override
        public ObservationCount and(ObservationCount other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
            }
            if (other.atLeastOnce) {
                return EXACTLY_ONCE;
            }
            return AT_MOST_ONCE;
        }
    },

    /**
     * The operator must perform at least once for the data-set.
     */
    AT_LEAST_ONCE(false, true) {
        @Override
        public ObservationCount and(ObservationCount other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
            }
            if (other.atMostOnce) {
                return EXACTLY_ONCE;
            }
            return AT_LEAST_ONCE;
        }
    },

    /**
     * The operator must perform exactly once for the data-set.
     */
    EXACTLY_ONCE(true, true) {
        @Override
        public ObservationCount and(ObservationCount other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
            }
            return EXACTLY_ONCE;
        }
    },
    ;

    /**
     * The operator must perform at most once.
     */
    public final boolean atMostOnce;

    /**
     * The operator must perform at least once.
     */
    public final boolean atLeastOnce;

    ObservationCount(boolean atMostOnce, boolean atLeastOnce) {
        this.atMostOnce = atMostOnce;
        this.atLeastOnce = atLeastOnce;
    }

    /**
     * Returns the combined constraint.
     * @param other the target constraint
     * @return the combined result
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public abstract ObservationCount and(ObservationCount other);

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}.{1}", //$NON-NLS-1$
                getDeclaringClass().getSimpleName(),
                name());
    }
}
