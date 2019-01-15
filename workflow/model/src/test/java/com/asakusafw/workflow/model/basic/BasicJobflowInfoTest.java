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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asakusafw.workflow.model.HadoopTaskInfo;
import com.asakusafw.workflow.model.InfoSerDe;
import com.asakusafw.workflow.model.JobflowInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Test for {@link BasicJobflowInfo}.
 */
public class BasicJobflowInfoTest {

    private static final Eq EQ = new Eq();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        BasicJobflowInfo info = new BasicJobflowInfo("testing");
        InfoSerDe.checkRestore(JobflowInfo.class, info, EQ);
    }

    /**
     * w/ tasks.
     */
    @Test
    public void tasks() {
        BasicJobflowInfo info = new BasicJobflowInfo("testing");
        info.addTask(TaskInfo.Phase.MAIN, new BasicHadoopTaskInfo("A"));
        BasicJobflowInfo restored = InfoSerDe.checkRestore(JobflowInfo.class, info, EQ);
        Map<String, HadoopTaskInfo> tasks = extract(restored.getTasks(TaskInfo.Phase.MAIN));
        assertThat(tasks.keySet(), containsInAnyOrder("A"));
    }

    /**
     * w/ tasks.
     */
    @Test
    public void tasks_dependency() {
        BasicJobflowInfo info = new BasicJobflowInfo("testing");
        BasicHadoopTaskInfo a = new BasicHadoopTaskInfo("A");
        BasicHadoopTaskInfo b = new BasicHadoopTaskInfo("B");
        BasicHadoopTaskInfo c = new BasicHadoopTaskInfo("C");
        BasicHadoopTaskInfo d = new BasicHadoopTaskInfo("D");
        b.addBlocker(a);
        c.addBlocker(a);
        d.addBlocker(b);
        d.addBlocker(c);
        info.addTask(TaskInfo.Phase.MAIN, a);
        info.addTask(TaskInfo.Phase.MAIN, b);
        info.addTask(TaskInfo.Phase.MAIN, c);
        info.addTask(TaskInfo.Phase.MAIN, d);
        BasicJobflowInfo restored = InfoSerDe.checkRestore(JobflowInfo.class, info, EQ);
        Map<String, HadoopTaskInfo> tasks = extract(restored.getTasks(TaskInfo.Phase.MAIN));

        assertThat(tasks.keySet(), containsInAnyOrder("A", "B", "C", "D"));
        assertThat(extract(tasks.get("A").getBlockers()).keySet(), hasSize(0));
        assertThat(extract(tasks.get("B").getBlockers()).keySet(), containsInAnyOrder("A"));
        assertThat(extract(tasks.get("C").getBlockers()).keySet(), containsInAnyOrder("A"));
        assertThat(extract(tasks.get("D").getBlockers()).keySet(), containsInAnyOrder("B", "C"));
    }

    /**
     * w/ tasks.
     */
    @Test
    public void tasks_phase() {
        BasicJobflowInfo info = new BasicJobflowInfo("testing");
        BasicHadoopTaskInfo a = new BasicHadoopTaskInfo("A");
        BasicHadoopTaskInfo b = new BasicHadoopTaskInfo("B");
        BasicHadoopTaskInfo c = new BasicHadoopTaskInfo("C");
        BasicHadoopTaskInfo d = new BasicHadoopTaskInfo("D");
        BasicHadoopTaskInfo e = new BasicHadoopTaskInfo("E");
        BasicHadoopTaskInfo f = new BasicHadoopTaskInfo("F");
        BasicHadoopTaskInfo g = new BasicHadoopTaskInfo("G");
        info.addTask(TaskInfo.Phase.INITIALIZE, a);
        info.addTask(TaskInfo.Phase.IMPORT, b);
        info.addTask(TaskInfo.Phase.PROLOGUE, c);
        info.addTask(TaskInfo.Phase.MAIN, d);
        info.addTask(TaskInfo.Phase.EPILOGUE, e);
        info.addTask(TaskInfo.Phase.EXPORT, f);
        info.addTask(TaskInfo.Phase.FINALIZE, g);
        BasicJobflowInfo restored = InfoSerDe.checkRestore(JobflowInfo.class, info, EQ);
        assertThat(extract(restored.getTasks(TaskInfo.Phase.INITIALIZE)).keySet(), containsInAnyOrder("A"));
        assertThat(extract(restored.getTasks(TaskInfo.Phase.IMPORT)).keySet(), containsInAnyOrder("B"));
        assertThat(extract(restored.getTasks(TaskInfo.Phase.PROLOGUE)).keySet(), containsInAnyOrder("C"));
        assertThat(extract(restored.getTasks(TaskInfo.Phase.MAIN)).keySet(), containsInAnyOrder("D"));
        assertThat(extract(restored.getTasks(TaskInfo.Phase.EPILOGUE)).keySet(), containsInAnyOrder("E"));
        assertThat(extract(restored.getTasks(TaskInfo.Phase.EXPORT)).keySet(), containsInAnyOrder("F"));
        assertThat(extract(restored.getTasks(TaskInfo.Phase.FINALIZE)).keySet(), containsInAnyOrder("G"));
    }

    private static Map<String, HadoopTaskInfo> extract(Collection<? extends TaskInfo> tasks) {
        return tasks.stream()
                .map(it -> (HadoopTaskInfo) it)
                .collect(Collectors.toMap(HadoopTaskInfo::getClassName, Function.identity()));
    }

    static class Eq implements BiPredicate<BasicJobflowInfo, BasicJobflowInfo> {
        @Override
        public boolean test(BasicJobflowInfo t, BasicJobflowInfo u) {
            return Objects.equals(t.getId(), u.getId());
        }
    }
}
