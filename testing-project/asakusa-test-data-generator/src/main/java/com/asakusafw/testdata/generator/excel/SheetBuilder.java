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

import java.text.MessageFormat;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * Appends test data/rule sheets to the workbook.
 * @since 0.2.0
 * @version 0.7.0
 * @see SheetEditor
 */
public class SheetBuilder {

    static final Logger LOG = LoggerFactory.getLogger(SheetBuilder.class);

    private final SheetEditor editor;

    private final ModelDeclaration model;

    private String sawDataSheet;

    private String sawRuleSheet;

    /**
     * Creates a new instance.
     * @param workbook target workbook to build sheets
     * @param model target model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SheetBuilder(Workbook workbook, ModelDeclaration model) {
        if (workbook == null) {
            throw new IllegalArgumentException("workbook must not be null"); //$NON-NLS-1$
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        this.editor = new SheetEditor(workbook);
        this.model = model;
    }

    /**
     * Creates a new instance.
     * @param workbook target workbook to build sheets
     * @param version the spreadsheet version of the target workbook
     * @param model target model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SheetBuilder(Workbook workbook, SpreadsheetVersion version, ModelDeclaration model) {
        this(workbook, model);
        if (version != WorkbookGenerator.getSpreadsheetVersion(workbook)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("SheetBuilder.errorInconsistentVersion"), //$NON-NLS-1$
                    WorkbookGenerator.getSpreadsheetVersion(workbook),
                    version));
        }
    }

    /**
     * Creates a data sheet with specified name.
     * @param name sheet name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void addData(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (sawDataSheet != null) {
            copy(sawDataSheet, name);
        } else {
            editor.addData(name, model);
        }
        sawDataSheet = name;
    }

    /**
     * Creates a rule sheet with specified name.
     * @param name sheet name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void addRule(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (sawRuleSheet != null) {
            copy(sawRuleSheet, name);
        } else {
            editor.addRule(name, model);
        }
        sawRuleSheet = name;
    }

    /**
     * Creates a clone of the specified sheet.
     * @param oldName the name of original sheet
     * @param newName the created sheet name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    private void copy(String oldName, String newName) {
        editor.copy(oldName, newName);
    }
}
