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
package com.asakusafw.testdriver.excel;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.asakusafw.testdriver.excel.ExcelRuleExtractor;
import com.asakusafw.testdriver.excel.NullityConditionKind;
import com.asakusafw.testdriver.excel.ValueConditionKind;
import com.asakusafw.testdriver.rule.DataModelCondition;

/**
 * Default implementation of {@link ExcelRuleExtractor}.
 * @since 0.2.0
 */
public class DefaultExcelRuleExtractor implements ExcelRuleExtractor {

    /**
     * Format ID which this extractor supports.
     * This must be set on the right cell of {@link RuleSheetFormat#FORMAT}.
     */
    public static final String FORMAT = "EVR-1.0.0";

    @Override
    public boolean supports(Sheet sheet) {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null"); //$NON-NLS-1$
        }
        RuleSheetFormat item = RuleSheetFormat.FORMAT;
        String title = getStringCell(sheet, item.getRowIndex(), item.getColumnIndex());
        if (title.equals(item.getTitle()) == false) {
            return false;
        }

        String format = getStringCell(sheet, item.getRowIndex(), item.getColumnIndex() + 1);
        return format != null && format.equals(FORMAT);
    }

    @Override
    public Set<DataModelCondition> extractDataModelCondition(Sheet sheet) throws FormatException {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null"); //$NON-NLS-1$
        }
        RuleSheetFormat item = RuleSheetFormat.TOTAL_CONDITION;
        String text = getStringCell(sheet, item.getRowIndex(), item.getColumnIndex() + 1);
        TotalConditionKind kind = TotalConditionKind.fromOption(text);
        if (kind == null) {
            throw new FormatException(MessageFormat.format(
                    "{0}の値が不正です: \"{1}\" 期待値:{2}",
                    RuleSheetFormat.TOTAL_CONDITION.getTitle(),
                    text,
                    Arrays.asList(TotalConditionKind.getOptions())));
        }
        return kind.getPredicates();
    }

    @Override
    public int extractPropertyRowStartIndex(Sheet sheet) throws FormatException {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null"); //$NON-NLS-1$
        }
        return RuleSheetFormat.PROPERTY_NAME.getRowIndex() + 1;
    }

    private String getStringCell(Sheet sheet, int rowIndex, int colIndex) {
        assert sheet != null;
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return "?";
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null || cell.getCellType() != Cell.CELL_TYPE_STRING) {
            return "?";
        }
        return cell.getStringCellValue();
    }

    @Override
    public String extractName(Row row) throws FormatException {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null"); //$NON-NLS-1$
        }
        // strict checking for cell type
        Cell cell = row.getCell(RuleSheetFormat.PROPERTY_NAME.getColumnIndex());
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
            throw new FormatException(MessageFormat.format(
                    "{0}は文字列で指定してください: ({1}, {2})",
                    RuleSheetFormat.PROPERTY_NAME.getTitle(),
                    cell.getRowIndex() + 1,
                    cell.getColumnIndex() + 1));
        }
        String name = cell.getStringCellValue();
        if (name.isEmpty()) {
            return null;
        }
        return name;
    }

    @Override
    public ValueConditionKind extractValueCondition(Row row) throws FormatException {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null"); //$NON-NLS-1$
        }
        String cell = getStringCell(row, RuleSheetFormat.VALUE_CONDITION);
        ValueConditionKind condition = ValueConditionKind.fromOption(cell);
        if (condition == null) {
            throw new FormatException(MessageFormat.format(
                    "{0}の値が不正です: \"{1}\" (row={2}, col={3}) 期待値:{2}",
                    RuleSheetFormat.VALUE_CONDITION.getTitle(),
                    cell,
                    row.getRowNum() + 1,
                    RuleSheetFormat.VALUE_CONDITION.getColumnIndex() + 1,
                    Arrays.asList(ValueConditionKind.getOptions())));
        }
        return condition;
    }

    @Override
    public NullityConditionKind extractNullityCondition(Row row) throws FormatException {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null"); //$NON-NLS-1$
        }
        String cell = getStringCell(row, RuleSheetFormat.NULLITY_CONDITION);
        NullityConditionKind condition = NullityConditionKind.fromOption(cell);
        if (condition == null) {
            throw new FormatException(MessageFormat.format(
                    "{0}の値が不正です: \"{1}\" (row={2}, col={3}) 期待値:{2}",
                    RuleSheetFormat.NULLITY_CONDITION.getTitle(),
                    cell,
                    row.getRowNum() + 1,
                    RuleSheetFormat.NULLITY_CONDITION.getColumnIndex() + 1,
                    Arrays.asList(NullityConditionKind.getOptions())));
        }
        return condition;
    }

    private String getStringCell(Row row, RuleSheetFormat item) throws FormatException {
        assert row != null;
        assert item != null;
        Cell cell = row.getCell(item.getColumnIndex());
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return "";
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue();
        }
        throw new FormatException(MessageFormat.format(
                "{0}は文字列で指定してください: (row={1}, col={2})",
                item.getTitle(),
                cell.getRowIndex() + 1,
                cell.getColumnIndex() + 1));
    }
}
