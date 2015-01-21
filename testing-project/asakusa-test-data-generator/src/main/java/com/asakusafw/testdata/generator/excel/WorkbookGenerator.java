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
package com.asakusafw.testdata.generator.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.testdata.generator.TemplateGenerator;

/**
 * Generates Excel workbooks for testing each data model.
 * @since 0.2.0
 * @version 0.5.3
 */
public class WorkbookGenerator implements TemplateGenerator {

    static final Logger LOG = LoggerFactory.getLogger(WorkbookGenerator.class);

    private final File output;

    private final WorkbookFormat format;

    /**
     * Creates a new instance.
     * @param output output directory
     * @param format workbook format to be generated
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public WorkbookGenerator(File output, WorkbookFormat format) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        this.output = output;
        this.format = format;
    }

    /**
     * Generates a workbook for the specified model.
     * @param model the target model
     * @throws IOException if failed to generate a workbook
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @Override
    public void generate(ModelDeclaration model) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        if (output.isDirectory() == false && output.mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "出力先のディレクトリを生成できませんでした: {0}",
                    output));
        }
        Workbook workbook = createWorkbook();
        SheetBuilder builder = new SheetBuilder(workbook, format.getVersion(), model);
        for (SheetFormat sheet : format.getSheets()) {
            switch (sheet.getKind()) {
            case DATA:
                LOG.debug("Building data sheet: {}.{}", model.getName(), sheet.getName()); //$NON-NLS-1$
                builder.addData(sheet.getName());
                break;
            case RULE:
                LOG.debug("Building rule sheet: {}.{}", model.getName(), sheet.getName()); //$NON-NLS-1$
                builder.addRule(sheet.getName());
                break;
            default:
                throw new AssertionError(MessageFormat.format(
                        "Unknown sheet format: {0}", //$NON-NLS-1$
                        sheet));
            }
        }

        File file = new File(output, format.getFileName(model));
        LOG.debug("Emitting workbook: {}", file); //$NON-NLS-1$

        OutputStream out = new FileOutputStream(file);
        try {
            workbook.write(out);
        } finally {
            out.close();
        }
        LOG.info(MessageFormat.format(
                "Excelワークブックを生成しました: {0}",
                file.getAbsolutePath()));
    }

    private Workbook createWorkbook() throws IOException {
        return createEmptyWorkbook(format.getVersion());
    }

    /**
     * Returns spreadsheet version for the target workbook.
     * @param fileName the target workbook path
     * @return the target version
     * @since 0.7.0
     */
    public static SpreadsheetVersion getSpreadsheetVersion(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null"); //$NON-NLS-1$
        }
        if (fileName.endsWith(".xls")) { //$NON-NLS-1$
            return SpreadsheetVersion.EXCEL97;
        } else if (fileName.endsWith("xlsx")) { //$NON-NLS-1$
            return SpreadsheetVersion.EXCEL2007;
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "サポートしていないExcelワークブックの形式です: {0}",
                    fileName));
        }
    }

    /**
     * Returns spreadsheet version for the target workbook.
     * @param workbook the target workbook
     * @return the target version
     * @since 0.7.0
     */
    public static SpreadsheetVersion getSpreadsheetVersion(Workbook workbook) {
        if (workbook == null) {
            throw new IllegalArgumentException("workbook must not be null"); //$NON-NLS-1$
        }
        if (workbook instanceof HSSFWorkbook) {
            return SpreadsheetVersion.EXCEL97;
        } else if (workbook instanceof XSSFWorkbook) {
            return SpreadsheetVersion.EXCEL2007;
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "サポートしていないExcelワークブックの形式です: {0}",
                    workbook.getClass().getName()));
        }
    }

    /**
     * Creates a new empty workbook for the target version.
     * @param version the target version
     * @return the created workbook
     * @throws IOException if failed to create the workbook
     * @since 0.5.3
     */
    public static Workbook createEmptyWorkbook(SpreadsheetVersion version) throws IOException {
        if (version == null) {
            throw new IllegalArgumentException("version must not be null"); //$NON-NLS-1$
        }
        switch (version) {
        case EXCEL97:
            return new HSSFWorkbook();
        case EXCEL2007:
            return new XSSFWorkbook();
        default:
            throw new IOException(MessageFormat.format(
                    "サポートしていないExcelワークブックの形式です: {0}",
                    version));
        }
    }

    @Override
    public String getTitle() {
        return MessageFormat.format(
                "generates Excel workbook ({0})",
                format);
    }
}
