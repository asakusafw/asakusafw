/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.directio.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceProvider;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.directio.SimpleDataDefinition;
import com.asakusafw.runtime.directio.hadoop.DirectIoTransactionEditor;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceCore;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceProfile;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Test for {@link DirectIoTransactionEditor}.
 */
public class DirectIoCommandTestRoot {

    /**
     * A temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Current configuration.
     */
    protected Configuration conf;

    /**
     * Editor.
     */
    protected DirectIoTransactionEditor editor;

    /**
     * Current repository.
     */
    protected DirectDataSourceRepository repo;

    /**
     * Storage mapped to c1.
     */
    protected File production1;

    /**
     * Storage mapped to c2.
     */
    protected File production2;

    private File temporary;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        File writeTest = folder.newFolder("write-test");
        Assume.assumeTrue(writable(writeTest, false));
        try {
            new File(writeTest, "example").createNewFile();
            Assume.assumeTrue(false);
        } catch (IOException e) {
            // ok.
        }

        this.conf = new Configuration();
        conf.set(HadoopDataSourceUtil.KEY_SYSTEM_DIR, folder.newFolder("system").getAbsoluteFile().toURI().toString());
        temporary = folder.newFolder("temp").getCanonicalFile();
        production1 = folder.newFolder("p1").getCanonicalFile();
        production2 = folder.newFolder("p2").getCanonicalFile();

        HadoopDataSourceProfile profile1 = new HadoopDataSourceProfile(
                conf, "t1", "c1",
                new Path(production1.toURI()),
                new Path(new File(temporary, "t1").toURI()));
        HadoopDataSourceProfile profile2 = new HadoopDataSourceProfile(
                conf, "t2", "c2",
                new Path(production2.toURI()),
                new Path(new File(temporary, "t2").toURI()));
        repo = new DirectDataSourceRepository(Arrays.asList(
                new MockProvider(profile1),
                new MockProvider(profile2)));

        editor = new DirectIoTransactionEditor(repo);
        editor.setConf(conf);
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (production1 != null) {
            writable(production1, true);
        }
        if (production2 != null) {
            writable(production2, true);
        }
        if (temporary != null) {
            writable(temporary, true);
        }
    }

    /**
     * Sets writable flag to target recursively.
     * @param target target path
     * @param writable flag
     * @return true iff is succeeded
     */
    protected boolean writable(File target, boolean writable) {
        if (target.exists() == false) {
            return false;
        }
        boolean succeed = true;
        if (target.isDirectory()) {
            for (File child : target.listFiles()) {
                succeed &= writable(child, writable);
            }
        }
        return succeed && target.setWritable(writable);
    }

    /**
     * Returns target file count.
     * @param dir target dir
     * @return count
     */
    protected int count(File dir) {
        int count = 0;
        for (File file : dir.listFiles()) {
            if (file.getName().startsWith(".") == false) {
                count++;
            }
        }
        return count;
    }

    /**
     * Creates a new indoubt transaction.
     * @param executionId target execution id
     * @throws IOException if failed
     * @throws InterruptedException if interrupted
     */
    protected void indoubt(String executionId) throws IOException, InterruptedException {
        Path txPath = HadoopDataSourceUtil.getTransactionInfoPath(conf, executionId);
        Path cmPath = HadoopDataSourceUtil.getCommitMarkPath(conf, executionId);
        FileSystem fs = txPath.getFileSystem(conf);
        fs.create(txPath).close();
        fs.create(cmPath).close();
        int index = 0;
        for (String path : repo.getContainerPaths()) {
            String id = repo.getRelatedId(path);
            DirectDataSource ds = repo.getRelatedDataSource(path);
            OutputTransactionContext txContext = HadoopDataSourceUtil.createContext(executionId, id);
            OutputAttemptContext aContext = new OutputAttemptContext(
                    txContext.getTransactionId(),
                    String.valueOf(index),
                    txContext.getOutputId(),
                    new Counter());

            ds.setupTransactionOutput(txContext);
            ds.setupAttemptOutput(aContext);
            try (ModelOutput<StringBuilder> output = ds.openOutput(
                    aContext,
                    SimpleDataDefinition.newInstance(StringBuilder.class, new MockFormat()),
                    "",
                    executionId,
                    new Counter())) {
                output.write(new StringBuilder("Hello, world!"));
            }
            ds.commitAttemptOutput(aContext);
            ds.cleanupAttemptOutput(aContext);

            index++;
        }
    }

    /**
     * Mock {@link DirectDataSourceProvider}.
     */
    protected static final class MockProvider implements DirectDataSourceProvider {

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

    /**
     * Mock {@link BinaryStreamFormat}.
     */
    protected static class MockFormat extends BinaryStreamFormat<StringBuilder> {

        MockFormat() {
            return;
        }

        @Override
        public Class<StringBuilder> getSupportedType() {
            return StringBuilder.class;
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
}
