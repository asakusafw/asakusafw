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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule based {@link Verifier}.
 * @since 0.2.3
 * @version 0.7.0
 */
public class VerifyRuleVerifier implements Verifier, Verifier.Validatable {

    private final DataModelSource expected;

    private final VerifyRule rule;

    /**
     * Creates a new instance.
     * @param expected the expected data set
     * @param rule the rule engine
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifyRuleVerifier(DataModelSource expected, VerifyRule rule) {
        if (expected == null) {
            throw new IllegalArgumentException("expected must not be null"); //$NON-NLS-1$
        }
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null"); //$NON-NLS-1$
        }
        this.expected = expected;
        this.rule = rule;
    }

    @Override
    public List<Difference> verify(DataModelSource results) throws IOException {
        VerifyEngine engine = new VerifyEngine(rule);
        try {
            engine.addExpected(expected);
        } finally {
            expected.close();
        }
        List<Difference> differences = new ArrayList<>();
        differences.addAll(engine.inspectInput(results));
        differences.addAll(engine.inspectRest());
        return differences;
    }

    @Override
    public void validate() throws IOException {
        VerifyEngine engine = new VerifyEngine(rule);
        try {
            engine.addExpected(expected);
        } finally {
            expected.close();
        }
    }

    @Override
    public void close() throws IOException {
        expected.close();
    }
}
