/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.testdata.generator.excel;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.testdriver.excel.RuleSheetFormat;

/**
 * Test for {@link SheetBuilderTest}.
 */
@RunWith(Parameterized.class)
public class SheetBuilderTest extends ExcelTesterRoot {

    private final SpreadsheetVersion version;

    /**
     * Returns test parameter sets.
     * @return test parameter sets
     */
    @Parameters
    public static List<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                { SpreadsheetVersion.EXCEL97 },
                { SpreadsheetVersion.EXCEL2007 },
        });
    }

    /**
     * Creates a new instance.
     * @param version the spreadsheet version
     */
    public SheetBuilderTest(SpreadsheetVersion version) {
        this.version = version;
    }

    /**
     * simple data.
     * @throws Exception if failed
     */
    @Test
    public void data_simple() throws Exception {
        Workbook workbook = WorkbookGenerator.createEmptyWorkbook(version);
        ModelDeclaration model = load("simple.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, version, model);
        builder.addData("MODEL");

        Sheet sheet = workbook.getSheet("MODEL");
        assertThat(sheet, not(nullValue()));
        assertThat(cell(sheet, 0, 0), is("value"));
    }

    /**
     * copy a data sheet.
     * @throws Exception if failed
     */
    @Test
    public void data_copy() throws Exception {
        Workbook workbook = WorkbookGenerator.createEmptyWorkbook(version);
        ModelDeclaration model = load("simple.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, version, model);
        builder.addData("MODEL");
        builder.addData("COPY");

        Sheet sheet = workbook.getSheet("COPY");
        assertThat(sheet, not(nullValue()));
        assertThat(cell(sheet, 0, 0), is("value"));
    }

    /**
     * primitives.
     * @throws Exception if failed
     */
    @Test
    public void data_primitives() throws Exception {
        Workbook workbook = WorkbookGenerator.createEmptyWorkbook(version);
        ModelDeclaration model = load("basic_type.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, version, model);
        builder.addData("PRIMITIVES");

        Sheet sheet = workbook.getSheet("PRIMITIVES");
        assertThat(sheet, not(nullValue()));
        checkDataSheet(sheet, model);
    }

    /**
     * check rule.
     * @throws Exception if failed
     */
    @Test
    public void rule() throws Exception {
        Workbook workbook = WorkbookGenerator.createEmptyWorkbook(version);
        ModelDeclaration model = load("basic_type.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, version, model);
        builder.addRule("RULE");

        Sheet sheet = workbook.getSheet("RULE");
        checkRuleSheet(sheet, model);
    }

    /**
     * copy a rule sheet.
     * @throws Exception if failed
     */
    @Test
    public void rule_copy() throws Exception {
        Workbook workbook = WorkbookGenerator.createEmptyWorkbook(version);
        ModelDeclaration model = load("simple.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, version, model);
        builder.addRule("MODEL");
        builder.addRule("COPY");

        Sheet sheet = workbook.getSheet("COPY");
        assertThat(sheet, not(nullValue()));
        for (RuleSheetFormat format : RuleSheetFormat.values()) {
            assertThat(format.name(), cell(sheet, format, 0, 0), is(format.getTitle()));
        }
    }
}
