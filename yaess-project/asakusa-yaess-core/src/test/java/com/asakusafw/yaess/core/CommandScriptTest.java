/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
 * Test for {@link CommandScript}.
 */
public class CommandScriptTest {

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
        CommandScript script = new CommandScript(
                "testing", set("blk1", "blk2"), "profile", "module",
                Arrays.asList("cmd1", "cmd2"),
                map("ASAKUSA_HOME", folder.getRoot().getAbsolutePath()));
        assertThat(script.getKind(), is(ExecutionScript.Kind.COMMAND));
        assertThat(script.getId(), is("testing"));
        assertThat(script.getBlockerIds(), is(set("blk1", "blk2")));
        assertThat(script.getProfileName(), is("profile"));
        assertThat(script.getModuleName(), is("module"));
        assertThat(script.getCommandLineTokens(), is(Arrays.asList("cmd1", "cmd2")));
        assertThat(script.getEnvironmentVariables().size(), is(1));
        assertThat(script.getEnvironmentVariables().get("ASAKUSA_HOME"), is(folder.getRoot().getAbsolutePath()));
    }

    /**
     * Resolves nothing.
     * @throws Exception if failed
     */
    @Test
    public void resolve_nothing() throws Exception {
        CommandScript script = new CommandScript(
                "testing", set("blk1", "blk2"), "profile", "module",
                Arrays.asList("cmd1", "cmd2"),
                map("ASAKUSA_HOME", folder.getRoot().getAbsolutePath()));

        ExecutionContext context = new ExecutionContext("b", "f", "e", ExecutionPhase.MAIN, map("arg", "ARG"));
        CommandScript resolved = script.resolve(context, handler());
        assertThat(resolved.isResolved(), is(true));
        assertThat(resolved, is(script));
    }

    /**
     * Resolves something.
     * @throws Exception if failed
     */
    @Test
    public void resolve() throws Exception {
        CommandScript script = new CommandScript(
                "testing", set("blk1", "blk2"), "profile", "module",
                Arrays.asList(
                        ExecutionScript.PLACEHOLDER_HOME + "/cmd1",
                        ExecutionScript.PLACEHOLDER_EXECUTION_ID,
                        ExecutionScript.PLACEHOLDER_ARGUMENTS),
                map("ASAKUSA_HOME", ExecutionScript.PLACEHOLDER_HOME));

        ExecutionContext context = new ExecutionContext("b", "f", "e", ExecutionPhase.MAIN, map("arg", "ARG"));
        CommandScript resolved = script.resolve(
                context,
                handler(ExecutionScriptHandler.KEY_ENV_PREFIX + "ASAKUSA_HOME", "ah"));
        assertThat(resolved.isResolved(), is(true));
        assertThat(resolved.getCommandLineTokens(), is(Arrays.asList("ah/cmd1", "e", context.getArgumentsAsString())));
        assertThat(resolved.getEnvironmentVariables().size(), is(1));
        assertThat(resolved.getEnvironmentVariables().get("ASAKUSA_HOME"), is("ah"));
    }

    private CommandScriptHandler handler(String... keyValuePairs) {
        Map<String, String> conf = map(keyValuePairs);
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<>(
                "command.testing", MockCommandScriptHandler.class, conf, ProfileContext.system(getClass().getClassLoader()));
        try {
            return profile.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
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
