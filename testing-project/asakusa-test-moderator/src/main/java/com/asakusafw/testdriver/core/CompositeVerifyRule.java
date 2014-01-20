/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import java.util.List;

/**
 * Composite verify rule.
 * @since 0.2.3
 */
public class CompositeVerifyRule implements VerifyRule {

    private final VerifyRule rule;

    private final TestRule[] fragments;

    /**
     * Creates a new instance.
     * @param rule the primary verify rule
     * @param fragments the extra verify rules
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CompositeVerifyRule(VerifyRule rule, List<? extends TestRule> fragments) {
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null"); //$NON-NLS-1$
        }
        if (fragments == null) {
            throw new IllegalArgumentException("fragments must not be null"); //$NON-NLS-1$
        }
        this.rule = rule;
        this.fragments = fragments.toArray(new TestRule[fragments.size()]);
    }

    @Override
    public Object getKey(DataModelReflection target) {
        return rule.getKey(target);
    }

    @Override
    public Object verify(DataModelReflection expected, DataModelReflection actual) {
        Object primary = rule.verify(expected, actual);
        if (primary != null) {
            return primary;
        }
        for (TestRule fragment : fragments) {
            Object extra = fragment.verify(expected, actual);
            if (extra != null) {
                return extra;
            }
        }
        return null;
    }
}
