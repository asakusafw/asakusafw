/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

/**
 * Data model tester.
 * @param <T> type of model
 * @since 0.2.3
 */
public interface ModelTester<T> {

    /**
     * Tests each model object and returns error report.
     * <p>
     * example:
     * </p>
<pre><code>
&#64;Override Object verify(T expected, T actual) {
    if (expected == null) {
        return "unexpected"; // unexpected results
    } else if (actual == null) {
        return null; // ignores unnecessary results
    } else if (expected.getValue() != actual.getValue()) {
        return "value"; // invalid
    }
    return null; // successfully verified
}
</code></pre>
     * @param expected the expected model object,
     *     or {@code null} if there are no corresponded to the actual model object
     * @param actual the actual model object in test results,
     *     or {@code null} if there are no corresponded to the expected model object
     * @return diagnostic message, or {@code null} if successfully tested
     */
    Object verify(T expected, T actual);
}
