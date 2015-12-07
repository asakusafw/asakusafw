/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.testdriver.inprocess;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.testdriver.TestDriverContext;

/**
 * Test for {@link EmulatorUtils}.
 */
public class EmulatorUtilsTest {

    /**
     * The framework configurator.
     */
    @Rule
    public final FrameworkDeployer framework = new FrameworkDeployer(false) {
        @Override
        protected void deploy() throws Throwable {
            context = new TestDriverContext(getClass());
            context.useSystemBatchApplicationsInstallationPath(true);
            context.setFrameworkHomePath(framework.getHome());
            File batchapps = new File(framework.getHome(), TestDriverContext.DEFAULT_BATCHAPPS_PATH);
            context.setBatchApplicationsInstallationPath(batchapps);
        }
    };

    TestDriverContext context;

    /**
     * Find jobflow library path.
     * @throws Exception if failed
     */
    @Test
    public void getJobflowLibraryPath() throws Exception {
        context.setCurrentBatchId("b");
        context.setCurrentFlowId("f");
        context.setCurrentExecutionId("e");
        File expected = new File(context.getBatchApplicationsInstallationPath(), "b/lib/jobflow-f.jar");
        File actual = EmulatorUtils.getJobflowLibraryPath(context);
        assertThat(actual.getCanonicalPath(), is(expected.getCanonicalPath()));
    }

    /**
     * Find batch library paths for empty libraries.
     * @throws Exception if failed
     */
    @Test
    public void getBatchLibraryPaths_empty() throws Exception {
        context.setCurrentBatchId("b");
        context.setCurrentFlowId("f");
        context.setCurrentExecutionId("e");

        Set<String> paths = normalize(EmulatorUtils.getBatchLibraryPaths(context));
        assertThat(paths, hasSize(0));
    }

    /**
     * Find batch library paths for empty libraries.
     * @throws Exception if failed
     */
    @Test
    public void getBatchLibraryPaths_found() throws Exception {
        context.setCurrentBatchId("b");
        context.setCurrentFlowId("f");
        context.setCurrentExecutionId("e");

        File base = context.getLibrariesPackageLocation(context.getCurrentBatchId());
        List<File> files = Arrays.asList(new File(base, "a.jar"), new File(base, "b.jar"));
        for (File file : files) {
            deploy("dummy.jar", file);
        }

        Set<String> paths = normalize(EmulatorUtils.getBatchLibraryPaths(context));
        assertThat(paths, is(normalize(files)));
    }

    private Set<String> normalize(Iterable<File> paths) throws IOException {
        Set<String> results = new HashSet<>();
        for (File file : paths) {
            results.add(file.getCanonicalPath());
        }
        return results;
    }

    private void deploy(String source, File dest) {
        try (InputStream in = getClass().getResourceAsStream(source)) {
            assertThat(in, is(notNullValue()));
            framework.dump(in, dest);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
