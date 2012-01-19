/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.dmdl.windgate.csv.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.dmdl.windgate.common.driver.GeneratorTesterRoot;
import com.asakusafw.runtime.io.csv.CsvConfiguration;
import com.asakusafw.runtime.io.csv.CsvParser;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelReader;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelWriter;

/**
 * Test for {@link CsvSupportEmitter}.
 */
public class CsvSupportEmitterTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new CsvSupportEmitter());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * A simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        ModelLoader loaded = generateJava("simple");
        ModelWrapper model = loaded.newModel("Simple");
        DataModelStreamSupport<?> support = (DataModelStreamSupport<?>) loaded.newObject("csv", "SimpleCsvSupport");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        DataModelStreamSupport<Object> unsafe = unsafe(support);

        model.set("value", new Text("Hello, world!"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = unsafe.createWriter("hello", output);
        writer.write(model.unwrap());
        writer.flush();
        output.close();

        Object buffer = loaded.newModel("Simple").unwrap();
        DataModelReader<Object> reader = unsafe.createReader("hello", new ByteArrayInputStream(output.toByteArray()));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(buffer));
        assertThat(reader.readTo(buffer), is(false));
    }

    /**
     * All types.
     * @throws Exception if failed
     */
    @Test
    public void types() throws Exception {
        ModelLoader loaded = generateJava("types");
        ModelWrapper model = loaded.newModel("Types");
        DataModelStreamSupport<?> support = (DataModelStreamSupport<?>) loaded.newObject("csv", "TypesCsvSupport");
        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        ModelWrapper empty = loaded.newModel("Types");

        ModelWrapper all = loaded.newModel("Types");
        all.set("c_int", 100);
        all.set("c_text", new Text("Hello, DMDL world!"));
        all.set("c_boolean", true);
        all.set("c_byte", (byte) 64);
        all.set("c_short", (short) 1023);
        all.set("c_long", 100000L);
        all.set("c_float", 1.5f);
        all.set("c_double", 2.5f);
        all.set("c_decimal", new BigDecimal("3.1415"));
        all.set("c_date", new Date(2011, 9, 1));
        all.set("c_datetime", new DateTime(2011, 12, 31, 23, 59, 59));

        DataModelStreamSupport<Object> unsafe = unsafe(support);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = unsafe.createWriter("hello", output);
        writer.write(empty.unwrap());
        writer.write(all.unwrap());
        writer.flush();
        output.close();

        Object buffer = loaded.newModel("Types").unwrap();
        DataModelReader<Object> reader = unsafe.createReader("hello", new ByteArrayInputStream(output.toByteArray()));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(empty.unwrap()));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(all.unwrap()));
        assertThat(reader.readTo(buffer), is(false));
    }

    /**
     * with attributes.
     * @throws Exception if failed
     */
    @Test
    public void attributes() throws Exception {
        ModelLoader loaded = generateJava("attributes");
        ModelWrapper model = loaded.newModel("Model");
        model.set("text_value", new Text("\u3042\u3044\u3046\u3048\u304a"));
        model.set("true_value", true);
        model.set("false_value", false);
        model.set("date_value", new Date(2011, 10, 11));
        model.set("date_time_value", new DateTime(2011, 1, 2, 13, 14, 15));
        DataModelStreamSupport<Object> support = unsafe(loaded.newObject("csv", "ModelCsvSupport"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = support.createWriter("hello", output);
        writer.write(model.unwrap());
        writer.flush();
        output.close();

        String[][] results = parse(5, new String(output.toByteArray(), "ISO-2022-jp"));
        assertThat(results, is(new String[][] {
                {"text_value", "true_value", "false_value", "date_value", "date_time_value"},
                {"\u3042\u3044\u3046\u3048\u304a", "T", "F", "2011/10/11", "2011/01/02+13:14:15"},
        }));
    }

    /**
     * with header.
     * @throws Exception if failed
     */
    @Test
    public void header() throws Exception {
        ModelLoader loaded = generateJava("field_name");
        ModelWrapper model = loaded.newModel("Model");
        DataModelStreamSupport<Object> support = unsafe(loaded.newObject("csv", "ModelCsvSupport"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = support.createWriter("hello", output);
        model.set("value", new Text("Hello, world!"));
        writer.write(model.unwrap());
        writer.flush();
        output.close();

        String[][] results = parse(1, new String(output.toByteArray(), "UTF-8"));
        assertThat(results, is(new String[][] {
                {"title"},
                {"Hello, world!"},
        }));
    }

    /**
     * with implicit field.
     * @throws Exception if failed
     */
    @Test
    public void implicit_field_name() throws Exception {
        ModelLoader loaded = generateJava("implicit_field_name");
        ModelWrapper model = loaded.newModel("Model");
        DataModelStreamSupport<Object> support = unsafe(loaded.newObject("csv", "ModelCsvSupport"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = support.createWriter("hello", output);
        model.set("value", new Text("Hello, world!"));
        writer.write(model.unwrap());
        writer.flush();
        output.close();

        String[][] results = parse(1, new String(output.toByteArray(), "UTF-8"));
        assertThat(results, is(new String[][] {
                {"value"},
                {"Hello, world!"},
        }));
    }

    /**
     * with file name.
     * @throws Exception if failed
     */
    @Test
    public void file_name() throws Exception {
        ModelLoader loaded = generateJava("file_name");
        ModelWrapper model = loaded.newModel("Model");
        ModelWrapper buffer = loaded.newModel("Model");
        DataModelStreamSupport<Object> support = unsafe(loaded.newObject("csv", "ModelCsvSupport"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = support.createWriter("hello", output);
        model.set("value", new Text("Hello, world!"));
        writer.write(model.unwrap());
        writer.flush();
        output.close();

        DataModelReader<Object> reader = support.createReader("testing", new ByteArrayInputStream(output.toByteArray()));
        assertThat(reader.readTo(buffer.unwrap()), is(true));
        assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello, world!")));
        assertThat(buffer.getOption("path"), is((Object) new StringOption("testing")));
        assertThat(reader.readTo(buffer.unwrap()), is(false));
    }

    /**
     * with line number.
     * @throws Exception if failed
     */
    @Test
    public void line_number() throws Exception {
        ModelLoader loaded = generateJava("line_number");
        ModelWrapper model = loaded.newModel("Model");
        model.set("value", new Text("Hello\nworld!"));
        ModelWrapper buffer = loaded.newModel("Model");
        DataModelStreamSupport<Object> support = unsafe(loaded.newObject("csv", "ModelCsvSupport"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = support.createWriter("hello", output);
        writer.write(model.unwrap());
        writer.write(model.unwrap());
        writer.flush();
        output.close();

        DataModelReader<Object> reader = support.createReader("testing", new ByteArrayInputStream(output.toByteArray()));
        assertThat(reader.readTo(buffer.unwrap()), is(true));
        assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello\nworld!")));
        assertThat(buffer.getOption("number"), is((Object) new IntOption(1)));
        assertThat(reader.readTo(buffer.unwrap()), is(true));
        assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello\nworld!")));
        assertThat(buffer.getOption("number"), is((Object) new IntOption(3)));
        assertThat(reader.readTo(buffer.unwrap()), is(false));
    }

    /**
     * with record number.
     * @throws Exception if failed
     */
    @Test
    public void record_number() throws Exception {
        ModelLoader loaded = generateJava("record_number");
        ModelWrapper model = loaded.newModel("Model");
        model.set("value", new Text("Hello\nworld!"));
        ModelWrapper buffer = loaded.newModel("Model");
        DataModelStreamSupport<Object> support = unsafe(loaded.newObject("csv", "ModelCsvSupport"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = support.createWriter("hello", output);
        writer.write(model.unwrap());
        writer.write(model.unwrap());
        writer.flush();
        output.close();

        DataModelReader<Object> reader = support.createReader("testing", new ByteArrayInputStream(output.toByteArray()));
        assertThat(reader.readTo(buffer.unwrap()), is(true));
        assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello\nworld!")));
        assertThat(buffer.getOption("number"), is((Object) new LongOption(1)));
        assertThat(reader.readTo(buffer.unwrap()), is(true));
        assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello\nworld!")));
        assertThat(buffer.getOption("number"), is((Object) new LongOption(2)));
        assertThat(reader.readTo(buffer.unwrap()), is(false));
    }

    /**
     * with ignored property.
     * @throws Exception if failed
     */
    @Test
    public void ignore() throws Exception {
        ModelLoader loaded = generateJava("ignore");
        ModelWrapper model = loaded.newModel("Model");
        model.set("value", new Text("Hello, world!"));
        model.set("ignored", new Text("ignored"));
        ModelWrapper buffer = loaded.newModel("Model");
        DataModelStreamSupport<Object> support = unsafe(loaded.newObject("csv", "ModelCsvSupport"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = support.createWriter("hello", output);
        writer.write(model.unwrap());
        writer.flush();
        output.close();

        DataModelReader<Object> reader = support.createReader("testing", new ByteArrayInputStream(output.toByteArray()));
        assertThat(reader.readTo(buffer.unwrap()), is(true));
        assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello, world!")));
        assertThat(buffer.getOption("ignored"), is((Object) new StringOption()));
        assertThat(reader.readTo(buffer.unwrap()), is(false));
    }

    /**
     * Compile with no attributes.
     * @throws Exception if failed
     */
    @Test
    public void no_attributes() throws Exception {
        ModelLoader loaded = generateJava("no_attributes");
        assertThat(loaded.exists("csv", "NoAttributesCsvSupport"), is(false));
    }

    /**
     * with invalid field.
     * @throws Exception if failed
     */
    @Test
    public void invalid_file_name() throws Exception {
        shouldSemanticError("invalid_file_name");
    }

    /**
     * with invalid line number.
     * @throws Exception if failed
     */
    @Test
    public void invalid_line_number() throws Exception {
        shouldSemanticError("invalid_line_number");
    }

    /**
     * with invalid record number.
     * @throws Exception if failed
     */
    @Test
    public void invalid_record_number() throws Exception {
        shouldSemanticError("invalid_record_number");
    }

    private String[][] parse(int columns, String string) {
        CsvConfiguration conf = new CsvConfiguration(
                CsvConfiguration.DEFAULT_CHARSET,
                CsvConfiguration.DEFAULT_HEADER_CELLS,
                CsvConfiguration.DEFAULT_TRUE_FORMAT,
                CsvConfiguration.DEFAULT_FALSE_FORMAT,
                CsvConfiguration.DEFAULT_DATE_FORMAT,
                CsvConfiguration.DEFAULT_DATE_TIME_FORMAT);
        ByteArrayInputStream input = new ByteArrayInputStream(string.getBytes(conf.getCharset()));
        CsvParser parser = new CsvParser(input, string, conf);
        List<String[]> results = new ArrayList<String[]>();
        try {
            StringOption buffer = new StringOption();
            while (parser.next()) {
                String[] line = new String[columns];
                for (int i = 0; i < columns; i++) {
                    parser.fill(buffer);
                    line[i] = buffer.or((String) null);
                }
                parser.endRecord();
                results.add(line);
            }
            parser.close();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return results.toArray(new String[results.size()][]);
    }

    /**
     * Compile with invalid attribute.
     * @throws Exception if failed
     */
    @Test
    public void invalid_attribute() throws Exception {
        shouldSemanticError("invalid_attribute");
    }

    @SuppressWarnings("unchecked")
    private DataModelStreamSupport<Object> unsafe(Object support) {
        return (DataModelStreamSupport<Object>) support;
    }
}
