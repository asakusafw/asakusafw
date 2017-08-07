/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.info.windgate;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asakusafw.info.InfoSerDe;

/**
 * Test for {@link WindGateInputInfo}.
 */
public class WindGateInputInfoTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        InfoSerDe.checkRestore(
                WindGateInputInfo.class,
                new WindGateInputInfo(
                        "test-name",
                        "com.example.WGD",
                        "test-profile",
                        "test-resource",
                        Arrays.stream(new String[][] {
                            { "A", "a" },
                            { "B", "b" },
                            { "C", "c" },
                        }).collect(Collectors.toMap(it -> it[0], it -> it[1]))));
    }
}
