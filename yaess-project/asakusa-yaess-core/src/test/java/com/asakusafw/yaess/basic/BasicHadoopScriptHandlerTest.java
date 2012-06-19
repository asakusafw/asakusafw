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

import org.junit.Test;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Test for {@link BasicHadoopScriptHandler}.
 */
public class BasicHadoopScriptHandlerTest extends BasicScriptHandlerTestRoot {

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        String target = new File(getAsakusaHome(), ProcessHadoopScriptHandler.PATH_EXECUTE).getAbsolutePath();
        File shell = putScript("arguments.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler("env.ASAKUSA_HOME", getAsakusaHome().getAbsolutePath());
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map("hello", "world", "key", "value"));
        execute(context, script, handler);

        List<String> results = getOutput(shell);
        assertThat(results.subList(0, 5), is(Arrays.asList(
                "com.example.Client",
                "tbatch",
                "tflow",
                "texec",
                context.getArgumentsAsString())));
    }

    /**
     * With properties.
     * @throws Exception if failed
     */
    @Test
    public void properties() throws Exception {
        String target = new File(getAsakusaHome(), ProcessHadoopScriptHandler.PATH_EXECUTE).getAbsolutePath();
        File shell = putScript("arguments.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map("hello", "world", "hoge", "foo"),
                map());

        HadoopScriptHandler handler = handler(
                "env.ASAKUSA_HOME", getAsakusaHome().getAbsolutePath(),
                "prop.hello", "handler",
                "prop.bar", "moga");
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        execute(context, script, handler);

        List<String> results = getOutput(shell);
        assertThat(results.subList(0, 5), is(Arrays.asList(
                "com.example.Client",
                "tbatch",
                "tflow",
                "texec",
                context.getArgumentsAsString())));

        List<String> rest = results.subList(5, results.size());
        int hello = rest.indexOf("hello=world");
        assertThat(hello, greaterThanOrEqualTo(1));
        assertThat(rest.get(hello - 1), is("-D"));

        int hoge = rest.indexOf("hoge=foo");
        assertThat(hoge, greaterThanOrEqualTo(1));
        assertThat(rest.get(hoge - 1), is("-D"));

        int bar = rest.indexOf("bar=moga");
        assertThat(bar, greaterThanOrEqualTo(1));
        assertThat(rest.get(bar - 1), is("-D"));
    }

    /**
     * Using complex prefix.
     * @throws Exception if failed
     */
    @Test
    public void complex_prefix() throws Exception {
        String target = new File(getAsakusaHome(), ProcessHadoopScriptHandler.PATH_EXECUTE).getAbsolutePath();
        File shell = putScript("arguments.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler(
                "env.ASAKUSA_HOME", getAsakusaHome().getAbsolutePath(),
                "command.0", "@[0]",
                "command.1", "@[2]-@[3]-@[4]");
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map("hello", "world", "key", "value"));
        execute(context, script, handler);

        List<String> results = getOutput(shell);
        assertThat(results.subList(0, 7), is(Arrays.asList(
                "tbatch-tflow-texec",
                shell.getAbsolutePath(),
                "com.example.Client",
                "tbatch",
                "tflow",
                "texec",
                context.getArgumentsAsString())));
    }

    /**
     * Show environment variables.
     * @throws Exception if failed
     */
    @Test
    public void environment() throws Exception {
        String target = new File(getAsakusaHome(), ProcessHadoopScriptHandler.PATH_EXECUTE).getAbsolutePath();
        File shell = putScript("environment.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map("script", "SCRIPT", "override", "SCRIPT"));

        HadoopScriptHandler handler = handler(
                "env.ASAKUSA_HOME", getAsakusaHome().getAbsolutePath(),
                "env.handler", "HANDLER",
                "env.override", "HANDLER");
        execute(script, handler);

        List<String> results = getOutput(shell);
        assertThat(results, hasItem(equalToIgnoringWhiteSpace("script=SCRIPT")));
        assertThat(results, hasItem(equalToIgnoringWhiteSpace("handler=HANDLER")));
        assertThat(results, hasItem(equalToIgnoringWhiteSpace("override=SCRIPT")));
    }

    /**
     * Invoke setup.
     * @throws Exception if failed
     */
    @Test
    public void setup() throws Exception {
        HadoopScriptHandler handler = handler("env.ASAKUSA_HOME", getAsakusaHome().getAbsolutePath());

        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.SETUP, map());
        handler.setUp(ExecutionMonitor.NULL, context);
    }

    /**
     * Exit abnormally.
     * @throws Exception if failed
     */
    @Test(expected = ExitCodeException.class)
    public void abnormal_exit() throws Exception {
        String target = new File(getAsakusaHome(), ProcessHadoopScriptHandler.PATH_EXECUTE).getAbsolutePath();
        putScript("abnormal.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler("env.ASAKUSA_HOME", getAsakusaHome().getAbsolutePath());
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * Asakusa home is missing.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void home_missing() throws Exception {
        String target = new File(getAsakusaHome(), ProcessHadoopScriptHandler.PATH_EXECUTE).getAbsolutePath();
        putScript("arguments.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler();
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * Script is missing.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void script_missing() throws Exception {
        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler("env.ASAKUSA_HOME", getAsakusaHome().getAbsolutePath());
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * Using invalid prefix.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_prefix() throws Exception {
        String target = new File(getAsakusaHome(), ProcessHadoopScriptHandler.PATH_EXECUTE).getAbsolutePath();
        putScript("arguments.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler(
                "env.ASAKUSA_HOME", getAsakusaHome().getAbsolutePath(),
                "command.0", "@[999]");
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    private HadoopScriptHandler handler(String... keyValuePairs) {
        Map<String, String> conf = map(keyValuePairs);
        ServiceProfile<HadoopScriptHandler> profile = new ServiceProfile<HadoopScriptHandler>(
                "hadoop", BasicHadoopScriptHandler.class, conf, ProfileContext.system(getClass().getClassLoader()));
        try {
            return profile.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
