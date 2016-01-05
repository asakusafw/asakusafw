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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.source.DmdlSourceResource;
import com.asakusafw.dmdl.util.AnalyzeTask;
import com.asakusafw.testdriver.excel.RuleSheetFormat;

/**
 * A common test root for this package.
 */
public class ExcelTesterRoot {

    /**
     * temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test name.
     */
    @Rule
    public TestName testName = new TestName();

    /**
     * Loads a DMDL script and returns the specified model.
     * @param dmdl DMDL script name
     * @param model model name
     * @return the specified model in the DMDL script
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected ModelDeclaration load(String dmdl, String model) {
        URL resource = getClass().getResource(dmdl);
        assertThat(dmdl, resource, not(nullValue()));

        DmdlSourceResource repo = new DmdlSourceResource(Arrays.asList(resource), Charset.forName("UTF-8"));
        ClassLoader loader = ExcelTesterRoot.class.getClassLoader();
        AnalyzeTask task = new AnalyzeTask(testName.getMethodName(), loader);
        try {
            DmdlSemantics results = task.process(repo);
            ModelDeclaration decl = results.findModelDeclaration(model);
            assertThat(dmdl + ":" + model, decl, not(nullValue()));
            return decl;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Verifies data sheet.
     * @param sheet the sheet
     * @param model target model
     */
    protected void checkDataSheet(Sheet sheet, ModelDeclaration model) {
        int index = 0;
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            assertThat(cell(sheet, 0, index++), is(property.getName().identifier));
        }
    }

    /**
     * Verifies rule sheet.
     * @param sheet the sheet
     * @param model target model
     */
    protected void checkRuleSheet(Sheet sheet, ModelDeclaration model) {
        assertThat(sheet, not(nullValue()));
        for (RuleSheetFormat format : RuleSheetFormat.values()) {
            assertThat(format.name(), cell(sheet, format, 0, 0), is(format.getTitle()));
        }
        assertThat(cell(sheet, RuleSheetFormat.FORMAT, 0, 1), is(RuleSheetFormat.FORMAT_VERSION));
        assertThat(cell(sheet, RuleSheetFormat.TOTAL_CONDITION, 0, 1), not(nullValue()));
        int index = 0;
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            assertThat(cell(sheet, RuleSheetFormat.PROPERTY_NAME, index + 1, 0), is(property.getName().identifier));
            assertThat(cell(sheet, RuleSheetFormat.VALUE_CONDITION, index + 1, 0), not(nullValue()));
            assertThat(cell(sheet, RuleSheetFormat.NULLITY_CONDITION, index + 1, 0), not(nullValue()));
            index++;
        }
    }

    /**
     * Obtain the cell using {@link RuleSheetFormat}.
     * @param sheet sheet
     * @param format format
     * @param rowOffset row offset from the format
     * @param colOffset column offset from the format
     * @return cell string
     */
    protected String cell(Sheet sheet, RuleSheetFormat format, int rowOffset, int colOffset) {
        return cell(sheet, format.getRowIndex() + rowOffset, format.getColumnIndex() + colOffset);
    }

    /**
     * Obtains the cell.
     * @param sheet the sheet
     * @param rowIndex row index
     * @param columnIndex column index
     * @return cell string
     */
    protected String cell(Sheet sheet, int rowIndex, int columnIndex) {
        Row row = sheet.getRow(rowIndex);
        assertThat(row, not(nullValue()));
        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        }
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        return cell.getStringCellValue();
    }

    /**
     * Opens the workbook.
     * @param file the target workbook
     * @return the opened workbook
     * @throws IOException if failed
     */
    protected Workbook openWorkbook(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            if (file.getName().endsWith(".xls")) {
                return new HSSFWorkbook(in);
            } else if (file.getName().endsWith(".xlsx")) {
                return new XSSFWorkbook(in);
            } else {
                throw new IOException(file.getPath());
            }
        }
    }
}
