/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.orc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.io.orc.CompressionKind;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.directio.hive.serde.FieldPropertyDescriptor;
import com.asakusafw.directio.hive.serde.mock.MockSimple;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link OrcFileFormat}.
 */
public class OrcFileFormatTest {

    /**
     * A temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private OrcFileFormat<MockSimple> format() {
        OrcFileFormat<MockSimple> format = new OrcFileFormat<MockSimple>(
                "testing",
                new OrcFormatConfiguration(),
                FieldPropertyDescriptor.extract(MockSimple.class));
        format.setConf(new org.apache.hadoop.conf.Configuration());
        return format;
    }

    /**
     * Test method for {@link AbstractOrcFileFormat#getFormatName()}.
     */
    @Test
    public void format_name() {
        assertThat(format().getFormatName(), equalTo("ORC"));
    }

    /**
     * Test method for {@link AbstractOrcFileFormat#getSupportedType()}.
     */
    @Test
    public void supported_type() {
        assertThat(format().getSupportedType(), equalTo((Object) MockSimple.class));
    }

    /**
     * {@code tblproperties} for default settings.
     */
    @Test
    public void table_properties_default() {
        Map<String, String> props = format().getTableProperties();
        assertThat(props.size(), is(1));
        assertThat(props, hasEntry("orc.compress", "SNAPPY"));
    }

    /**
     * {@code tblproperties} for custom settings.
     */
    @Test
    public void table_properties_custom() {
        long stripeSize = 99L * 1024 * 1024;
        OrcFileFormat<MockSimple> format = format();
        format.getFormatConfiguration()
            .withFormatVersion(OrcFile.Version.V_0_11)
            .withCompressionKind(CompressionKind.ZLIB)
            .withStripeSize(stripeSize);
        Map<String, String> props = format.getTableProperties();
        assertThat(props.size(), is(2));
        assertThat(props, hasEntry("orc.compress", "ZLIB"));
        assertThat(props, hasEntry("orc.stripe.size", String.valueOf(stripeSize)));
    }

    /**
     * simple I/O.
     * @throws Exception if failed
     */
    @Test
    public void io_simple() throws Exception {
        OrcFileFormat<MockSimple> format = format();
        doSimple(format);
    }

    /**
     * I/O with fragment.
     * @throws Exception if failed
     */
    @Test
    public void io_fragment() throws Exception {
        File file = folder.newFile();
        Assume.assumeThat(file.delete() || file.exists() == false, is(true));

        OrcFileFormat<MockSimple> format = format();
        LocalFileSystem fs = FileSystem.getLocal(format.getConf());
        ModelOutput<MockSimple> output = format.createOutput(
                MockSimple.class,
                fs, new Path(file.toURI()),
                new Counter());
        try {
            output.write(new MockSimple(100, "Hello, world!"));
        } finally {
            output.close();
        }
        assertThat(file.exists(), is(true));

        FileStatus stat = fs.getFileStatus(new Path(file.toURI()));
        List<DirectInputFragment> fragments = format.computeInputFragments(new StripedDataFormat.InputContext(
                MockSimple.class,
                Arrays.asList(stat), fs,
                -1L, -1L,
                false, false));

        assertThat(fragments, hasSize(1));
        DirectInputFragment first = fragments.get(0);

        ModelInput<MockSimple> input = format.createInput(
                MockSimple.class,
                fs, new Path(first.getPath()),
                first.getOffset(), first.getSize(),
                new Counter());
        try {
            MockSimple buf = new MockSimple();
            assertThat(input.readTo(buf), is(true));
            assertThat(buf.number, is(new IntOption(100)));
            assertThat(buf.string, is(new StringOption("Hello, world!")));

            assertThat(input.readTo(buf), is(false));
        } finally {
            input.close();
        }
    }


    /**
     * I/O with {@code 0.11}.
     * @throws Exception if failed
     */
    @Test
    public void io_v_0_11() throws Exception {
        OrcFileFormat<MockSimple> format = format();
        format.getFormatConfiguration().withFormatVersion(OrcFile.Version.V_0_11);
        doSimple(format);
    }

    private void doSimple(OrcFileFormat<MockSimple> format) throws IOException, InterruptedException {
        File file = folder.newFile();
        Assume.assumeThat(file.delete() || file.exists() == false, is(true));
        LocalFileSystem fs = FileSystem.getLocal(format.getConf());
        ModelOutput<MockSimple> output = format.createOutput(
                MockSimple.class,
                fs, new Path(file.toURI()),
                new Counter());
        try {
            output.write(new MockSimple(100, "Hello, world!"));
        } finally {
            output.close();
        }
        assertThat(file.exists(), is(true));

        ModelInput<MockSimple> input = format.createInput(
                MockSimple.class,
                fs, new Path(file.toURI()),
                0, file.length(),
                new Counter());
        try {
            MockSimple buf = new MockSimple();
            assertThat(input.readTo(buf), is(true));
            assertThat(buf.number, is(new IntOption(100)));
            assertThat(buf.string, is(new StringOption("Hello, world!")));

            assertThat(input.readTo(buf), is(false));
        } finally {
            input.close();
        }
    }
}
