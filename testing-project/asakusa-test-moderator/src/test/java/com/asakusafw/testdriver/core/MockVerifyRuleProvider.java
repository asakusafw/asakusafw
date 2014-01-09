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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock {@link VerifyRuleProvider}.
 * @since 0.2.0
 */
public class MockVerifyRuleProvider implements VerifyRuleProvider {

    private final Map<URI, VerifyRule> rules = new HashMap<URI, VerifyRule>();

    /**
     * Creates a new instance includes {@code default:rule=<perfect matcher>}.
     */
    public MockVerifyRuleProvider() {
        try {
            add(new URI("default:rule"), new VerifyRule() {
                @Override
                public Object getKey(DataModelReflection target) {
                    return target;
                }

                @Override
                public Object verify(DataModelReflection expected, DataModelReflection actual) {
                    if (expected == null) {
                        return actual == null ? null : false;
                    }
                    return expected.equals(actual) ? null : false;
                }
            });
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Add a {@link VerifyRule} and its URI.
     * @param uri URI
     * @param rule {@link VerifyRule} to be provided
     * @return this object (for method chain)
     */
    public final MockVerifyRuleProvider add(URI uri, VerifyRule rule) {
        rules.put(uri, rule);
        return this;
    }

    @Override
    public <T> VerifyRule get(DataModelDefinition<T> definition, VerifyContext context, URI source) {
        return rules.get(source);
    }
}
