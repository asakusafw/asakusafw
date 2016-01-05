/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.util.EnumSet;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import com.asakusafw.testdriver.rule.DataModelCondition;

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
     * using xlsx.
     * @throws Exception if occur
     */
    @Test
    public void xssf() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("simple.xlsx");
        assertThat(extractor.supports(sheet), is(true));
    }

    /**
     * not supported because is not an excel rule.
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
     * {@link DataModelCondition} - strict.
     * @throws Exception if occur
     */
    @Test
    public void extractDataModelCondition_strict() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("total_strict.xls");
        assertThat(extractor.extractDataModelCondition(sheet),
                is((Object) EnumSet.noneOf(DataModelCondition.class)));
    }

    /**
     * {@link DataModelCondition} - expected only.
     * @throws Exception if occur
     */
    @Test
    public void extractDataModelCondition_expect() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("total_expect.xls");
        assertThat(extractor.extractDataModelCondition(sheet),
                is((Object) EnumSet.of(DataModelCondition.IGNORE_UNEXPECTED)));
    }

    /**
     * {@link DataModelCondition} - actual only.
     * @throws Exception if occur
     */
    @Test
    public void extractDataModelCondition_actual() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("total_actual.xls");
        assertThat(extractor.extractDataModelCondition(sheet),
                is((Object) EnumSet.of(DataModelCondition.IGNORE_ABSENT)));
    }

    /**
     * {@link DataModelCondition} - only both exists.
     * @throws Exception if occur
     */
    @Test
    public void extractDataModelCondition_intersect() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("total_intersect.xls");
        assertThat(extractor.extractDataModelCondition(sheet),
                is((Object) EnumSet.of(DataModelCondition.IGNORE_UNEXPECTED,
                        DataModelCondition.IGNORE_ABSENT)));
    }

    /**
     * {@link DataModelCondition} - skip.
     * @throws Exception if occur
     */
    @Test
    public void extractDataModelCondition_skip() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("total_skip.xls");
        assertThat(extractor.extractDataModelCondition(sheet),
                is((Object) EnumSet.allOf(DataModelCondition.class)));
    }

    /**
     * start row.
     * @throws Exception if occur
     */
    @Test
    public void extractPropertyRowStartIndex() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("simple.xls");
        assertThat(extractor.extractPropertyRowStartIndex(sheet), is(3));
    }

    /**
     * name.
     * @throws Exception if occur
     */
    @Test
    public void extractName() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("name.xls");
        assertThat(extractor.extractName(sheet.getRow(3)), is("value"));
        assertThat(extractor.extractName(sheet.getRow(4)), is("a"));
        assertThat(extractor.extractName(sheet.getRow(5)), is("very_long_name"));
    }

    /**
     * name - empty string.
     * @throws Exception if occur
     */
    @Test
    public void extractName_empty() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("name.xls");
        assertThat(extractor.extractName(sheet.getRow(6)), is(nullValue()));
    }

    /**
     * name - blank cell.
     * @throws Exception if occur
     */
    @Test
    public void extractName_blank() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("name.xls");
        assertThat(extractor.extractName(sheet.getRow(7)), is(nullValue()));
    }

    /**
     * value.
     * @throws Exception if occur
     */
    @Test
    public void extractValueCondition() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("value.xls");
        assertThat(extractor.extractValueCondition(sheet.getRow(3)), is(ValueConditionKind.ANY));
        assertThat(extractor.extractValueCondition(sheet.getRow(4)), is(ValueConditionKind.KEY));
        assertThat(extractor.extractValueCondition(sheet.getRow(5)), is(ValueConditionKind.EQUAL));
        assertThat(extractor.extractValueCondition(sheet.getRow(6)), is(ValueConditionKind.CONTAIN));
        assertThat(extractor.extractValueCondition(sheet.getRow(7)), is(ValueConditionKind.TODAY));
        assertThat(extractor.extractValueCondition(sheet.getRow(8)), is(ValueConditionKind.NOW));
    }

    /**
     * @throws Exception if occur
     */
    @Test
    public void extractNullityCondition() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("nullity.xls");
        assertThat(extractor.extractNullityCondition(sheet.getRow(3)), is(NullityConditionKind.NORMAL));
        assertThat(extractor.extractNullityCondition(sheet.getRow(4)), is(NullityConditionKind.ACCEPT_ABSENT));
        assertThat(extractor.extractNullityCondition(sheet.getRow(5)), is(NullityConditionKind.DENY_ABSENT));
        assertThat(extractor.extractNullityCondition(sheet.getRow(6)), is(NullityConditionKind.ACCEPT_PRESENT));
        assertThat(extractor.extractNullityCondition(sheet.getRow(7)), is(NullityConditionKind.DENY_PRESENT));
    }

    /**
     * {@link DataModelCondition} - unknown kind.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractDataModelCondition_unknown() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("total_unknown.xls");
        extractor.extractDataModelCondition(sheet);
    }

    /**
     * {@link DataModelCondition} - blank cell.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractDataModelCondition_blank() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("total_blank.xls");
        extractor.extractDataModelCondition(sheet);
    }

    /**
     * {@link DataModelCondition} - invalid cell.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractDataModelCondition_invalid() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("total_invalid.xls");
        extractor.extractDataModelCondition(sheet);
    }

    /**
     * {@link DataModelCondition} - missing row.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractDataModelCondition_missing() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("total_missing.xls");
        extractor.extractDataModelCondition(sheet);
    }

    /**
     * name - invalid type.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractName_invalid() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("name.xls");
        extractor.extractName(sheet.getRow(8));
    }

    /**
     * value - unknown kind.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractValueCondition_unknown() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("value.xls");
        extractor.extractValueCondition(sheet.getRow(9));
    }

    /**
     * value - empty string.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractValueCondition_empty() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("value.xls");
        extractor.extractValueCondition(sheet.getRow(10));
    }

    /**
     * value - blank cell.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractValueCondition_blank() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("value.xls");
        extractor.extractValueCondition(sheet.getRow(11));
    }

    /**
     * value - invalid type.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractValueCondition_invalid() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("value.xls");
        extractor.extractValueCondition(sheet.getRow(12));
    }

    /**
     * nullity - unknown kind.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractNullityCondition_unknown() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("nullity.xls");
        extractor.extractNullityCondition(sheet.getRow(8));
    }

    /**
     * nullity - empty string.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractNullityCondition_empty() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("nullity.xls");
        extractor.extractNullityCondition(sheet.getRow(9));
    }

    /**
     * nullity - blank cell.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractNullityCondition_blank() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("nullity.xls");
        extractor.extractNullityCondition(sheet.getRow(10));
    }

    /**
     * nullity - invalid cell.
     * @throws Exception if occur
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractNullityCondition_invalid() throws Exception {
        ExcelRuleExtractor extractor = new DefaultExcelRuleExtractor();
        Sheet sheet = sheet("nullity.xls");
        extractor.extractNullityCondition(sheet.getRow(11));
    }

    private Sheet sheet(String name) {
        try (InputStream in = getClass().getResourceAsStream("rule/" + name)) {
            assertThat(name, in, not(nullValue()));
            Workbook book = Util.openWorkbookFor(name, in);
            return book.getSheetAt(0);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
