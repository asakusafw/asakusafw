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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asakusafw.workflow.model.BatchInfo;
import com.asakusafw.workflow.model.InfoSerDe;
import com.asakusafw.workflow.model.JobflowInfo;

/**
 * Test for {@link BasicBatchInfo}.
 */
public class BasicBatchInfoTest {

    private static final Eq EQ = new Eq();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        BasicBatchInfo info = new BasicBatchInfo("testing");
        InfoSerDe.checkRestore(BatchInfo.class, info, EQ);
    }

    /**
     * w/ elements.
     */
    @Test
    public void elements() {
        BasicBatchInfo info = new BasicBatchInfo("testing");
        BasicJobflowInfo a = new BasicJobflowInfo("A");
        BasicJobflowInfo b = new BasicJobflowInfo("B");
        BasicJobflowInfo c = new BasicJobflowInfo("C");
        BasicJobflowInfo d = new BasicJobflowInfo("D");
        b.addBlocker(a);
        c.addBlocker(a);
        d.addBlocker(b);
        d.addBlocker(c);
        info.addElement(a);
        info.addElement(b);
        info.addElement(c);
        info.addElement(d);
        BasicBatchInfo restored = InfoSerDe.checkRestore(BatchInfo.class, info, EQ);
        Map<String, JobflowInfo> tasks = extract(restored.getElements());

        assertThat(tasks.keySet(), containsInAnyOrder("A", "B", "C", "D"));
        assertThat(extract(tasks.get("A").getBlockers()).keySet(), hasSize(0));
        assertThat(extract(tasks.get("B").getBlockers()).keySet(), containsInAnyOrder("A"));
        assertThat(extract(tasks.get("C").getBlockers()).keySet(), containsInAnyOrder("A"));
        assertThat(extract(tasks.get("D").getBlockers()).keySet(), containsInAnyOrder("B", "C"));
    }

    private static Map<String, JobflowInfo> extract(Collection<? extends JobflowInfo> tasks) {
        return tasks.stream()
                .map(it -> (JobflowInfo) it)
                .collect(Collectors.toMap(JobflowInfo::getId, Function.identity()));
    }

    static class Eq implements BiPredicate<BasicBatchInfo, BasicBatchInfo> {
        @Override
        public boolean test(BasicBatchInfo t, BasicBatchInfo u) {
            return Objects.equals(t.getId(), u.getId());
        }
    }
}
