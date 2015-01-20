/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSink;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
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
     * using xslx.
     * @throws Exception if occur
     */
    @Test
    public void xssf() throws Exception {
        verify("simple.xlsx", ".xlsx");
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

    /**
     * many columns.
     * @throws Exception if occur
     */
    @Test
    public void many_columns() throws Exception {
        Object[] value = new Object[256];
        Map<PropertyName, PropertyType> map = new TreeMap<PropertyName, PropertyType>();
        for (int i = 0; i < value.length; i++) {
            map.put(PropertyName.newInstance(String.format("p%04x", i)), PropertyType.INT);
            value[i] = i;
        }
        ArrayModelDefinition def = new ArrayModelDefinition(map);

        File file = folder.newFile("temp.xls");
        ExcelSheetSinkFactory factory = new ExcelSheetSinkFactory(file);
        DataModelSink sink = factory.createSink(def, new TestContext.Empty());
        try {
            sink.put(def.toReflection(value));
        } finally {
            sink.close();
        }

        InputStream in = new FileInputStream(file);
        try {
            Workbook workbook = Util.openWorkbookFor(file.getPath(), in);
            Sheet sheet = workbook.getSheetAt(0);
            Row title = sheet.getRow(0);
            assertThat(title.getLastCellNum(), is((short) 256));

            Row content = sheet.getRow(1);
            for (int i = 0; i < title.getLastCellNum(); i++) {
                assertThat(content.getCell(i).getNumericCellValue(), is((double) (Integer) value[i]));
            }
        } finally {
            in.close();
        }
    }

    private void verify(String file) throws IOException {
        verify(file, ".xls");
    }

    private void verify(String file, String extension) throws IOException {
        Set<DataModelReflection> expected = collect(open(file));
        File temp = folder.newFile("temp" + extension);
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

    private ExcelSheetDataModelSource open(URL resource) throws IOException {
        URI uri;
        try {
            uri = resource.toURI();
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        InputStream in = resource.openStream();
        try {
            Workbook book = Util.openWorkbookFor(resource.getFile(), in);
            Sheet sheet = book.getSheetAt(0);
            return new ExcelSheetDataModelSource(SIMPLE, uri, sheet);
        } finally {
            in.close();
        }
    }
}
