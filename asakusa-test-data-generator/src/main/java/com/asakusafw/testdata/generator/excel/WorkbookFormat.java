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

import static com.asakusafw.testdata.generator.excel.SheetFormat.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * Format of workbook.
 * @since 0.2.0
 */
public enum WorkbookFormat {

    /**
     * Only data sheet.
     */
    DATA("{0}-data.xls", data("data")),

    /**
     * Only rule sheet.
     */
    RULE("{0}-rule.xls", rule("rule")),

    /**
     * Input data sheet and output data sheet.
     */
    INOUT("{0}-inout.xls", data("input"), data("output")),

    /**
     * Expected data sheet and rule sheet.
     */
    INSPECT("{0}-inspect.xls", data("expected"), rule("rule")),

    /**
     * Input and output data sheet and rule sheet.
     */
    ALL("{0}.xls", data("input"), data("output"), rule("rule")),
    ;
    private final String namePattern;

    private final List<SheetFormat> sheets;

    private WorkbookFormat(String namePattern, SheetFormat... sheets) {
        assert namePattern != null;
        assert sheets != null;
        this.namePattern = namePattern;
        List<SheetFormat> results = new ArrayList<SheetFormat>(sheets.length);
        Collections.addAll(results, sheets);
        this.sheets = Collections.unmodifiableList(results);
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
