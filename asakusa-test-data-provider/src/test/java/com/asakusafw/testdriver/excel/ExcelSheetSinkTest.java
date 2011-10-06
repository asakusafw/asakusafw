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
package com.asakusafw.testdriver.excel;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSink;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link ExcelSheetSink}.
 */
public class ExcelSheetSinkTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * simple.
     * @throws Exception if occur
     */
    @Test
    public void simple() throws Exception {
        verify("simple.xls");
    }

    /**
     * automatically creates folder.
     * @throws Exception if occur
     */
    @Test
    public void create_folder() throws Exception {
        File container = folder.newFolder("middle");
        File file = new File(container, "file.xls");
        assertThat(container.delete(), is(true));

        assertThat(file.isFile(), is(false));

        ExcelSheetSinkFactory factory = new ExcelSheetSinkFactory(file);
        DataModelSink sink = factory.createSink(SIMPLE, new TestContext.Empty());
        try {
            sink.put(SIMPLE.toReflection(new Simple()));
        } finally {
            sink.close();
        }
        assertThat(file.isFile(), is(true));
    }

    /**
     * multiple rows.
     * @throws Exception if occur
     */
    @Test
    public void multiple() throws Exception {
        verify("multiple.xls");
    }

    /**
     * contains blank cells.
     * @throws Exception if occur
     */
    @Test
    public void blank_cell() throws Exception {
        verify("blank_cell.xls");
    }

    /**
     * stringified by '.
     * @throws Exception if occur
     */
    @Test
    public void stringify() throws Exception {
        verify("stringify.xls");
    }

    /**
     * empty string.
     * @throws Exception if occur
     */
    @Test
    public void empty_string() throws Exception {
        verify("empty_string.xls");
    }

    /**
     * boolean values.
     * @throws Exception if occur
     */
    @Test
    public void boolean_values() throws Exception {
        verify("boolean.xls");
    }

    /**
     * byte values.
     * @throws Exception if occur
     */
    @Test
    public void byte_values() throws Exception {
        verify("byte.xls");
    }

    /**
     * short values.
     * @throws Exception if occur
     */
    @Test
    public void short_values() throws Exception {
        verify("short.xls");
    }

    /**
     * int values.
     * @throws Exception if occur
     */
    @Test
    public void int_values() throws Exception {
        verify("int.xls");
    }

    /**
     * long values.
     * @throws Exception if occur
     */
    @Test
    public void long_values() throws Exception {
        verify("long.xls");
    }

    /**
     * float values.
     * @throws Exception if occur
     */
    @Test
    public void float_values() throws Exception {
        verify("float.xls");
    }

    /**
     * double values.
     * @throws Exception if occur
     */
    @Test
    public void double_values() throws Exception {
        verify("double.xls");
    }

    /**
     * big integer values.
     * @throws Exception if occur
     */
    @Test
    public void integer_values() throws Exception {
        verify("integer.xls");
    }

    /**
     * big decimal values.
     * @throws Exception if occur
     */
    @Test
    public void decimal_values() throws Exception {
        verify("decimal.xls");
    }

    /**
     * date values.
     * @throws Exception if occur
     */
    @Test
    public void date_values() throws Exception {
        verify("date.xls");
    }

    /**
     * date values.
     * @throws Exception if occur
     */
    @Test
    public void datetime_values() throws Exception {
        verify("datetime.xls");
    }

    /**
     * contains blank row.
     * @throws Exception if occur
     */
    @Test
    public void blank_row() throws Exception {
        verify("blank_row.xls");
    }

    /**
     * contains blank row but is decorated.
     * @throws Exception if occur
     */
    @Test
    public void decorated_blank_row() throws Exception {
        verify("decorated_blank_row.xls");
    }

    private void verify(String file) throws IOException {
        Set<DataModelReflection> expected = collect(open(file));
        File temp = folder.newFile("temp.xls");
        ExcelSheetSinkFactory factory = new ExcelSheetSinkFactory(temp);
        DataModelSink sink = factory.createSink(SIMPLE, new TestContext.Empty());
        try {
            for (DataModelReflection model : expected) {
                sink.put(model);
            }
        } finally {
            sink.close();
        }

        Set<DataModelReflection> actual = collect(open(temp.toURI().toURL()));
        assertThat(actual, is(expected));
    }

    private Set<DataModelReflection> collect(ExcelSheetDataModelSource source) throws IOException {
        Set<DataModelReflection> results = new HashSet<DataModelReflection>();
        try {
            while (true) {
                DataModelReflection next = source.next();
                if (next == null) {
                    break;
                }
                assertThat(next.toString(), results.contains(source), is(false));
                results.add(next);
            }
        } finally {
            source.close();
        }
        return results;
    }

    private ExcelSheetDataModelSource open(String file) throws IOException {
        URL resource = getClass().getResource("data/" + file);
        assertThat(file, resource, not(nullValue()));
        return open(resource);
    }

    private ExcelSheetDataModelSource open(URL resource) throws AssertionError, IOException {
        URI uri;
        try {
            uri = resource.toURI();
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        InputStream in = resource.openStream();
        try {
            HSSFWorkbook book = new HSSFWorkbook(in);
            Sheet sheet = book.getSheetAt(0);
            return new ExcelSheetDataModelSource(SIMPLE, uri, sheet);
        } finally {
            in.close();
        }
    }
}
