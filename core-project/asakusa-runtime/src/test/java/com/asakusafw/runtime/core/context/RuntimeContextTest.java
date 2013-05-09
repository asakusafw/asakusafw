/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.core.context;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;

/**
 * Test for {@link RuntimeContext}.
 */
public class RuntimeContextTest {

    /**
     * Keeps global {@link RuntimeContext} clean.
     */
    @Rule
    public final RuntimeContextKeeper keeper = new RuntimeContextKeeper();

    /**
     * temporary class path.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Apply empty.
     */
    @Test
    public void apply() {
        RuntimeContext applied = RuntimeContext.DEFAULT.apply(new HashMap<String, String>());
        assertThat(applied, is(RuntimeContext.DEFAULT));
    }

    /**
     * Apply batch ID.
     */
    @Test
    public void apply_batchId() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(RuntimeContext.KEY_BATCH_ID, "testing.batch");
        RuntimeContext applied = RuntimeContext.DEFAULT.apply(map);
        assertThat(applied, is(not(RuntimeContext.DEFAULT)));
        assertThat(applied, is(RuntimeContext.DEFAULT.batchId("testing.batch")));
    }

    /**
     * Apply execution mode.
     */
    @Test
    public void apply_mode_prod() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(RuntimeContext.KEY_EXECUTION_MODE, ExecutionMode.PRODUCTION.getSymbol());
        RuntimeContext applied = RuntimeContext.DEFAULT.apply(map);
        assertThat(applied, is(RuntimeContext.DEFAULT.mode(ExecutionMode.PRODUCTION)));
    }

    /**
     * Apply execution mode.
     */
    @Test
    public void apply_mode_sim() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(RuntimeContext.KEY_EXECUTION_MODE, ExecutionMode.SIMULATION.getSymbol());
        RuntimeContext applied = RuntimeContext.DEFAULT.apply(map);
        assertThat(applied, is(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION)));
    }

    /**
     * Apply invalid execution mode.
     */
    @Test
    public void apply_mode_invalid() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(RuntimeContext.KEY_EXECUTION_MODE, "==INVALID==");
        RuntimeContext applied = RuntimeContext.DEFAULT.apply(map);
        assertThat(applied, is(RuntimeContext.DEFAULT));
    }

    /**
     * Apply verification code.
     */
    @Test
    public void apply_verificationCode() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(RuntimeContext.KEY_BUILD_ID, "testing.verify");
        RuntimeContext applied = RuntimeContext.DEFAULT.apply(map);
        assertThat(applied, is(not(RuntimeContext.DEFAULT)));
        assertThat(applied, is(RuntimeContext.DEFAULT.buildId("testing.verify")));
    }

    /**
     * Unapply empty context.
     */
    @Test
    public void unapply_empty() {
        Map<String, String> map = RuntimeContext.DEFAULT.unapply();
        assertThat(RuntimeContext.DEFAULT.apply(map), is(RuntimeContext.DEFAULT));
    }

    /**
     * Unapply context.
     */
    @Test
    public void unapply() {
        RuntimeContext context = RuntimeContext.DEFAULT
            .mode(ExecutionMode.SIMULATION)
            .batchId("testing.batch")
            .buildId("testing.verify");
        Map<String, String> map = context.unapply();
        assertThat(RuntimeContext.DEFAULT.apply(map), is(context));
    }

    /**
     * Test for simulation.
     */
    @Test
    public void isSimulation_production() {
        assertThat(RuntimeContext.DEFAULT.mode(ExecutionMode.PRODUCTION).isSimulation(), is(false));
    }

    /**
     * Test for simulation.
     */
    @Test
    public void isSimulation_simulation() {
        assertThat(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION).isSimulation(), is(true));
    }

    /**
     * Test for executable.
     */
    @Test
    public void canExecute_normal_prod() {
        RuntimeContext context = RuntimeContext.DEFAULT.mode(ExecutionMode.PRODUCTION);
        assertThat(context.canExecute(new Object()), is(true));
    }

    /**
     * Test for executable.
     */
    @Test
    public void canExecute_supported_prod() {
        RuntimeContext context = RuntimeContext.DEFAULT.mode(ExecutionMode.PRODUCTION);
        assertThat(context.canExecute(new SimulatableObject()), is(true));
    }

    /**
     * Test for executable.
     */
    @Test
    public void canExecute_normal_sim() {
        RuntimeContext context = RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION);
        assertThat(context.canExecute(new Object()), is(false));
    }

    /**
     * Test for executable.
     */
    @Test
    public void canExecute_supported_sim() {
        RuntimeContext context = RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION);
        assertThat(context.canExecute(new SimulatableObject()), is(true));
    }

    /**
     * Test for verify app.
     */
    @Test
    public void verifyApplication_ok() {
        RuntimeContext context = RuntimeContext.DEFAULT.batchId("batch").buildId("OK");
        ClassLoader loader = loader("batch", "OK", RuntimeContext.getRuntimeVersion());
        context.verifyApplication(loader);
    }

    /**
     * Test for verify app.
     */
    @Test(expected = InconsistentApplicationException.class)
    public void verifyApplication_build_fail() {
        RuntimeContext context = RuntimeContext.DEFAULT.batchId("batch").buildId("NG");
        ClassLoader loader = loader("batch", "OK", RuntimeContext.getRuntimeVersion());
        context.verifyApplication(loader);
    }

    /**
     * Test for verify app.
     */
    @Test(expected = InconsistentApplicationException.class)
    public void verifyApplication_runtime_fail() {
        RuntimeContext context = RuntimeContext.DEFAULT.batchId("batch").buildId("OK");
        ClassLoader loader = loader("batch", "OK", "NG");
        context.verifyApplication(loader);
    }

    /**
     * Test for verify app.
     */
    @Test
    public void verifyApplication_orthogonal() {
        RuntimeContext context = RuntimeContext.DEFAULT.batchId("batch").buildId("NG");
        ClassLoader loader = loader("other", "OK", RuntimeContext.getRuntimeVersion());
        context.verifyApplication(loader);
    }

    /**
     * Set to global.
     */
    @Test
    public void global() {
        assertThat(RuntimeContext.get(), is(RuntimeContext.DEFAULT));

        RuntimeContext context = RuntimeContext.DEFAULT
            .mode(ExecutionMode.SIMULATION)
            .batchId("testing.batch")
            .buildId("testing.verify");
        RuntimeContext.set(context);
        assertThat(RuntimeContext.get(), is(context));
    }

    private ClassLoader loader(String batchId, String buildId, String rtVersion) {
        File file = new File(folder.getRoot(), RuntimeContext.PATH_APPLICATION_INFO);
        assertThat(file.getParentFile().mkdirs(), is(true));
        Properties p = new Properties();
        p.setProperty(RuntimeContext.KEY_BATCH_ID, batchId);
        p.setProperty(RuntimeContext.KEY_BUILD_ID, buildId);
        p.setProperty(RuntimeContext.KEY_RUNTIME_VERSION, rtVersion);
        try {
            OutputStream s = new FileOutputStream(file);
            try {
                p.store(s, "testing");
            } finally {
                s.close();
            }
            return new URLClassLoader(new URL[] {
                    folder.getRoot().toURI().toURL(),
            });
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @SimulationSupport
    private static class SimulatableObject {
        public SimulatableObject() {
            return;
        }
    }
}
