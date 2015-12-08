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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.VerifyRule;

/**
 * Builds condition based {@link VerifyRule}s.
 * @since 0.2.0
 */
public class VerifyRuleBuilder {

    private final DataModelDefinition<?> definition;

    private final Set<DataModelCondition> dataModelConditions;

    private final Map<PropertyName, Property> propertyConditions;

    /**
     * Creates a new instance.
     * @param definition the definition of target data model type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifyRuleBuilder(DataModelDefinition<?> definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        this.definition = definition;
        this.dataModelConditions = new HashSet<>();
        this.propertyConditions = new LinkedHashMap<>();
    }

    /**
     * Adds a rule to accept if actual data is absent.
     * Note that if one of the added rules is accepted, this model object will be also accepted.
     * @return this object (for method chain)
     */
    public VerifyRuleBuilder acceptIfAbsent() {
        this.dataModelConditions.add(DataModelCondition.IGNORE_ABSENT);
        return this;
    }

    /**
     * Adds a rule to accept if expected data is absent.
     * Note that if one of the added rules is accepted, this model object will be also accepted.
     * @return this object (for method chain)
     */
    public VerifyRuleBuilder acceptIfUnexpected() {
        this.dataModelConditions.add(DataModelCondition.IGNORE_UNEXPECTED);
        return this;
    }

    /**
     * Returns the sub rule builder for the specified property.
     * Note that this model will be only accepted if all properties are accepted.
     * @param name the property name
     * @return the rule builder for the property
     * @throws IllegalArgumentException if the property does not exist,
     *     or if some parameters were {@code null}
     */
    public Property property(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        String[] words = name.split("_|-|\\s+"); //$NON-NLS-1$
        PropertyName propertyName = PropertyName.newInstance(words);
        PropertyType type = definition.getType(propertyName);
        if (type == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("VerifyRuleBuilder.errorUndefinedProperty"), //$NON-NLS-1$
                    definition.getModelClass().getName(),
                    propertyName));
        }
        Property subBuilder = propertyConditions.get(propertyName);
        if (subBuilder == null) {
            subBuilder = new Property(propertyName, type);
            propertyConditions.put(propertyName, subBuilder);
        }
        return subBuilder;
    }

    /**
     * Returns a {@link VerifyRule} from since added rules.
     * @return the created {@link VerifyRule}
     */
    public VerifyRule toVerifyRule() {
        List<PropertyName> keys = new ArrayList<>();
        List<PropertyCondition<?>> properties = new ArrayList<>();
        for (Map.Entry<PropertyName, Property> entry : propertyConditions.entrySet()) {
            Property property = entry.getValue();
            if (property.key) {
                keys.add(entry.getKey());
            }
            if (property.predicates.isEmpty() == false) {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                PropertyCondition<?> cond = new PropertyCondition(
                        entry.getKey(),
                        definition.getType(entry.getKey()).getRepresentation(),
                        property.predicates);
                properties.add(cond);
            }
        }
        return new VerifyRuleInterpretor(keys, dataModelConditions, properties);
    }

    /**
     * Builds verify conditions for individual properties.
     * @since 0.2.0
     */
    public static class Property {

        private final PropertyName name;

        private final PropertyType type;

        boolean key;

        final List<ValuePredicate<?>> predicates;

        Property(PropertyName name, PropertyType type) {
            assert name != null;
            assert type != null;
            this.name = name;
            this.type = type;
            this.key = false;
            this.predicates = new ArrayList<>();
        }

        /**
         * Returns the name of this property.
         * @return the name
         */
        public PropertyName getName() {
            return name;
        }

        /**
         * Returns the type of this property.
         * @return the type
         */
        public PropertyType getType() {
            return type;
        }

        /**
         * Make this property as a key.
         * @return this object (for method chain)
         */
        public Property asKey() {
            this.key = true;
            return this;
        }

        /**
         * Adds acceptable predicate between expected value and actual value to this property.
         * Note that if one of the added rules is accepted, this property will be also accepted.
         * @param predicate acceptable predicate
         * @return this object (for method chain)
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Property accept(ValuePredicate<?> predicate) {
            if (predicate == null) {
                throw new IllegalArgumentException("predicate must not be null"); //$NON-NLS-1$
            }
            this.predicates.add(predicate);
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Property [name="); //$NON-NLS-1$
            builder.append(name);
            builder.append(", type="); //$NON-NLS-1$
            builder.append(type);
            builder.append(", key="); //$NON-NLS-1$
            builder.append(key);
            builder.append(", predicates="); //$NON-NLS-1$
            builder.append(predicates);
            builder.append("]"); //$NON-NLS-1$
            return builder.toString();
        }
    }
}
