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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceProvider;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Test for {@link DirectIoFinalizer}.
 */
public class DirectIoFinalizerTest {

    /**
     * A temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private DirectIoFinalizer testee;

    private DirectDataSourceRepository repo;

    private File production1;

    private File production2;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        File writeTest = folder.newFolder("write-test");
        Assume.assumeTrue(writeTest.setWritable(false));
        try {
            new File(writeTest, "example").createNewFile();
            Assume.assumeTrue(false);
        } catch (IOException e) {
            // ok.
        }

        Configuration conf = new Configuration();
        conf.set(HadoopDataSourceUtil.KEY_SYSTEM_DIR, folder.newFolder("system").getAbsoluteFile().toURI().toString());
        production1 = folder.newFolder("p1").getCanonicalFile();
        production2 = folder.newFolder("p2").getCanonicalFile();

        HadoopDataSourceProfile profile1 = new HadoopDataSourceProfile(
                conf, "t1", "c1",
                new Path(production1.toURI()),
                new Path(new File(folder.getRoot(), "t1").getCanonicalFile().toURI()));
        HadoopDataSourceProfile profile2 = new HadoopDataSourceProfile(
                conf, "t2", "c2",
                new Path(production2.toURI()),
                new Path(new File(folder.getRoot(), "t2").getCanonicalFile().toURI()));
        repo = new DirectDataSourceRepository(Arrays.asList(
                new MockProvider(profile1),
                new MockProvider(profile2)));

        testee = new DirectIoFinalizer(repo);
        testee.setConf(conf);
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (production1 != null) {
            production1.setWritable(true);
        }
        if (production2 != null) {
            production2.setWritable(true);
        }
    }

    /**
     * Finalizes single.
     * @throws Exception if failed
     */
    @Test
    public void finalizeSingle() throws Exception {
        indoubt("ex1");
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        assertThat(testee.finalizeSingle("__UNKNOWN__"), is(false));
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        assertThat(testee.finalizeSingle("ex1"), is(true));
        assertThat(count(production1), is(1));
        assertThat(count(production2), is(1));

        assertThat(testee.finalizeSingle("ex1"), is(false));
    }

    /**
     * Finalizes single partial applied.
     * @throws Exception if failed
     */
    @Test
    public void finalizeSingle_partial() throws Exception {
        indoubt("ex1");
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        production1.setWritable(false);
        try {
            testee.finalizeSingle("ex1");
        } catch (IOException e) {
            // ok.
        }
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(1));

        production1.setWritable(true);
        assertThat(testee.finalizeSingle("ex1"), is(true));
        assertThat(count(production1), is(1));
        assertThat(count(production2), is(1));

        assertThat(testee.finalizeSingle("ex1"), is(false));
    }

    /**
     * Finalizes all.
     * @throws Exception if failed
     */
    @Test
    public void finalizeAll() throws Exception {
        indoubt("ex1");
        indoubt("ex2");
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        assertThat(testee.finalizeAll(), is(2));
        assertThat(count(production1), is(2));
        assertThat(count(production2), is(2));

        assertThat(testee.finalizeAll(), is(0));
    }

    /**
     * Finalizes all partial applied.
     * @throws Exception if failed
     */
    @Test
    public void finalizeAll_partial() throws Exception {
        indoubt("ex1");
        indoubt("ex2");
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        production1.setWritable(false);
        try {
            testee.finalizeAll();
        } catch (IOException e) {
            // ok.
        }
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(1));

        production1.setWritable(true);
        assertThat(testee.finalizeAll(), is(2));
        assertThat(count(production1), is(2));
        assertThat(count(production2), is(2));

        assertThat(testee.finalizeAll(), is(0));
    }

    /**
     * Finalizes single via program entry.
     * @throws Exception if failed
     */
    @Test
    public void runSingle() throws Exception {
        indoubt("ex1");
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        assertThat(testee.run(new String[] {"__UNKNOWN__"}), is(0));
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        assertThat(testee.run(new String[] {"ex1"}), is(0));
        assertThat(count(production1), is(1));
        assertThat(count(production2), is(1));

        assertThat(testee.run(new String[] {"ex1"}), is(0));
    }

    /**
     * Finalizes single via program entry but failed.
     * @throws Exception if failed
     */
    @Test
    public void runSingle_failure() throws Exception {
        indoubt("ex1");

        production1.setWritable(false);
        assertThat(testee.run(new String[] {"ex1"}), is(not(0)));
    }

    /**
     * Finalizes all via program entry.
     * @throws Exception if failed
     */
    @Test
    public void runAll() throws Exception {
        indoubt("ex1");
        indoubt("ex2");
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        assertThat(testee.run(new String[] {}), is(0));
        assertThat(count(production1), is(2));
        assertThat(count(production2), is(2));

        assertThat(testee.run(new String[] {}), is(0));
    }

    /**
     * Finalizes single via program entry but failed.
     * @throws Exception if failed
     */
    @Test
    public void runAll_failure() throws Exception {
        indoubt("ex1");

        production1.setWritable(false);
        assertThat(testee.run(new String[] {}), is(not(0)));
    }

    /**
     * Invalid arguments for program entry.
     * @throws Exception if failed
     */
    @Test
    public void runInvalid() throws Exception {
        assertThat(testee.run(new String[] {"1", "2"}), is(not(0)));
    }

    private int count(File dir) {
        int count = 0;
        for (File file : dir.listFiles()) {
            if (file.getName().startsWith(".") == false) {
                count++;
            }
        }
        return count;
    }

    private void indoubt(String executionId) throws IOException, InterruptedException {
        Configuration conf = testee.getConf();
        Path cmPath = HadoopDataSourceUtil.getCommitMarkPath(conf, executionId);
        cmPath.getFileSystem(conf).create(cmPath).close();
        int index = 0;
        for (String path : repo.getContainerPaths()) {
            String id = repo.getRelatedId(path);
            DirectDataSource ds = repo.getRelatedDataSource(path);
            OutputTransactionContext txContext = HadoopDataSourceUtil.createContext(executionId, id);
            OutputAttemptContext aContext = new OutputAttemptContext(
                    txContext.getTransactionId(),
                    String.valueOf(index),
                    txContext.getOutputId());

            ds.setupTransactionOutput(txContext);
            ds.setupAttemptOutput(aContext);
            ModelOutput<StringBuilder> output = ds.openOutput(
                    aContext,
                    StringBuilder.class,
                    new MockFormat(),
                    "",
                    executionId,
                    new Counter());
            try {
                output.write(new StringBuilder("Hello, world!"));
            } finally {
                output.close();
            }
            ds.commitAttemptOutput(aContext);
            ds.cleanupAttemptOutput(aContext);

            index++;
        }
    }

    private static final class MockProvider implements DirectDataSourceProvider {

        HadoopDataSourceProfile profile;

        MockProvider(HadoopDataSourceProfile profile) {
            this.profile = profile;
        }

        @Override
        public String getId() {
            return profile.getId();
        }

        @Override
        public String getPath() {
            return profile.getContextPath();
        }

        @Override
        public DirectDataSource newInstance() throws IOException, InterruptedException {
            return new HadoopDataSourceCore(profile);
        }
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
