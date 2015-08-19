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
package com.asakusafw.testdriver.excel.legacy;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.asakusafw.testdriver.excel.ExcelRuleExtractor;
import com.asakusafw.testdriver.excel.NullityConditionKind;
import com.asakusafw.testdriver.excel.ValueConditionKind;
import com.asakusafw.testdriver.rule.DataModelCondition;

/**
 * Legacy fasion (0.1.x) {@link ExcelRuleExtractor}.
 * @since 0.2.0
 */
public class LegacyExcelRuleExtractor implements ExcelRuleExtractor {

    @Override
    public boolean supports(Sheet sheet) {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null"); //$NON-NLS-1$
        }
        if (getStringCell(sheet, 0, 0) != null) {
            return false;
        }
        ConditionSheetItem item = ConditionSheetItem.TABLE_NAME;
        String cell = getStringCell(sheet, item.getRow(), item.getCol());
        return cell != null && cell.equals(item.getName());
    }

    @Override
    public Set<DataModelCondition> extractDataModelCondition(Sheet sheet) throws FormatException {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null"); //$NON-NLS-1$
        }
        ConditionSheetItem item = ConditionSheetItem.ROW_MATCHING_CONDITION;
        String cell = getStringCell(sheet, item.getRow(), item.getCol() + 1);
        if (cell == null) {
            cell = ""; //$NON-NLS-1$
        }
        RowMatchingCondition condition = RowMatchingCondition.getConditonByJapanseName(cell);
        if (condition == null) {
            throw new FormatException(MessageFormat.format(
                    Messages.getString("LegacyExcelRuleExtractor.errorInvalidRowMatchingCondition"), //$NON-NLS-1$
                    ConditionSheetItem.ROW_MATCHING_CONDITION.getName(),
                    cell,
                    Arrays.asList(RowMatchingCondition.getJapaneseNames())));
        }
        switch (condition) {
        case NONE:
            return EnumSet.allOf(DataModelCondition.class);
        case EXACT:
            return EnumSet.noneOf(DataModelCondition.class);
        case PARTIAL:
            return EnumSet.of(DataModelCondition.IGNORE_UNEXPECTED);
        default:
            throw new AssertionError(condition);
        }
    }

    @Override
    public int extractPropertyRowStartIndex(Sheet sheet) throws FormatException {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null"); //$NON-NLS-1$
        }
        return ConditionSheetItem.COLUMN_NAME.getRow() + 1;
    }

    private String getStringCell(Sheet sheet, int rowIndex, int colIndex) {
        assert sheet != null;
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null || cell.getCellType() != Cell.CELL_TYPE_STRING) {
            return null;
        }
        return cell.getStringCellValue();
    }

    @Override
    public String extractName(Row row) throws FormatException {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null"); //$NON-NLS-1$
        }
        // strict checking for cell type
        Cell cell = row.getCell(ConditionSheetItem.COLUMN_NAME.getCol());
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
            throw new FormatException(MessageFormat.format(
                    Messages.getString("LegacyExcelRuleExtractor.errorInvalidNameType"), //$NON-NLS-1$
                    ConditionSheetItem.COLUMN_NAME.getName(),
                    cell.getRowIndex() + 1,
                    cell.getColumnIndex() + 1));
        }
        String name = cell.getStringCellValue();
        if (name.isEmpty()) {
            return null;
        }
        return name.toLowerCase();
    }

    @Override
    public ValueConditionKind extractValueCondition(Row row) throws FormatException {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null"); //$NON-NLS-1$
        }
        if (isKeyProperty(row)) {
            return ValueConditionKind.KEY;
        }
        String cell = getStringCell(row, ConditionSheetItem.MATCHING_CONDITION);
        ColumnMatchingCondition condition = ColumnMatchingCondition.getConditonByJapanseName(cell);
        if (condition == null) {
            throw new FormatException(MessageFormat.format(
                    Messages.getString("LegacyExcelRuleExtractor.errorInvalidColumnMatchingCondition"), //$NON-NLS-1$
                    ConditionSheetItem.MATCHING_CONDITION.getName(),
                    cell,
                    row.getRowNum() + 1,
                    ConditionSheetItem.MATCHING_CONDITION.getCol() + 1,
                    Arrays.asList(RowMatchingCondition.getJapaneseNames())));
        }
        switch (condition) {
        case EXACT:
            return ValueConditionKind.EQUAL;
        case NONE:
            return ValueConditionKind.ANY;
        case NOW:
            return ValueConditionKind.NOW;
        case PARTIAL:
            return ValueConditionKind.CONTAIN;
        case TODAY:
            return ValueConditionKind.TODAY;
        default:
            throw new AssertionError(condition);
        }
    }

    private boolean isKeyProperty(Row row) throws FormatException {
        assert row != null;
        String cell = getStringCell(row, ConditionSheetItem.KEY_FLAG);
        return cell.isEmpty() == false;
    }

    @Override
    public NullityConditionKind extractNullityCondition(Row row) throws FormatException {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null"); //$NON-NLS-1$
        }
        String cell = getStringCell(row, ConditionSheetItem.NULL_VALUE_CONDITION);
        NullValueCondition condition = NullValueCondition.getConditonByJapanseName(cell);
        if (condition == null) {
            throw new FormatException(MessageFormat.format(
                    Messages.getString("LegacyExcelRuleExtractor.errorInvalidNullValueCondition"), //$NON-NLS-1$
                    ConditionSheetItem.NULL_VALUE_CONDITION.getName(),
                    cell,
                    row.getRowNum() + 1,
                    ConditionSheetItem.NULL_VALUE_CONDITION.getCol() + 1,
                    Arrays.asList(RowMatchingCondition.getJapaneseNames())));
        }
        switch (condition) {
        case NORMAL:
            return NullityConditionKind.NORMAL;
        case NOT_NULL_IS_NG:
            return NullityConditionKind.DENY_PRESENT;
        case NOT_NULL_IS_OK:
            return NullityConditionKind.ACCEPT_PRESENT;
        case NULL_IS_NG:
            return NullityConditionKind.DENY_ABSENT;
        case NULL_IS_OK:
            return NullityConditionKind.ACCEPT_ABSENT;
        default:
            throw new AssertionError(condition);
        }
    }

    @Override
    public String extractComments(Row row) throws FormatException {
        String value = getStringCell(row, ConditionSheetItem.COLUMN_COMMENT);
        return value == null ? "" : value; //$NON-NLS-1$
    }

    @Override
    public String extractOptions(Row row) throws FormatException {
        // no such field
        return ""; //$NON-NLS-1$
    }

    private String getStringCell(Row row, ConditionSheetItem item) throws FormatException {
        assert row != null;
        assert item != null;
        Cell cell = row.getCell(item.getCol());
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return ""; //$NON-NLS-1$
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue();
        }
        throw new FormatException(MessageFormat.format(
                Messages.getString("LegacyExcelRuleExtractor.errorInvalidStringCell"), //$NON-NLS-1$
                item.getName(),
                cell.getRowIndex() + 1,
                cell.getColumnIndex() + 1));
    }
}
