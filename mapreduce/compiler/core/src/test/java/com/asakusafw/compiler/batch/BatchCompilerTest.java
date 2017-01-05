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
package com.asakusafw.compiler.batch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Export;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.testing.BatchInfo;
import com.asakusafw.compiler.testing.TemporaryOutputDescription;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * Test for {@link BatchCompiler}.
 */
public class BatchCompilerTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * The test helper.
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * simple case
     * @throws Exception if exception was occurred
     */
    @Test
    public void simple() throws Exception {
        BatchInfo info = tester.compileBatch(SimpleBatch.class);
        try (ModelOutput<Ex1> output = tester.openOutput(Ex1.class, tester.getImporter(info, "x"))) {
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

        assertThat(tester.run(info), is(true));

        List<Ex1> input = tester.getList(
                Ex1.class,
                seqfile(tester.getExporter(info, "x")).asPrefix(),
                (o1, o2) -> o1.getSidOption().compareTo(o2.getSidOption()));
        assertThat(input.size(), is(3));
        assertThat(input.get(0).getValue(), is(200));
        assertThat(input.get(1).getValue(), is(300));
        assertThat(input.get(2).getValue(), is(400));
    }

    /**
     * ordered jobflows.
     * @throws Exception if exception was occurred
     */
    @Test
    public void ordered() throws Exception {
        BatchInfo info = tester.compileBatch(OrderedBatch.class);
        try (ModelOutput<Ex1> output = tester.openOutput(Ex1.class, tester.getImporter(info, "first"))) {
            Ex1 ex1 = new Ex1();
            ex1.setValue(100);
            output.write(ex1);
        }
        assertThat(tester.run(info), is(true));

        List<Ex1> input = tester.getList(
                Ex1.class,
                seqfile(tester.getExporter(info, "second")).asPrefix());
        assertThat(input.size(), is(1));
        assertThat(input.get(0).getValue(), is(103));
    }

    /**
     * w/ flow rendezvous.
     * @throws Exception if exception was occurred
     */
    @Test
    public void join() throws Exception {
        BatchInfo info = tester.compileBatch(JoinBatch.class);
        try (ModelOutput<Ex1> output = tester.openOutput(Ex1.class, tester.getImporter(info, "first"))) {
            Ex1 ex1 = new Ex1();
            ex1.setValue(100);
            output.write(ex1);
        }
        assertThat(tester.run(info), is(true));

        List<Ex1> input = tester.getList(
                Ex1.class,
                seqfile(tester.getExporter(info, "join")).asPrefix(),
                (o1, o2) -> o1.getValueOption().compareTo(o2.getValueOption()));
        assertThat(input.size(), is(2));
        assertThat(input.get(0).getValue(), is(113)); // from second
        assertThat(input.get(1).getValue(), is(114)); // from side
    }

    private Location seqfile(Export exporter) {
        ExporterDescription desc = exporter.getDescription().getExporterDescription();
        assertThat(desc, instanceOf(TemporaryOutputDescription.class));
        TemporaryOutputDescription d = (TemporaryOutputDescription) desc;
        return Location.fromPath(d.getPathPrefix(), '/');
    }
}
