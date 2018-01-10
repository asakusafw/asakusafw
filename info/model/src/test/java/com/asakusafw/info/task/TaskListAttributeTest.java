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
package com.asakusafw.info.task;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.asakusafw.info.Attribute;
import com.asakusafw.info.InfoSerDe;
import com.asakusafw.info.task.TaskInfo.Phase;

/**
 * Test for {@link TaskListAttribute}.
 */
public class TaskListAttributeTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        InfoSerDe.checkRestore(
                Attribute.class,
                new TaskListAttribute(Arrays.asList(
                        new TaskInfo(
                                "a",
                                Phase.IMPORT,
                                "windgate",
                                "testing",
                                Collections.emptyList()),
                        new TaskInfo(
                                "b",
                                Phase.MAIN,
                                "vanilla",
                                "vanilla",
                                Collections.emptyList()),
                        new TaskInfo(
                                "c",
                                Phase.FINALIZE,
                                "windgate",
                                "testing",
                                Collections.emptyList()),
                        new TaskInfo(
                                "d",
                                Phase.FINALIZE,
                                "finalizer",
                                "vanilla",
                                Arrays.asList("c")))));
    }
}
