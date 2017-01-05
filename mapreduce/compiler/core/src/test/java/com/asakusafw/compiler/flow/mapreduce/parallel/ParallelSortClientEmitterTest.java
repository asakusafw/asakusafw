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
package com.asakusafw.compiler.flow.mapreduce.parallel;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.testing.TemporaryOutputDescription;
import com.asakusafw.compiler.testing.flow.IndependentOutExporterDesc;
import com.asakusafw.compiler.testing.flow.IndependentOutputJob;
import com.asakusafw.compiler.testing.flow.MultipleOutputJob;
import com.asakusafw.compiler.testing.flow.NestedOutExporterDesc;
import com.asakusafw.compiler.testing.flow.NestedOutputJob;
import com.asakusafw.compiler.testing.flow.Out1ExporterDesc;
import com.asakusafw.compiler.testing.flow.Out2ExporterDesc;
import com.asakusafw.compiler.testing.flow.Out3ExporterDesc;
import com.asakusafw.compiler.testing.flow.Out4ExporterDesc;
import com.asakusafw.compiler.testing.flow.SingleOutputJob;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.windows.WindowsSupport;

/**
 * Test for {@link ParallelSortClientEmitter}.
 */
public class ParallelSortClientEmitterTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * Test helper.
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * Output single file set.
     * @throws Exception if failed
     */
    @Test
    public void single() throws Exception {
        single0();
    }

    /**
     * Output single file set.
     * @throws Exception if failed
     */
    @Test
    public void single_legacy() throws Exception {
        tester.options().putExtraAttribute(ParallelSortClientEmitter.ATTRIBUTE_LEGACY, "true");
        single0();
    }

    private void single0() throws IOException {
        JobflowInfo info = tester.compileJobflow(SingleOutputJob.class);

        try (ModelOutput<Ex1> source = tester.openOutput(Ex1.class, tester.getImporter(info, "input"))) {
            writeTestData(source);
        }

        assertThat(tester.run(info), is(true));

        List<Ex1> out1 = getList(Out1ExporterDesc.class);
        checkSids(out1);
        checlValues(out1, 100);
    }

    /**
     * Output multiple file sets.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        multiple0();
    }

    /**
     * Output multiple file sets.
     * @throws Exception if failed
     */
    @Test
    public void multiple_legacy() throws Exception {
        tester.options().putExtraAttribute(ParallelSortClientEmitter.ATTRIBUTE_LEGACY, "true");
        multiple0();
    }

    private void multiple0() throws IOException {
        JobflowInfo info = tester.compileJobflow(MultipleOutputJob.class);

        try (ModelOutput<Ex1> source = tester.openOutput(Ex1.class, tester.getImporter(info, "input"))) {
            writeTestData(source);
        }

        assertThat(tester.run(info), is(true));

        List<Ex1> out1 = getList(Out1ExporterDesc.class);
        checkSids(out1);
        checlValues(out1, 100);

        List<Ex1> out2 = getList(Out2ExporterDesc.class);
        checkSids(out2);
        checlValues(out2, 200);

        List<Ex1> out3 = getList(Out3ExporterDesc.class);
        checkSids(out3);
        checlValues(out3, 300);

        List<Ex1> out4 = getList(Out4ExporterDesc.class);
        checkSids(out4);
        checlValues(out4, 400);
    }

    /**
     * Output files into nested directory.
     * @throws Exception if failed
     */
    @Test
    public void independent() throws Exception {
        independent0();
    }

    /**
     * Output files into nested directory.
     * @throws Exception if failed
     */
    @Test
    public void independent_legacy() throws Exception {
        tester.options().putExtraAttribute(ParallelSortClientEmitter.ATTRIBUTE_LEGACY, "true");
        independent0();
    }

    private void independent0() throws IOException {
        JobflowInfo info = tester.compileJobflow(IndependentOutputJob.class);

        try (ModelOutput<Ex1> source = tester.openOutput(Ex1.class, tester.getImporter(info, "input"))) {
            writeTestData(source);
        }

        assertThat(tester.run(info), is(true));

        List<Ex1> out1 = getList(Out1ExporterDesc.class);
        checkSids(out1);
        checlValues(out1, 100);

        List<Ex1> out2 = getList(IndependentOutExporterDesc.class);
        checkSids(out2);
        checlValues(out2, 200);
    }

    /**
     * Output files into nested directory.
     * @throws Exception if failed
     */
    @Test
    public void nested() throws Exception {
        nested0();
    }

    /**
     * Output files into nested directory.
     * @throws Exception if failed
     */
    @Test
    public void nested_legacy() throws Exception {
        tester.options().putExtraAttribute(ParallelSortClientEmitter.ATTRIBUTE_LEGACY, "true");
        nested0();
    }

    private void nested0() throws IOException {
        JobflowInfo info = tester.compileJobflow(NestedOutputJob.class);

        try (ModelOutput<Ex1> source = tester.openOutput(Ex1.class, tester.getImporter(info, "input"))) {
            writeTestData(source);
        }

        assertThat(tester.run(info), is(true));

        List<Ex1> out1 = getList(Out1ExporterDesc.class);
        checkSids(out1);
        checlValues(out1, 100);

        List<Ex1> out2 = getList(NestedOutExporterDesc.class);
        checkSids(out2);
        checlValues(out2, 200);
    }

    private void checkSids(List<Ex1> results) {
        assertThat(results.size(), is(10));
        assertThat(results.get(0).getSidOption().isNull(), is(true));
        for (int i = 1; i < 10; i++) {
            assertThat(results.get(i).getSid(), is((long) i));
        }
    }

    private void checlValues(List<Ex1> results, int value) {
        for (Ex1 ex1 : results) {
            assertThat(ex1.getValueOption(), is(new IntOption(value)));
        }
    }

    private void writeTestData(ModelOutput<Ex1> source) throws IOException {
        Ex1 value = new Ex1();
        source.write(value);
        value.setSid(1);
        source.write(value);
        value.setSid(2);
        source.write(value);
        value.setSid(3);
        source.write(value);
        value.setSid(4);
        source.write(value);
        value.setSid(5);
        source.write(value);
        value.setSid(6);
        source.write(value);
        value.setSid(7);
        source.write(value);
        value.setSid(8);
        source.write(value);
        value.setSid(9);
        source.write(value);
    }

    private List<Ex1> getList(Class<? extends TemporaryOutputDescription> exporter) {
        try {
            TemporaryOutputDescription instance = exporter.newInstance();
            return tester.getList(
                    Ex1.class,
                    Location.fromPath(instance.getPathPrefix(), '/'),
                    (o1, o2) -> o1.getSidOption().compareTo(o2.getSidOption()));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
