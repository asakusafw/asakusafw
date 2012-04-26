/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.multidispatch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.VariableResolver;
import com.asakusafw.yaess.multidispatch.FailCommandScriptHandler.MessageException;

/**
 * Test for {@link ExecutionScriptHandlerDispatcher}.
 */
public class ExecutionScriptHandlerDispatcherTest {

    private static final String BATCH_ID = "batch";

    private static final String PREFIX = "testing";

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * obtains handler ID.
     * @throws Exception if failed
     */
    @Test
    public void getHandlerId() throws Exception {
        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);
        assertThat(dispatcher.getHandlerId(), is(PREFIX));
    }

    /**
     * obtains resource ID.
     * @throws Exception if failed
     */
    @Test
    public void getResourceId() throws Exception {
        put("testing.*=other");

        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        declare(conf, "other", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext something = context("something");
        assertThat(dispatcher.getResourceId(something, null), is("default"));

        ExecutionContext testing = context("testing");
        assertThat(dispatcher.getResourceId(testing, null), is("other"));
    }

    /**
     * obtains prop.
     * @throws Exception if failed
     */
    @Test
    public void getProperties() throws Exception {
        put("testing.*=other");

        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        declare(conf, "other", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext something = context("something");
        assertThat(dispatcher.getProperties(something, null).get("value"), is("default"));

        ExecutionContext testing = context("testing");
        assertThat(dispatcher.getProperties(testing, null).get("value"), is("other"));
    }

    /**
     * obtains env.
     * @throws Exception if failed
     */
    @Test
    public void getEnvironmentVariables() throws Exception {
        put("testing.*=other");

        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        declare(conf, "other", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext something = context("something");
        assertThat(dispatcher.getEnvironmentVariables(something, null).get("value"), is("default"));

        ExecutionContext testing = context("testing");
        assertThat(dispatcher.getEnvironmentVariables(testing, null).get("value"), is("other"));
    }

    /**
     * run setUp.
     * @throws Exception if failed
     */
    @Test
    public void setUp() throws Exception {
        put("testing.*=other");

        Map<String, String> conf = createConf();
        declare(conf, "default", FailCommandScriptHandler.class);
        declare(conf, "other", FailCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext something = context("something", ExecutionPhase.SETUP);
        try {
            dispatcher.setUp(ExecutionMonitor.NULL, something);
            fail();
        } catch (MessageException e) {
            assertThat(e.message, is("default"));
            assertThat(e.context.getPhase(), is(ExecutionPhase.SETUP));
        }

        ExecutionContext testing = context("testing", ExecutionPhase.SETUP);
        try {
            dispatcher.setUp(ExecutionMonitor.NULL, testing);
            fail();
        } catch (MessageException e) {
            assertThat(e.message, is("other"));
            assertThat(e.context.getPhase(), is(ExecutionPhase.SETUP));
        }
    }

    /**
     * runs execute.
     * @throws Exception if failed
     */
    @Test
    public void execute() throws Exception {
        put("testing.*=other");

        Map<String, String> conf = createConf();
        declare(conf, "default", FailCommandScriptHandler.class);
        declare(conf, "other", FailCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        CommandScript script = script("stage");
        ExecutionContext something = context("something", ExecutionPhase.MAIN);
        try {
            dispatcher.execute(ExecutionMonitor.NULL, something, script);
            fail();
        } catch (MessageException e) {
            assertThat(e.message, is("default"));
            assertThat(e.context.getPhase(), is(ExecutionPhase.MAIN));
        }

        ExecutionContext testing = context("testing");
        try {
            dispatcher.execute(ExecutionMonitor.NULL, testing, script);
            fail();
        } catch (MessageException e) {
            assertThat(e.message, is("other"));
            assertThat(e.context.getPhase(), is(ExecutionPhase.MAIN));
        }
    }

    /**
     * match to stage.
     * @throws Exception if failed
     */
    @Test
    public void match_stage() throws Exception {
        put("flow.main.stage=match");

        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        declare(conf, "match", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext context = context("flow", ExecutionPhase.MAIN);
        CommandScript script = script("stage");

        assertThat(dispatcher.getResourceId(context, script), is("match"));

        ExecutionContext otherFlow = context("otherflow", ExecutionPhase.MAIN);
        assertThat(dispatcher.getResourceId(otherFlow, script), is("default"));

        ExecutionContext otherPhase = context("flow", ExecutionPhase.PROLOGUE);
        assertThat(dispatcher.getResourceId(otherPhase, script), is("default"));

        CommandScript otherScript = script("otherstage");
        assertThat(dispatcher.getResourceId(context, otherScript), is("default"));
    }

    /**
     * match to phase.
     * @throws Exception if failed
     */
    @Test
    public void match_phase() throws Exception {
        put("flow.main.*=match");

        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        declare(conf, "match", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext context = context("flow", ExecutionPhase.MAIN);
        CommandScript script = script("stage");
        assertThat(dispatcher.getResourceId(context, script), is("match"));

        ExecutionContext otherFlow = context("otherflow", ExecutionPhase.MAIN);
        assertThat(dispatcher.getResourceId(otherFlow, script), is("default"));

        ExecutionContext otherPhase = context("flow", ExecutionPhase.PROLOGUE);
        assertThat(dispatcher.getResourceId(otherPhase, script), is("default"));

        CommandScript otherScript = script("otherstage");
        assertThat(dispatcher.getResourceId(context, otherScript), is("match"));
    }

    /**
     * match to flow.
     * @throws Exception if failed
     */
    @Test
    public void match_flow() throws Exception {
        put("flow.*=match");

        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        declare(conf, "match", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext context = context("flow", ExecutionPhase.MAIN);
        CommandScript script = script("stage");
        assertThat(dispatcher.getResourceId(context, script), is("match"));

        ExecutionContext otherFlow = context("otherflow", ExecutionPhase.MAIN);
        assertThat(dispatcher.getResourceId(otherFlow, script), is("default"));

        ExecutionContext otherPhase = context("flow", ExecutionPhase.PROLOGUE);
        assertThat(dispatcher.getResourceId(otherPhase, script), is("match"));

        CommandScript otherScript = script("otherstage");
        assertThat(dispatcher.getResourceId(context, otherScript), is("match"));
    }

    /**
     * match to batch.
     * @throws Exception if failed
     */
    @Test
    public void match_batch() throws Exception {
        put("*=match");

        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        declare(conf, "match", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext context = context("flow", ExecutionPhase.MAIN);
        CommandScript script = script("stage");
        assertThat(dispatcher.getResourceId(context, script), is("match"));

        ExecutionContext otherFlow = context("otherflow", ExecutionPhase.MAIN);
        assertThat(dispatcher.getResourceId(otherFlow, script), is("match"));

        ExecutionContext otherPhase = context("flow", ExecutionPhase.PROLOGUE);
        assertThat(dispatcher.getResourceId(otherPhase, script), is("match"));

        CommandScript otherScript = script("otherstage");
        assertThat(dispatcher.getResourceId(context, otherScript), is("match"));
    }

    /**
     * runs cleanUp.
     * @throws Exception if failed
     */
    @Test
    public void cleanUp() throws Exception {
        put("testing.*=other");

        Map<String, String> conf = createConf();
        declare(conf, "default", FailCommandScriptHandler.class);
        declare(conf, "other", FailCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext something = context("something", ExecutionPhase.CLEANUP);
        try {
            dispatcher.cleanUp(ExecutionMonitor.NULL, something);
            fail();
        } catch (MessageException e) {
            assertThat(e.message, is("default"));
            assertThat(e.context.getPhase(), is(ExecutionPhase.CLEANUP));
        }

        ExecutionContext testing = context("testing", ExecutionPhase.CLEANUP);
        try {
            dispatcher.cleanUp(ExecutionMonitor.NULL, testing);
            fail();
        } catch (MessageException e) {
            assertThat(e.message, is("other"));
            assertThat(e.context.getPhase(), is(ExecutionPhase.CLEANUP));
        }
    }

    /**
     * obtains resource ID.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void unknown_resource() throws Exception {
        put("testing.*=unknown");

        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        declare(conf, "other", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);

        ExecutionContext testing = context("testing");
        dispatcher.getResourceId(testing, null);
    }

    /**
     * conf directory is not defined.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void missing_confdir() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        declare(conf, "default", MockCommandScriptHandler.class);
        create(conf);
    }

    /**
     * conf directory is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_confdir() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(ExecutionScriptHandlerDispatcher.KEY_CONF, "${invalid}");
        declare(conf, "default", MockCommandScriptHandler.class);
        create(conf);
    }

    /**
     * conf directory is not created.
     * @throws Exception if failed
     */
    @Test
    public void confdir_not_exist() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(ExecutionScriptHandlerDispatcher.KEY_CONF, new File(folder.getRoot(), "empty").getAbsolutePath());
        declare(conf, "default", MockCommandScriptHandler.class);
        CommandScriptHandlerDispatcher dispatcher = create(conf);
        assertThat(dispatcher.getResourceId(context("flow"), null), is("default"));
    }

    /**
     * obtains resource ID.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_subcomponent() throws Exception {
        Map<String, String> conf = createConf();
        declare(conf, "default", MockCommandScriptHandler.class);
        conf.put("invalid.subcomponent", "invalid");
        create(conf);
    }

    /**
     * obtains resource ID.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void missing_defaultcomponent() throws Exception {
        Map<String, String> conf = createConf();
        declare(conf, "other", MockCommandScriptHandler.class);
        create(conf);
    }

    private CommandScriptHandlerDispatcher create(Map<String, String> conf) throws InterruptedException, IOException {
        ServiceProfile<CommandScriptHandlerDispatcher> profile = new ServiceProfile<CommandScriptHandlerDispatcher>(
                PREFIX,
                CommandScriptHandlerDispatcher.class,
                conf,
                new ProfileContext(
                        getClass().getClassLoader(),
                        new VariableResolver(Collections.<String, String>emptyMap())));
        return profile.newInstance();
    }

    private Map<String, String> createConf() {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put("conf", folder.getRoot().getAbsolutePath());
        return conf;
    }

    private void declare(Map<String, String> conf, String name, Class<?> aClass) {
        conf.put(name, aClass.getName());
        conf.put(name + "." + ExecutionScriptHandler.KEY_RESOURCE, name);
        conf.put(name + "." + ExecutionScriptHandler.KEY_ENV_PREFIX + "value", name);
        conf.put(name + "." + ExecutionScriptHandler.KEY_PROP_PREFIX + "value", name);
    }

    private void put(String... pairs) throws IOException {
        Properties p = new Properties();
        for (String pair : pairs) {
            String[] atoms = pair.split("=", 2);
            p.setProperty(atoms[0], atoms[1]);
        }
        File file = folder.newFile(BATCH_ID + ExecutionScriptHandlerDispatcher.SUFFIX_CONF);
        OutputStream output = new FileOutputStream(file);
        try {
            p.store(output, BATCH_ID);
        } finally {
            output.close();
        }
    }

    private ExecutionContext context(String flowId) {
        return context(flowId, ExecutionPhase.MAIN);
    }

    private ExecutionContext context(String flowId, ExecutionPhase phase) {
        return new ExecutionContext(BATCH_ID, flowId, "exec", phase, Collections.<String, String>emptyMap());
    }

    CommandScript script(String scriptId) {
        CommandScript script = new CommandScript(
                scriptId,
                Collections.<String>emptySet(),
                "profile",
                "module",
                Collections.singletonList("hello"),
                Collections.<String, String>emptyMap());
        return script;
    }
}
