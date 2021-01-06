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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asakusafw.workflow.model.CommandTaskInfo.ConfigurationResolver;
import com.asakusafw.workflow.model.CommandToken;
import com.asakusafw.workflow.model.Element.Attribute;
import com.asakusafw.workflow.model.HadoopTaskInfo;
import com.asakusafw.workflow.model.InfoSerDe;
import com.asakusafw.workflow.model.MockAttribute;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Test for {@link BasicCommandTaskInfo}.
 */
public class BasicCommandTaskInfoTest {

    private static final Eq EQ = new Eq();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        BasicCommandTaskInfo info = new BasicCommandTaskInfo(
                "m",
                "p",
                "cmd",
                Arrays.asList(CommandToken.of("testing")));
        InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
    }

    /**
     * w/ arguments.
     */
    @Test
    public void arguments() {
        BasicCommandTaskInfo info = new BasicCommandTaskInfo(
                "m",
                "p",
                "cmd",
                Arrays.asList(
                        CommandToken.of("text"),
                        CommandToken.BATCH_ID,
                        CommandToken.FLOW_ID,
                        CommandToken.EXECUTION_ID,
                        CommandToken.BATCH_ARGUMENTS));
        InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
    }

    /**
     * w/ attributes.
     */
    @Test
    public void attributes() {
        BasicCommandTaskInfo info = new BasicCommandTaskInfo(
                "m",
                "p",
                "cmd",
                Arrays.asList(CommandToken.of("testing")),
                new Res());
        info.addAttribute(new MockAttribute("A"));
        InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
    }

    /**
     * w/ resolver.
     */
    @Test
    public void resolver() {
        BasicCommandTaskInfo info = new BasicCommandTaskInfo(
                "m",
                "p",
                "cmd",
                Arrays.asList(CommandToken.of("testing")),
                new Res());
        InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
    }

    /**
     * w/ blockers.
     */
    @Test
    public void blockers() {
        BasicCommandTaskInfo info = new BasicCommandTaskInfo(
                "m",
                "p",
                "cmd",
                Arrays.asList(CommandToken.of("testing")));
        info.addBlocker(new BasicHadoopTaskInfo("A"));
        info.addBlocker(new BasicHadoopTaskInfo("B"));
        info.addBlocker(new BasicHadoopTaskInfo("C"));
        BasicCommandTaskInfo restored = InfoSerDe.checkRestore(TaskInfo.class, info, EQ);
        assertThat(restored.getBlockers().stream()
                .map(it -> ((HadoopTaskInfo) it).getClassName())
                .collect(Collectors.toList()),
                containsInAnyOrder("A", "B", "C"));
    }

    static class Res implements ConfigurationResolver {
        @Override
        public List<CommandToken> apply(Map<String, String> configurations) {
            return configurations.entrySet().stream()
                    .map(it -> String.format("-D%s=%s", it.getKey(), it.getValue()))
                    .map(CommandToken::of)
                    .collect(Collectors.toList());
        }
    }

    static class Eq implements BiPredicate<BasicCommandTaskInfo, BasicCommandTaskInfo> {
        @Override
        public boolean test(BasicCommandTaskInfo t, BasicCommandTaskInfo u) {
            return Objects.equals(t.getModuleName(), u.getModuleName())
                    && Objects.equals(t.getProfileName(), u.getProfileName())
                    && Objects.equals(t.getCommand(), u.getCommand())
                    && Objects.equals(
                            t.getArguments(Collections.singletonMap("a", "A")),
                            u.getArguments(Collections.singletonMap("a", "A")))
                    && Objects.equals(
                            t.getAttributes(Attribute.class).collect(Collectors.toList()),
                            u.getAttributes(Attribute.class).collect(Collectors.toList()));
        }
    }
}
