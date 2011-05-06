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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.json.Simple;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link ExcelSheetDataModelSource}.
 */
public class ExcelSheetDataModelSourceTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    /**
     * simple.
     * @throws Exception if occur
     */
    @Test
    public void simple() throws Exception {
        ExcelSheetDataModelSource source = open("simple.xls");
        Simple simple = next(source);
        assertThat(simple.number, is(100));
        assertThat(simple.text, is("Hello, world!"));
        end(source);
    }

    private ExcelSheetDataModelSource open(String file) throws IOException {
        URL resource = getClass().getResource(file);
        assertThat(file, resource, not(nullValue()));
        URI uri;
        try {
            uri = resource.toURI();
        }
        catch (URISyntaxException e) {
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

    private Simple next(ExcelSheetDataModelSource source) throws IOException {
        DataModelReflection next = source.next();
        assertThat(next, is(not(nullValue())));
        return SIMPLE.toObject(next);
    }

    private void end(ExcelSheetDataModelSource source) throws IOException {
        DataModelReflection next = source.next();
        assertThat(String.valueOf(next), next, nullValue());
    }
}
