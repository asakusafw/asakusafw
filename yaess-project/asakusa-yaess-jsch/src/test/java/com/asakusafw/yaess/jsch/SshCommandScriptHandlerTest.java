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
package com.asakusafw.yaess.jsch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assume;
import org.junit.Test;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;
import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.CommandScriptHandler;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.PhaseMonitor;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Test for {@link SshCommandScriptHandler}.
 */
public class SshCommandScriptHandlerTest extends SshScriptHandlerTestRoot {

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
        assertThat(results, has(equalToIgnoringWhiteSpace("script=SCRIPT")));
        assertThat(results, has(equalToIgnoringWhiteSpace("handler=HANDLER")));
        assertThat(results, has(equalToIgnoringWhiteSpace("override=SCRIPT")));
    }

    /**
     * Passes runtime context.
     * @throws Exception if failed
     */
    @Test
    public void runtime_context() throws Exception {
        File shell = putScript("environment.sh", "bin/script.sh");

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList(shell.getAbsolutePath()),
                map("script", "SCRIPT", "override", "SCRIPT"));

        CommandScriptHandler handler = handler("env.handler", "HANDLER", "env.override", "HANDLER");

        RuntimeContext rc = RuntimeContext.DEFAULT
            .batchId("b")
            .mode(ExecutionMode.SIMULATION)
            .buildId("OK");
        ExecutionContext context = new ExecutionContext("b", "f", "e", ExecutionPhase.MAIN, map(), rc.unapply());
        execute(context, script, handler);

        Map<String, String> map = new HashMap<String, String>();
        for (String line : getOutput(shell)) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] kv = line.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            map.put(kv[0], kv[1]);
        }

        assertThat(RuntimeContext.DEFAULT.apply(map), is(rc));
    }

    /**
     * Mandatory configuration about SSH is missing.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void missing_config() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        putScript("arguments.sh", new File(target));

        Map<String, String> conf = map();
        conf.put("command.0", target);
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<CommandScriptHandler>(
                "command.default", SshCommandScriptHandler.class, conf, ProfileContext.system(getClass().getClassLoader()));
        profile.newInstance();
    }

    /**
     * Private key is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_id() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        putScript("arguments.sh", new File(target));

        Map<String, String> conf = map();
        conf.put(JschProcessExecutor.KEY_USER, "${USER}");
        conf.put(JschProcessExecutor.KEY_HOST, "localhost");
        conf.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath() + "__INVALID__");
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<CommandScriptHandler>(
                "command.default", SshCommandScriptHandler.class, conf, ProfileContext.system(getClass().getClassLoader()));
        profile.newInstance();
    }

    /**
     * Exit abnormally.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void abnormal_exit() throws Exception {
        File shell = putScript("abnormal.sh", "bin/script.sh");

        CommandScript script = new CommandScript(
                "testing", set(), "profile", "module",
                Arrays.asList(shell.getAbsolutePath(), "Hello, world!"),
                map());

        CommandScriptHandler handler = handler();
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(PhaseMonitor.NULL, context, script);
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
        conf.put(JschProcessExecutor.KEY_USER, "${USER}");
        conf.put(JschProcessExecutor.KEY_HOST, "localhost");
        conf.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<CommandScriptHandler>(
                "command.default", SshCommandScriptHandler.class, conf, ProfileContext.system(getClass().getClassLoader()));
        try {
            return profile.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
