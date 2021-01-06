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
package com.asakusafw.workflow.model.basic;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asakusafw.workflow.model.Element.Attribute;
import com.asakusafw.workflow.model.HadoopTaskInfo;
import com.asakusafw.workflow.model.InfoSerDe;
import com.asakusafw.workflow.model.MockAttribute;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Test for {@link BasicHadoopTaskInfo}.
 */
public class BasicHadoopTaskInfoTest {

    private static final Eq EQ = new Eq();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        BasicHadoopTaskInfo info = new BasicHadoopTaskInfo("X");
        InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
    }

    /**
     * w/ attributes.
     */
    @Test
    public void attributes() {
        BasicHadoopTaskInfo info = new BasicHadoopTaskInfo("X");
        info.addAttribute(new MockAttribute("A"));
        InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
    }

    /**
     * w/ blockers.
     */
    @Test
    public void blockers() {
        BasicHadoopTaskInfo info = new BasicHadoopTaskInfo("X");
        info.addBlocker(new BasicHadoopTaskInfo("A"));
        info.addBlocker(new BasicHadoopTaskInfo("B"));
        info.addBlocker(new BasicHadoopTaskInfo("C"));
        BasicHadoopTaskInfo restored = InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
        assertThat(restored.getBlockers().stream()
                .map(it -> ((HadoopTaskInfo) it).getClassName())
                .collect(Collectors.toList()),
                containsInAnyOrder("A", "B", "C"));
    }

    static class Eq implements BiPredicate<BasicHadoopTaskInfo, BasicHadoopTaskInfo> {
        @Override
        public boolean test(BasicHadoopTaskInfo t, BasicHadoopTaskInfo u) {
            return Objects.equals(t.getModuleName(), u.getModuleName())
                    && Objects.equals(t.getClassName(), u.getClassName())
                    && Objects.equals(
                            t.getAttributes(Attribute.class).collect(Collectors.toList()),
                            u.getAttributes(Attribute.class).collect(Collectors.toList()));
        }
    }
}
