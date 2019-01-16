/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.PropertyName;

/**
 * Condition of property.
 * @param <T> upper bound type of the property
 * @since 0.2.0
 */
public class PropertyCondition<T> {

    static final Logger LOG = LoggerFactory.getLogger(PropertyCondition.class);

    private final PropertyName name;

    private final Class<? extends T> type;

    private final List<? extends ValuePredicate<? super T>> predicates;

    /**
     * Creates a new instance.
     * @param name the property name
     * @param type the property type
     * @param predicates the value predicates
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PropertyCondition(
            PropertyName name,
            Class<? extends T> type,
            List<? extends ValuePredicate<? super T>> predicates) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (predicates == null) {
            throw new IllegalArgumentException("predicates must not be null"); //$NON-NLS-1$
        }
        if (predicates.isEmpty()) {
            throw new IllegalArgumentException("predicates must not be empty"); //$NON-NLS-1$
        }
        this.name = name;
        this.type = type;
        this.predicates = predicates;
    }

    /**
     * Returns the target property name.
     * @return the target property name
     */
    public PropertyName getPropertyName() {
        return name;
    }

    /**
     * Returns {@code true} iff exists a predicate accepts the pair.
     * @param expected the expected value
     * @param actual the actual value
     * @return {@code true} iff exists a predicate accepts the pair
     */
    public boolean accepts(Object expected, Object actual) {
        T e = type.cast(expected);
        T a = type.cast(actual);
        for (ValuePredicate<? super T> predicate : predicates) {
            try {
                if (predicate.accepts(e, a)) {
                    return true;
                }
            } catch (IllegalArgumentException ex) {
                // this means "ERROR" in ternary, continue checking rest conditions
                if (LOG.isTraceEnabled()) {
                    LOG.trace(MessageFormat.format(
                            "Error occurred while evaluating predicate: " //$NON-NLS-1$
                            + "predicate={0}, expected={1}, actual={2}", //$NON-NLS-1$
                            predicate,
                            e, a), ex);
                }
            }
        }
        return false;
    }

    /**
     * Returns an expected value as readable text.
     * @param expected the expected value (nullable)
     * @param actual the actual value (nullable)
     * @return readable text
     */
    public String describeExpected(Object expected, Object actual) {
        T e = type.cast(expected);
        T a = type.cast(actual);
        StringBuilder buf = new StringBuilder();
        boolean sawDescription = false;
        for (ValuePredicate<? super T> pred : predicates) {
            String description = pred.describeExpected(e, a);
            if (description == null) {
                continue;
            }
            if (sawDescription) {
                buf.append(Messages.getString("PropertyCondition.messageOrDescription")); //$NON-NLS-1$
            }
            sawDescription = true;
            buf.append(MessageFormat.format(
                    Messages.getString("PropertyCondition.messageQuotedDescription"), //$NON-NLS-1$
                    description));
        }
        if (sawDescription) {
            return buf.toString();
        } else {
            return Messages.getString("PropertyCondition.messageUnsupportedData"); //$NON-NLS-1$
        }
    }
}
