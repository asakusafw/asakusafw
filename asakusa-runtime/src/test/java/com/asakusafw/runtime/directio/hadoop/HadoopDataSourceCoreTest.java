/**
 * Copyright 2012 Asakusa Framework Team.
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.SearchPattern;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Test for {@link HadoopDataSourceCore}.
 */
public class HadoopDataSourceCoreTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private Configuration conf;

    private File mapping;

    private File temporary;

    private FileSystem fs;

    private HadoopDataSourceProfile profile;

    private OutputAttemptContext context;

    private final Counter counter = new Counter();

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        conf = new Configuration(true);
        mapping = new File(temp.getRoot(), "mapping").getCanonicalFile();
        temporary = new File(temp.getRoot(), "temporary").getCanonicalFile();
        fs = FileSystem.get(URI.create("file:///"), conf);
        profile = new HadoopDataSourceProfile(
                "testing",
                "testing",
                fs,
                new Path(mapping.toURI()),
                new Path(temporary.toURI()));
        context = new OutputAttemptContext("tx", "atmpt", profile.getId());
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
                StringBuilder.class,
                new MockFormat(),
                "input",
                SearchPattern.compile("**"));
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
                StringBuilder.class,
                new MockFormat(),
                "input",
                SearchPattern.compile("**"));
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
                StringBuilder.class,
                new MockFormat(),
                "input",
                SearchPattern.compile("**"));
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
                StringBuilder.class,
                new MockFormat(),
                "input",
                SearchPattern.compile("**"));
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
        ModelOutput<StringBuilder> output = core.openOutput(
                context,
                StringBuilder.class,
                new MockFormat(),
                "output",
                "file.txt",
                counter);
        try {
            output.write(new StringBuilder("Hello, world!"));
        } finally {
            output.close();
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
     * output multiple records.
     * @throws Exception if failed
     */
    @Test
    public void output_multirecord() throws Exception {
        HadoopDataSourceCore core = new HadoopDataSourceCore(profile);
        setup(core);
        ModelOutput<StringBuilder> output = core.openOutput(
                context,
                StringBuilder.class,
                new MockFormat(),
                "output",
                "file.txt",
                counter);
        try {
            output.write(new StringBuilder("Hello, world!"));
        } finally {
            output.close();
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
            ModelOutput<StringBuilder> output = core.openOutput(
                    context,
                    StringBuilder.class,
                    new MockFormat(),
                    "output",
                    "file" + i + ".txt",
                    counter);
            try {
                for (int j = 0; j < i + 1; j++) {
                    output.write(new StringBuilder("Hello" + j));
                }
            } finally {
                output.close();
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
        ModelOutput<StringBuilder> output = core.openOutput(
                context,
                StringBuilder.class,
                new MockFormat(),
                "output",
                "file.txt",
                counter);
        try {
            output.write(new StringBuilder("Hello, world!"));
        } finally {
            output.close();
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
        boolean result = core.delete("delete", SearchPattern.compile("**/*"));

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
        boolean result = core.delete("delete", SearchPattern.compile("**/*"));

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
                profile.getId(),
                profile.getContextPath(),
                profile.getFileSystem(),
                profile.getFileSystemPath(),
                new Path(profile.getFileSystemPath(), "_TEMP"));
        HadoopDataSourceCore core = new HadoopDataSourceCore(shareTempProfile);

        File onProd = new File(mapping, "file.txt");
        File onTemp = new File(mapping, "_TEMP/temp.txt");
        put(onProd, "production");
        put(onTemp, "temporary");

        assertThat(onProd.exists(), is(true));
        assertThat(onTemp.exists(), is(true));

        boolean result = core.delete("", SearchPattern.compile("**/*"));
        assertThat(result, is(true));
        assertThat(onProd.exists(), is(false));
        assertThat(onTemp.exists(), is(true));
    }

    private List<String> consume(
            HadoopDataSourceCore core, List<DirectInputFragment> fragments) throws IOException, InterruptedException {
        List<String> results = new ArrayList<String>();
        for (DirectInputFragment fragment : fragments) {
            ModelInput<StringBuilder> input = core.openInput(StringBuilder.class, new MockFormat(), fragment, counter);
            try {
                StringBuilder buf = new StringBuilder();
                while (input.readTo(buf)) {
                    results.add(buf.toString());
                }
            } finally {
                input.close();
            }
        }
        return results;
    }

    private List<String> get(File target) throws IOException {
        Scanner s = new Scanner(target, "UTF-8");
        try {
            List<String> results = new ArrayList<String>();
            while (s.hasNextLine()) {
                results.add(s.nextLine());
            }
            return results;
        } finally {
            s.close();
        }
    }

    private void put(File target, String... contents) throws IOException {
        target.getParentFile().mkdirs();
        PrintWriter w = new PrintWriter(target, "UTF-8");
        try {
            for (String line : contents) {
                w.println(line);
            }
        } finally {
            w.close();
        }
    }

    private void put(File target, long size) throws IOException {
        byte[] buf = "Hello, world\n".getBytes();
        long rest = size;
        target.getParentFile().mkdirs();
        OutputStream out = new FileOutputStream(target);
        try {
            OutputStream bufferred = new BufferedOutputStream(out);
            while (rest > 0) {
                int count = (int) Math.min(buf.length, rest);
                bufferred.write(buf, 0, count);
                rest -= count;
            }
            bufferred.close();
        } finally {
            out.close();
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

    private static class MockFormat extends BinaryStreamFormat<StringBuilder> {

        MockFormat() {
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
            final Scanner s = new Scanner(stream, "UTF-8");
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
            final PrintWriter w = new PrintWriter(new OutputStreamWriter(stream));
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
}
