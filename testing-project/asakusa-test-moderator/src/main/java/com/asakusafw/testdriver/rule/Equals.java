/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

/**
 * Accepts iff both values are equal and not null.
 * @since 0.2.0
 */
public class Equals implements ValuePredicate<Object> {

    @Override
    public boolean accepts(Object expected, Object actual) {
        if (expected == null || actual == null) {
            throw new IllegalArgumentException();
        }
        return expected.equals(actual);
    }

    @Override
    public String describeExpected(Object expected, Object actual) {
        if (expected == null) {
            return "(error)";
        }
        return MessageFormat.format(
                "= {0}",
                Util.format(expected));
    }
}
