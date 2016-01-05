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
package com.asakusafw.testdata.generator.excel;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.testdata.generator.excel.SheetFormat.Kind;

/**
 * Test for {@link WorkbookGenerator}.
 * @since 0.2.0
 */
public class WorkbookGeneratorTest extends ExcelTesterRoot {

    /**
     * data.
     * @throws Exception if occur
     */
    @Test
    public void data() throws Exception {
        ModelDeclaration model = load("simple.dmdl", "simple");
        WorkbookGenerator generator = new WorkbookGenerator(folder.getRoot(), WorkbookFormat.DATA);

        generator.generate(model);
        Workbook workbook = open(folder.getRoot(), model, WorkbookFormat.DATA);

        Sheet sheet = workbook.getSheet(WorkbookFormat.DATA.getSheets().get(0).getName());
        checkDataSheet(sheet, model);
    }

    /**
     * data using xlsx.
     * @throws Exception if occur
     */
    @Test
    public void datax() throws Exception {
        ModelDeclaration model = load("simple.dmdl", "simple");
        WorkbookGenerator generator = new WorkbookGenerator(folder.getRoot(), WorkbookFormat.DATAX);

        generator.generate(model);
        Workbook workbook = open(folder.getRoot(), model, WorkbookFormat.DATAX);

        Sheet sheet = workbook.getSheet(WorkbookFormat.DATAX.getSheets().get(0).getName());
        checkDataSheet(sheet, model);
    }

    /**
     * rule.
     * @throws Exception if occur
     */
    @Test
    public void rule() throws Exception {
        ModelDeclaration model = load("simple.dmdl", "simple");
        WorkbookGenerator generator = new WorkbookGenerator(folder.getRoot(), WorkbookFormat.RULE);

        generator.generate(model);
        Workbook workbook = open(folder.getRoot(), model, WorkbookFormat.RULE);

        Sheet sheet = workbook.getSheet(WorkbookFormat.RULE.getSheets().get(0).getName());
        checkRuleSheet(sheet, model);
    }

    /**
     * rule using xlsx.
     * @throws Exception if occur
     */
    @Test
    public void rulex() throws Exception {
        ModelDeclaration model = load("simple.dmdl", "simple");
        WorkbookGenerator generator = new WorkbookGenerator(folder.getRoot(), WorkbookFormat.RULEX);

        generator.generate(model);
        Workbook workbook = open(folder.getRoot(), model, WorkbookFormat.RULEX);

        Sheet sheet = workbook.getSheet(WorkbookFormat.RULEX.getSheets().get(0).getName());
        checkRuleSheet(sheet, model);
    }

    /**
     * all formats.
     * @throws Exception if occur
     */
    @Test
    public void all_formats() throws Exception {
        ModelDeclaration model = load("basic_type.dmdl", "simple");
        for (WorkbookFormat format : WorkbookFormat.values()) {
            File dir = folder.newFolder(format.name());
            WorkbookGenerator generator = new WorkbookGenerator(dir, format);
            generator.generate(model);
            Workbook workbook = open(dir, model, format);
            for (SheetFormat sheetForm : format.getSheets()) {
                Sheet sheet = workbook.getSheet(sheetForm.getName());
                if (sheetForm.getKind() == Kind.DATA) {
                    checkDataSheet(sheet, model);
                } else {
                    checkRuleSheet(sheet, model);
                }
            }
        }
    }

    /**
     * many columns.
     * @throws Exception if occur
     */
    @Test
    public void many_columns() throws Exception {
        ModelDeclaration model = load("many_columns.dmdl", "many_columns");
        WorkbookGenerator generator = new WorkbookGenerator(folder.getRoot(), WorkbookFormat.DATA);

        generator.generate(model);
        Workbook workbook = open(folder.getRoot(), model, WorkbookFormat.DATA);

        Sheet sheet = workbook.getSheet(WorkbookFormat.DATA.getSheets().get(0).getName());
        assertThat(sheet.getRow(0).getLastCellNum(), is((short) SpreadsheetVersion.EXCEL97.getMaxColumns()));
    }

    /**
     * many columns using xlsx.
     * @throws Exception if occur
     */
    @Test
    public void many_columnsx() throws Exception {
        ModelDeclaration model = load("many_columns.dmdl", "many_columns");
        WorkbookGenerator generator = new WorkbookGenerator(folder.getRoot(), WorkbookFormat.DATAX);

        generator.generate(model);
        Workbook workbook = open(folder.getRoot(), model, WorkbookFormat.DATAX);

        Sheet sheet = workbook.getSheet(WorkbookFormat.DATA.getSheets().get(0).getName());
        assertThat(sheet.getRow(0).getLastCellNum(), is((short) 300));
    }

    /**
     * invalid output.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void invalid_output() throws Exception {
        ModelDeclaration model = load("simple.dmdl", "simple");
        WorkbookGenerator generator = new WorkbookGenerator(folder.newFile("invalid"), WorkbookFormat.DATA);
        generator.generate(model);
    }

    private Workbook open(File dir, ModelDeclaration model, WorkbookFormat format) throws IOException {
        File file = new File(dir, format.getFileName(model));
        return openWorkbook(file);
    }
}
