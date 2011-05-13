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
package com.asakusafw.testdata.generator.excel;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.testdriver.excel.RuleSheetFormat;

/**
 * Test for {@link SheetBuilderTest}.
 * @since 0.2.0
 */
public class SheetBuilderTest extends ExcelTesterRoot {

    /**
     * simple data.
     */
    @Test
    public void data_simple() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        ModelDeclaration model = load("simple.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, model);
        builder.addData("MODEL");

        HSSFSheet sheet = workbook.getSheet("MODEL");
        assertThat(sheet, not(nullValue()));
        assertThat(cell(sheet, 0, 0), is("value"));
    }

    /**
     * copy a data sheet.
     */
    @Test
    public void data_copy() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        ModelDeclaration model = load("simple.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, model);
        builder.addData("MODEL");
        builder.addData("COPY");

        HSSFSheet sheet = workbook.getSheet("COPY");
        assertThat(sheet, not(nullValue()));
        assertThat(cell(sheet, 0, 0), is("value"));
    }

    /**
     * primitives.
     */
    @Test
    public void data_primitives() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        ModelDeclaration model = load("basic_type.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, model);
        builder.addData("PRIMITIVES");

        HSSFSheet sheet = workbook.getSheet("PRIMITIVES");
        assertThat(sheet, not(nullValue()));
        checkDataSheet(sheet, model);
    }

    /**
     * check rule.
     */
    @Test
    public void rule() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        ModelDeclaration model = load("basic_type.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, model);
        builder.addRule("RULE");

        HSSFSheet sheet = workbook.getSheet("RULE");
        checkRuleSheet(sheet, model);
    }

    /**
     * copy a rule sheet.
     */
    @Test
    public void rule_copy() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        ModelDeclaration model = load("simple.dmdl", "simple");
        SheetBuilder builder = new SheetBuilder(workbook, model);
        builder.addRule("MODEL");
        builder.addRule("COPY");

        HSSFSheet sheet = workbook.getSheet("COPY");
        assertThat(sheet, not(nullValue()));
        for (RuleSheetFormat format : RuleSheetFormat.values()) {
            assertThat(format.name(), cell(sheet, format, 0, 0), is(format.getTitle()));
        }
    }
}
