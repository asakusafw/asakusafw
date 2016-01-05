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
package com.asakusafw.compiler.fileio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.fileio.external.Ex1MockExporterDescription;
import com.asakusafw.compiler.fileio.flow.IndependentOutExporterDesc;
import com.asakusafw.compiler.fileio.flow.IndependentOutputJob;
import com.asakusafw.compiler.fileio.flow.InvalidFileNameOutputJob;
import com.asakusafw.compiler.fileio.flow.MissingPathOutputJob;
import com.asakusafw.compiler.fileio.flow.MixedInputJob;
import com.asakusafw.compiler.fileio.flow.MultipleOutputJob;
import com.asakusafw.compiler.fileio.flow.NestedOutExporterDesc;
import com.asakusafw.compiler.fileio.flow.NestedOutputJob;
import com.asakusafw.compiler.fileio.flow.NormalInputJob;
import com.asakusafw.compiler.fileio.flow.Out1ExporterDesc;
import com.asakusafw.compiler.fileio.flow.Out2ExporterDesc;
import com.asakusafw.compiler.fileio.flow.Out3ExporterDesc;
import com.asakusafw.compiler.fileio.flow.Out4ExporterDesc;
import com.asakusafw.compiler.fileio.flow.RootOutputJob;
import com.asakusafw.compiler.fileio.flow.SingleOutputJob;
import com.asakusafw.compiler.fileio.flow.SingularOutputJob;
import com.asakusafw.compiler.fileio.flow.TinyInputJob;
import com.asakusafw.compiler.fileio.model.Ex1;
import com.asakusafw.compiler.fileio.model.Ex2;
import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.sequencefile.SequenceFileModelOutput;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.vocabulary.external.FileExporterDescription;

/**
 * Test for {@link HadoopFileIoProcessor}.
 */
public class HadoopFileIoProcessorTest {

    static final Logger LOG = LoggerFactory.getLogger(HadoopFileIoProcessorTest.class);

    static volatile boolean enableOutput = false;

    /**
     * Test helper.
     */
    @Rule
    public final CompilerTester tester = new CompilerTester();

    /**
     * Run only enable output.
     */
    @Rule
    public final TestWatcher check = new TestWatcher() {

        @Override
        protected void starting(Description description) {
            Assume.assumeTrue(description.getMethodName().startsWith("output_") == false || enableOutput);
        }
    };

    /**
     * Checks Hadoop environment.
     */
    @BeforeClass
    public static void checkHadoop() {
        enableOutput = hasMapreduce370();
    }

    private static boolean hasMapreduce370() {
        try {
            File corelib = findHadoopCoreLib();
            if (corelib != null) {
                String classFile = "org/apache/hadoop/mapreduce/lib/output/MultipleOutputs.class";
                LOG.debug("Searching MultipleOutputs");
                JarFile jar = new JarFile(corelib);
                try {
                    boolean found = jar.getJarEntry(classFile) != null;
                    LOG.debug("Searched MultipleOutputs: found={}", found);
                    return found;
                } finally {
                    jar.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static File findHadoopCoreLib() {
        String hadoop = System.getenv("HADOOP_HOME");
        if (hadoop != null) {
            File home = new File(hadoop);
            for (File file : home.listFiles()) {
                String name = file.getName();
                if (name.startsWith("hadoop-core-") && name.endsWith(".jar")) {
                    LOG.debug("hadoop-core.jar is found on: {}", file.getAbsoluteFile());
                    return file;
                }
            }
        }
        LOG.debug("hadoop-core.jar is not found");
        return null;
    }

    /**
     * validated.
     * @throws Exception if failed
     */
    @Test
    public void validate() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.ENABLED.getSymbol());
        tester.compileJobflow(SingleOutputJob.class);
    }

    /**
     * missing path.
     * @throws Exception expected
     */
    @Test(expected = IOException.class)
    public void validate_invalid_compiler_option() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                "__INVALID__");
        tester.compileJobflow(SingleOutputJob.class);
    }

    /**
     * missing path.
     * @throws Exception expected
     */
    @Test(expected = IOException.class)
    public void validate_missing_path() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.ENABLED.getSymbol());
        tester.compileJobflow(MissingPathOutputJob.class);
    }

    /**
     * invalid file name.
     * @throws Exception expected
     */
    @Test(expected = IOException.class)
    public void validate_inavalid_file_name() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.ENABLED.getSymbol());
        tester.compileJobflow(InvalidFileNameOutputJob.class);
    }

    /**
     * singular path.
     * @throws Exception expected
     */
    @Test(expected = IOException.class)
    public void validate_singular_file() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.ENABLED.getSymbol());
        tester.compileJobflow(SingularOutputJob.class);
    }

    /**
     * root folder.
     * @throws Exception expected
     */
    @Test(expected = IOException.class)
    public void validate_root() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.ENABLED.getSymbol());
        tester.compileJobflow(RootOutputJob.class);
    }

    /**
     * Input single dataset.
     * @throws Exception if failed
     */
    @Test
    public void input_single() throws Exception {
        JobflowInfo info = tester.compileJobflow(NormalInputJob.class);

        ModelOutput<Ex1> s10 = openOutput(Ex1.class, Location.fromPath("target/testing/in/normal1-0", '/'));
        writeEx1(s10, 1, 2);

        ModelOutput<Ex1> s20 = openOutput(Ex1.class, Location.fromPath("target/testing/in/normal2-0", '/'));
        writeEx1(s20, 3, 4, 5);

        ModelOutput<Ex1> s21 = openOutput(Ex1.class, Location.fromPath("target/testing/in/normal2-1", '/'));
        writeEx1(s21, 6, 7, 8, 9);

        assertThat(tester.run(info), is(true));
        checkResults(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    /**
     * Input a tiny dataset.
     * @throws Exception if failed
     */
    @Test
    public void input_tiny() throws Exception {
        tester.options().setHashJoinForTiny(true);
        JobflowInfo info = tester.compileJobflow(TinyInputJob.class);

        ModelOutput<Ex2> s10 = openOutput(Ex2.class, Location.fromPath("target/testing/in/tiny1-0", '/'));
        writeEx2(s10, 1, 2);

        ModelOutput<Ex2> s20 = openOutput(Ex2.class, Location.fromPath("target/testing/in/tiny2-0", '/'));
        writeEx2(s20, 3, 4, 5);

        ModelOutput<Ex2> s21 = openOutput(Ex2.class, Location.fromPath("target/testing/in/tiny2-1", '/'));
        writeEx2(s21, 6, 7, 8, 9);

        assertThat(tester.run(info), is(true));
        checkResults(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    /**
     * Input mixed datasets.
     * @throws Exception if failed
     */
    @Test
    public void input_mixed() throws Exception {
        tester.options().setHashJoinForTiny(true);
        JobflowInfo info = tester.compileJobflow(MixedInputJob.class);

        ModelOutput<Ex1> s10 = openOutput(Ex1.class, Location.fromPath("target/testing/in/normal1-0", '/'));
        writeEx1(s10, 1, 2);
        ModelOutput<Ex1> s20 = openOutput(Ex1.class, Location.fromPath("target/testing/in/normal2-0", '/'));
        writeEx1(s20, 3, 4);

        ModelOutput<Ex2> t10 = openOutput(Ex2.class, Location.fromPath("target/testing/in/tiny1-0", '/'));
        writeEx2(t10, 5, 6);
        ModelOutput<Ex2> t20 = openOutput(Ex2.class, Location.fromPath("target/testing/in/tiny2-0", '/'));
        writeEx2(t20, 7, 8);

        assertThat(tester.run(info), is(true));
        checkResults(1, 2, 3, 4, 5, 6, 7, 8);
    }

    private void writeEx1(ModelOutput<Ex1> output, int... sids) throws IOException {
        try {
            Ex1 value = new Ex1();
            for (int sid : sids) {
                value.setSid(sid);
                value.setValue(sid);
                value.setStringAsString(String.valueOf(sid));
                output.write(value);
            }
        } finally {
            output.close();
        }
    }

    private void writeEx2(ModelOutput<Ex2> output, int... sids) throws IOException {
        try {
            Ex2 value = new Ex2();
            for (int sid : sids) {
                value.setSid(sid);
                value.setValue(sid);
                value.setStringAsString(String.valueOf(sid));
                output.write(value);
            }
        } finally {
            output.close();
        }
    }

    private void checkResults(int... sids) {
        List<Ex1> list;
        try {
            Ex1MockExporterDescription instance = new Ex1MockExporterDescription();
            list = tester.getList(
                    Ex1.class,
                    Location.fromPath(instance.getPathPrefix(), '/'),
                    new Comparator<Ex1>() {
                        @Override
                        public int compare(Ex1 o1, Ex1 o2) {
                            return o1.getSidOption().compareTo(o2.getSidOption());
                        }
                    });
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        assertThat(list.size(), is(sids.length));
        for (int i = 0; i < sids.length; i++) {
            assertThat(list.get(i).getSid(), is((long) sids[i]));
        }
    }

    /**
     * Fail for MAPREDUCE-370.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void mapreduce_370() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.DISABLED.getSymbol());
        tester.compileJobflow(SingleOutputJob.class);
    }

    /**
     * Output single file set.
     * @throws Exception if failed
     */
    @Test
    public void output_single() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.ENABLED.getSymbol());
        JobflowInfo info = tester.compileJobflow(SingleOutputJob.class);

        ModelOutput<Ex1> source = tester.openOutput(Ex1.class, tester.getImporter(info, "input"));
        writeTestData(source);
        source.close();

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
    public void output_multiple() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.ENABLED.getSymbol());
        JobflowInfo info = tester.compileJobflow(MultipleOutputJob.class);

        ModelOutput<Ex1> source = tester.openOutput(Ex1.class, tester.getImporter(info, "input"));
        writeTestData(source);
        source.close();

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
    public void output_independent() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.ENABLED.getSymbol());
        JobflowInfo info = tester.compileJobflow(IndependentOutputJob.class);

        ModelOutput<Ex1> source = tester.openOutput(Ex1.class, tester.getImporter(info, "input"));
        writeTestData(source);
        source.close();

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
    public void output_nested() throws Exception {
        tester.options().putExtraAttribute(
                HadoopFileIoProcessor.OPTION_EXPORTER_ENABLED,
                GenericOptionValue.ENABLED.getSymbol());
        JobflowInfo info = tester.compileJobflow(NestedOutputJob.class);

        ModelOutput<Ex1> source = tester.openOutput(Ex1.class, tester.getImporter(info, "input"));
        writeTestData(source);
        source.close();

        assertThat(tester.run(info), is(true));

        List<Ex1> out1 = getList(Out1ExporterDesc.class);
        checkSids(out1);
        checlValues(out1, 100);

        List<Ex1> out2 = getList(NestedOutExporterDesc.class);
        checkSids(out2);
        checlValues(out2, 200);
    }

    private <T> ModelOutput<T> openOutput(Class<T> aClass, Location location) throws IOException {
        Configuration conf = tester.configuration();
        Path path = new Path(location.toPath('/'));
        FileSystem fs = path.getFileSystem(conf);
        SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, NullWritable.class, aClass);
        return new SequenceFileModelOutput<T>(writer);
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

    private List<Ex1> getList(Class<? extends FileExporterDescription> exporter) {
        try {
            FileExporterDescription instance = exporter.newInstance();
            Path path = new Path(Location.fromPath(instance.getPathPrefix(), '/').toString());
            FileSystem fs = path.getFileSystem(tester.configuration());
            FileStatus[] statuses = fs.globStatus(path);
            List<Ex1> results = new ArrayList<Ex1>();
            for (FileStatus status : statuses) {
                SequenceFile.Reader reader = new SequenceFile.Reader(fs, status.getPath(), tester.configuration());
                try {
                    Ex1 model = new Ex1();
                    while (reader.next(NullWritable.get(), model)) {
                        Ex1 copy = new Ex1();
                        copy.copyFrom(model);
                        results.add(copy);
                    }
                } finally {
                    reader.close();
                }
            }
            Collections.sort(results, new Comparator<Ex1>() {
                @Override
                public int compare(Ex1 o1, Ex1 o2) {
                    return o1.getSidOption().compareTo(o2.getSidOption());
                }
            });
            return results;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
