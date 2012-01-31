/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
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
 */
public class SheetBuilder {

    static final Logger LOG = LoggerFactory.getLogger(SheetBuilder.class);

    private static final int MINIMUM_COLUMN_WIDTH = 2560;

    private static final int MAX_COLUMN_INDEX = 255;

    private final WorkbookInfo info;

    private final ModelDeclaration model;

    private String sawDataSheet;

    private String sawRuleSheet;

    /**
     * Creates a new instance.
     * @param workbook target workbook to build sheets
     * @param model target model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SheetBuilder(HSSFWorkbook workbook, ModelDeclaration model) {
        if (workbook == null) {
            throw new IllegalArgumentException("workbook must not be null"); //$NON-NLS-1$
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        this.info = new WorkbookInfo(workbook);
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
        HSSFSheet sheet = info.workbook.createSheet(name);
        HSSFRow titleRow = sheet.createRow(0);
        HSSFRow valueRow = sheet.createRow(1);
        int index = 0;
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            if (index > MAX_COLUMN_INDEX) {
                LOG.warn("データシートに追加できるプロパティ数は{}までです: {}", MAX_COLUMN_INDEX, model.getName());
                break;
            }
            HSSFCell title = titleRow.createCell(index);
            title.setCellStyle(info.titleStyle);
            title.setCellValue(property.getName().identifier);

            HSSFCell value = valueRow.createCell(index);
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

    private void adjustDataWidth(HSSFSheet sheet) {
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
        HSSFSheet sheet = info.workbook.createSheet(name);
        fillRuleTitles(sheet);
        fillRuleFormat(sheet);
        fillRuleTotalCondition(sheet);
        fillRulePropertyConditions(sheet);
        adjustRuleWidth(sheet);
        sawRuleSheet = name;
    }

    private void fillRuleTitles(HSSFSheet sheet) {
        assert sheet != null;
        for (RuleSheetFormat title : RuleSheetFormat.values()) {
            setTitle(sheet, title);
        }
    }

    private void fillRuleFormat(HSSFSheet sheet) {
        assert sheet != null;
        HSSFCell value = getCell(sheet, RuleSheetFormat.FORMAT, 0, 1);
        value.setCellStyle(info.lockedStyle);
        value.setCellValue(RuleSheetFormat.FORMAT_VERSION);
    }

    private void fillRuleTotalCondition(HSSFSheet sheet) {
        assert sheet != null;
        HSSFCell value = getCell(sheet, RuleSheetFormat.TOTAL_CONDITION, 0, 1);
        value.setCellStyle(info.optionsStyle);
        String[] options = TotalConditionKind.getOptions();
        value.setCellValue(options[0]);
        setExplicitListConstraint(sheet, options,
                value.getRowIndex(), value.getColumnIndex(),
                value.getRowIndex(), value.getColumnIndex());
    }

    private void setTitle(HSSFSheet sheet, RuleSheetFormat item) {
        assert sheet != null;
        assert item != null;
        HSSFCell cell = getCell(sheet, item.getRowIndex(), item.getColumnIndex());
        cell.setCellStyle(info.titleStyle);
        cell.setCellValue(item.getTitle());
    }

    private void fillRulePropertyConditions(HSSFSheet sheet) {
        int index = 1;
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            HSSFCell name = getCell(sheet, RuleSheetFormat.PROPERTY_NAME, index, 0);
            name.setCellStyle(info.lockedStyle);
            name.setCellValue(property.getName().identifier);

            HSSFCell value = getCell(sheet, RuleSheetFormat.VALUE_CONDITION, index, 0);
            value.setCellStyle(info.optionsStyle);
            if (index == 1) {
                value.setCellValue(ValueConditionKind.KEY.getText());
            } else {
                value.setCellValue(ValueConditionKind.ANY.getText());
            }

            HSSFCell nullity = getCell(sheet, RuleSheetFormat.NULLITY_CONDITION, index, 0);
            nullity.setCellStyle(info.optionsStyle);
            nullity.setCellValue(NullityConditionKind.NORMAL.getText());

            HSSFCell comments = getCell(sheet, RuleSheetFormat.COMMENTS, index, 0);
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

    private void adjustRuleWidth(HSSFSheet sheet) {
        assert sheet != null;
        int lastColumn = 0;
        for (RuleSheetFormat format : RuleSheetFormat.values()) {
            lastColumn = Math.max(lastColumn, format.getColumnIndex());
        }
        adjustColumnWidth(sheet, lastColumn);
    }

    private void adjustColumnWidth(HSSFSheet sheet, int lastColumn) {
        assert sheet != null;
        for (int i = 0; i <= lastColumn; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            if (width < MINIMUM_COLUMN_WIDTH) {
                sheet.setColumnWidth(i, MINIMUM_COLUMN_WIDTH);
            }
        }
    }

    private HSSFCell getCell(HSSFSheet sheet, RuleSheetFormat item, int rowOffset, int columnOffset) {
        assert sheet != null;
        assert item != null;
        return getCell(sheet, item.getRowIndex() + rowOffset, item.getColumnIndex() + columnOffset);
    }

    private HSSFCell getCell(HSSFSheet sheet, int rowIndex, int columnIndex) {
        assert sheet != null;
        HSSFRow row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        HSSFCell cell = row.getCell(columnIndex, Row.CREATE_NULL_AS_BLANK);
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
        HSSFWorkbook workbook = info.workbook;
        int oldIndex = workbook.getSheetIndex(oldName);
        if (oldIndex < 0) {
            throw new IllegalArgumentException();
        }
        HSSFSheet newSheet = workbook.cloneSheet(oldIndex);
        int newIndex = workbook.getSheetIndex(newSheet);
        workbook.setSheetName(newIndex, newName);
    }

    private void setExplicitListConstraint(
            HSSFSheet sheet,
            String[] list,
            int firstRow, int firstCol,
            int lastRow, int lastCol) {
        assert sheet != null;
        assert list != null;
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DVConstraint constraint = DVConstraint.createExplicitListConstraint(list);
        HSSFDataValidation validation = new HSSFDataValidation(addressList, constraint);
        validation.setEmptyCellAllowed(true);
        validation.setSuppressDropDownArrow(false);
        sheet.addValidationData(validation);
    }
}
