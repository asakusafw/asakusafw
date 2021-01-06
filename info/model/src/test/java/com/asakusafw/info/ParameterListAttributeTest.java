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
package com.asakusafw.info;

import java.util.Arrays;

import org.junit.Test;

/**
 * Test for {@link ParameterListAttribute}.
 */
public class ParameterListAttributeTest {

    /**
     * ser/de.
     */
    @Test
    public void serde() {
        InfoSerDe.checkRestore(
                Attribute.class,
                new ParameterListAttribute(
                        Arrays.asList(new ParameterInfo[] {
                                new ParameterInfo("a", null, true, null),
                                new ParameterInfo("b", null, true, null),
                                new ParameterInfo("c", null, true, null),
                        }),
                        true));

    }
}
