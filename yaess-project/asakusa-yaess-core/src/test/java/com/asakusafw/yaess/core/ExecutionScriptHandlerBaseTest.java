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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Test for {@link ExecutionScriptHandlerBase}.
 */
public class ExecutionScriptHandlerBaseTest {

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        Map<String, String> conf = new HashMap<>();
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<CommandScriptHandler>(
                "command.*",
                MockCommandScriptHandler.class,
                conf,
                ProfileContext.system(getClass().getClassLoader()));
        CommandScriptHandler handler = profile.newInstance();

        assertThat(handler.getHandlerId(), is("command.*"));
        assertThat(handler.getResourceId(context(), null), is(ExecutionScriptHandler.DEFAULT_RESOURCE_ID));
        assertThat(handler.getEnvironmentVariables(context(), null).size(), is(0));
    }

    /**
     * With environment variables.
     * @throws Exception if failed
     */
    @Test
    public void environment_variables() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(ExecutionScriptHandler.KEY_ENV_PREFIX + "a", "A");
        conf.put(ExecutionScriptHandler.KEY_ENV_PREFIX + "b", "B");
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<CommandScriptHandler>(
                "command.*",
                MockCommandScriptHandler.class,
                conf,
                ProfileContext.system(getClass().getClassLoader()));
        CommandScriptHandler handler = profile.newInstance();

        assertThat(handler.getHandlerId(), is("command.*"));
        assertThat(handler.getEnvironmentVariables(context(), null).size(), is(2));
        assertThat(handler.getEnvironmentVariables(context(), null).get("a"), is("A"));
        assertThat(handler.getEnvironmentVariables(context(), null).get("b"), is("B"));
    }

    /**
     * With resource override.
     * @throws Exception if failed
     */
    @Test
    public void resource() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(ExecutionScriptHandler.KEY_RESOURCE, "testing");
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<CommandScriptHandler>(
                "command.*",
                MockCommandScriptHandler.class,
                conf,
                ProfileContext.system(getClass().getClassLoader()));
        CommandScriptHandler handler = profile.newInstance();

        assertThat(handler.getHandlerId(), is("command.*"));
        assertThat(handler.getResourceId(context(), null), is("testing"));
    }

    /**
     * With placeholders in environment variables.
     * @throws Exception if failed
     */
    @Test
    public void variables() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(ExecutionScriptHandler.KEY_ENV_PREFIX + "hoge", "${VAR}");
        conf.put(ExecutionScriptHandler.KEY_RESOURCE, "alt");

        Map<String, String> entries = new HashMap<>();
        entries.put("VAR", "foo");
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<CommandScriptHandler>(
                "command.*",
                MockCommandScriptHandler.class,
                conf,
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(entries)));

        CommandScriptHandler handler = profile.newInstance();

        assertThat(handler.getHandlerId(), is("command.*"));
        assertThat(handler.getResourceId(context(), null), is("alt"));
        assertThat(handler.getEnvironmentVariables(context(), null).size(), is(1));
        assertThat(handler.getEnvironmentVariables(context(), null).get("hoge"), is("foo"));
    }

    /**
     * With unresolable placeholders in environment variables.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void variables_unresolved() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(ExecutionScriptHandler.KEY_ENV_PREFIX + "hoge", "${__INVALID__}");
        conf.put(ExecutionScriptHandler.KEY_RESOURCE, "alt");
        Map<String, String> entries = new HashMap<>();
        ServiceProfile<CommandScriptHandler> profile = new ServiceProfile<CommandScriptHandler>(
                "command.*",
                MockCommandScriptHandler.class,
                conf,
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(entries)));

        profile.newInstance();
    }

    private ExecutionContext context() {
        return new ExecutionContext("b", "f", "e", ExecutionPhase.MAIN, Collections.<String, String>emptyMap());
    }
}
