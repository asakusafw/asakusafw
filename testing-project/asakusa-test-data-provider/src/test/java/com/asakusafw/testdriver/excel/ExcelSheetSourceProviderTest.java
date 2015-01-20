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

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceProvider;
import com.asakusafw.testdriver.core.SpiDataModelSourceProvider;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link ExcelSheetSourceProvider}.
 * @since 0.2.0
 */
public class ExcelSheetSourceProviderTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    /**
     * opens sheet by its number.
     * @throws Exception if occur
     */
    @Test
    public void open_bynumber() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = uri("data/workbook.xls", ":1");
        DataModelSource source = provider.open(SIMPLE, uri, new TestContext.Empty());
        assertThat(source, not(nullValue()));

        Simple s1 = next(source);
        assertThat(s1.number, is(200));
        assertThat(s1.text, is("bbb"));

        end(source);
    }

    /**
     * opens sheet by its name.
     * @throws Exception if occur
     */
    @Test
    public void open_byname() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = uri("data/workbook.xls", "c");
        DataModelSource source = provider.open(SIMPLE, uri, new TestContext.Empty());
        assertThat(source, not(nullValue()));

        Simple s1 = next(source);
        assertThat(s1.number, is(300));
        assertThat(s1.text, is("ccc"));

        end(source);
    }

    /**
     * opens sheet via service provider.
     * @throws Exception if occur
     */
    @Test
    public void spi() throws Exception {
        DataModelSourceProvider provider = new SpiDataModelSourceProvider(ExcelSheetSourceProvider.class.getClassLoader());
        URI uri = uri("data/workbook.xls", ":1");
        DataModelSource source = provider.open(SIMPLE, uri, new TestContext.Empty());
        assertThat(source, not(nullValue()));

        Simple s1 = next(source);
        assertThat(s1.number, is(200));
        assertThat(s1.text, is("bbb"));

        end(source);
    }

    /**
     * integration with test-data-generator.
     * @throws Exception if occur
     */
    @Test
    public void integration() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = uri("it/simple.xls", "input");
        DataModelSource source = provider.open(SIMPLE, uri, new TestContext.Empty());
        assertThat(source, not(nullValue()));

        Simple s1 = next(source);
        assertThat(s1.number, is(100));
        assertThat(s1.text, is("aaa"));

        Simple s2 = next(source);
        assertThat(s2.number, is(200));
        assertThat(s2.text, is("bbb"));

        Simple s3 = next(source);
        assertThat(s3.number, is(300));
        assertThat(s3.text, is("ccc"));

        end(source);
    }

    /**
     * invalid file name.
     * @throws Exception if occur
     */
    @Test
    public void invalid_file() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = uri("data/simple.json", ":1");
        DataModelSource source = provider.open(SIMPLE, uri, new TestContext.Empty());
        assertThat(source, is(nullValue()));
    }

    /**
     * fragment unspecified.
     * @throws Exception if occur
     */
    @Test
    public void missing_fragment() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = uri("data/workbook.xls", null);
        DataModelSource source = provider.open(SIMPLE, uri, new TestContext.Empty());
        assertThat(source, not(nullValue()));

        // the first sheet
        Simple s1 = next(source);
        assertThat(s1.number, is(100));
        assertThat(s1.text, is("aaa"));

        end(source);
    }

    /**
     * invalid fragment.
     * @throws Exception if occur
     */
    @Test
    public void invalid_fragment() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = uri("data/workbook.xls", ":");
        DataModelSource source = provider.open(SIMPLE, uri, new TestContext.Empty());
        assertThat(source, is(nullValue()));
    }

    /**
     * workbook not found.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void not_found() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = new URI("file:///__________no_such_file________.xls#:0");
        provider.open(SIMPLE, uri, new TestContext.Empty());
    }

    /**
     * invalid workbook format.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void invalid_workbook() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = uri("data/invalid_format.xls", ":0");
        provider.open(SIMPLE, uri, new TestContext.Empty());
    }

    /**
     * invalid sheet number.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void invalid_sheet_bynumber() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = uri("data/workbook.xls", ":100");
        provider.open(SIMPLE, uri, new TestContext.Empty());
    }

    /**
     * invalid sheet name.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void invalid_sheet_byname() throws Exception {
        ExcelSheetSourceProvider provider = new ExcelSheetSourceProvider();
        URI uri = uri("data/workbook.xls", "no such sheet");
        provider.open(SIMPLE, uri, new TestContext.Empty());
    }

    private Simple next(DataModelSource source) throws IOException {
        DataModelReflection next = source.next();
        assertThat(next, is(not(nullValue())));
        return SIMPLE.toObject(next);
    }

    private void end(DataModelSource source) throws IOException {
        DataModelReflection next = source.next();
        assertThat(String.valueOf(next), next, nullValue());
        source.close();
    }

    private URI uri(String file, String fragment) throws Exception {
        URL url = getClass().getResource(file);
        assertThat(file, url, not(nullValue()));
        URI resource = url.toURI();
        URI uri = new URI(
                resource.getScheme(),
                resource.getUserInfo(),
                resource.getHost(),
                resource.getPort(),
                resource.getPath(),
                resource.getQuery(),
                fragment);
        return uri;
    }
}
