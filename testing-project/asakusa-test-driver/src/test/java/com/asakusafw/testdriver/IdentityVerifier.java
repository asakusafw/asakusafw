/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import java.text.MessageFormat;

import com.asakusafw.testdriver.core.ModelVerifier;

/**
 * Verifies two objects by perfect match.
 * @since 0.2.0
 */
public class IdentityVerifier implements ModelVerifier<Object> {

    @Override
    public Object getKey(Object target) {
        return target;
    }

    @Override
    public Object verify(Object expected, Object actual) {
        if (expected == null) {
            return MessageFormat.format("Invalid actual: {0}", actual);
        } else if (actual == null) {
            return MessageFormat.format("Missing actual for: {0}", expected);
        } else if (expected.equals(actual) == false) {
            return MessageFormat.format("Inconsistent: {0} <=> {1}", expected, actual);
        }
        return null;
    }
}
