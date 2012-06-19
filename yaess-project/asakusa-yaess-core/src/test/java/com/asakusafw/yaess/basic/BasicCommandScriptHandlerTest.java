/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assume;
import org.junit.Test;

import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.CommandScriptHandler;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Test for {@link BasicCommandScriptHandler}.
 */
public class BasicCommandScriptHandlerTest extends BasicScriptHandlerTestRoot {

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File shell = putScript("arguments.sh", "bin/script.sh");

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList(shell.getAbsolutePath(), "Hello, world!"),
                map());

        CommandScriptHandler handler = handler();
        execute(script, handler);

        List<String> results = getOutput(shell);
        assertThat(results, is(Arrays.asList("Hello, world!")));
    }

    /**
     * With many arguments.
     * @throws Exception if failed
     */
    @Test
    public void multiple_arguments() throws Exception {
        File shell = putScript("arguments.sh", "bin/script.sh");

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList(shell.getAbsolutePath(), "A", "B", "C", "D"),
                map());

        CommandScriptHandler handler = handler();
        execute(script, handler);

        List<String> results = getOutput(shell);
        assertThat(results, is(Arrays.asList("A", "B", "C", "D")));
    }

    /**
     * Using prefix.
     * @throws Exception if failed
     */
    @Test
    public void with_prefix() throws Exception {
        File shell = putScript("arguments.sh", "bin/script.sh");

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList("Hello, world!"),
                map());

        CommandScriptHandler handler = handler("command.0", shell.getAbsolutePath());
        execute(script, handler);

        List<String> results = getOutput(shell);
        assertThat(results, is(Arrays.asList("Hello, world!")));
    }

    /**
     * Using complex prefix.
     * @throws Exception if failed
     */
    @Test
    public void complex_prefix() throws Exception {
        File shell = putScript("arguments.sh", "bin/script.sh");

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList("A", "B", "C"),
                map());

        CommandScriptHandler handler = handler(
                "command.0", shell.getAbsolutePath(),
                "command.1", "@[2]-@[0]-@[1]");
        execute(script, handler);

        List<String> results = getOutput(shell);
        assertThat(results, is(Arrays.asList("C-A-B", "A", "B", "C")));
    }

    /**
     * Show environment variables.
     * @throws Exception if failed
     */
    @Test
    public void environment() throws Exception {
        File shell = putScript("environment.sh", "bin/script.sh");

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList(shell.getAbsolutePath()),
                map("script", "SCRIPT", "override", "SCRIPT"));

        CommandScriptHandler handler = handler("env.handler", "HANDLER", "env.override", "HANDLER");
        execute(script, handler);

        List<String> results = getOutput(shell);
        assertThat(results, hasItem(equalToIgnoringWhiteSpace("script=SCRIPT")));
        assertThat(results, hasItem(equalToIgnoringWhiteSpace("handler=HANDLER")));
        assertThat(results, hasItem(equalToIgnoringWhiteSpace("override=SCRIPT")));
    }

    /**
     * Exit abnormally.
     * @throws Exception if failed
     */
    @Test(expected = ExitCodeException.class)
    public void abnormal_exit() throws Exception {
        File shell = putScript("abnormal.sh", "bin/script.sh");

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList(shell.getAbsolutePath(), "Hello, world!"),
                map());

        CommandScriptHandler handler = handler();
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * Invoke setup.
     * @throws Exception if failed
     */
    @Test
    public void setup() throws Exception {
        CommandScriptHandler handler = handler();

        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.SETUP, map());
        handler.setUp(ExecutionMonitor.NULL, context);
    }

    /**
     * Invoke cleanup.
     * @throws Exception if failed
     */
    @Test
    public void cleanup() throws Exception {
        CommandScriptHandler handler = handler();

        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.CLEANUP, map());
        handler.cleanUp(ExecutionMonitor.NULL, context);
    }

    /**
     * Script is missing.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void script_missing() throws Exception {
        File shell = putScript("arguments.sh", "bin/script.sh");
        Assume.assumeThat(shell.delete(), is(true));

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList(shell.getAbsolutePath(), "Hello, world!"),
                map());

        CommandScriptHandler handler = handler();
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * Using invalid prefix.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invaid_prefix() throws Exception {
        File shell = putScript("arguments.sh", "bin/script.sh");

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList("Hello, world!"),
                map());

        CommandScriptHandler handler = handler(
                "command.0", shell.getAbsolutePath(),
                "command.1", "@[1]");
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    private CommandScriptHandler handler(String... keyValuePairs) {
        Map<String, String> conf = map(keyValuePairs);
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<CommandScriptHandler>(
                "command.testing", BasicCommandScriptHandler.class, conf, ProfileContext.system(getClass().getClassLoader()));
        try {
            return profile.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
