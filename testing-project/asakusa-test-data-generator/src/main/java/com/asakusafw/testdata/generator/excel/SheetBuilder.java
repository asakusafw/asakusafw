/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.testdriver.excel.NullityConditionKind;
import com.asakusafw.testdriver.excel.RuleSheetFormat;
import com.asakusafw.testdriver.excel.TotalConditionKind;
import com.asakusafw.testdriver.excel.ValueConditionKind;

/**
 * Appends test data/rule sheets to the workbook.
 * @since 0.2.0
 * @version 0.5.3
 */
public class SheetBuilder {

    static final Logger LOG = LoggerFactory.getLogger(SheetBuilder.class);

    private static final int MINIMUM_COLUMN_WIDTH = 2560;

    private final WorkbookInfo info;

    private final ModelDeclaration model;

    private final SpreadsheetVersion version;

    private String sawDataSheet;

    private String sawRuleSheet;

    /**
     * Creates a new instance.
     * @param workbook target workbook to build sheets
     * @param version the spreadsheet version of the target workbook
     * @param model target model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SheetBuilder(Workbook workbook, SpreadsheetVersion version, ModelDeclaration model) {
        if (workbook == null) {
            throw new IllegalArgumentException("workbook must not be null"); //$NON-NLS-1$
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        this.info = new WorkbookInfo(workbook);
        this.version = version;
        this.model = model;
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
            return;
        }
        Sheet sheet = info.workbook.createSheet(name);
        Row titleRow = sheet.createRow(0);
        Row valueRow = sheet.createRow(1);
        int index = 0;
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            if (index >= version.getMaxColumns()) {
                LOG.warn("データシートに追加できるプロパティ数は{}までです: {}", version.getMaxColumns(), model.getName());
                break;
            }
            Cell title = titleRow.createCell(index);
            title.setCellStyle(info.titleStyle);
            title.setCellValue(property.getName().identifier);

            Cell value = valueRow.createCell(index);
            value.setCellStyle(info.dataStyle);
            if (property.getType() instanceof BasicType) {
                BasicType type = (BasicType) property.getType();
                switch (type.getKind()) {
                case DATE:
                    value.setCellStyle(info.dateDataStyle);
                    break;
                case DATETIME:
                    value.setCellStyle(info.datetimeDataStyle);
                    break;
                default:
                    break;
                }
            }
            index++;
        }
        adjustDataWidth(sheet);
        sawDataSheet = name;
    }

    private void adjustDataWidth(Sheet sheet) {
        assert sheet != null;
        int lastColumn = sheet.getRow(0).getLastCellNum();
        adjustColumnWidth(sheet, lastColumn);
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
            return;
        }
        Sheet sheet = info.workbook.createSheet(name);
        fillRuleTitles(sheet);
        fillRuleFormat(sheet);
        fillRuleTotalCondition(sheet);
        fillRulePropertyConditions(sheet);
        adjustRuleWidth(sheet);
        sawRuleSheet = name;
    }

    private void fillRuleTitles(Sheet sheet) {
        assert sheet != null;
        for (RuleSheetFormat title : RuleSheetFormat.values()) {
            setTitle(sheet, title);
        }
    }

    private void fillRuleFormat(Sheet sheet) {
        assert sheet != null;
        Cell value = getCell(sheet, RuleSheetFormat.FORMAT, 0, 1);
        value.setCellStyle(info.lockedStyle);
        value.setCellValue(RuleSheetFormat.FORMAT_VERSION);
    }

    private void fillRuleTotalCondition(Sheet sheet) {
        assert sheet != null;
        Cell value = getCell(sheet, RuleSheetFormat.TOTAL_CONDITION, 0, 1);
        value.setCellStyle(info.optionsStyle);
        String[] options = TotalConditionKind.getOptions();
        value.setCellValue(options[0]);
        setExplicitListConstraint(sheet, options,
                value.getRowIndex(), value.getColumnIndex(),
                value.getRowIndex(), value.getColumnIndex());
    }

    private void setTitle(Sheet sheet, RuleSheetFormat item) {
        assert sheet != null;
        assert item != null;
        Cell cell = getCell(sheet, item.getRowIndex(), item.getColumnIndex());
        cell.setCellStyle(info.titleStyle);
        cell.setCellValue(item.getTitle());
    }

    private void fillRulePropertyConditions(Sheet sheet) {
        int index = 1;
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            Cell name = getCell(sheet, RuleSheetFormat.PROPERTY_NAME, index, 0);
            name.setCellStyle(info.lockedStyle);
            name.setCellValue(property.getName().identifier);

            Cell value = getCell(sheet, RuleSheetFormat.VALUE_CONDITION, index, 0);
            value.setCellStyle(info.optionsStyle);
            if (index == 1) {
                value.setCellValue(ValueConditionKind.KEY.getText());
            } else {
                value.setCellValue(ValueConditionKind.ANY.getText());
            }

            Cell nullity = getCell(sheet, RuleSheetFormat.NULLITY_CONDITION, index, 0);
            nullity.setCellStyle(info.optionsStyle);
            nullity.setCellValue(NullityConditionKind.NORMAL.getText());

            Cell comments = getCell(sheet, RuleSheetFormat.COMMENTS, index, 0);
            comments.setCellStyle(info.dataStyle);
            comments.setCellValue(property.getDescription() == null
                    ? property.getType().toString() : property.getDescription().getText());

            index++;
        }

        int start = RuleSheetFormat.PROPERTY_NAME.getRowIndex() + 1;
        int end = RuleSheetFormat.PROPERTY_NAME.getRowIndex() + index;
        setExplicitListConstraint(sheet,
                ValueConditionKind.getOptions(),
                start, RuleSheetFormat.VALUE_CONDITION.getColumnIndex(),
                end, RuleSheetFormat.VALUE_CONDITION.getColumnIndex());
        setExplicitListConstraint(sheet,
                NullityConditionKind.getOptions(),
                start, RuleSheetFormat.NULLITY_CONDITION.getColumnIndex(),
                end, RuleSheetFormat.NULLITY_CONDITION.getColumnIndex());
    }

    private void adjustRuleWidth(Sheet sheet) {
        assert sheet != null;
        int lastColumn = 0;
        for (RuleSheetFormat format : RuleSheetFormat.values()) {
            lastColumn = Math.max(lastColumn, format.getColumnIndex());
        }
        adjustColumnWidth(sheet, lastColumn);
    }

    private void adjustColumnWidth(Sheet sheet, int lastColumn) {
        assert sheet != null;
        for (int i = 0; i <= lastColumn; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            if (width < MINIMUM_COLUMN_WIDTH) {
                sheet.setColumnWidth(i, MINIMUM_COLUMN_WIDTH);
            }
        }
    }

    private Cell getCell(Sheet sheet, RuleSheetFormat item, int rowOffset, int columnOffset) {
        assert sheet != null;
        assert item != null;
        return getCell(sheet, item.getRowIndex() + rowOffset, item.getColumnIndex() + columnOffset);
    }

    private Cell getCell(Sheet sheet, int rowIndex, int columnIndex) {
        assert sheet != null;
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(columnIndex, Row.CREATE_NULL_AS_BLANK);
        return cell;
    }

    /**
     * Creates a clone of the specified sheet.
     * @param oldName the name of original sheet
     * @param newName the created sheet name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    private void copy(String oldName, String newName) {
        if (oldName == null) {
            throw new IllegalArgumentException("oldName must not be null"); //$NON-NLS-1$
        }
        if (newName == null) {
            throw new IllegalArgumentException("newName must not be null"); //$NON-NLS-1$
        }
        Workbook workbook = info.workbook;
        int oldIndex = workbook.getSheetIndex(oldName);
        if (oldIndex < 0) {
            throw new IllegalArgumentException();
        }
        Sheet newSheet = workbook.cloneSheet(oldIndex);
        int newIndex = workbook.getSheetIndex(newSheet);
        workbook.setSheetName(newIndex, newName);
    }

    private void setExplicitListConstraint(
            Sheet sheet,
            String[] list,
            int firstRow, int firstCol,
            int lastRow, int lastCol) {
        assert sheet != null;
        assert list != null;
        DataValidationHelper helper = sheet.getDataValidationHelper();
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DataValidationConstraint constraint = helper.createExplicitListConstraint(list);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setEmptyCellAllowed(true);
        sheet.addValidationData(validation);
    }
}
