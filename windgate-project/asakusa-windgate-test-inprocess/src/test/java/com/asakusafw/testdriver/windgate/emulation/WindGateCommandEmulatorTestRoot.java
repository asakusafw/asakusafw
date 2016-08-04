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
package com.asakusafw.testdriver.windgate.emulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.ExternalResource;

import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.vocabulary.windgate.Constants;

/**
 * Common base class of WindGate command emulators.
 */
public abstract class WindGateCommandEmulatorTestRoot {

    /**
     * The default profile name for testing.
     */
    public static final String PROFILE = "testing";

    /**
     * Environment name of testing base directory.
     */
    public static final String ENV_TEST_BASE_DIR = "TEST_TMP";

    static final String DUMMY_FRAMEWORK_VERSION = "0.0.0.testing";

    static final String FLOW_ID = "flow";

    static final String EXECUTION_ID = "exec";

    /**
     * The framework configurator.
     */
    @Rule
    public final FrameworkDeployer framework = new FrameworkDeployer(false) {
        @Override
        protected void deploy() throws Throwable {
            copy(new File("src/test/dist"), getHome());
            context =  new TestDriverContext(WindGateCommandEmulatorTestRoot.class) {
                @Override
                public String getDevelopmentEnvironmentVersion() {
                    return DUMMY_FRAMEWORK_VERSION;
                }
            };
            context.useSystemBatchApplicationsInstallationPath(true);
            context.setFrameworkHomePath(getHome());
            context.setBatchApplicationsInstallationPath(
                    new File(getHome(), TestDriverContext.DEFAULT_BATCHAPPS_PATH));
            context.getEnvironmentVariables().put(ENV_TEST_BASE_DIR, getWork("windgate").getAbsolutePath());
            context.setCurrentBatchId("b");
            context.setCurrentFlowId("f");
            context.setCurrentExecutionId("e");
        }
    };

    /**
     * Disposes objects.
     */
    @Rule
    public final ExternalResource disposer = new ExternalResource() {
        @Override
        protected void after() {
            if (context != null) {
                context.cleanUpTemporaryResources();
            }
        }
    };

    TestDriverContext context;

    /**
     * Returns the context.
     * @return the context
     */
    public TestDriverContext getContext() {
        return context;
    }

    /**
     * Returns the working directory.
     * @return the working directory
     */
    public File getLocalBase() {
        return new File(context.getEnvironmentVariables().get(ENV_TEST_BASE_DIR), "file");
    }

    /**
     * Returns a windgate related command.
     * @param path the command path from the framework home
     * @param args the command arguments
     * @return the built command
     */
    public TestExecutionPlan.Command command(String path, String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(new File(framework.getHome(), path).getPath());
        Collections.addAll(cmd, args);
        return new TestExecutionPlan.Command(
                cmd,
                Constants.MODULE_NAME,
                PROFILE,
                Collections.emptyMap());
    }
}
