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
package com.asakusafw.testdriver.tools.runner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;

import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Export;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Import;
import com.asakusafw.compiler.testing.BatchInfo;
import com.asakusafw.compiler.testing.TemporaryOutputDescription;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TesterTestRoot;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.testdriver.testing.batch.SimpleBatch;
import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * Test for {@link BatchTestRunner}.
 */
public class BatchTestRunnerTest {

    /**
     * Compiler testing tool.
     */
    @Rule
    public final CompilerTester tester = new CompilerTester(ConfigurationFactory.getDefault(), true) {
        @Override
        protected void configure(Description description) throws IOException {
            TesterTestRoot.installTo(framework());
        }
    };

    /**
     * Keeps system properties.
     */
    @Rule
    public final ExternalResource propertiesKeeper = new ExternalResource() {
        private Properties props;
        @Override
        public void before() {
            // Note: must initialize configurator before escape system properties
            TestingEnvironmentConfigurator.initialize();
            props = System.getProperties();
            Properties escape = new Properties();
            escape.putAll(props);
            System.setProperties(escape);
        }
        @Override
        public void after() {
            if (props != null) {
                System.setProperties(props);
            }
        }
    };

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        BatchInfo info = tester.compileBatch(SimpleBatch.class);
        deploy(info);

        put(info, "simple", "Hello1", "Hello2", "Hello3");
        int exitCode = BatchTestRunner.execute(new String[] {
                "-b", getBatchId(info),
        });
        assertThat(exitCode, is(0));
        List<String> results = get(info, "simple");
        assertThat(results, containsInAnyOrder("Hello1?", "Hello2?", "Hello3?"));
    }

    /**
     * With batch arguments.
     * @throws Exception if failed
     */
    @Test
    public void arguments() throws Exception {
        BatchInfo info = tester.compileBatch(SimpleBatch.class);
        deploy(info);

        put(info, "simple", "Hello1", "Hello2", "Hello3");
        int exitCode = BatchTestRunner.execute(new String[] {
                "-b", getBatchId(info),
                "-A", "t1=x",
                "-A", "t2=y",
                "-A", "t3=z",
        });
        assertThat(exitCode, is(0));
        List<String> results = get(info, "simple");
        assertThat(results, containsInAnyOrder("Hello1?xyz", "Hello2?xyz", "Hello3?xyz"));
    }

    /**
     * Use structured API.
     * @throws Exception if failed
     */
    @Test
    public void structured() throws Exception {
        BatchInfo info = tester.compileBatch(SimpleBatch.class);
        deploy(info);

        put(info, "simple", "Hello1", "Hello2", "Hello3");
        Map<String, String> args = new HashMap<>();
        args.put("t1", "x");
        args.put("t2", "y");
        args.put("t3", "z");
        int exitCode = BatchTestRunner.execute(getBatchId(info), args);
        assertThat(exitCode, is(0));
        List<String> results = get(info, "simple");
        assertThat(results, containsInAnyOrder("Hello1?xyz", "Hello2?xyz", "Hello3?xyz"));
    }

    /**
     * Batch ID is not specified.
     * @throws Exception if failed
     */
    @Test
    public void no_batch_id() throws Exception {
        BatchInfo info = tester.compileBatch(SimpleBatch.class);
        deploy(info);
        int exitCode = BatchTestRunner.execute(new String[0]);
        assertThat(exitCode, is(not(0)));
    }

    /**
     * Batch is not found.
     * @throws Exception if failed
     */
    @Test
    public void missing_batch() throws Exception {
        BatchInfo info = tester.compileBatch(SimpleBatch.class);
        deploy(info);

        put(info, "simple", "Hello1");
        int exitCode = BatchTestRunner.execute(getBatchId(info) + "__MISS");
        assertThat(exitCode, is(not(0)));
    }

    /**
     * Error occurred while executing batch app.
     * @throws Exception if failed
     */
    @Test
    public void error_batch() throws Exception {
        BatchInfo info = tester.compileBatch(SimpleBatch.class);
        deploy(info);

        put(info, "simple", "ERROR");
        int exitCode = BatchTestRunner.execute(getBatchId(info));
        assertThat(exitCode, is(not(0)));
    }

    private String getBatchId(BatchInfo info) {
        return info.getWorkflow().getDescription().getClass().getAnnotation(Batch.class).name();
    }

    private void deploy(BatchInfo info) throws IOException {
        File framework = tester.framework().getHome();
        File batchapps = new File(framework, TestDriverContext.DEFAULT_BATCHAPPS_PATH);
        deployTo(info.getOutputDirectory(), new File(batchapps, getBatchId(info)));
        System.setProperty(TestDriverContext.KEY_FRAMEWORK_PATH, framework.getAbsolutePath());
        System.clearProperty(TestDriverContext.KEY_BATCHAPPS_PATH);
        Assume.assumeThat(System.getenv(TestDriverContext.ENV_BATCHAPPS_PATH), is(nullValue()));
    }

    private void deployTo(File compilerOutput, File targetDirectory) throws IOException {
        tester.framework().copy(compilerOutput, targetDirectory);
    }

    private void put(BatchInfo info, String name, String... values) {
        Import importer = tester.getImporter(info, name);
        try (ModelOutput<Simple> output = tester.openOutput(Simple.class, importer);) {
            Simple model = new Simple();
            for (String value : values) {
                model.setValueAsString(value);
                output.write(model);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private List<String> get(BatchInfo info, String name) {
        List<String> results = new ArrayList<>();
        Export exporter = tester.getExporter(info, name);
        for (Location location : getLocations(exporter)) {
            try (ModelInput<Simple> input = tester.openInput(Simple.class, location)) {
                Simple model = new Simple();
                while (input.readTo(model)) {
                    results.add(model.getValueAsString());
                }
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return results;
    }

    private Set<Location> getLocations(Export exporter) {
        ExporterDescription description = exporter.getDescription().getExporterDescription();
        assertThat(description, is(instanceOf(TemporaryOutputDescription.class)));
        String prefix = ((TemporaryOutputDescription) description).getPathPrefix();
        return Collections.singleton(Location.fromPath(prefix, '/'));
    }
}
