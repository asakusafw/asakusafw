/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.io.IOUtils;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.directio.hive.parquet.mock.MockSimpleWithLong;
import com.asakusafw.directio.hive.parquet.mock.WithDate;
import com.asakusafw.directio.hive.parquet.mock.WithDateTime;
import com.asakusafw.directio.hive.parquet.mock.WithDecimal;
import com.asakusafw.directio.hive.parquet.mock.WithFour;
import com.asakusafw.directio.hive.parquet.mock.WithString;
import com.asakusafw.directio.hive.parquet.mock.WithStringSupports;
import com.asakusafw.directio.hive.parquet.mock.WithTimestampSupports;
import com.asakusafw.directio.hive.serde.DataModelDescriptorEditor;
import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;
import com.asakusafw.directio.hive.serde.FieldPropertyDescriptor;
import com.asakusafw.directio.hive.serde.StringValueSerdeFactory;
import com.asakusafw.directio.hive.serde.TimestampValueSerdeFactory;
import com.asakusafw.directio.hive.serde.ValueSerde;
import com.asakusafw.directio.hive.serde.ValueSerdeFactory;
import com.asakusafw.directio.hive.serde.mock.MockSimple;
import com.asakusafw.directio.hive.serde.mock.MockTypes;
import com.asakusafw.info.hive.BuiltinStorageFormatInfo;
import com.asakusafw.info.hive.StorageFormatInfo;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.windows.WindowsSupport;

import parquet.column.ParquetProperties.WriterVersion;

/**
 * Test for {@link ParquetFileFormat}.
 */
public class ParquetFileFormatTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    private static final long LOCAL_TIMEZONE_OFFSET =
            TimeUnit.MILLISECONDS.toSeconds(TimeZone.getDefault().getRawOffset());

    // test data may be created different timestamp
    private static final long TESTDATA_TIMEZONE_OFFSET =
            TimeUnit.MILLISECONDS.toSeconds(TimeZone.getTimeZone("JST").getRawOffset());


    /**
     * A temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private <T> ParquetFileFormat<T> format(Class<T> type, String... removes) {
        return format(type, Collections.emptyMap(), removes);
    }

    private <T> ParquetFileFormat<T> format(
            Class<T> type,
            Map<String, ? extends ValueSerde> edits,
            String... removes) {
        ParquetFileFormat<T> format = new ParquetFileFormat<>(
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
     * Test method for {@link AbstractParquetFileFormat#getSchema()}.
     */
    @Test
    public void format_name() {
        assertThat(
                format(MockSimple.class).getSchema().getStorageFormat(),
                equalTo((Object) BuiltinStorageFormatInfo.of(StorageFormatInfo.FormatKind.PARQUET)));
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
        Map<String, String> props = format(MockSimple.class).getSchema().getProperties();
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
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("decimalOption", ValueSerdeFactory.getDecimal(10, 2));

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
        in.decimalOption.modify(new BigDecimal("7.89"));

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
     * I/O with decimals.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_decimals() throws Exception {
        for (int p = 2; p <= HiveDecimal.MAX_PRECISION; p++) {
            Map<String, ValueSerde> edits = new HashMap<>();
            edits.put("decimalOption", ValueSerdeFactory.getDecimal(p, 2));

            ParquetFileFormat<MockTypes> format = format(MockTypes.class, edits);
            MockTypes in = new MockTypes();
            if (p < 3) {
                in.decimalOption.modify(new BigDecimal("0.14"));
            } else {
                in.decimalOption.modify(new BigDecimal("3.14"));
            }
            MockTypes out = restore(format, in);
            assertThat(out.decimalOption, equalTo(in.decimalOption));
        }
    }

    /**
     * I/O with decimals.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_decimals_int32() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("decimalOption", ValueSerdeFactory.getDecimal(9, 2));

        int count = 100;
        ParquetFileFormat<MockTypes> format = format(MockTypes.class, edits);
        List<MockTypes> inputs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MockTypes in = new MockTypes();
            in.decimalOption.modify(new BigDecimal("7.89"));
            inputs.add(in);
        }

        List<MockTypes> outputs = restore(format, inputs);
        MockTypes sample = inputs.get(0);
        for (MockTypes out : outputs) {
            assertThat(out.decimalOption, equalTo(sample.decimalOption));
        }
    }

    /**
     * I/O with decimals.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_decimals_int64() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("decimalOption", ValueSerdeFactory.getDecimal(18, 2));

        int count = 100;
        ParquetFileFormat<MockTypes> format = format(MockTypes.class, edits);
        List<MockTypes> inputs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MockTypes in = new MockTypes();
            in.decimalOption.modify(new BigDecimal("7.89"));
            inputs.add(in);
        }

        List<MockTypes> outputs = restore(format, inputs);
        MockTypes sample = inputs.get(0);
        for (MockTypes out : outputs) {
            assertThat(out.decimalOption, equalTo(sample.decimalOption));
        }
    }

    /**
     * I/O with decimals.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_decimals_binary() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("decimalOption", ValueSerdeFactory.getDecimal(38, 2));

        int count = 100;
        ParquetFileFormat<MockTypes> format = format(MockTypes.class, edits);
        List<MockTypes> inputs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MockTypes in = new MockTypes();
            in.decimalOption.modify(new BigDecimal("-7.89"));
            inputs.add(in);
        }

        List<MockTypes> outputs = restore(format, inputs);
        MockTypes sample = inputs.get(0);
        for (MockTypes out : outputs) {
            assertThat(out.decimalOption, equalTo(sample.decimalOption));
        }
    }

    /**
     * I/O with all supported types.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_types_large() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("decimalOption", ValueSerdeFactory.getDecimal(10, 2));

        int count = 1000;
        ParquetFileFormat<MockTypes> format = format(MockTypes.class, edits);
        List<MockTypes> inputs = new ArrayList<>();
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
            in.decimalOption.modify(new BigDecimal("7.89"));
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
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("decimalOption", ValueSerdeFactory.getDecimal(10, 2));

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
        try (ModelOutput<MockSimple> output = format.createOutput(
                MockSimple.class,
                fs, new Path(file.toURI()),
                new Counter())) {
            output.write(new MockSimple(100, "Hello, world!"));
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

        try (ModelInput<MockSimple> input = format.createInput(
                MockSimple.class,
                fs, new Path(first.getPath()),
                first.getOffset(), first.getSize(),
                new Counter())) {
            MockSimple buf = new MockSimple();
            assertThat(input.readTo(buf), is(true));
            assertThat(buf.number, is(new IntOption(100)));
            assertThat(buf.string, is(new StringOption("Hello, world!")));

            assertThat(input.readTo(buf), is(false));
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
     * using strings.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_string() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<>();
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
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("decimal", StringValueSerdeFactory.DECIMAL);
        edits.put("date", StringValueSerdeFactory.DATE);
        edits.put("datetime", StringValueSerdeFactory.DATETIME);
        ParquetFileFormat<WithStringSupports> format = format(WithStringSupports.class, edits);

        int count = 1000;
        List<WithStringSupports> inputs = new ArrayList<>();
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

    /**
     * using timestamps.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_timestamp() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("date", TimestampValueSerdeFactory.DATE);
        edits.put("datetime", ValueSerdeFactory.DATETIME);
        ParquetFileFormat<WithTimestampSupports> format = format(WithTimestampSupports.class, edits);

        WithTimestampSupports in = new WithTimestampSupports();
        in.date.modify(new Date(2015, 7, 1));
        in.datetime.modify(new DateTime(2015, 7, 1, 12, 34, 56));

        WithTimestampSupports out = restore(format, in);
        assertThat(out.date, is(in.date));
        assertThat(out.datetime, is(in.datetime));
    }

    /**
     * using char.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_char() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("value", ValueSerdeFactory.getChar(10));
        ParquetFileFormat<WithString> format = format(WithString.class, edits);

        WithString in = new WithString();
        in.value.modify("Hello, world!");

        WithString out = restore(format, in);
        assertThat(out.value, is(new StringOption("Hello, world!".substring(0, 10))));
    }

    /**
     * using varchar.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_varchar() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("value", ValueSerdeFactory.getVarchar(10));
        ParquetFileFormat<WithString> format = format(WithString.class, edits);

        WithString in = new WithString();
        in.value.modify("Hello, world!");

        WithString out = restore(format, in);
        assertThat(out.value, is(new StringOption("Hello, world!".substring(0, 10))));
    }

    /**
     * using varchar.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void io_varchar_dict() throws Exception {
        Map<String, ValueSerde> edits = new HashMap<>();
        edits.put("value", ValueSerdeFactory.getVarchar(10));
        ParquetFileFormat<WithString> format = format(WithString.class, edits);

        int count = 1000;
        List<WithString> inputs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            WithString in = new WithString();
            in.value.modify("Hello");
            inputs.add(in);
        }
        List<WithString> outputs = restore(format, inputs);
        for (WithString out : outputs) {
            assertThat(out.value, is(new StringOption("Hello")));
            count--;
        }
        assertThat(count, is(0));
    }

    /**
     * loading char type which generated by hive.
     * @throws Exception if failed
     */
    @Test
    public void format_char() throws Exception {
        checkString("char-10-hello-parquet.parquet", ValueSerdeFactory.getChar(10),
                "Hello, Parquet!".substring(0, 10));
    }

    /**
     * loading varchar type which generated by hive.
     * @throws Exception if failed
     */
    @Test
    public void format_varchar() throws Exception {
        checkString("varchar-10-hello-parquet.parquet", ValueSerdeFactory.getVarchar(10),
                "Hello, Parquet!".substring(0, 10));
    }

    private void checkString(String file, ValueSerde serde, String expected) throws IOException, InterruptedException {
        WithString buf = new WithString();
        ParquetFileFormat<WithString> format = format(
                WithString.class,
                Collections.singletonMap("value", serde));
        try (ModelInput<WithString> input = load(format, file)) {
            assertThat(input.readTo(buf), is(true));
            assertThat(input.readTo(new WithString()), is(false));
        }
        assertThat(buf.value, is(new StringOption(expected)));
    }

    /**
     * loading decimal type which generated by hive.
     * @throws Exception if failed
     */
    @Test
    public void format_decimal() throws Exception {
        checkDecimal("decimal-9_2-3_14.parquet");
        checkDecimal("decimal-18_2-3_14.parquet");
        checkDecimal("decimal-38_2-3_14.parquet");
    }

    private void checkDecimal(String file) throws IOException, InterruptedException {
        Pattern p = Pattern.compile("decimal-(\\d+)_(\\d+)-(.+)\\.parquet");
        Matcher matcher = p.matcher(file);
        assertThat(matcher.matches(), is(true));

        int precision = Integer.parseInt(matcher.group(1));
        int scale = Integer.parseInt(matcher.group(2));
        WithDecimal buf = new WithDecimal();
        ParquetFileFormat<WithDecimal> format = format(
                WithDecimal.class,
                Collections.singletonMap("value", ValueSerdeFactory.getDecimal(precision, scale)));
        try (ModelInput<WithDecimal> input = load(format, file)) {
            assertThat(input.readTo(buf), is(true));
            assertThat(input.readTo(new WithDecimal()), is(false));
        }
        BigDecimal expected = new BigDecimal(matcher.group(3).replace('_', '.'));
        assertThat(buf.value, is(new DecimalOption(expected)));
    }

    /**
     * loading timestamp type which generated be hive.
     * @throws Exception if failed
     */
    @Test
    public void format_timestamp() throws Exception {
        checkDateTime("timestamp-1970-01-01-00-00-00.parquet");
        checkDateTime("timestamp-1970-01-01-12-34-56.parquet");
        checkDateTime("timestamp-2014-12-01-23-59-59.parquet");
    }

    @SuppressWarnings("deprecation")
    private void checkDateTime(String file) throws IOException, InterruptedException {
        Pattern p = Pattern.compile("timestamp-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)\\.parquet");
        Matcher matcher = p.matcher(file);
        assertThat(matcher.matches(), is(true));
        WithDateTime buf = new WithDateTime();
        try (ModelInput<WithDateTime> input = load(WithDateTime.class, file)) {
            assertThat(input.readTo(buf), is(true));
            assertThat(input.readTo(new WithDateTime()), is(false));
        }
        // fix timezone
        buf.value.modify(buf.value.get().getElapsedSeconds() + TESTDATA_TIMEZONE_OFFSET - LOCAL_TIMEZONE_OFFSET);
        DateTime expected = new DateTime(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(4)),
                Integer.parseInt(matcher.group(5)),
                Integer.parseInt(matcher.group(6)));
        assertThat(buf.value, is(new DateTimeOption(expected)));
    }

    /**
     * loading date type which generated be hive.
     * @throws Exception if failed
     */
    @Test
    public void format_date() throws Exception {
        checkDate("date-1970-01-01.parquet");
        checkDate("date-2015-12-31.parquet");
        checkDate("date-1995-05-23.parquet");
    }

    private void checkDate(String file) throws IOException, InterruptedException {
        Pattern p = Pattern.compile("date-(\\d+)-(\\d+)-(\\d+)\\.parquet");
        Matcher matcher = p.matcher(file);
        assertThat(matcher.matches(), is(true));
        WithDate buf = new WithDate();
        try (ModelInput<WithDate> input = load(WithDate.class, file)) {
            assertThat(input.readTo(buf), is(true));
            assertThat(input.readTo(new WithDate()), is(false));
        }
        Date expected = new Date(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
        assertThat(buf.value, is(new DateOption(expected)));
    }

    private <T> ModelInput<T> load(Class<T> modelType, String name) throws IOException, InterruptedException {
        ParquetFileFormat<T> format = format(modelType);
        return load(format, name);
    }

    private <T> ModelInput<T> load(ParquetFileFormat<T> format, String name) throws IOException, InterruptedException {
        File target = folder.newFile();
        try (InputStream in = getClass().getResourceAsStream(name)) {
            assertThat(in, is(notNullValue()));
            IOUtils.copyBytes(in, new FileOutputStream(target), 1024, true);
        }
        FileSystem fs = FileSystem.getLocal(format.getConf());
        return format.createInput(
                format.getSupportedType(),
                fs, new Path(target.toURI()),
                0, -1,
                new Counter());
    }

    private <T> T restore(ParquetFileFormat<T> format, T value) throws IOException, InterruptedException {
        List<T> in = new ArrayList<>();
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
        try (ModelOutput<T> output = format.createOutput(
                format.getSupportedType(),
                fs, new Path(file.toURI()),
                new Counter())) {
            for (T value : values) {
                output.write(value);
            }
        }
        assertThat(file.exists(), is(true));
        return file;
    }

    private <T> List<T> load(ParquetFileFormat<T> format, File file) throws IOException, InterruptedException {
        LocalFileSystem fs = FileSystem.getLocal(format.getConf());
        try (ModelInput<T> input = format.createInput(
                format.getSupportedType(),
                fs, new Path(file.toURI()),
                0, file.length(),
                new Counter())) {
            List<T> results = new ArrayList<>();
            while (true) {
                @SuppressWarnings("unchecked")
                T value = (T) format.getDataModelDescriptor().createDataModelObject();
                if (input.readTo(value) == false) {
                    break;
                }
                results.add(value);
            }
            return results;
        }
    }
}
