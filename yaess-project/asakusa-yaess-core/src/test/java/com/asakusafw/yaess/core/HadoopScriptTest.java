/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link HadoopScript}.
 */
public class HadoopScriptTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Simple testing.
     */
    @Test
    public void simple() {
        HadoopScript script = new HadoopScript(
                "testing", set("blk1", "blk2"), "com.example.Client",
                map("com.example", "prop"),
                map("ASAKUSA_HOME", folder.getRoot().getAbsolutePath()));
        assertThat(script.getKind(), is(ExecutionScript.Kind.HADOOP));
        assertThat(script.getId(), is("testing"));
        assertThat(script.getBlockerIds(), is(set("blk1", "blk2")));
        assertThat(script.getClassName(), is("com.example.Client"));
        assertThat(script.getHadoopProperties().size(), is(1));
        assertThat(script.getHadoopProperties().get("com.example"), is("prop"));
        assertThat(script.getEnvironmentVariables().size(), is(1));
        assertThat(script.getEnvironmentVariables().get("ASAKUSA_HOME"), is(folder.getRoot().getAbsolutePath()));
    }

    /**
     * Resolves nothing.
     * @throws Exception if failed
     */
    @Test
    public void resolve_nothing() throws Exception {
        HadoopScript script = new HadoopScript(
                "testing", set("blk1", "blk2"), "com.example.Client",
                map("com.example", "prop"),
                map("ASAKUSA_HOME", folder.getRoot().getAbsolutePath()));

        ExecutionContext context = new ExecutionContext("b", "f", "e", ExecutionPhase.MAIN, map("arg", "ARG"));
        HadoopScript resolved = script.resolve(context, handler());
        assertThat(resolved.isResolved(), is(true));
        assertThat(resolved, is(script));
    }

    /**
     * Resolves something.
     * @throws Exception if failed
     */
    @Test
    public void resolve() throws Exception {
        HadoopScript script = new HadoopScript(
                "testing", set(), "com.example.Client",
                map("home", ExecutionScript.PLACEHOLDER_HOME,
                        "exec", ExecutionScript.PLACEHOLDER_EXECUTION_ID,
                        "args", ExecutionScript.PLACEHOLDER_ARGUMENTS),
                map("ASAKUSA_HOME", ExecutionScript.PLACEHOLDER_HOME));

        ExecutionContext context = new ExecutionContext("b", "f", "e", ExecutionPhase.MAIN, map("arg", "ARG"));
        HadoopScript resolved = script.resolve(context, handler(ExecutionScriptHandler.KEY_ENV_PREFIX + "ASAKUSA_HOME", "ah"));
        assertThat(resolved.isResolved(), is(true));
        assertThat(resolved.getHadoopProperties().size(), is(3));
        assertThat(resolved.getHadoopProperties().get("home"), is("ah"));
        assertThat(resolved.getHadoopProperties().get("exec"), is("e"));
        assertThat(resolved.getHadoopProperties().get("args"), is(context.getArgumentsAsString()));
        assertThat(resolved.getEnvironmentVariables().size(), is(1));
        assertThat(resolved.getEnvironmentVariables().get("ASAKUSA_HOME"), is("ah"));
    }

    private HadoopScriptHandler handler(String... keyValuePairs) {
        Map<String, String> conf = map(keyValuePairs);
        ServiceProfile<HadoopScriptHandler> profile = new ServiceProfile<HadoopScriptHandler>(
                "hadoop", MockHadoopScriptHandler.class, conf, ProfileContext.system(getClass().getClassLoader()));
        try {
            return profile.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Set<String> set(String... values) {
        return new TreeSet<String>(Arrays.asList(values));
    }

    private Map<String, String> map(String... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0;
        Map<String, String> conf = new HashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            conf.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return conf;
    }
}
