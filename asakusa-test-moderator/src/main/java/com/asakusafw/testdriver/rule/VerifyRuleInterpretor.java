/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.VerifyRule;

/**
 * Simple condition based {@link VerifyRule} implementation.
 * @since 0.2.0
 */
public class VerifyRuleInterpretor implements VerifyRule {

    private final List<PropertyName> keys;

    private final Set<DataModelCondition> modelConditions;

    private final List<? extends PropertyCondition<?>> propertyConditions;

    /**
     * Creates a new instance.
     * @param keys property names of verify identities
     * @param modelConditions existensial conditions for expected/actual model objects
     * @param propertyConditions individual property conditions
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifyRuleInterpretor(
            List<PropertyName> keys,
            Set<DataModelCondition> modelConditions,
            List<? extends PropertyCondition<?>> propertyConditions) {
        if (keys == null) {
            throw new IllegalArgumentException("keys must not be null"); //$NON-NLS-1$
        }
        if (modelConditions == null) {
            throw new IllegalArgumentException("modelConditions must not be null"); //$NON-NLS-1$
        }
        if (propertyConditions == null) {
            throw new IllegalArgumentException("propertyConditions must not be null"); //$NON-NLS-1$
        }
        this.keys = keys;
        this.modelConditions = modelConditions;
        this.propertyConditions = propertyConditions;
    }

    @Override
    public Map<PropertyName, Object> getKey(DataModelReflection target) {
        Map<PropertyName, Object> results = new LinkedHashMap<PropertyName, Object>();
        for (PropertyName name : keys) {
            results.put(name, target.getValue(name));
        }
        return results;
    }

    @Override
    public Object verify(DataModelReflection expected, DataModelReflection actual) {
        if (expected == null) {
            if (modelConditions.contains(DataModelCondition.IGNORE_UNEXPECTED)) {
                return null;
            } else {
                return MessageFormat.format(
                        "結果に対する期待値がありません: キー={0}",
                        Util.formatMap(getKey(actual)));
            }
        }
        if (actual == null) {
            if (modelConditions.contains(DataModelCondition.IGNORE_ABSENT)) {
                return null;
            } else {
                return MessageFormat.format(
                        "期待値に対する結果がありません: キー={0}",
                        Util.formatMap(getKey(expected)));
            }
        }
        return checkProperties(expected, actual);
    }

    private Object checkProperties(DataModelReflection expected, DataModelReflection actual) {
        List<String> differences = new ArrayList<String>(1);
        for (PropertyCondition<?> condition : propertyConditions) {
            Object e = expected.getValue(condition.getPropertyName());
            Object a = actual.getValue(condition.getPropertyName());
            if (condition.accepts(e, a) == false) {
                differences.add(MessageFormat.format(
                        "\"{0}\"(={1})が正しくありません: {2}",
                        condition.getPropertyName(),
                        Util.format(actual),
                        condition.describeExpected(e, a)));
            }
        }
        return differences.isEmpty() ? null : differences;
    }
}
