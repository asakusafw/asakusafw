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
package com.asakusafw.testtools.templategen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddressList;

import com.asakusafw.testtools.ColumnInfo;
import com.asakusafw.testtools.ColumnMatchingCondition;
import com.asakusafw.testtools.ConditionSheetItem;
import com.asakusafw.testtools.Constants;
import com.asakusafw.testtools.NullValueCondition;
import com.asakusafw.testtools.RowMatchingCondition;
import com.asakusafw.testtools.db.DbUtils;

/**
 * Excel Bookを構築する。
 */
public class ExcelBookBuilder {

    private static final String CELL_TRUE = "○";

    private static final String CELL_FALSE = "";

    private static final String CELL_EMPTY = "";

    private final Connection conn;
    private final String tableName;
    private final String databaseName;
    private HSSFWorkbook workbook;
    private ColumnInfo[] columnInfos;
    private HSSFCellStyle commonStyle;
    private HSSFCellStyle titleStyle;
    private HSSFCellStyle centerAlignStyle;
    private HSSFCellStyle fixedValueStyle;
    private HSSFCellStyle centerAlignFixedValueStyle;
    private HSSFCellStyle dateTimeStyle;
    private HSSFCellStyle dateStyle;

    /**
     * インスタンスを生成する。
     * @param conn コネクション
     * @param tableName ソースとなるテーブル名
     * @param databaseName ソースとなるデータベース名
     */
    public ExcelBookBuilder(Connection conn, String tableName, String databaseName) {
        this.conn = conn;
        this.tableName = tableName;
        this.databaseName = databaseName;
    }

    /**
     * Excelブックを生成する。
     * @param outputDirectory 出力先のディレクトリ
     * @throws IOException ブックの生成やファイルの書き出しに失敗した場合
     * @throws SQLException テーブル情報の取得に失敗した場合
     */
    public void build(File outputDirectory) throws IOException, SQLException {
        // カラム情報の取得
        columnInfos = DatabaseSchema.collectColumns(conn, databaseName, tableName);

        // ワークブックを生成
        workbook = new HSSFWorkbook();

        // セルスタイルを作成
        configureColumnStyle();

        // 入力データのシートと出力データのシートを生成
        HSSFSheet inputSheet = createInputDataSheet(Constants.INPUT_DATA_SHEET_NAME);
        int inputSheetIndex = workbook.getSheetIndex(inputSheet);
        HSSFSheet outputSheet = workbook.cloneSheet(inputSheetIndex);
        int outputSheetIndex = workbook.getSheetIndex(outputSheet);
        workbook.setSheetName(outputSheetIndex, Constants.OUTPUT_DATA_SHEET_NAME);

        // テスト条件のシートを生成
        createTestConditionSheet(Constants.TEST_CONDITION_SHEET_NAME);

        // ファイルの生成
        String bookName = tableName + ".xls";
        File outputFile = new File(outputDirectory, bookName);
        OutputStream os = new FileOutputStream(outputFile);
        try {
            workbook.write(os);
        } finally {
            DbUtils.closeQuietly(os);
        }
    }

    private void configureColumnStyle() {
        assert workbook != null;
        HSSFFont font = workbook.createFont();
        font.setFontName("ＭＳ ゴシック");

        commonStyle = workbook.createCellStyle();
        commonStyle.setFont(font);
        commonStyle.setBorderTop(CellStyle.BORDER_THIN);
        commonStyle.setBorderBottom(CellStyle.BORDER_THIN);
        commonStyle.setBorderLeft(CellStyle.BORDER_THIN);
        commonStyle.setBorderRight(CellStyle.BORDER_THIN);

        titleStyle = workbook.createCellStyle();
        titleStyle.cloneStyleFrom(commonStyle);
        titleStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        titleStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        titleStyle.setAlignment(CellStyle.ALIGN_CENTER);

        centerAlignStyle = workbook.createCellStyle();
        centerAlignStyle.cloneStyleFrom(commonStyle);
        centerAlignStyle.setAlignment(CellStyle.ALIGN_CENTER);

        fixedValueStyle = workbook.createCellStyle();
        fixedValueStyle.cloneStyleFrom(commonStyle);
        fixedValueStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        fixedValueStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());

        centerAlignFixedValueStyle = workbook.createCellStyle();
        centerAlignFixedValueStyle.cloneStyleFrom(fixedValueStyle);
        centerAlignFixedValueStyle.setAlignment(CellStyle.ALIGN_CENTER);

        CreationHelper helper = workbook.getCreationHelper();
        DataFormat df = helper.createDataFormat();

        dateTimeStyle = workbook.createCellStyle();
        dateTimeStyle.cloneStyleFrom(commonStyle);
        dateTimeStyle.setDataFormat(df.getFormat("yyyy-mm-dd hh:mm:ss"));

        dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(commonStyle);
        dateStyle.setDataFormat(df.getFormat("yyyy-mm-dd"));
    }

    private HSSFCell getCell(HSSFSheet sheet, int rownum, int col) {
        HSSFRow row = sheet.getRow(rownum);
        if (row == null) {
            row = sheet.createRow(rownum);
        }
        HSSFCell cell = row.getCell(col);
        if (cell == null) {
            cell = row.createCell(col);
        }
        cell.setCellStyle(commonStyle);
        return cell;
    }

    private HSSFSheet createTestConditionSheet(String sheetName) {
        // タイトルのセルを作成, 同時にカラム位置の最大値を取得
        int maxColumn = 0;
        HSSFSheet sheet = workbook.createSheet(sheetName);
        for (ConditionSheetItem item : ConditionSheetItem.values()) {
            HSSFCell cell = getCell(sheet, item.getRow(), item.getCol());
            cell.setCellValue(item.getName());
            cell.setCellStyle(titleStyle);
            if (maxColumn < item.getCol()) {
                maxColumn = item.getCol();
            }
        }

        // テーブル名とテーブルのマッチング条件を設定
        HSSFCell tableNameCell = getCell(sheet, ConditionSheetItem.TABLE_NAME
                .getRow(), ConditionSheetItem.TABLE_NAME.getCol() + 1);
        tableNameCell.setCellStyle(fixedValueStyle);
        tableNameCell.setCellValue(tableName);

        HSSFCell rowMatichingConditionCell = getCell(sheet,
                ConditionSheetItem.ROW_MATCHING_CONDITION.getRow(),
                ConditionSheetItem.ROW_MATCHING_CONDITION.getCol() + 1);
        rowMatichingConditionCell.setCellValue(RowMatchingCondition.NONE.getJapaneseName());

        // 各カラムの情報を設定
        int startRow = ConditionSheetItem.NO.getRow();
        int endRow = configureColumns(sheet, startRow);

        // 入力規則を設定
        setExplicitListConstraint(sheet,
                RowMatchingCondition.getJapaneseNames(),
                ConditionSheetItem.ROW_MATCHING_CONDITION.getRow(),
                ConditionSheetItem.ROW_MATCHING_CONDITION.getRow(),
                ConditionSheetItem.ROW_MATCHING_CONDITION.getCol() + 1,
                ConditionSheetItem.ROW_MATCHING_CONDITION.getCol() + 1);

        setExplicitListConstraint(sheet,
                ColumnMatchingCondition.getJapaneseNames(),
                startRow + 1,
                endRow,
                ConditionSheetItem.MATCHING_CONDITION.getCol(),
                ConditionSheetItem.MATCHING_CONDITION.getCol());

        setExplicitListConstraint(sheet,
                NullValueCondition.getJapaneseNames(),
                startRow + 1,
                endRow,
                ConditionSheetItem.NULL_VALUE_CONDITION.getCol(),
                ConditionSheetItem.NULL_VALUE_CONDITION.getCol());


        // カラム幅の調整
        for (int i = 0; i <= maxColumn + 1; i++) {
            sheet.autoSizeColumn(i);
        }
        return sheet;
    }

    private int configureColumns(HSSFSheet sheet, int startRow) {
        assert columnInfos != null;
        int row = startRow;
        int no = 0;
        for (ColumnInfo info : columnInfos) {
            row++;
            no++;

            HSSFCell noCell = getCell(sheet, row, ConditionSheetItem.NO.getCol());
            noCell.setCellStyle(centerAlignFixedValueStyle);
            noCell.setCellValue(no);

            HSSFCell columnNameCell = getCell(sheet, row, ConditionSheetItem.COLUMN_NAME.getCol());
            columnNameCell.setCellStyle(fixedValueStyle);
            columnNameCell.setCellValue(info.getColumnName());

            HSSFCell columnCommentCell = getCell(sheet, row, ConditionSheetItem.COLUMN_COMMENT.getCol());
            columnCommentCell.setCellStyle(fixedValueStyle);
            columnCommentCell.setCellValue(info.getColumnComment());

            HSSFCell dataTypeCell = getCell(sheet, row, ConditionSheetItem.DATA_TYPE.getCol());
            dataTypeCell.setCellStyle(centerAlignFixedValueStyle);
            dataTypeCell.setCellValue(info.getDataType().getDataTypeString());

            HSSFCell widthCell = getCell(sheet, row, ConditionSheetItem.WIDTH.getCol());
            widthCell.setCellStyle(centerAlignFixedValueStyle);
            switch (info.getDataType()) {
            case CHAR:
            case VARCHAR:
                widthCell.setCellValue(info.getCharacterMaximumLength());
                break;
            case DECIMAL:
                widthCell.setCellValue(info.getNumericPrecision());
                break;
            case DATE:
            case DATETIME:
            case INT:
            case LONG:
            case SMALL_INT:
            case TIMESTAMP:
            case TINY_INT:
                widthCell.setCellValue(CELL_EMPTY);
                break;
            default:
                throw new RuntimeException(MessageFormat.format(
                        "Unkonwn data type: {0}",
                        info.getDataType().name()));
            }

            HSSFCell scaleCell = getCell(sheet, row, ConditionSheetItem.SCALE.getCol());
            scaleCell.setCellStyle(centerAlignFixedValueStyle);
            switch (info.getDataType()) {
            case DECIMAL:
                scaleCell.setCellValue(info.getNumericScale());
                break;
            case CHAR:
            case DATE:
            case DATETIME:
            case INT:
            case LONG:
            case SMALL_INT:
            case TIMESTAMP:
            case TINY_INT:
            case VARCHAR:
                scaleCell.setCellValue(CELL_EMPTY);
                break;
            default:
                throw new RuntimeException(MessageFormat.format(
                        "Unkonwn data type: {0}",
                        info.getDataType().name()));
            }

            HSSFCell nullableCell = getCell(sheet, row, ConditionSheetItem.NULLABLE.getCol());
            nullableCell.setCellStyle(centerAlignFixedValueStyle);
            if (info.isNullable()) {
                nullableCell.setCellValue(CELL_TRUE);
            } else {
                nullableCell.setCellValue(CELL_FALSE);
            }

            HSSFCell pkCell = getCell(sheet, row, ConditionSheetItem.KEY_FLAG.getCol());
            pkCell.setCellStyle(centerAlignStyle);
            if (info.isKey()) {
                pkCell.setCellValue(CELL_TRUE);
            } else {
                pkCell.setCellValue(CELL_FALSE);
            }

            HSSFCell machingCondtionCell = getCell(sheet, row, ConditionSheetItem.MATCHING_CONDITION.getCol());
            machingCondtionCell.setCellStyle(centerAlignStyle);
            machingCondtionCell.setCellValue(ColumnMatchingCondition.NONE.getJapaneseName());

            HSSFCell nullValueConditionCell = getCell(sheet, row, ConditionSheetItem.NULL_VALUE_CONDITION.getCol());
            nullValueConditionCell.setCellStyle(centerAlignStyle);
            nullValueConditionCell.setCellValue(NullValueCondition.NORMAL.getJapaneseName());

        }
        int endRow = row;
        return endRow;
    }

    private void setExplicitListConstraint(
            HSSFSheet sheet,
            String[] list,
            int firstRow,
            int lastRow,
            int firstCol,
            int lastCol) {
        //データの入力規則を設定するセルを設定する
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DVConstraint constraint = DVConstraint.createExplicitListConstraint(list);
        HSSFDataValidation validation = new HSSFDataValidation(addressList, constraint);
        validation.setEmptyCellAllowed(true);
        validation.setSuppressDropDownArrow(false);
        sheet.addValidationData(validation);
    }

    /**
     * 入力データのシートを生成する。
     * @param sheetName 入力データのシート名
     * @return 生成したシート
     * @throws SQLException データベースから入力データを取得するのに失敗した場合
     */
    private HSSFSheet createInputDataSheet(String sheetName) throws SQLException {
        HSSFSheet sheet = workbook.createSheet(sheetName);

        // カラム名を設定
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < columnInfos.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(columnInfos[i].getColumnName());
            cell.setCellStyle(titleStyle);
        }

        // DBのデータを設定
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql =
            "SELECT * FROM "
            + databaseName + "." + tableName
            + " limit 0, " + Constants.MAX_ROWS;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                row = sheet.createRow(row.getRowNum() + 1);
                for (int i = 0; i < columnInfos.length; i++) {
                    ColumnInfo info = columnInfos[i];
                    HSSFCell cell = row.createCell(i);
                    cell.setCellStyle(commonStyle);
                    switch (info.getDataType()) {
                    case CHAR:
                    case VARCHAR:
                        String str = rs.getString(info.getColumnName());
                        if (!rs.wasNull()) {
                            cell.setCellValue(str);
                        }
                        break;
                    case DATE:
                        Date date = rs.getDate(info.getColumnName());
                        if (!rs.wasNull()) {
                            cell.setCellValue(new java.util.Date(date.getTime()));
                            cell.setCellStyle(dateStyle);
                        }
                        break;
                    case DATETIME:
                    case TIMESTAMP:
                        Timestamp ts = rs.getTimestamp(info.getColumnName());
                        if (!rs.wasNull()) {
                            cell.setCellValue(new java.util.Date(ts.getTime()));
                            cell.setCellStyle(dateTimeStyle);
                        }
                        break;
                    case DECIMAL:
                        BigDecimal decimal = rs.getBigDecimal(info.getColumnName());
                        if (!rs.wasNull()) {
                            cell.setCellValue(decimal.toPlainString());
                        }
                        break;
                    case TINY_INT:
                    case SMALL_INT:
                    case INT:
                    case LONG:
                        long value = rs.getLong(info.getColumnName());
                        if (!rs.wasNull()) {
                            cell.setCellValue(Long.toString(value));
                        }
                        break;
                    default:
                        assert false;
                        break;
                    }
                }
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // カラム幅の調整
        for (int i = 0; i < columnInfos.length; i++) {
            sheet.autoSizeColumn(i);
        }
        return sheet;
    }
}
