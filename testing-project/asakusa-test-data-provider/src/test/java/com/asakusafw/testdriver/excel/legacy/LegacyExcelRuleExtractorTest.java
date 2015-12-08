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
package com.asakusafw.testdriver.excel.legacy;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import com.asakusafw.testdriver.excel.ExcelRuleExtractor;
import com.asakusafw.testdriver.excel.NullityConditionKind;
import com.asakusafw.testdriver.excel.ValueConditionKind;
import com.asakusafw.testdriver.rule.DataModelCondition;

/**
 * Test for {@link LegacyExcelRuleExtractor}.
 * @since 0.2.0
 */
public class LegacyExcelRuleExtractorTest {

    /**
     * supported.
     */
    @Test
    public void supports() {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("legacy-rule.xls");
        assertThat(extractor.supports(sheet), is(true));
    }

    /**
     * not supported.
     */
    @Test
    public void supports_not() {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        assertThat(extractor.supports(sheet), is(false));
    }

    /**
     * total condition - ignore.
     * @throws Exception if failed
     */
    @Test
    public void extractDataModelCondition_ignore() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("ignore.xls");
        assertThat(extractor.extractDataModelCondition(sheet),
                is((Object) EnumSet.allOf(DataModelCondition.class)));
    }

    /**
     * total condition - strict match.
     * @throws Exception if failed
     */
    @Test
    public void extractDataModelCondition_strict() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("strict.xls");
        assertThat(extractor.extractDataModelCondition(sheet),
                is((Object) EnumSet.noneOf(DataModelCondition.class)));
    }

    /**
     * total condition - ignore unexpected.
     * @throws Exception if failed
     */
    @Test
    public void extractDataModelCondition_partial() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("partial.xls");
        assertThat(extractor.extractDataModelCondition(sheet),
                is((Object) EnumSet.of(DataModelCondition.IGNORE_UNEXPECTED)));
    }

    /**
     * total condition - invalid.
     * @throws Exception if failed
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractDataModelCondition_invalid() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        extractor.extractDataModelCondition(sheet);
    }

    /**
     * row start.
     * @throws Exception if failed
     */
    @Test
    public void extractPropertyRowStartIndex() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("legacy-rule.xls");
        assertThat(extractor.extractPropertyRowStartIndex(sheet), is(3));
    }

    /**
     * names.
     * @throws Exception if failed
     */
    @Test
    public void extractName() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("legacy-rule.xls");
        assertThat(extractor.extractName(sheet.getRow(3)), is("sid"));
        assertThat(extractor.extractName(sheet.getRow(4)), is("version_no"));
        assertThat(extractor.extractName(sheet.getRow(5)), is("rgst_datetime"));
        assertThat(extractor.extractName(sheet.getRow(6)), is("updt_datetime"));
        assertThat(extractor.extractName(sheet.getRow(7)), is("code"));
    }

    /**
     * empty names.
     * @throws Exception if failed
     */
    @Test
    public void extractName_empty() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        assertThat(extractor.extractName(sheet.getRow(3)), is(nullValue()));
        assertThat(extractor.extractName(sheet.getRow(4)), is(nullValue()));
    }

    /**
     * invalid names.
     * @throws Exception if failed
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractName_invalid() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        extractor.extractName(sheet.getRow(5));
    }

    /**
     * value conditions.
     * @throws Exception if failed
     */
    @Test
    public void extractValueCondition() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("legacy-rule.xls");
        assertThat(extractor.extractValueCondition(sheet.getRow(3)), is(ValueConditionKind.ANY));
        assertThat(extractor.extractValueCondition(sheet.getRow(4)), is(ValueConditionKind.EQUAL));
        assertThat(extractor.extractValueCondition(sheet.getRow(5)), is(ValueConditionKind.CONTAIN));
        assertThat(extractor.extractValueCondition(sheet.getRow(6)), is(ValueConditionKind.NOW));
        assertThat(extractor.extractValueCondition(sheet.getRow(7)), is(ValueConditionKind.TODAY));
    }

    /**
     * value conditions - unknown value.
     * @throws Exception if failed
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractValueCondition_unknown() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        extractor.extractValueCondition(sheet.getRow(3));
    }

    /**
     * value conditions - blank value.
     * @throws Exception if failed
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractValueCondition_empty() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        extractor.extractValueCondition(sheet.getRow(4));
    }

    /**
     * value conditions - invalid type.
     * @throws Exception if failed
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractValueCondition_invalid_type() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        extractor.extractValueCondition(sheet.getRow(5));
    }

    /**
     * key.
     * @throws Exception if failed
     */
    @Test
    public void extractValueCondition_key() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("key.xls");
        assertThat(extractor.extractValueCondition(sheet.getRow(3)), is(ValueConditionKind.KEY));
        assertThat(extractor.extractNullityCondition(sheet.getRow(3)), is(NullityConditionKind.NORMAL));
    }

    /**
     * key - invalid type.
     * @throws Exception if failed
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractValueCondition_key_invalid_type() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        extractor.extractValueCondition(sheet.getRow(6));
    }

    /**
     * key - unknown.
     * @throws Exception if failed
     */
    @Test
    public void extractValueCondition_key_unknown() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        assertThat(extractor.extractValueCondition(sheet.getRow(7)), is(ValueConditionKind.KEY));
    }

    /**
     * nullity conditions.
     * @throws Exception if failed
     */
    @Test
    public void extractNullityCondition() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("legacy-rule.xls");
        assertThat(extractor.extractNullityCondition(sheet.getRow(3)), is(NullityConditionKind.NORMAL));
        assertThat(extractor.extractNullityCondition(sheet.getRow(4)), is(NullityConditionKind.ACCEPT_ABSENT));
        assertThat(extractor.extractNullityCondition(sheet.getRow(5)), is(NullityConditionKind.DENY_ABSENT));
        assertThat(extractor.extractNullityCondition(sheet.getRow(6)), is(NullityConditionKind.ACCEPT_PRESENT));
        assertThat(extractor.extractNullityCondition(sheet.getRow(7)), is(NullityConditionKind.DENY_PRESENT));
    }

    /**
     * nullity conditions - unknown.
     * @throws Exception if failed
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractNullityCondition_unknown() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        extractor.extractNullityCondition(sheet.getRow(3));
    }

    /**
     * nullity conditions - blank.
     * @throws Exception if failed
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractNullityCondition_blank() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        extractor.extractNullityCondition(sheet.getRow(4));
    }

    /**
     * nullity conditions - invalid type.
     * @throws Exception if failed
     */
    @Test(expected = ExcelRuleExtractor.FormatException.class)
    public void extractNullityCondition_invalid_type() throws Exception {
        ExcelRuleExtractor extractor = new LegacyExcelRuleExtractor();
        Sheet sheet = sheet("invalid.xls");
        extractor.extractNullityCondition(sheet.getRow(5));
    }

    private Sheet sheet(String name) {
        try (InputStream in = getClass().getResourceAsStream(name)) {
            assertThat(name, in, not(nullValue()));
            Workbook book = new HSSFWorkbook(in);
            return book.getSheetAt(0);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
