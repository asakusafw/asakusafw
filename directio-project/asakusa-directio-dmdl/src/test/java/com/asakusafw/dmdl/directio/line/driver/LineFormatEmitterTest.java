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
package com.asakusafw.dmdl.directio.line.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.io.Text;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.directio.common.driver.GeneratorTesterRoot;
import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.FragmentableDataFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Test for {@link LineFormatEmitter}.
 */
public class LineFormatEmitterTest extends GeneratorTesterRoot {

    static final String HELLO_JP = "\u3053\u3093\u306b\u3061\u306f\u3001\u4e16\u754c\uff01";

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new LineFormatEmitter());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line",
                "simple = { value : TEXT; };",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "SimpleLineFormat"));
        check(loader, "Simple", format);
        assertThat(format, is(splittable()));
    }

    /**
     * w/ compression.
     * @throws Exception if failed
     */
    @Test
    public void compression() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line(compression=\"gzip\")",
                "simple = { value : TEXT; };",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "SimpleLineFormat"));
        check(loader, "Simple", format);
        assertThat(format, is(not(splittable())));
    }

    /**
     * w/ charset.
     * @throws Exception if failed
     */
    @Test
    public void charset() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line(charset=\"ISO-2022-JP\")",
                "simple = { value : TEXT; };",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "SimpleLineFormat"));
        check(loader, "Simple", format);
        assertThat(format, is(splittable()));
    }

    /**
     * w/ explicit body.
     * @throws Exception if failed
     */
    @Test
    public void body() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    dummy_header : TEXT;",
                "    @directio.line.body",
                "    value : TEXT;",
                "    dummy_footer : TEXT;",
                "};",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "ComplexLineFormat"));
        check(loader, "Complex", format);
        assertThat(format, is(splittable()));
    }

    /**
     * w/ file name.
     * @throws Exception if failed
     */
    @Test
    public void file_name() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    @directio.line.file_name",
                "    path : TEXT;",
                "    @directio.line.body",
                "    value : TEXT;",
                "};",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "ComplexLineFormat"));
        assertThat(format, is(splittable()));

        byte[] contents = contents(new String[] {
                "Hello1",
                "Hello2",
                "Hello3",
        }).getBytes("UTF-8");
        try (ModelInput<Object> reader = format.createInput(
                format.getSupportedType(), "testing", new ByteArrayInputStream(contents),
                0, contents.length)) {
            ModelWrapper model = loader.newModel("Complex");
            assertThat(reader.readTo(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello1")));
            assertThat(model.get("path"), is((Object) new Text("testing")));
            model.reset();
            assertThat(reader.readTo(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello2")));
            assertThat(model.get("path"), is((Object) new Text("testing")));
            model.reset();
            assertThat(reader.readTo(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello3")));
            assertThat(model.get("path"), is((Object) new Text("testing")));
            model.reset();
            assertThat(reader.readTo(model.unwrap()), is(false));
        }
    }

    /**
     * w/ line number.
     * @throws Exception if failed
     */
    @Test
    public void line_number() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    @directio.line.line_number",
                "    line_num : INT;",
                "    @directio.line.body",
                "    value : TEXT;",
                "};",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "ComplexLineFormat"));
        assertThat(format, is(not(splittable())));

        byte[] contents = contents(new String[] {
                "Hello1",
                "Hello2",
                "Hello3",
        }).getBytes("UTF-8");
        try (ModelInput<Object> reader = format.createInput(
                format.getSupportedType(), "testing", new ByteArrayInputStream(contents),
                0, contents.length)) {
            ModelWrapper model = loader.newModel("Complex");
            assertThat(reader.readTo(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello1")));
            assertThat(model.get("line_num"), is((Object) 1));
            model.reset();
            assertThat(reader.readTo(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello2")));
            assertThat(model.get("line_num"), is((Object) 2));
            model.reset();
            assertThat(reader.readTo(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello3")));
            assertThat(model.get("line_num"), is((Object) 3));
            model.reset();
            assertThat(reader.readTo(model.unwrap()), is(false));
        }
    }

    /**
     * w/ line number.
     * @throws Exception if failed
     */
    @Test
    public void line_number_long() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    @directio.line.line_number",
                "    line_num : LONG;",
                "    @directio.line.body",
                "    value : TEXT;",
                "};",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "ComplexLineFormat"));
        assertThat(format, is(not(splittable())));

        byte[] contents = contents(new String[] {
                "Hello1",
                "Hello2",
                "Hello3",
        }).getBytes("UTF-8");
        try (ModelInput<Object> reader = format.createInput(
                format.getSupportedType(), "testing", new ByteArrayInputStream(contents),
                0, contents.length)) {
            ModelWrapper model = loader.newModel("Complex");
            assertThat(reader.readTo(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello1")));
            assertThat(model.get("line_num"), is((Object) 1L));
            model.reset();
            assertThat(reader.readTo(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello2")));
            assertThat(model.get("line_num"), is((Object) 2L));
            model.reset();
            assertThat(reader.readTo(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello3")));
            assertThat(model.get("line_num"), is((Object) 3L));
            model.reset();
            assertThat(reader.readTo(model.unwrap()), is(false));
        }
    }

    private Matcher<? super FragmentableDataFormat<?>> splittable() {
        return new BaseMatcher<FragmentableDataFormat<?>>() {
            @Override
            public boolean matches(Object item) {
                FragmentableDataFormat<?> format = (FragmentableDataFormat<?>) item;
                try {
                    return format.getMinimumFragmentSize() >= 0;
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("splittable");
            }
        };
    }

    /**
     * test for writer.
     * @throws Exception if failed
     */
    @Test
    public void writer() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line",
                "simple = { value : TEXT; };",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "SimpleLineFormat"));
        ByteArrayOutputStream contents = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = writer(format, contents)) {
            ModelWrapper model = loader.newModel("Simple");
            model.set("value", new Text("Hello1"));
            writer.write(model.unwrap());
            model.set("value", new Text("Hello2"));
            writer.write(model.unwrap());
            model.set("value", new Text("Hello3"));
            writer.write(model.unwrap());
        }
        assertThat(new String(contents.toByteArray(), "UTF-8"), is(contents(new String[] {
                "Hello1",
                "Hello2",
                "Hello3",
        })));
    }

    /**
     * test for reader.
     * @throws Exception if failed
     */
    @Test
    public void reader() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line",
                "simple = { value : TEXT; };",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "SimpleLineFormat"));
        ModelInput<Object> reader = reader(format, contents(new String[] {
                "Hello1",
                "Hello2",
                "Hello3",
        }).getBytes("UTF-8"), -1L);

        ModelWrapper model = loader.newModel("Simple");
        assertThat(reader.readTo(model.unwrap()), is(true));
        assertThat(model.get("value"), is((Object) new Text("Hello1")));
        assertThat(reader.readTo(model.unwrap()), is(true));
        assertThat(model.get("value"), is((Object) new Text("Hello2")));
        assertThat(reader.readTo(model.unwrap()), is(true));
        assertThat(model.get("value"), is((Object) new Text("Hello3")));
        assertThat(reader.readTo(model.unwrap()), is(false));
    }

    /**
     * test for writer w/ MS932.
     * @throws Exception if failed
     */
    @Test
    public void writer_ms932() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line(charset=\"MS932\")",
                "simple = { value : TEXT; };",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "SimpleLineFormat"));
        ByteArrayOutputStream contents = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = writer(format, contents)) {
            ModelWrapper model = loader.newModel("Simple");
            model.set("value", new Text(HELLO_JP + "1"));
            writer.write(model.unwrap());
            model.set("value", new Text(HELLO_JP + "2"));
            writer.write(model.unwrap());
            model.set("value", new Text(HELLO_JP + "3"));
            writer.write(model.unwrap());
        }
        assertThat(new String(contents.toByteArray(), "MS932"), is(contents(new String[] {
                HELLO_JP + "1",
                HELLO_JP + "2",
                HELLO_JP + "3",
        })));
    }

    /**
     * test for reader w/ MS932.
     * @throws Exception if failed
     */
    @Test
    public void reader_ms932() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line(charset=\"MS932\")",
                "simple = { value : TEXT; };",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "SimpleLineFormat"));
        ModelInput<Object> reader = reader(format, contents(new String[] {
                HELLO_JP + "1",
                HELLO_JP + "2",
                HELLO_JP + "3",
        }).getBytes("MS932"), -1L);

        ModelWrapper model = loader.newModel("Simple");
        assertThat(reader.readTo(model.unwrap()), is(true));
        assertThat(model.get("value"), is((Object) new Text(HELLO_JP + "1")));
        assertThat(reader.readTo(model.unwrap()), is(true));
        assertThat(model.get("value"), is((Object) new Text(HELLO_JP + "2")));
        assertThat(reader.readTo(model.unwrap()), is(true));
        assertThat(model.get("value"), is((Object) new Text(HELLO_JP + "3")));
        assertThat(reader.readTo(model.unwrap()), is(false));
    }

    /**
     * test for restoring compression.
     * @throws Exception if failed
     */
    @Test
    public void compression_restore() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line(compression=\"gzip\")",
                "simple = { value : TEXT; };",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "SimpleLineFormat"));
        ByteArrayOutputStream contents = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = writer(format, contents)) {
            ModelWrapper model = loader.newModel("Simple");
            model.set("value", new Text("Hello1"));
            writer.write(model.unwrap());
            model.set("value", new Text("Hello2"));
            writer.write(model.unwrap());
            model.set("value", new Text("Hello3"));
            writer.write(model.unwrap());
        }
        try (InputStream input = new GZIPInputStream(new ByteArrayInputStream(contents.toByteArray()));
                Scanner scanner = new Scanner(new InputStreamReader(input, "UTF-8"));) {
            assertThat(scanner.hasNextLine(), is(true));
            assertThat(scanner.nextLine(), is("Hello1"));
            assertThat(scanner.hasNextLine(), is(true));
            assertThat(scanner.nextLine(), is("Hello2"));
            assertThat(scanner.hasNextLine(), is(true));
            assertThat(scanner.nextLine(), is("Hello3"));
            assertThat(scanner.hasNextLine(), is(false));
        }
    }

    /**
     * infer body.
     * @throws Exception if failed
     */
    @Test
    public void infer_body() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    other_type : LONG;",
                "    @directio.line.file_name",
                "    path : TEXT;",
                "    value : TEXT;",
                "};",
        });
        loader.newObject("line", "ComplexLineFormat");
    }

    /**
     * w/ extra properties.
     * @throws Exception if failed
     */
    @Test
    public void extra_property() throws Exception {
        ModelLoader loader = generateJavaFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    @directio.line.body",
                "    value : TEXT;",
                "    extra : TEXT;",
                "};",
        });
        BinaryStreamFormat<Object> format = unsafe(loader.newObject("line", "ComplexLineFormat"));
        check(loader, "Complex", format);
    }

    /**
     * w/ extra body.
     * @throws Exception if failed
     */
    @Test
    public void invalid_extra_bodies() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    @directio.line.body",
                "    path : TEXT;",
                "    @directio.line.body",
                "    extra : TEXT;",
                "};",
        });
    }

    /**
     * w/o body (ambiguous implicit bodies).
     * @throws Exception if failed
     */
    @Test
    public void invalid_ambiguous_implicit_bodies() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    path : TEXT;",
                "    value : TEXT;",
                "};",
        });
    }

    /**
     * w/o body (missing implicit bodies).
     * @throws Exception if failed
     */
    @Test
    public void invalid_missing_implicit_bodies() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    value : LONG;",
                "};",
        });
    }

    /**
     * body w/ inconsistent type.
     * @throws Exception if failed
     */
    @Test
    public void invalid_inconsnstent_body_type() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    @directio.line.body",
                "    value : LONG;",
                "};",
        });
    }

    /**
     * file name w/ inconsistent type.
     * @throws Exception if failed
     */
    @Test
    public void invalid_inconsnstent_file_name_type() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    @directio.line.body",
                "    value : TEXT;",
                "    @directio.line.file_name",
                "    path : INT;",
                "};",
        });
    }

    /**
     * line number w/ inconsistent type.
     * @throws Exception if failed
     */
    @Test
    public void invalid_inconsnstent_line_number_type() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    @directio.line.body",
                "    value : TEXT;",
                "    @directio.line.line_number",
                "    number : TEXT;",
                "};",
        });
    }

    /**
     * conflict property attributes.
     * @throws Exception if failed
     */
    @Test
    public void invalid_conflict_property_attributes() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.line",
                "complex = {",
                "    @directio.line.body",
                "    @directio.line.file_name",
                "    value : TEXT;",
                "};",
        });
    }

    private void check(
            ModelLoader loader, String name,
            BinaryStreamFormat<Object> format) throws IOException, InterruptedException {
        check(loader, name, format, -1L);
    }

    private void check(
            ModelLoader loader, String name,
            BinaryStreamFormat<Object> format,
            long fragmentSize) throws IOException, InterruptedException {
        ModelWrapper model = loader.newModel(name);
        assertThat(format.getSupportedType(), equalTo((Object) model.getModelClass()));

        model.set("value", new Text("Hello, world!"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = writer(format, output)) {
            writer.write(model.unwrap());
        }
        Object buffer = loader.newModel(name).unwrap();
        try (ModelInput<Object> reader = reader(format, output.toByteArray(), fragmentSize)) {
            assertThat(reader.readTo(buffer), is(true));
            assertThat(buffer, is(model.unwrap()));
            assertThat(reader.readTo(buffer), is(false));
            assertThat(buffer, is(model.unwrap()));
        }
    }

    private ModelInput<Object> reader(
            BinaryStreamFormat<Object> format,
            byte[] contents,
            long fragmentSize) throws IOException, InterruptedException {
        return format.createInput(
                format.getSupportedType(), "testinig", new ByteArrayInputStream(contents),
                0, fragmentSize);
    }

    private ModelOutput<Object> writer(
            BinaryStreamFormat<Object> format,
            OutputStream output) throws IOException, InterruptedException {
        return format.createOutput(format.getSupportedType(), "testing", output);
    }

    @SuppressWarnings("unchecked")
    private BinaryStreamFormat<Object> unsafe(Object support) {
        return (BinaryStreamFormat<Object>) support;
    }

    private String contents(String[] lines) {
        StringBuilder buf = new StringBuilder();
        for (String line : lines) {
            buf.append(line).append('\n');
        }
        return buf.toString();
    }
}
