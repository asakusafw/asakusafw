/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import static com.asakusafw.runtime.directio.hadoop.HadoopDataSourceProfile.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FilterFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RawLocalFileSystem;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.DirectDataSourceProfile;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Test for {@link HadoopDataSourceProfile}.
 */
public class HadoopDataSourceProfileTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * simple conversion.
     * @throws Exception if failed
     */
    @Test
    public void convert() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(KEY_PATH, folder.getRoot().getCanonicalFile().toURI().toString());
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        Configuration conf = new Configuration();
        HadoopDataSourceProfile result = HadoopDataSourceProfile.convert(profile, conf);

        assertThat(result.getId(), is("testing"));
        assertThat(result.getContextPath(), is("context"));
        assertThat(result.getFileSystem().getUri().getScheme(), is("file"));
        assertThat(new File(result.getFileSystemPath().toUri()), is(folder.getRoot()));
        assertThat(new File(result.getTemporaryFileSystemPath().getParent().toUri()), is(folder.getRoot()));

        assertThat(result.isOutputStaging(), is(true));
        assertThat(result.isOutputStreaming(), is(true));
        assertThat(result.isCombineBlocks(), is(true));
        assertThat(result.isSplitBlocks(), is(true));
    }

    /**
     * convert without no path.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void convert_nopath() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        Configuration conf = new Configuration();
        HadoopDataSourceProfile.convert(profile, conf);
    }

    /**
     * convert with relative path.
     * @throws Exception if failed
     */
    @Test
    public void convert_relpath() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(KEY_PATH, "relative");
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        Configuration conf = new Configuration();
        HadoopDataSourceProfile result = HadoopDataSourceProfile.convert(profile, conf);

        FileSystem defaultFs = FileSystem.get(conf);
        Path path = new Path(defaultFs.getWorkingDirectory(), "relative").makeQualified(defaultFs);
        assertThat(result.getFileSystem().getCanonicalServiceName(), is(defaultFs.getCanonicalServiceName()));
        assertThat(result.getFileSystemPath(), is(path));
    }

    /**
     * simple conversion.
     * @throws Exception if failed
     */
    @Test
    public void convert_all() throws Exception {
        File prod = folder.newFolder("path");
        File temp = folder.newFolder("temp");
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(KEY_PATH, prod.getCanonicalFile().toURI().toString());
        attributes.put(KEY_TEMP, temp.getCanonicalFile().toURI().toString());
        attributes.put(KEY_MIN_FRAGMENT, "123");
        attributes.put(KEY_PREF_FRAGMENT, "1234");
        attributes.put(KEY_OUTPUT_STAGING, "false");
        attributes.put(KEY_OUTPUT_STREAMING, "false");
        attributes.put(KEY_SPLIT_BLOCKS, "false");
        attributes.put(KEY_COMBINE_BLOCKS, "false");
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        Configuration conf = new Configuration();
        HadoopDataSourceProfile result = HadoopDataSourceProfile.convert(profile, conf);

        assertThat(result.getId(), is("testing"));
        assertThat(result.getContextPath(), is("context"));
        assertThat(result.getFileSystem().getUri().getScheme(), is("file"));
        assertThat(new File(result.getFileSystemPath().toUri()), is(prod));
        assertThat(new File(result.getTemporaryFileSystemPath().toUri()), is(temp));

        assertThat(result.getMinimumFragmentSize(new MockFormat(9999, -1)), is(123L));
        assertThat(result.getMinimumFragmentSize(new MockFormat(100, -1)), is(100L));
        assertThat(result.getMinimumFragmentSize(new MockFormat(-1, -1)), is(lessThan(0L)));

        assertThat(result.getPreferredFragmentSize(new MockFormat(9999, -1)), is(1234L));
        assertThat(result.getPreferredFragmentSize(new MockFormat(9999, 234)), is(234L));
        assertThat(result.getPreferredFragmentSize(new MockFormat(-1, -1)), is(lessThan(0L)));

        assertThat(result.isOutputStaging(), is(false));
        assertThat(result.isOutputStreaming(), is(false));
        assertThat(result.isCombineBlocks(), is(false));
        assertThat(result.isSplitBlocks(), is(false));
    }

    /**
     * fs is inconsistent between prod and temp.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void convert_inconsistent_fs() throws Exception {
        Configuration conf = new Configuration();
        conf.setClass("fs.mock.impl", MockFs.class, FileSystem.class);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(KEY_PATH, folder.getRoot().toURI().toString());
        attributes.put(KEY_TEMP, "mock://" + folder.getRoot().toURI().toString());
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        HadoopDataSourceProfile.convert(profile, conf);
    }

    /**
     * the minimum fragment size is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void convert_minSize_notInt() throws Exception {
        Configuration conf = new Configuration();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(KEY_MIN_FRAGMENT, "INVALID");
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        HadoopDataSourceProfile.convert(profile, conf);
    }

    /**
     * the minimum fragment size is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void convert_minSize_zero() throws Exception {
        Configuration conf = new Configuration();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(KEY_MIN_FRAGMENT, "0");
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        HadoopDataSourceProfile.convert(profile, conf);
    }

    /**
     * the preferred fragment size is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void convert_prefSize_notInt() throws Exception {
        Configuration conf = new Configuration();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(KEY_PREF_FRAGMENT, "INVALID");
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        HadoopDataSourceProfile.convert(profile, conf);
    }

    /**
     * the preferred fragment size is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void convert_prefSize_zero() throws Exception {
        Configuration conf = new Configuration();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(KEY_PREF_FRAGMENT, "0");
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        HadoopDataSourceProfile.convert(profile, conf);
    }

    /**
     * the minimum fragment size is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void convert_unknown_properties() throws Exception {
        Configuration conf = new Configuration();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(KEY_PATH, folder.getRoot().getCanonicalFile().toURI().toString());
        attributes.put("__INVALID__", "value");
        DirectDataSourceProfile profile = new DirectDataSourceProfile(
                "testing",
                HadoopDataSource.class,
                "context",
                attributes);
        HadoopDataSourceProfile.convert(profile, conf);
    }

    /**
     * A mock filesystem.
     */
    public static class MockFs extends FilterFileSystem {

        /**
         * Creates a new instance.
         */
        public MockFs() {
            super(new RawLocalFileSystem());
        }

        @Override
        public URI getUri() {
            return URI.create("mock://localhost");
        }
    }

    private static class MockFormat extends BinaryStreamFormat<Object> {

        private final long min;

        private final long pref;

        MockFormat(long min, long pref) {
            this.min = min;
            this.pref = pref;
        }

        @Override
        public Class<Object> getSupportedType() {
            return Object.class;
        }

        @Override
        public long getPreferredFragmentSize() throws IOException, InterruptedException {
            return pref;
        }

        @Override
        public long getMinimumFragmentSize() throws IOException, InterruptedException {
            return min;
        }

        @Override
        public ModelInput<Object> createInput(Class<? extends Object> dataType, String path,
                InputStream stream, long offset, long fragmentSize) throws IOException,
                InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ModelOutput<Object> createOutput(Class<? extends Object> dataType, String path,
                OutputStream stream) throws IOException, InterruptedException {
            throw new UnsupportedOperationException();
        }
    }
}
