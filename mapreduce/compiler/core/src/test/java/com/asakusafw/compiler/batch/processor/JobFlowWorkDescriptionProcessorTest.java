/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch.processor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.batch.BatchCompilerEnvironmentProvider;
import com.asakusafw.compiler.batch.BatchCompilingEnvironment;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;

/**
 * Test for {@link JobFlowWorkDescriptionProcessor}.
 */
public class JobFlowWorkDescriptionProcessorTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * The environment provider.
     */
    @Rule
    public BatchCompilerEnvironmentProvider prov = new BatchCompilerEnvironmentProvider();

    /**
     * The test helper.
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * simple case.
     * @throws Exception if exception was occurred
     */
    @Test
    public void simple() throws Exception {
        BatchCompilingEnvironment env = prov.getEnvironment();
        JobFlowWorkDescriptionProcessor proc = new JobFlowWorkDescriptionProcessor();
        proc.initialize(env);

        JobFlowWorkDescription jobflow = new JobFlowWorkDescription(SimpleJobFlow.class);
        JobflowModel model = proc.process(jobflow);

        File jar = JobFlowWorkDescriptionProcessor.getPackageLocation(
                env.getConfiguration().getOutputDirectory(),
                model.getFlowId());
        JobflowInfo info = DirectFlowCompiler.toInfo(model, jar, jar);

        try (ModelOutput<Ex1> output = tester.openOutput(
                Ex1.class,
                Location.fromPath("target/testing/SimpleJobFlow/importer/out", '/'))) {
            Ex1 ex1 = new Ex1();
            ex1.setSid(100);
            ex1.setValue(100);
            output.write(ex1);
            ex1.setSid(300);
            ex1.setValue(300);
            output.write(ex1);
            ex1.setSid(200);
            ex1.setValue(200);
            output.write(ex1);
        }

        tester.run(info);

        List<Ex1> input = tester.getList(
                Ex1.class,
                Location.fromPath("target/testing/SimpleJobFlow/exporter/out-*", '/'),
                (o1, o2) -> o1.getSidOption().compareTo(o2.getSidOption()));
        assertThat(input.size(), is(3));
        assertThat(input.get(0).getValue(), is(200));
        assertThat(input.get(1).getValue(), is(300));
        assertThat(input.get(2).getValue(), is(400));
    }
}
