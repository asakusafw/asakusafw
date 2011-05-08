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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

/**
 * Test for {@link DefaultExcelRuleExtractor}.
 * @since 0.2.0
 */
public class DefaultExcelRuleExtractorTest {

    /**
     * supports.
     * @throws Exception if occur
     */
    @Test
    public void supports() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("simple.xls");
        assertThat(extractor.supports(sheet), is(true));
    }

    /**
     * not supported because is not a excel rule.
     * @throws Exception if occur
     */
    @Test
    public void supports_not() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("invalid_format.xls");
        assertThat(extractor.supports(sheet), is(false));
    }

    /**
     * not supported because is not a valid version.
     * @throws Exception if occur
     */
    @Test
    public void supports_invalid_version() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("invalid_format_version.xls");
        assertThat(extractor.supports(sheet), is(false));
    }

    /**
     * @throws Exception if occur
     */
    @Test
    public void extractDataModelCondition() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("simple.xls");
        assertThat(extractor.supports(sheet), is(true));
        // TODO
    }

    /**
     * @throws Exception if occur
     */
    @Test
    public void extractPropertyRowStartIndex() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("simple.xls");
        assertThat(extractor.supports(sheet), is(true));
        // TODO
    }

    /**
     * @throws Exception if occur
     */
    @Test
    public void extractName() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("simple.xls");
        assertThat(extractor.supports(sheet), is(true));
        // TODO
    }

    /**
     * @throws Exception if occur
     */
    @Test
    public void extractValueCondition() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("simple.xls");
        assertThat(extractor.supports(sheet), is(true));
        // TODO
    }

    /**
     * @throws Exception if occur
     */
    @Test
    public void extractNullityCondition() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("simple.xls");
        assertThat(extractor.supports(sheet), is(true));
        // TODO
    }

    private Sheet sheet(String name) {
        InputStream in = getClass().getResourceAsStream("rule/" + name);
        assertThat(name, in, not(nullValue()));
        try {
            Workbook book = new HSSFWorkbook(in);
            return book.getSheetAt(0);
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }
}
