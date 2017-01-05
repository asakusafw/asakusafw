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
package com.asakusafw.runtime.directio.hadoop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataDefinition;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.SimpleDataDefinition;
import com.asakusafw.runtime.directio.util.CountInputStream;
import com.asakusafw.runtime.directio.util.CountOutputStream;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.windows.WindowsSupport;

/**
 * Test for {@link HadoopDataSourceCore}.
 */
@RunWith(Parameterized.class)
public class HadoopDataSourceCoreTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private final DataDefinition<StringBuilder> definition;

    private Configuration conf;

    private File mapping;

    private File temporary;

    private File localtemp;

    private HadoopDataSourceProfile profile;

    private OutputAttemptContext context;

    private final Counter counter = new Counter();

    /**
     * Returns the parameters.
     * @return the parameters
     */
    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { new MockStreamFormat() },
                { new MockFileFormat() },
        });
    }

    /**
     * Creates a new instance.
     * @param format the format.
     */
    public HadoopDataSourceCoreTest(DataFormat<StringBuilder> format) {
        this.definition = SimpleDataDefinition.newInstance(StringBuilder.class, format);
    }

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        conf = new Configuration(true);
        if (definition.getDataFormat() instanceof Configurable) {
            ((Configurable) definition.getDataFormat()).setConf(conf);
        }
        mapping = new File(temp.getRoot(), "mapping").getCanonicalFile();
        temporary = new File(temp.getRoot(), "temporary").getCanonicalFile();
        localtemp = new File(temp.getRoot(), "localtemp").getCanonicalFile();
        profile = new HadoopDataSourceProfile(
                conf,
                "testing",
                "testing",
                new Path(mapping.toURI()),
                new Path(temporary.toURI()));
        context = new OutputAttemptContext("tx", "atmpt", profile.getId(), new Counter());
    }

    /**
     * simple input.
     * @throws Exception if failed
     */
    @Test
    public void input() throws Exception {
        put(new File(mapping, "input/file.txt"), "Hello, world!");
        profile.setMinimumFragmentSize(-1);

        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        List<DirectInputFragment> fragments = core.findInputFragments(
                definition,
                "input",
                FilePattern.compile("**"));
        assertThat(fragments.size(), is(1));

        List<String> results = consume(core, fragments);
        assertThat(counter.get(), is(greaterThan(0L)));
        assertThat(results.size(), is(1));
        assertThat(results, hasItem("Hello, world!"));
    }

    /**
     * input multiple records.
     * @throws Exception if failed
     */
    @Test
    public void input_multirecord() throws Exception {
        put(new File(mapping, "input/file.txt"), "Hello1", "Hello2", "Hello3");
        profile.setMinimumFragmentSize(-1);

        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        List<DirectInputFragment> fragments = core.findInputFragments(
                definition,
                "input",
                FilePattern.compile("**"));
        assertThat(fragments.size(), is(1));

        List<String> results = consume(core, fragments);
        assertThat(counter.get(), is(greaterThan(0L)));
        assertThat(results.size(), is(3));
        assertThat(results, hasItem("Hello1"));
        assertThat(results, hasItem("Hello2"));
        assertThat(results, hasItem("Hello3"));
    }

    /**
     * input multiple records.
     * @throws Exception if failed
     */
    @Test
    public void input_large() throws Exception {
        long fragmentSize = 1 * 1024 * 1024;
        int fragmentCount = 20;
        put(new File(mapping, "input/file.txt"), fragmentSize * fragmentCount);
        profile.setMinimumFragmentSize(1);
        profile.setPreferredFragmentSize(fragmentSize);
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        List<DirectInputFragment> fragments = core.findInputFragments(
                definition,
                "input",
                FilePattern.compile("**"));
        assertThat(fragments.size(), is(greaterThanOrEqualTo(fragmentCount / 2)));
        for (DirectInputFragment fragment : fragments) {
            assertThat(fragment.getSize(), is(greaterThanOrEqualTo(fragmentSize / 2)));
            assertThat(fragment.getSize(), is(lessThanOrEqualTo(fragmentSize * 2)));
        }
    }

    /**
     * input multiple files.
     * @throws Exception if failed
     */
    @Test
    public void input_multifile() throws Exception {
        put(new File(mapping, "input/file1.txt"), "Hello1");
        put(new File(mapping, "input/file2.txt"), "Hello2");
        put(new File(mapping, "input/file3.txt"), "Hello3");
        profile.setMinimumFragmentSize(-1);

        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        List<DirectInputFragment> fragments = core.findInputFragments(
                definition,
                "input",
                FilePattern.compile("**"));
        assertThat(fragments.size(), is(3));

        List<String> results = consume(core, fragments);
        assertThat(counter.get(), is(greaterThan(0L)));
        assertThat(results.size(), is(3));
        assertThat(results, hasItem("Hello1"));
        assertThat(results, hasItem("Hello2"));
        assertThat(results, hasItem("Hello3"));
    }

    /**
     * simple output.
     * @throws Exception if failed
     */
    @Test
    public void output() throws Exception {
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        setup(core);
        try (ModelOutput<StringBuilder> output = core.openOutput(
                context,
                definition,
                "output",
                "file.txt",
                counter)){
            output.write(new StringBuilder("Hello, world!"));
        }
        assertThat(counter.get(), is(greaterThan(0L)));

        File target = new File(mapping, "output/file.txt");
        assertThat(target.exists(), is(false));
        commitAttempt(core);

        assertThat(target.exists(), is(false));
        commitTransaction(core);

        assertThat(target.exists(), is(true));

        assertThat(get(target), is(Arrays.asList("Hello, world!")));
    }

    /**
     * output without staging.
     * @throws Exception if failed
     */
    @Test
    public void output_nostaging() throws Exception {
        profile.setOutputStaging(false);
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        setup(core);
        try (ModelOutput<StringBuilder> output = core.openOutput(
                context,
                definition,
                "output",
                "file.txt",
                counter)) {
            output.write(new StringBuilder("Hello, world!"));
        }
        assertThat(counter.get(), is(greaterThan(0L)));

        File target = new File(mapping, "output/file.txt");
        assertThat(target.exists(), is(false));

        commitAttempt(core);
        assertThat(target.exists(), is(true));

        commitTransaction(core);
        assertThat(target.exists(), is(true));

        assertThat(get(target), is(Arrays.asList("Hello, world!")));
    }

    /**
     * output without streaming.
     * @throws Exception if failed
     */
    @Test
    public void output_nostreaming() throws Exception {
        profile.setOutputStreaming(false);
        profile.getLocalFileSystem().getConf().set(
                HadoopDataSourceUtil.KEY_LOCAL_TEMPDIR,
                localtemp.getPath());
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        setup(core);
        try (ModelOutput<StringBuilder> output = core.openOutput(
                context,
                definition,
                "output",
                "file.txt",
                counter)) {
            output.write(new StringBuilder("Hello, world!"));
        }
        assertThat(counter.get(), is(greaterThan(0L)));

        File target = new File(mapping, "output/file.txt");
        assertThat(target.exists(), is(false));

        commitAttempt(core);
        assertThat(target.exists(), is(false));

        commitTransaction(core);
        assertThat(target.exists(), is(true));

        assertThat(get(target), is(Arrays.asList("Hello, world!")));
    }

    /**
     * output without streaming nor staging.
     * @throws Exception if failed
     */
    @Test
    public void output_nomove() throws Exception {
        profile.setOutputStaging(false);
        profile.setOutputStreaming(false);
        profile.getLocalFileSystem().getConf().set(
                HadoopDataSourceUtil.KEY_LOCAL_TEMPDIR,
                localtemp.getPath());
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        setup(core);
        try (ModelOutput<StringBuilder> output = core.openOutput(
                context,
                definition,
                "output",
                "file.txt",
                counter)) {
            output.write(new StringBuilder("Hello, world!"));
        }
        assertThat(counter.get(), is(greaterThan(0L)));

        File target = new File(mapping, "output/file.txt");
        assertThat(target.exists(), is(false));

        commitAttempt(core);
        assertThat(target.exists(), is(true));

        commitTransaction(core);
        assertThat(target.exists(), is(true));

        assertThat(get(target), is(Arrays.asList("Hello, world!")));
    }

    /**
     * output multiple records.
     * @throws Exception if failed
     */
    @Test
    public void output_multirecord() throws Exception {
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        setup(core);
        try (ModelOutput<StringBuilder> output = core.openOutput(
                context,
                definition,
                "output",
                "file.txt",
                counter)) {
            output.write(new StringBuilder("Hello, world!"));
        }

        File target = new File(mapping, "output/file.txt");
        assertThat(target.exists(), is(false));
        commitAttempt(core);

        assertThat(target.exists(), is(false));
        commitTransaction(core);

        assertThat(target.exists(), is(true));

        assertThat(get(target), is(Arrays.asList("Hello, world!")));
    }

    /**
     * output multiple files.
     * @throws Exception if failed
     */
    @Test
    public void output_multifile() throws Exception {
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        setup(core);
        for (int i = 0; i < 3; i++) {
            try (ModelOutput<StringBuilder> output = core.openOutput(
                    context,
                    definition,
                    "output",
                    "file" + i + ".txt",
                    counter)) {
                for (int j = 0; j < i + 1; j++) {
                    output.write(new StringBuilder("Hello" + j));
                }
            }
        }
        commit(core);
        assertThat(get(new File(mapping, "output/file0.txt")), is(Arrays.asList("Hello0")));
        assertThat(get(new File(mapping, "output/file1.txt")), is(Arrays.asList("Hello0", "Hello1")));
        assertThat(get(new File(mapping, "output/file2.txt")), is(Arrays.asList("Hello0", "Hello1", "Hello2")));
    }

    /**
     * rollback output.
     * @throws Exception if failed
     */
    @Test
    public void output_rollback() throws Exception {
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        setup(core);
        try (ModelOutput<StringBuilder> output = core.openOutput(
                context,
                definition,
                "output",
                "file.txt",
                counter)) {
            output.write(new StringBuilder("Hello, world!"));
        }
        cleanup(core);
        assertThat(new File(mapping, "output/file.txt").exists(), is(false));
    }

    /**
     * simple delete.
     * @throws Exception if failed
     */
    @Test
    public void delete() throws Exception {
        File file = new File(mapping, "delete/file.txt");
        put(file, "Hello, world!");

        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);

        assertThat(file.exists(), is(true));
        boolean result = core.delete("delete", FilePattern.compile("**/*"), true, counter);

        assertThat(result, is(true));
        assertThat(file.exists(), is(false));
    }

    /**
     * simple delete.
     * @throws Exception if failed
     */
    @Test
    public void delete_multifile() throws Exception {
        File[] files = {
                new File(mapping, "delete/file.txt"),
                new File(mapping, "delete/file2.txt"),
                new File(mapping, "delete/a/file.txt"),
                new File(mapping, "delete/a/b/file.txt"),
        };
        for (File file : files) {
            put(file, "Hello, world!");
        }
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);

        for (File file : files) {
            assertThat(file.exists(), is(true));
        }
        boolean result = core.delete("delete", FilePattern.compile("**/*"), true, counter);

        assertThat(result, is(true));
        for (File file : files) {
            assertThat(file.exists(), is(false));
        }
    }

    /**
     * simple delete.
     * @throws Exception if failed
     */
    @Test
    public void delete_sharetemp() throws Exception {
        HadoopDataSourceProfile shareTempProfile = new HadoopDataSourceProfile(
                conf,
                profile.getId(),
                profile.getContextPath(),
                profile.getFileSystemPath(),
                new Path(profile.getFileSystemPath(), "_TEMP"));
        HadoopDataSourceCore core = new HadoopDataSourceCore(shareTempProfile);

        File onProd = new File(mapping, "file.txt");
        File onTemp = new File(mapping, "_TEMP/temp.txt");
        put(onProd, "production");
        put(onTemp, "temporary");

        assertThat(onProd.exists(), is(true));
        assertThat(onTemp.exists(), is(true));

        boolean result = core.delete("", FilePattern.compile("**/*"), true, counter);
        assertThat(result, is(true));
        assertThat(onProd.exists(), is(false));
        assertThat(onTemp.exists(), is(true));
    }

    /**
     * simple delete.
     * @throws Exception if failed
     */
    @Test
    public void delete_all() throws Exception {
        File file = new File(mapping, "file.txt");
        put(file, "Hello, world!");

        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);

        assertThat(file.exists(), is(true));
        boolean result = core.delete("", FilePattern.compile("**"), true, counter);

        assertThat(result, is(true));
        assertThat(file.exists(), is(false));
        assertThat("the root directory must not be deleted", mapping.exists(), is(true));
    }

    private List<String> consume(
            HadoopDataSourceCore core, List<DirectInputFragment> fragments) throws IOException, InterruptedException {
        List<String> results = new ArrayList<>();
        for (DirectInputFragment fragment : fragments) {
            try (ModelInput<StringBuilder> input = core.openInput(definition, fragment, counter)) {
                StringBuilder buf = new StringBuilder();
                while (input.readTo(buf)) {
                    results.add(buf.toString());
                }
            }
        }
        return results;
    }

    private List<String> get(File target) throws IOException {
        try (Scanner s = new Scanner(target, "UTF-8")) {
            List<String> results = new ArrayList<>();
            while (s.hasNextLine()) {
                results.add(s.nextLine());
            }
            return results;
        }
    }

    private void put(File target, String... contents) throws IOException {
        target.getParentFile().mkdirs();
        try (PrintWriter w = new PrintWriter(target, "UTF-8")) {
            for (String line : contents) {
                w.println(line);
            }
        }
    }

    private void put(File target, long size) throws IOException {
        byte[] buf = "Hello, world\n".getBytes();
        long rest = size;
        target.getParentFile().mkdirs();

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(target))) {
            while (rest > 0) {
                int count = (int) Math.min(buf.length, rest);
                out.write(buf, 0, count);
                rest -= count;
            }
        }
    }

    private void setup(HadoopDataSourceCore core) throws IOException, InterruptedException {
        core.setupTransactionOutput(context.getTransactionContext());
        core.setupAttemptOutput(context);
    }

    private void commit(HadoopDataSourceCore core) throws IOException, InterruptedException {
        commitAttempt(core);
        commitTransaction(core);
    }

    private void commitAttempt(HadoopDataSourceCore core) throws IOException, InterruptedException {
        core.commitAttemptOutput(context);
        core.cleanupAttemptOutput(context);
    }

    private void commitTransaction(HadoopDataSourceCore core) throws IOException, InterruptedException {
        core.commitTransactionOutput(context.getTransactionContext());
        core.cleanupTransactionOutput(context.getTransactionContext());
    }

    private void cleanup(HadoopDataSourceCore core) throws IOException, InterruptedException {
        core.cleanupAttemptOutput(context);
        core.cleanupTransactionOutput(context.getTransactionContext());
    }

    private static class MockStreamFormat extends BinaryStreamFormat<StringBuilder> {

        MockStreamFormat() {
            return;
        }

        @Override
        public Class<StringBuilder> getSupportedType() {
            return StringBuilder.class;
        }

        @Override
        public long getPreferredFragmentSize() throws IOException, InterruptedException {
            return -1;
        }

        @Override
        public long getMinimumFragmentSize() throws IOException, InterruptedException {
            return 1;
        }

        @Override
        public ModelInput<StringBuilder> createInput(Class<? extends StringBuilder> dataType, String path,
                InputStream stream, long offset, long fragmentSize) throws IOException,
                InterruptedException {
            Scanner s = new Scanner(stream, "UTF-8");
            return new ModelInput<StringBuilder>() {
                @Override
                public boolean readTo(StringBuilder model) throws IOException {
                    if (s.hasNextLine()) {
                        model.delete(0, model.length());
                        model.append(s.nextLine());
                        return true;
                    }
                    return false;
                }
                @Override
                public void close() throws IOException {
                    s.close();
                }
            };
        }

        @Override
        public ModelOutput<StringBuilder> createOutput(Class<? extends StringBuilder> dataType, String path,
                OutputStream stream) throws IOException, InterruptedException {
            PrintWriter w = new PrintWriter(new OutputStreamWriter(stream));
            return new ModelOutput<StringBuilder>() {
                @Override
                public void write(StringBuilder model) throws IOException {
                    w.println(model.toString());
                }
                @Override
                public void close() throws IOException {
                    w.close();
                }
            };
        }
    }

    private static class MockFileFormat extends HadoopFileFormat<StringBuilder> {

        private final MockStreamFormat format = new MockStreamFormat();

        MockFileFormat() {
            return;
        }

        @Override
        public Class<StringBuilder> getSupportedType() {
            return format.getSupportedType();
        }

        @Override
        public long getPreferredFragmentSize() throws IOException, InterruptedException {
            return format.getPreferredFragmentSize();
        }

        @Override
        public long getMinimumFragmentSize() throws IOException, InterruptedException {
            return format.getMinimumFragmentSize();
        }

        @Override
        public ModelInput<StringBuilder> createInput(
                Class<? extends StringBuilder> dataType,
                FileSystem fileSystem,
                Path path,
                long offset,
                long fragmentSize,
                Counter counter) throws IOException, InterruptedException {
            FileSystem fs = FileSystem.get(path.toUri(), getConf());
            FSDataInputStream in = fs.open(path);
            boolean succeed = false;
            try {
                in.seek(offset);
                ModelInput<StringBuilder> result = format.createInput(
                        dataType,
                        path.toString(),
                        new CountInputStream(in, counter),
                        offset,
                        fragmentSize);
                succeed = true;
                return result;
            } finally {
                if (succeed == false) {
                    in.close();
                }
            }
        }

        @Override
        public ModelOutput<StringBuilder> createOutput(
                Class<? extends StringBuilder> dataType,
                FileSystem fileSystem,
                Path path,
                Counter counter) throws IOException, InterruptedException {
            FileSystem fs = FileSystem.get(path.toUri(), getConf());
            FSDataOutputStream out = fs.create(path);
            return format.createOutput(dataType, path.toString(), new CountOutputStream(out, counter));
        }
    }
}
