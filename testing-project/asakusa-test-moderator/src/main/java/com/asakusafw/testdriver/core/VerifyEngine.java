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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Verifies model objects using {@link VerifyRule} and expected data set.
 * @since 0.2.0
 */
public class VerifyEngine {

    private final VerifyRule rule;

    private final Map<Object, DataModelReflection> expectedRest;

    private final Map<Object, DataModelReflection> sawActual;

    /**
     * Creates a new instance.
     * @param rule the verification strategy
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifyEngine(VerifyRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null"); //$NON-NLS-1$
        }
        this.rule = rule;
        this.expectedRest = new LinkedHashMap<Object, DataModelReflection>();
        this.sawActual = new HashMap<Object, DataModelReflection>();
    }

    /**
     * Appends the expected data model objects.
     * <p>
     * If this already saw data model objects with same key in the expected input,
     * the old one will be replaced with in input.
     * Note that the expected input will be closed.
     * </p>
     * @param expected the expected input
     * @return this object (for method chain)
     * @throws IOException if failed to obtain model objects from the input,
     *     or the input key is already registered
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifyEngine addExpected(DataModelSource expected) throws IOException {
        if (expected == null) {
            throw new IllegalArgumentException("expected must not be null"); //$NON-NLS-1$
        }
        try {
            while (true) {
                DataModelReflection next = expected.next();
                if (next == null) {
                    break;
                }
                Object key = rule.getKey(next);
                DataModelReflection old = expectedRest.put(key, next);
                if (old != null) {
                    throw new IOException(MessageFormat.format(
                            "期待値のキーが重複しています: {0} ({1} <=> {2})",
                            key,
                            old,
                            next));
                }
            }
        } finally {
            expected.close();
        }
        return this;
    }

    /**
     * Verifies the input sequence and returns diagnostics.
     * <p>
     * First, this engine search a expected model object corresponded to each input object using their
     * {@link VerifyRule#getKey(DataModelReflection) keys}.
     * If there are the such pairs, then this engine invokes
     * {@link VerifyRule#verify(DataModelReflection, DataModelReflection)
     *     VerifyRule.verifyModel(eachExpected, eachInput)}
     * for each pair, and then removes the expected data.
     * Otherwise, this engine invokes
     * {@link VerifyRule#verify(DataModelReflection, DataModelReflection)
     *     VerifyRule.verifyModel(null, eachInput)}.
     * for each input without corresponding expected object.
     * </p>
     * <p>
     * If there are any differences between expected objects and input objects,
     * the resulting list includes them.
     * </p>
     * @param input the input to verify
     * @return differences between the input and the expected, or an empty list if successfully verified
     * @throws IOException if failed to obtain model objects from the input,
     *     or the input key is already presented
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #inspectRest()
     */
    public List<Difference> inspectInput(DataModelSource input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null"); //$NON-NLS-1$
        }
        List<Difference> results = new ArrayList<Difference>();
        try {
            while (true) {
                DataModelReflection actual = input.next();
                if (actual == null) {
                    break;
                }
                Object key = rule.getKey(actual);
                DataModelReflection saw = sawActual.get(key);
                if (saw != null) {
                    results.add(new Difference(actual, null, MessageFormat.format(
                            "結果のキーが重複しています: {0} ({1} <=> {2})",
                            key,
                            saw,
                            actual)));
                } else {
                    sawActual.put(key, actual);
                    DataModelReflection expected = expectedRest.remove(key);
                    Difference diff = verify(key, expected, actual);
                    if (diff != null) {
                        results.add(diff);
                    }
                }
            }
        } finally {
            input.close();
        }
        return results;
    }

    /**
     * Verifies the rest of expected data objects.
     * <p>
     * This engine invokes
     * {@link VerifyRule#verify(DataModelReflection, DataModelReflection)
     *     VerifyRule.verifyModel(eachExpected, null)}
     * for each expected data objects, and clears them from this engine.
     * </p>
     * <p>
     * Note that the rest of expected data mean &quot;expected but appeared in input.&quot;
     * You should invoke {@link #inspectInput(DataModelSource)} for each input before invoke this method.
     * </p>
     * @return diagnostics for expected but not appeared in input objects
     * @see #inspectInput(DataModelSource)
     */
    public List<Difference> inspectRest() {
        List<Difference> results = new ArrayList<Difference>();
        for (Map.Entry<Object, DataModelReflection> entry : expectedRest.entrySet()) {
            Difference diff = verify(entry.getKey(), entry.getValue(), null);
            if (diff != null) {
                results.add(diff);
            }
        }
        expectedRest.clear();
        sawActual.clear();
        return results;
    }

    private Difference verify(Object key, DataModelReflection expected, DataModelReflection actual) {
        assert key != null;
        assert expected != null || actual != null;
        Object result = rule.verify(expected, actual);
        if (result == null) {
            return null;
        }
        return new Difference(expected, actual, result);
    }
}
