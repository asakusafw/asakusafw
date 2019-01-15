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
package com.asakusafw.yaess.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link FlowScript}.
 */
public class FlowScriptTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        Map<ExecutionPhase, List<? extends ExecutionScript>> exec = new HashMap<>();
        exec.put(ExecutionPhase.MAIN, Arrays.asList(hadoop(1)));
        Set<ExecutionScript.Kind> kinds = EnumSet.allOf(ExecutionScript.Kind.class);
        FlowScript script = new FlowScript("testing", set("b1", "b2"), exec, kinds);

        assertThat(script.getId(), is("testing"));
        assertThat(script.getBlockerIds(), is(set("b1", "b2")));
        assertThat(script.getScripts().size(), is(ExecutionPhase.values().length));
        assertThat(script.getScripts().get(ExecutionPhase.MAIN), hasItem(hadoop(1)));
    }

    /**
     * Loads flow.
     * @throws Exception if failed
     */
    @Test
    public void loadFlow() throws Exception {
        Map<ExecutionPhase, List<? extends ExecutionScript>> exec = new HashMap<>();
        exec.put(ExecutionPhase.IMPORT, Arrays.asList(command(0)));
        exec.put(ExecutionPhase.MAIN, Arrays.asList(hadoop(1), command(2, 1)));
        exec.put(ExecutionPhase.EXPORT, Arrays.asList(command(100)));
        Set<ExecutionScript.Kind> kinds = EnumSet.allOf(ExecutionScript.Kind.class);
        FlowScript script = new FlowScript("testing", set("b1", "b2"), exec, kinds);

        exec.put(ExecutionPhase.INITIALIZE, Arrays.asList(command(100)));
        FlowScript dummy = new FlowScript("dummy", set(), exec, kinds);


        Properties p = new Properties();
        script.storeTo(p);
        dummy.storeTo(p);

        FlowScript loaded = FlowScript.load(p, "testing");
        assertThat(loaded, is(script));
    }

    /**
     * Loads flow.
     * @throws Exception if failed
     */
    @Test
    public void loadFlow_enables() throws Exception {
        Map<ExecutionPhase, List<? extends ExecutionScript>> exec = new HashMap<>();
        exec.put(ExecutionPhase.IMPORT, Arrays.asList(command(0)));
        exec.put(ExecutionPhase.MAIN, Arrays.asList(command(1, 2)));
        exec.put(ExecutionPhase.EXPORT, Arrays.asList(command(100)));
        Set<ExecutionScript.Kind> kinds = EnumSet.of(ExecutionScript.Kind.COMMAND);
        FlowScript script = new FlowScript("testing", set(), exec, kinds);

        Properties p = new Properties();
        script.storeTo(p);

        FlowScript loaded = FlowScript.load(p, "testing");
        assertThat(loaded, is(script));
        assertThat(loaded.getEnabledScriptKinds(), is(kinds));
    }

    /**
     * Loads flow.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFlow_missing() throws Exception {
        Properties p = new Properties();
        FlowScript.load(p, "testing");
    }

    /**
     * Loads phase.
     * @throws Exception if failed
     */
    @Test
    public void loadPhase() throws Exception {
        Map<ExecutionPhase, List<? extends ExecutionScript>> exec = new HashMap<>();
        exec.put(ExecutionPhase.IMPORT, Arrays.asList(command(0)));
        exec.put(ExecutionPhase.MAIN, Arrays.asList(hadoop(1), command(2, 1)));
        exec.put(ExecutionPhase.EXPORT, Arrays.asList(command(100)));
        Set<ExecutionScript.Kind> kinds = EnumSet.allOf(ExecutionScript.Kind.class);
        FlowScript script = new FlowScript("testing", set("b1", "b2"), exec, kinds);

        exec.put(ExecutionPhase.INITIALIZE, Arrays.asList(command(100)));
        FlowScript dummy = new FlowScript("dummy", set(), exec, kinds);

        Properties p = new Properties();
        script.storeTo(p);
        dummy.storeTo(p);

        Set<ExecutionScript> loaded = FlowScript.load(p, "testing", ExecutionPhase.MAIN);
        assertThat(loaded.size(), is(2));
        assertThat(loaded, hasItem(hadoop(1)));
        assertThat(loaded, hasItem(command(2, 1)));
    }

    /**
     * Loads empty phase.
     * @throws Exception if failed
     */
    @Test
    public void loadPhase_empty() throws Exception {
        Map<ExecutionPhase, List<? extends ExecutionScript>> exec = new HashMap<>();
        Set<ExecutionScript.Kind> kinds = EnumSet.allOf(ExecutionScript.Kind.class);
        FlowScript script = new FlowScript("testing", set("b1", "b2"), exec, kinds);

        exec.put(ExecutionPhase.INITIALIZE, Arrays.asList(command(100)));
        FlowScript dummy = new FlowScript("dummy", set(), exec, kinds);

        Properties p = new Properties();
        script.storeTo(p);
        dummy.storeTo(p);

        Set<ExecutionScript> loaded = FlowScript.load(p, "testing", ExecutionPhase.MAIN);
        assertThat(loaded.size(), is(0));
    }

    /**
     * Loads phase from missing flow.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadPhase_missing() throws Exception {
        Properties p = new Properties();
        FlowScript.load(p, "testing", ExecutionPhase.MAIN);
    }

    private ExecutionScript hadoop(int num, int... blockers) {
        Set<String> blockerIds = toBlockerIds(blockers);
        return new HadoopScript(
                toId(num),
                blockerIds,
                "Class" + num,
                map("prop", "value" + num),
                map("ASAKUSA_HOME", "/home/" + num + "/asakusa"));
    }

    private ExecutionScript command(int num, int... blockers) {
        Set<String> blockerIds = toBlockerIds(blockers);
        return new CommandScript(
                toId(num),
                blockerIds,
                "profile" + num,
                "module" + num,
                Arrays.asList("a", "b", toId(num)),
                map("ASAKUSA_HOME", "/home/" + num + "/asakusa"));
    }

    private String toId(int num) {
        return "id" + num;
    }

    private Set<String> toBlockerIds(int... blockers) {
        Set<String> blockerIds = new HashSet<>();
        for (int blocker : blockers) {
            blockerIds.add(toId(blocker));
        }
        return blockerIds;
    }

    private Set<String> set(String... values) {
        return new TreeSet<>(Arrays.asList(values));
    }

    private Map<String, String> map(String... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0;
        Map<String, String> conf = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            conf.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return conf;
    }
}
