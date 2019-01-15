/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import com.asakusafw.workflow.model.DeleteTaskInfo;
import com.asakusafw.workflow.model.DeleteTaskInfo.PathKind;
import com.asakusafw.workflow.model.Element.Attribute;
import com.asakusafw.workflow.model.InfoSerDe;
import com.asakusafw.workflow.model.MockAttribute;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Test for {@link BasicDeleteTaskInfo}.
 */
public class BasicDeleteTaskInfoTest {

    private static final Eq EQ = new Eq();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        BasicDeleteTaskInfo info = new BasicDeleteTaskInfo(PathKind.LOCAL_FILE_SYSTEM, "X");
        InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
    }

    /**
     * w/ attributes.
     */
    @Test
    public void attributes() {
        BasicDeleteTaskInfo info = new BasicDeleteTaskInfo("m", PathKind.LOCAL_FILE_SYSTEM, "p");
        info.addAttribute(new MockAttribute("A"));
        InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
    }

    /**
     * w/ blockers.
     */
    @Test
    public void blockers() {
        BasicDeleteTaskInfo info = new BasicDeleteTaskInfo(PathKind.LOCAL_FILE_SYSTEM, "X");
        info.addBlocker(new BasicDeleteTaskInfo(PathKind.LOCAL_FILE_SYSTEM, "A"));
        info.addBlocker(new BasicDeleteTaskInfo(PathKind.LOCAL_FILE_SYSTEM, "B"));
        info.addBlocker(new BasicDeleteTaskInfo(PathKind.LOCAL_FILE_SYSTEM, "C"));
        BasicDeleteTaskInfo restored = InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
        assertThat(restored.getBlockers().stream()
                .map(it -> ((DeleteTaskInfo) it).getPath())
                .collect(Collectors.toList()),
                containsInAnyOrder("A", "B", "C"));
    }

    static class Eq implements BiPredicate<BasicDeleteTaskInfo, BasicDeleteTaskInfo> {
        @Override
        public boolean test(BasicDeleteTaskInfo t, BasicDeleteTaskInfo u) {
            return Objects.equals(t.getModuleName(), u.getModuleName())
                    && Objects.equals(t.getPathKind(), u.getPathKind())
                    && Objects.equals(t.getPath(), u.getPath())
                    && Objects.equals(
                            t.getAttributes(Attribute.class).collect(Collectors.toList()),
                            u.getAttributes(Attribute.class).collect(Collectors.toList()));
        }
    }

}
