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
package com.asakusafw.directio.hive.parquet;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import parquet.column.ParquetProperties.WriterVersion;

import com.asakusafw.directio.hive.parquet.mock.MockSimpleWithLong;
import com.asakusafw.directio.hive.parquet.mock.WithDateTime;
import com.asakusafw.directio.hive.parquet.mock.WithFour;
import com.asakusafw.directio.hive.parquet.mock.WithStringSupports;
import com.asakusafw.directio.hive.serde.DataModelDescriptorEditor;
import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;
import com.asakusafw.directio.hive.serde.FieldPropertyDescriptor;
import com.asakusafw.directio.hive.serde.StringValueSerdeFactory;
import com.asakusafw.directio.hive.serde.ValueSerde;
import com.asakusafw.directio.hive.serde.mock.MockSimple;
import com.asakusafw.directio.hive.serde.mock.MockTypes;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link ParquetFileFormat}.
 */
public class ParquetFileFormatTest {

    /**
     * A temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private <T> ParquetFileFormat<T> format(Class<T> type, String... removes) {
        return format(type, Collections.<String, ValueSerde>emptyMap(), removes);
    }

    private <T> ParquetFileFormat<T> format(
            Class<T> type,
            Map<String, ? extends ValueSerde> edits,
            String... removes) {
        ParquetFileFormat<T> format = new ParquetFileFormat<T>(
                "testing",
                new ParquetFormatConfiguration(),
                new DataModelDescriptorEditor(FieldPropertyDescriptor.extract(type))
                    .editAll(edits)
                    .removeAll(Arrays.asList(removes))
                    .build());
        format.setConf(new org.apache.hadoop.conf.Configuration());
        return format;
    }

    /**
     * Test method for {@link AbstractParquetFileFormat#getFormatName()}.
     */
    @Test
    public void format_name() {
        assertThat(format(MockSimple.class).getFormatName(), equalTo("PARQUET"));
    }

    /**
     * Test method for {@link AbstractParquetFileFormat#getSupportedType()}.
     */
    @Test
    public void supported_type() {
        assertThat(format(MockSimple.class).getSupportedType(), equalTo((Object) MockSimple.class));
    }

    /**
     * {@code tblproperties} for default settings.
     */
    @Test
    public void table_properties_default() {
        Map<String, String> props = format(MockSimple.class).getTableProperties();
        assertThat(props.size(), is(0));
    }

    /**
     * simple I/O.
     * @throws Exception if failed
     */
    @Test
    public void io_simple() throws Exception {
        ParquetFileFormat<MockSimple> format = format(MockSimple.class);
        MockSimple in = new MockSimple(100, "Hello, world!");
        MockSimple out = restore(format, in);
        assertThat(out.number, is(new IntOption(100)));
        assertThat(out.string, is(new StringOption("Hello, world!")));
    }

    /**
     * I/O with all supported types.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_types() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<String, ValueSerde>();
        edits.put("decimalOption", StringValueSerdeFactory.DECIMAL);
        edits.put("dateOption", StringValueSerdeFactory.DATE);
        edits.put("dateTimeOption", StringValueSerdeFactory.DATETIME);

        ParquetFileFormat<MockTypes> format = format(MockTypes.class, edits);
        MockTypes in = new MockTypes();
        in.booleanOption.modify(true);
        in.byteOption.modify((byte) 1);
        in.shortOption.modify((short) 2);
        in.intOption.modify(3);
        in.longOption.modify(4L);
        in.floatOption.modify(5f);
        in.doubleOption.modify(6d);
        in.dateOption.modify(new Date(2014, 6, 1));
        in.dateTimeOption.modify(new DateTime(2014, 6, 1, 2, 3, 4));
        in.stringOption.modify("Hello, world!");
        in.decimalOption.modify(new BigDecimal("7.8"));

        MockTypes out = restore(format, in);
        assertThat(out.booleanOption, equalTo(in.booleanOption));
        assertThat(out.byteOption, equalTo(in.byteOption));
        assertThat(out.shortOption, equalTo(in.shortOption));
        assertThat(out.intOption, equalTo(in.intOption));
        assertThat(out.longOption, equalTo(in.longOption));
        assertThat(out.floatOption, equalTo(in.floatOption));
        assertThat(out.doubleOption, equalTo(in.doubleOption));
        assertThat(out.dateOption, equalTo(in.dateOption));
        assertThat(out.dateTimeOption, equalTo(in.dateTimeOption));
        assertThat(out.stringOption, equalTo(in.stringOption));
        assertThat(out.decimalOption, equalTo(in.decimalOption));
    }

    /**
     * I/O with all supported types.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_types_large() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<String, ValueSerde>();
        edits.put("decimalOption", StringValueSerdeFactory.DECIMAL);
        edits.put("dateOption", StringValueSerdeFactory.DATE);
        edits.put("dateTimeOption", StringValueSerdeFactory.DATETIME);

        int count = 1000;
        ParquetFileFormat<MockTypes> format = format(MockTypes.class, edits);
        List<MockTypes> inputs = new ArrayList<MockTypes>();
        for (int i = 0; i < count; i++) {
            MockTypes in = new MockTypes();
            in.booleanOption.modify(true);
            in.byteOption.modify((byte) 1);
            in.shortOption.modify((short) 2);
            in.intOption.modify(3);
            in.longOption.modify(4L);
            in.floatOption.modify(5f);
            in.doubleOption.modify(6d);
            in.dateOption.modify(new Date(2014, 6, 1));
            in.dateTimeOption.modify(new DateTime(2014, 6, 1, 2, 3, 4));
            in.stringOption.modify("Hello, world!");
            in.decimalOption.modify(new BigDecimal("7.8"));
            inputs.add(in);
        }

        List<MockTypes> outputs = restore(format, inputs);
        MockTypes sample = inputs.get(0);
        for (MockTypes out : outputs) {
            assertThat(out.booleanOption, equalTo(sample.booleanOption));
            assertThat(out.byteOption, equalTo(sample.byteOption));
            assertThat(out.shortOption, equalTo(sample.shortOption));
            assertThat(out.intOption, equalTo(sample.intOption));
            assertThat(out.longOption, equalTo(sample.longOption));
            assertThat(out.floatOption, equalTo(sample.floatOption));
            assertThat(out.doubleOption, equalTo(sample.doubleOption));
            assertThat(out.dateOption, equalTo(sample.dateOption));
            assertThat(out.dateTimeOption, equalTo(sample.dateTimeOption));
            assertThat(out.stringOption, equalTo(sample.stringOption));
            assertThat(out.decimalOption, equalTo(sample.decimalOption));
        }
    }

    /**
     * I/O with all supported types with {@code null}s.
     * @throws Exception if failed
     */
    @Test
    public void io_nulls() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<String, ValueSerde>();
        edits.put("decimalOption", StringValueSerdeFactory.DECIMAL);
        edits.put("dateOption", StringValueSerdeFactory.DATE);
        edits.put("dateTimeOption", StringValueSerdeFactory.DATETIME);

        ParquetFileFormat<MockTypes> format = format(MockTypes.class, edits);
        MockTypes in = new MockTypes();
        MockTypes out = restore(format, in);
        assertThat(out.booleanOption, equalTo(in.booleanOption));
        assertThat(out.byteOption, equalTo(in.byteOption));
        assertThat(out.shortOption, equalTo(in.shortOption));
        assertThat(out.intOption, equalTo(in.intOption));
        assertThat(out.longOption, equalTo(in.longOption));
        assertThat(out.floatOption, equalTo(in.floatOption));
        assertThat(out.doubleOption, equalTo(in.doubleOption));
        assertThat(out.dateOption, equalTo(in.dateOption));
        assertThat(out.dateTimeOption, equalTo(in.dateTimeOption));
        assertThat(out.stringOption, equalTo(in.stringOption));
        assertThat(out.decimalOption, equalTo(in.decimalOption));
    }

    /**
     * I/O with fragment.
     * @throws Exception if failed
     */
    @Test
    public void io_fragment() throws Exception {
        File file = folder.newFile();
        Assume.assumeThat(file.delete() || file.exists() == false, is(true));

        ParquetFileFormat<MockSimple> format = format(MockSimple.class);
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
     * I/O with {@code v2}.
     * @throws Exception if failed
     */
    @Test
    public void io_v_2() throws Exception {
        ParquetFileFormat<MockSimple> format = format(MockSimple.class);
        format.getFormatConfiguration().withWriterVersion(WriterVersion.PARQUET_2_0);
        MockSimple in = new MockSimple(100, "Hello, world!");
        MockSimple out = restore(format, in);
        assertThat(out.number, is(new IntOption(100)));
        assertThat(out.string, is(new StringOption("Hello, world!")));
    }

    /**
     * Field mapping by its name.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void mapping_by_position() throws Exception {
        ParquetFileFormat<WithFour> f1 = format(WithFour.class, "col1", "col3");
        ParquetFileFormat<WithFour> f2 = format(WithFour.class, "col2", "col3");
        f2.getFormatConfiguration().withFieldMappingStrategy(FieldMappingStrategy.POSITION);

        WithFour in = new WithFour();
        in.col0.modify(0);
        in.col1.modify(1);
        in.col2.modify(2);
        in.col3.modify(3);

        File file = save(f1, Arrays.asList(in));
        List<WithFour> results = load(f2, file);
        assertThat(results, hasSize(1));

        WithFour out = results.get(0);
        assertThat(out.col0, is(new IntOption(0)));
        assertThat(out.col1, is(new IntOption(2)));
        assertThat(out.col2, is(new IntOption()));
        assertThat(out.col3, is(new IntOption()));
    }

    /**
     * Field mapping by its name.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void mapping_by_name() throws Exception {
        ParquetFileFormat<WithFour> f1 = format(WithFour.class, "col1", "col3");
        ParquetFileFormat<WithFour> f2 = format(WithFour.class, "col2", "col3");
        f2.getFormatConfiguration().withFieldMappingStrategy(FieldMappingStrategy.NAME);

        WithFour in = new WithFour();
        in.col0.modify(0);
        in.col1.modify(1);
        in.col2.modify(2);
        in.col3.modify(3);

        File file = save(f1, Arrays.asList(in));
        List<WithFour> results = load(f2, file);
        assertThat(results, hasSize(1));

        WithFour out = results.get(0);
        assertThat(out.col0, is(new IntOption(0)));
        assertThat(out.col1, is(new IntOption()));
        assertThat(out.col2, is(new IntOption()));
        assertThat(out.col3, is(new IntOption()));
    }

    /**
     * fail on missing source.
     * @throws Exception if failed
     */
    @Test
    public void fail_on_missing_source() throws Exception {
        ParquetFileFormat<WithFour> f1 = format(WithFour.class, "col3");
        ParquetFileFormat<WithFour> f2 = format(WithFour.class);
        f2.getFormatConfiguration()
            .withFieldMappingStrategy(FieldMappingStrategy.NAME)
            .withOnMissingSource(ExceptionHandlingStrategy.FAIL);

        WithFour in = new WithFour();
        File file = save(f1, Arrays.asList(in));
        try {
            load(f2, file);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * fail on missing target.
     * @throws Exception if failed
     */
    @Test
    public void fail_on_missing_target() throws Exception {
        ParquetFileFormat<WithFour> f1 = format(WithFour.class);
        ParquetFileFormat<WithFour> f2 = format(WithFour.class, "col3");
        f2.getFormatConfiguration()
            .withFieldMappingStrategy(FieldMappingStrategy.NAME)
            .withOnMissingTarget(ExceptionHandlingStrategy.FAIL);

        WithFour in = new WithFour();
        File file = save(f1, Arrays.asList(in));
        try {
            load(f2, file);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * ignore on incompatible type.
     * @throws Exception if failed
     */
    @Test
    public void ignore_on_incompatible_type() throws Exception {
        ParquetFileFormat<MockSimple> f1 = format(MockSimple.class);
        ParquetFileFormat<MockSimpleWithLong> f2 = format(MockSimpleWithLong.class);
        f2.getFormatConfiguration()
            .withFieldMappingStrategy(FieldMappingStrategy.NAME)
            .withOnIncompatibleType(ExceptionHandlingStrategy.IGNORE);

        MockSimple in = new MockSimple(100, "Hello, world!");
        File file = save(f1, Arrays.asList(in));
        List<MockSimpleWithLong> results = load(f2, file);
        assertThat(results, hasSize(1));
        MockSimpleWithLong out = results.get(0);
        assertThat(out.number, is(new LongOption()));
        assertThat(out.string, is(in.string));
    }

    /**
     * fail on incompatible type.
     * @throws Exception if failed
     */
    @Test
    public void fail_on_incompatible_type() throws Exception {
        ParquetFileFormat<MockSimple> f1 = format(MockSimple.class);
        ParquetFileFormat<MockSimpleWithLong> f2 = format(MockSimpleWithLong.class);
        f2.getFormatConfiguration()
            .withFieldMappingStrategy(FieldMappingStrategy.NAME)
            .withOnIncompatibleType(ExceptionHandlingStrategy.FAIL);

        MockSimple in = new MockSimple(100, "Hello, world!");
        File file = save(f1, Arrays.asList(in));
        try {
            load(f2, file);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * loading timestamp type which generated by impala.
     * @throws Exception if failed
     */
    @Ignore
    @Test
    public void format_timestamp() throws Exception {
        ModelInput<WithDateTime> input = load(WithDateTime.class, "impala-timestamp.bin");
        try {
            WithDateTime buf = new WithDateTime();
            assertThat(input.readTo(buf), is(true));
            // TODO check

            assertThat(input.readTo(buf), is(false));
        } finally {
            input.close();
        }
    }

    /**
     * using strings.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_string() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<String, ValueSerde>();
        edits.put("decimal", StringValueSerdeFactory.DECIMAL);
        edits.put("date", StringValueSerdeFactory.DATE);
        edits.put("datetime", StringValueSerdeFactory.DATETIME);
        ParquetFileFormat<WithStringSupports> format = format(WithStringSupports.class, edits);

        WithStringSupports in = new WithStringSupports();
        in.decimal.modify(new BigDecimal("123.45"));
        in.date.modify(new Date(2014, 7, 1));
        in.datetime.modify(new DateTime(2014, 7, 1, 12, 34, 56));

        WithStringSupports out = restore(format, in);
        assertThat(out.decimal, is(in.decimal));
        assertThat(out.date, is(in.date));
        assertThat(out.datetime, is(in.datetime));
    }

    /**
     * using strings with dictionary.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_string_dict() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<String, ValueSerde>();
        edits.put("decimal", StringValueSerdeFactory.DECIMAL);
        edits.put("date", StringValueSerdeFactory.DATE);
        edits.put("datetime", StringValueSerdeFactory.DATETIME);
        ParquetFileFormat<WithStringSupports> format = format(WithStringSupports.class, edits);

        int count = 1000;
        List<WithStringSupports> inputs = new ArrayList<WithStringSupports>();
        for (int i = 0; i < count; i++) {
            WithStringSupports object = new WithStringSupports();
            object.decimal.modify(new BigDecimal("123.45"));
            object.date.modify(new Date(2014, 7, 1));
            object.datetime.modify(new DateTime(2014, 7, 1, 12, 34, 56));
            inputs.add(object);
        }
        WithStringSupports sample = inputs.get(0);
        List<WithStringSupports> outputs = restore(format, inputs);
        for (WithStringSupports out : outputs) {
            assertThat(out.decimal, is(sample.decimal));
            assertThat(out.date, is(sample.date));
            assertThat(out.datetime, is(sample.datetime));
        }
    }

    private <T> ModelInput<T> load(Class<T> modelType, String name) throws IOException, InterruptedException {
        File target = folder.newFile();
        InputStream in = getClass().getResourceAsStream(name);
        assertThat(in, is(notNullValue()));
        try {
            IOUtils.copyBytes(in, new FileOutputStream(target), 1024, true);
        } finally {
            in.close();
        }
        ParquetFileFormat<T> format = format(modelType);
        FileSystem fs = FileSystem.getLocal(format.getConf());
        return format.createInput(
                modelType,
                fs, new Path(target.toURI()),
                0, -1,
                new Counter());
    }

    private <T> T restore(ParquetFileFormat<T> format, T value) throws IOException, InterruptedException {
        List<T> in = new ArrayList<T>();
        in.add(value);
        return restore(format, in).get(0);
    }

    private <T> List<T> restore(ParquetFileFormat<T> format, List<T> values) throws IOException, InterruptedException {
        File file = save(format, values);
        List<T> results = load(format, file);
        assertThat(values, hasSize(results.size()));
        return results;
    }

    private <T> File save(ParquetFileFormat<T> format, List<T> values) throws IOException, InterruptedException {
        File file = folder.newFile();
        Assume.assumeThat(file.delete() || file.exists() == false, is(true));
        LocalFileSystem fs = FileSystem.getLocal(format.getConf());
        ModelOutput<T> output = format.createOutput(
                format.getSupportedType(),
                fs, new Path(file.toURI()),
                new Counter());
        try {
            for (T value : values) {
                output.write(value);
            }
        } finally {
            output.close();
        }
        assertThat(file.exists(), is(true));
        return file;
    }

    private <T> List<T> load(ParquetFileFormat<T> format, File file) throws IOException, InterruptedException {
        LocalFileSystem fs = FileSystem.getLocal(format.getConf());
        ModelInput<T> input = format.createInput(
                format.getSupportedType(),
                fs, new Path(file.toURI()),
                0, file.length(),
                new Counter());
        try {
            List<T> results = new ArrayList<T>();
            while (true) {
                @SuppressWarnings("unchecked")
                T value = (T) format.getDataModelDescriptor().createDataModelObject();
                if (input.readTo(value) == false) {
                    break;
                }
                results.add(value);
            }
            return results;
        } finally {
            input.close();
        }
    }
}
