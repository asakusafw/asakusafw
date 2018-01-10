/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import static com.asakusafw.testdata.generator.excel.SheetFormat.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;

import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * Format of workbook.
 * @since 0.2.0
 * @version 0.5.3
 */
public enum WorkbookFormat {

    /**
     * Only data sheet.
     */
    DATA("{0}-data.xls", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL97,
            data("data")), //$NON-NLS-1$

    /**
     * Only rule sheet.
     */
    RULE("{0}-rule.xls", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL97,
            rule("rule")), //$NON-NLS-1$

    /**
     * Input data sheet and output data sheet.
     */
    INOUT("{0}-inout.xls", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL97,
            data("input"), data("output")), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Expected data sheet and rule sheet.
     */
    INSPECT("{0}-inspect.xls", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL97,
            data("expected"), rule("rule")), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Input and output data sheet and rule sheet.
     */
    ALL("{0}.xls", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL97,
            data("input"), data("output"), rule("rule")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    /**
     * Only data sheet (Excel 2007).
     * @since 0.5.3
     */
    DATAX("{0}-data.xlsx", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL2007,
            data("data")), //$NON-NLS-1$

    /**
     * Only rule sheet (Excel 2007).
     * @since 0.5.3
     */
    RULEX("{0}-rule.xlsx", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL2007, rule("rule")), //$NON-NLS-1$

    /**
     * Input data sheet and output data sheet (Excel 2007).
     * @since 0.5.3
     */
    INOUTX("{0}-inout.xlsx", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL2007,
            data("input"), data("output")), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Expected data sheet and rule sheet (Excel 2007).
     * @since 0.5.3
     */
    INSPECTX("{0}-inspect.xlsx", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL2007,
            data("expected"), rule("rule")), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Input and output data sheet and rule sheet (Excel 2007).
     * @since 0.5.3
     */
    ALLX("{0}.xlsx", //$NON-NLS-1$
            SpreadsheetVersion.EXCEL2007,
            data("input"), data("output"), rule("rule")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ;
    private final String namePattern;

    private final SpreadsheetVersion version;

    private final List<SheetFormat> sheets;

    WorkbookFormat(String namePattern, SpreadsheetVersion version, SheetFormat... sheets) {
        assert namePattern != null;
        assert version != null;
        assert sheets != null;
        this.namePattern = namePattern;
        this.version = version;
        List<SheetFormat> results = new ArrayList<>(sheets.length);
        Collections.addAll(results, sheets);
        this.sheets = Collections.unmodifiableList(results);
    }

    /**
     * Returns the target spreadsheet version.
     * @return the version
     * @since 0.5.3
     */
    public SpreadsheetVersion getVersion() {
        return version;
    }

    /**
     * Returns the format of each sheet.
     * @return the format of each sheet
     */
    public List<SheetFormat> getSheets() {
        return sheets;
    }

    /**
     * Returns a simple file name for the model.
     * @param model target model
     * @return simple file name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public String getFileName(ModelDeclaration model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        return MessageFormat.format(namePattern, model.getName().identifier);
    }

    /**
     * Returns predefined format by its name.
     * @param format the name
     * @return the corresponded format, or {@code null} if not exists
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static WorkbookFormat findByName(String format) {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        for (WorkbookFormat item : values()) {
            if (item.name().equalsIgnoreCase(format)) {
                return item;
            }
        }
        return null;
    }
}
