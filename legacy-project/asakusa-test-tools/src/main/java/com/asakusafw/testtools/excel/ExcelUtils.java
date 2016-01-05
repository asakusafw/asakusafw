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
package com.asakusafw.testtools.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.io.Writable;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import com.asakusafw.modelgen.emitter.JavaName;
import com.asakusafw.modelgen.source.MySqlDataType;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.testtools.ColumnInfo;
import com.asakusafw.testtools.ColumnMatchingCondition;
import com.asakusafw.testtools.ConditionSheetItem;
import com.asakusafw.testtools.Configuration;
import com.asakusafw.testtools.Constants;
import com.asakusafw.testtools.NullValueCondition;
import com.asakusafw.testtools.RowMatchingCondition;
import com.asakusafw.testtools.TestDataHolder;

/**
 * Excelに関するユーティリティ群。
 * @author shinichi.umegane
 */
public class ExcelUtils {

    // TODO MessageFormat.formatの検討

    // TODO 行番号、列番号に関する起算の明記

    /**
     * Excelの数値のセルをlongに変換する場合の最大値。
     */
    public static final long EXCEL_MAX_LONG = 999999999999999L;

    /**
     * Excelの数値のセルをlongに変換する場合の最小値。
     */
    public static final long EXCEL_MIN_LONG = -999999999999999L;

    private final String filename;
    private final String tablename;
    private final RowMatchingCondition rowMatchingCondition;
    private final HSSFWorkbook workbook;
    private final HSSFSheet inputDataSheet;
    private final HSSFSheet outputDataSheet;
    private final HSSFSheet testConditionSheet;

    /**
     * 日付型のフォーマッタ。
     */
    private final DateFormat dateFormat
        = new SimpleDateFormat(com.asakusafw.runtime.value.Date.FORMAT);

    /**
     * 日次型のフォーマッタ。
     */
    private final DateFormat dateTimeFormat
           = new SimpleDateFormat(DateTime.FORMAT);

    /**
     * カラム情報のリスト。
     */
    private final List<ColumnInfo> columnInfos;


    /**
     * 指定のExcelファイルを利用してインスタンスを生成する。
     * @param filename Excelファイルのファイル名
     * @throws IOException Excelファイルの読み出しに失敗した場合
     */
    public ExcelUtils(String filename) throws IOException {
        this.filename = filename;
        InputStream is = new FileInputStream(filename);
        workbook = new HSSFWorkbook(is);
        inputDataSheet = workbook.getSheet(Constants.INPUT_DATA_SHEET_NAME);
        if (inputDataSheet == null) {
            throw new IOException("Excelファイル: " + filename + "に入力データのシートが存在しません");
        }
        outputDataSheet = workbook.getSheet(Constants.OUTPUT_DATA_SHEET_NAME);
        if (outputDataSheet == null) {
            throw new IOException("Excelファイル: " + filename + "に出力データのシートが存在しません");
        }
        testConditionSheet = workbook.getSheet(Constants.TEST_CONDITION_SHEET_NAME);
        if (testConditionSheet == null) {
            throw new IOException("Excelファイル: " + filename + "にテスト条件データのシートが存在しません");
        }

        HSSFCell tableNameCell = getCell(testConditionSheet,
                ConditionSheetItem.TABLE_NAME.getRow(),
                ConditionSheetItem.TABLE_NAME.getCol() + 1);
        tablename = tableNameCell.getStringCellValue();
        if (tablename == null || tablename.length() == 0) {
            throw new IOException("Excelファイル: " + filename + "にテーブル名が定義されていません");
        }

        HSSFCell rowMatchingConditionCell = getCell(testConditionSheet,
                ConditionSheetItem.ROW_MATCHING_CONDITION.getRow(),
                ConditionSheetItem.ROW_MATCHING_CONDITION.getCol() + 1);
        String rowMatchingConditionStr = rowMatchingConditionCell.getStringCellValue();
        if (rowMatchingConditionStr == null || rowMatchingConditionStr.length() == 0) {
            throw new IOException("Excelファイル: " + filename + "にテーブルの比較条件が定義されていません");
        }
        rowMatchingCondition = RowMatchingCondition.getConditonByJapanseName(rowMatchingConditionStr);
        if (rowMatchingCondition == null) {
            throw new IOException("Excelファイル: " + filename + "のテーブルの比較条件に不正な値が設定されています");
        }
        columnInfos = createColumnInfos();
    }

    /**
     * シート、行、カラム位置を指定してセルを取得する。
     * @param sheet 対象のシート
     * @param row 対象の行
     * @param col 対象のカラム番号
     * @return 対応するセルの内容
     */
    private HSSFCell getCell(HSSFSheet sheet, HSSFRow row, int col) {
        HSSFCell cell = row.getCell(col);
        if (cell == null) {
            String fmt = "Excelファイルが異常です(空セル), file = %s, sheet = %s, row = %d, col = %d";
            String msg = String.format(fmt, filename, sheet.getSheetName(), row.getRowNum() + 1, col + 1);
            throw new InvalidExcelBookException(msg);
        }
        return cell;
    }

    /**
     * シート、行位置、カラム位置を指定してセルを取得する。
     * @param sheet 対象のシート
     * @param rownum 対象の行番号
     * @param col 対象のカラム番号
     * @return 対応するセルの内容
     */
    private HSSFCell getCell(HSSFSheet sheet, int rownum, int col) {
        HSSFRow row = sheet.getRow(rownum);
        if (isEmpty(row)) {
            String fmt = "Excelファイルが異常です(空行), file = %s, sheet = %s, row = %d";
            String msg = String.format(fmt, filename, sheet.getSheetName(), rownum);
            throw new InvalidExcelBookException(msg);
        }
        HSSFCell cell = getCell(sheet, row, col);
        return cell;
    }

    /**
     * テスト条件のシートの指定の行から、指定の項目のセルを取り出す。
     * @param item テスト条件の項目
     * @param row 対象の行
     * @return 対応するセルの内容
     */
    private HSSFCell getCell(ConditionSheetItem item, HSSFRow row) {
        int col = item.getCol();
        HSSFCell cell = getCell(testConditionSheet, row, col);
        return cell;
    }

    /**
     * テスト条件のシートの指定の行から、指定の項目のセルの値(文字列)を取り出す。
     * @param sheet 対象のシート
     * @param item テスト条件の項目
     * @param row 対象の行
     * @return 対応するセルの内容
     */
    private String getStringCellValue(HSSFSheet sheet, ConditionSheetItem item, HSSFRow row) {
        HSSFCell cell = getCell(item, row);
        String ret;
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            double dval = cell.getNumericCellValue();
            ret = Double.toString(dval);
            ret = ret.replaceAll("\\.0*$", "");
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            ret = "";
        } else if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
            String fmt = "Excelファイルが異常です。文字列のセルに文字列以外の値が設定されています。 file = %s, sheet = %s, row = %d, col = %d";
            int rownum = row.getRowNum() + 1;
            int col = item.getCol() + 1;
            String msg = String.format(fmt, filename, sheet.getSheetName(), rownum, col);
            throw new InvalidExcelBookException(msg);
        } else {
            ret = cell.getStringCellValue();
        }
        return ret;
    }

    private Double getDubleCellValue(HSSFSheet sheet, ConditionSheetItem item, HSSFRow row) {
        HSSFCell cell = getCell(item, row);
        Double ret;
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            String str = cell.getStringCellValue();
            if (str == null || str.length() == 0) {
                ret = null;
            } else {
                try {
                    ret = Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    String fmt = "Excelファイルが異常です。数値のセルに数値以外の値が設定されています。 file = %s, sheet = %s, row = %d, col = %d";
                    int rownum = row.getRowNum() + 1;
                    int col = item.getCol() + 1;
                    String msg = String.format(fmt, filename, sheet.getSheetName(), rownum, col);
                    throw new InvalidExcelBookException(msg);
                }
            }
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            ret = null;
        } else if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
            String fmt = "Excelファイルが異常です。数値のセルに数値以外の値が設定されています。 file = %s, sheet = %s, row = %d, col = %d";
            int rownum = row.getRowNum() + 1;
            int col = item.getCol() + 1;
            String msg = String.format(fmt, filename, sheet.getSheetName(), rownum, col);
            throw new InvalidExcelBookException(msg);
        } else {
            ret =  cell.getNumericCellValue();
        }
        return ret;
    }

    /**
     * テスト条件が不正だったときのメッセージを組み立てる。
     * @param item 不正だったテスト条件の項目
     * @param row 対象の行
     * @return 不正の内容を表すメッセージ
     */
    private String creaetExceptionMessage(ConditionSheetItem item, HSSFRow row) {
        String fmt = "Excelファイルが異常です。テスト条件のシートの定義が不正です。 file = %s, 行 = %d, 項目 = %s";
        String msg = String.format(fmt, filename, row.getRowNum() + 1, item.getName());
        return msg;
    }

    /**
     * テスト条件のシートのカラム情報を返す。
     * @return カラム一覧
     * @throws IOException カラム情報の取得に失敗した場合
     */
    private List<ColumnInfo> createColumnInfos() throws IOException {
        List<ColumnInfo> list = new ArrayList<ColumnInfo>();

        int rownum = ConditionSheetItem.NO.getRow();
        for (;;) {
            rownum++;
            HSSFRow row = testConditionSheet.getRow(rownum);
            if (isEmpty(row)) {
                break;
            }

            // カラム名
            String columnName = getStringCellValue(testConditionSheet, ConditionSheetItem.COLUMN_NAME, row);
            if (columnName.length() == 0) {
                String msg = creaetExceptionMessage(ConditionSheetItem.COLUMN_NAME, row);
                throw new InvalidExcelBookException(msg);
            }

            // カラムコメント
            String columnComment = getStringCellValue(testConditionSheet, ConditionSheetItem.COLUMN_COMMENT, row);

            // データ型
            String dataTypeStr = getStringCellValue(testConditionSheet, ConditionSheetItem.DATA_TYPE, row);
            MySqlDataType dataType = MySqlDataType.getDataTypeByString(dataTypeStr);
            if (dataType == null) {
                String msg = creaetExceptionMessage(ConditionSheetItem.DATA_TYPE, row);
                throw new InvalidExcelBookException(msg);
            }

            // 文字列長、桁数、精度
            Double dWidth = getDubleCellValue(testConditionSheet, ConditionSheetItem.WIDTH, row);
            Double dScale = getDubleCellValue(testConditionSheet, ConditionSheetItem.SCALE, row);

            long characterMaximumLength = 0;
            int numericPrecision = 0;
            int numericScale = 0;
            switch(dataType) {
            case CHAR:
            case VARCHAR:
                if (dWidth == null) {
                    String msg = creaetExceptionMessage(ConditionSheetItem.WIDTH, row);
                    throw new InvalidExcelBookException(msg);
                }
                characterMaximumLength = dWidth.longValue();
                break;
            case DECIMAL:
                if (dWidth == null) {
                    String msg = creaetExceptionMessage(ConditionSheetItem.WIDTH, row);
                    throw new InvalidExcelBookException(msg);
                }
                numericPrecision = dWidth.intValue();
                if (dScale == null) {
                    String msg = creaetExceptionMessage(ConditionSheetItem.SCALE, row);
                    throw new InvalidExcelBookException(msg);
                }
                numericScale = dScale.intValue();
                break;
            default:
                // その他のデータ型に対しては文字列長、桁数、精度、を指定しない
                break;
            }

            // キー項目フラグ
            String keyStr = getStringCellValue(testConditionSheet, ConditionSheetItem.KEY_FLAG, row);
            boolean key = true;
            if (keyStr.trim().length() == 0) {
                key = false;
            }
            // NULL可
            String nullableStr = getStringCellValue(testConditionSheet, ConditionSheetItem.NULLABLE, row);
            boolean nullable = true;
            if (nullableStr.trim().length() == 0) {
                nullable = false;
            }

            // テスト条件
            String columnMatchingConditionStr = getStringCellValue(
                    testConditionSheet, ConditionSheetItem.MATCHING_CONDITION, row);
            ColumnMatchingCondition columnMatchingCondition
                = ColumnMatchingCondition.getConditonByJapanseName(columnMatchingConditionStr);
            if (columnMatchingCondition == null) {
                String msg = creaetExceptionMessage(ConditionSheetItem.MATCHING_CONDITION, row);
                throw new InvalidExcelBookException(msg);
            }

            // NULL値の扱い
            String nullValueConditionStr = getStringCellValue(
                    testConditionSheet, ConditionSheetItem.NULL_VALUE_CONDITION, row);
            NullValueCondition nullValueCondition
                = NullValueCondition.getConditonByJapanseName(nullValueConditionStr);
            if (nullValueCondition == null) {
                String msg = creaetExceptionMessage(ConditionSheetItem.NULL_VALUE_CONDITION, row);
                throw new InvalidExcelBookException(msg);
            }

            ColumnInfo info = new ColumnInfo(tablename, columnName,
                    columnComment, dataType, characterMaximumLength,
                    numericPrecision, numericScale, nullable, key,
                    columnMatchingCondition, nullValueCondition);

            list.add(info);
        }
        return list;
    }


    /**
     * このシートに対応するモデルクラスを返す。
     * @return 対応するモデルクラス
     */
    public Class<? extends Writable> getModelClass() {
        Configuration conf = Configuration.getInstance();
        String pkgName = conf.getModelPackage();
        String simpleName = JavaName.of(tablename).toTypeName();
        Class<? extends Writable> cl;
        cl = findModelClass(pkgName, null, simpleName);
        if (cl != null) {
            return cl;
        }
        cl = findModelClass(pkgName, com.asakusafw.modelgen.Constants.SOURCE_TABLE, simpleName);
        if (cl != null) {
            return cl;
        }
        cl = findModelClass(pkgName, com.asakusafw.modelgen.Constants.SOURCE_VIEW, simpleName);
        if (cl != null) {
            return cl;
        }
        throw new RuntimeException(new ClassNotFoundException(
                buildModelClassName(pkgName, null, simpleName)));
    }

    private Class<? extends Writable> findModelClass(
            String pkgName,
            String sourceOrNull,
            String simpleName) {
        assert pkgName != null;
        assert simpleName != null;
        String qualifiedName = buildModelClassName(pkgName, sourceOrNull, simpleName);
        Class<? extends Writable> cl;
        try {
            cl = Class.forName(qualifiedName).asSubclass(Writable.class);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return cl;
    }

    private String buildModelClassName(
            String pkgName,
            String sourceOrNull,
            String simpleName) {
        assert pkgName != null;
        assert simpleName != null;
        StringBuilder modelClassName = new StringBuilder();
        modelClassName.append(pkgName);
        modelClassName.append('.');
        if (sourceOrNull != null) {
            modelClassName.append(sourceOrNull);
            modelClassName.append('.');
        }
        modelClassName.append(com.asakusafw.modelgen.Constants.CATEGORY_MODEL);
        modelClassName.append('.');
        modelClassName.append(simpleName);
        String qualifiedName = modelClassName.toString();
        return qualifiedName;
    }

    /**
     * 指定のシートの情報からモデルオブジェクトのリストを作成する。
     * @param sheet 入力データのシートまたは出力データのシート
     * @return モデルオブジェクトのリスト
     */
    private List<Writable> createDatalList(HSSFSheet sheet) {
        List<Writable> list = new ArrayList<Writable>();

        Class<?> modelClass = getModelClass();

        int rownum = 0; // 0行目はコメント行、最初にインクリメントされて
                        // 1行目から処理する
        for (;;) {
            rownum++;
            HSSFRow row = sheet.getRow(rownum);
            if (isEmpty(row)) {
                break;
            }
            Writable model;
            try {
                model = (Writable) modelClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            for (int col = 0; col < columnInfos.size(); col++) {
                HSSFCell cell = row.getCell(col, Row.CREATE_NULL_AS_BLANK);
                MySqlDataType type = columnInfos.get(col).getDataType();
                ValueOption<?> vo;
                switch (type) {
                case CHAR:
                case VARCHAR:
                    vo = getStringOption(cell);
                    break;
                case DATE:
                    vo = getDateOption(cell);
                    break;
                case DATETIME:
                case TIMESTAMP:
                    vo = getDateTimeOption(cell);
                    break;
                case DECIMAL:
                    vo = getDecimalOption(cell);
                    break;
                case TINY_INT:
                    vo = getByteOption(cell);
                    break;
                case SMALL_INT:
                    vo = getShortOption(cell);
                    break;
                case INT:
                    vo = getIntOption(cell);
                    break;
                case LONG:
                    vo = getLongOption(cell);
                    break;
                default:
                    throw new RuntimeException("Unsupported data type: " + type);
                }
                try {
                    String setterName = columnInfos.get(col).getSetterName();
                    Method setter = model.getClass().getMethod(setterName, vo.getClass());
                    setter.invoke(model, vo);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            list.add(model);
        }
        return list;
    }

    /**
     * Returns {@code true} iff the specified row does not exist or has only blank cells.
     * @param row the target row
     * @return {@code true} if is empty
     */
    private boolean isEmpty(HSSFRow row) {
        if (row == null) {
            return true;
        }
        for (Iterator<Cell> iter = row.cellIterator(); iter.hasNext();) {
            if (iter.next().getCellType() != Cell.CELL_TYPE_BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * セルからByteOptionを取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private ByteOption getByteOption(HSSFCell cell) {
        Long l = getLong(cell);
        ByteOption op = new ByteOption();
        if (l == null) {
            op.setNull();
        } else {
            if (l < Byte.MIN_VALUE || Byte.MAX_VALUE < l) {
                String msg = createExceptionMsg(cell, "表現可能な範囲外の数値(" + l + ")");
                throw new NumberFormatException(msg);
            }
            op.modify(l.byteValue());
        }
        return op;
    }

    /**
     * セルからShortOptionを取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private ShortOption getShortOption(HSSFCell cell) {
        Long l = getLong(cell);
        ShortOption op = new ShortOption();
        if (l == null) {
            op.setNull();
        } else {
            if (l < Short.MIN_VALUE || Short.MAX_VALUE < l) {
                String msg = createExceptionMsg(cell, "表現可能な範囲外の数値(" + l + ")");
                throw new NumberFormatException(msg);
            }
            op.modify(l.shortValue());
        }
        return op;
    }

    /**
     * セルからIntOptionを取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private IntOption getIntOption(HSSFCell cell) {
        Long l = getLong(cell);
        IntOption op = new IntOption();
        if (l == null) {
            op.setNull();
        } else {
            if (l < Integer.MIN_VALUE || Integer.MAX_VALUE < l) {
                String msg = createExceptionMsg(cell, "表現可能な範囲外の数値(" + l + ")");
                throw new NumberFormatException(msg);
            }
            op.modify(l.intValue());
        }
        return op;
    }

    /**
     * セルからLongOptionを取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private LongOption getLongOption(HSSFCell cell) {
        Long l = getLong(cell);
        LongOption op = new LongOption();
        if (l == null) {
            op.setNull();
        } else {
            op.modify(l);
        }
        return op;
    }


    /**
     * セルからDateOptionを取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private DateOption getDateOption(HSSFCell cell) {
        Date date = getDate(cell);
        DateOption op = new DateOption();
        if (date == null) {
            op.setNull();
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int s = cal.get(Calendar.SECOND);
            if (h != 0 || min != 0 || s != 0) {
                String msg = createExceptionMsg(cell, "時分秒が0でない値は日付型に変換できません");
                throw new CellTypeMismatchException(msg);
            }
            int days = com.asakusafw.runtime.value.DateUtil.getDayFromDate(y, m + 1, d);
            op.modify(days);
        }
        return op;
    }

    /**
     * セルからDateTimeOptionを取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private DateTimeOption getDateTimeOption(HSSFCell cell) {
        Date date = getDate(cell);
        DateTimeOption op = new DateTimeOption();
        if (date == null) {
            op.setNull();
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int s = cal.get(Calendar.SECOND);
            int days = com.asakusafw.runtime.value.DateUtil.getDayFromDate(y, m + 1, d);
            int secs = com.asakusafw.runtime.value.DateUtil.getSecondFromTime(h, min, s);
            DateTime dt = new DateTime();
            dt.setElapsedSeconds((long) days * 86400 + secs);
            op.modify(dt);
        }
        return op;
    }

    /**
     * セルからStringOptionを取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private StringOption getStringOption(HSSFCell cell) {
        String str;
        switch(cell.getCellType()) {
        case Cell.CELL_TYPE_BLANK:
            str = null;
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            if (cell.getBooleanCellValue()) {
                str = "1";
            } else {
                str = "0";
            }
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                double d = cell.getNumericCellValue();
                Date date = DateUtil.getJavaDate(d);
                str = dateTimeFormat.format(date);
            } else {
                double d = cell.getNumericCellValue();
                str = Double.toString(d);
                str = str.replaceAll("\\.0*$", "");
            }
            break;
        case Cell.CELL_TYPE_STRING:
            str = cell.getStringCellValue();
            break;
        case Cell.CELL_TYPE_ERROR:
        case Cell.CELL_TYPE_FORMULA:
        default:
            String msg = createCellTypeMismatchExceptionMsg(cell, "文字列");
            throw new CellTypeMismatchException(msg);
        }
        StringOption stringOption = new StringOption();
        stringOption.modify(str);
        return stringOption;
    }

    /**
     * セルからDecimalOptionを取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private DecimalOption getDecimalOption(HSSFCell cell) {
        BigDecimal bigDecimal;
        switch(cell.getCellType()) {
        case Cell.CELL_TYPE_BLANK:
            bigDecimal = null;
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            if (cell.getBooleanCellValue()) {
                bigDecimal = new BigDecimal(1);
            } else {
                bigDecimal = new BigDecimal(0);
            }
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                String msg = createCellTypeMismatchExceptionMsg(cell, "DECIMAL");
                throw new CellTypeMismatchException(msg);
            } else {
                double d = cell.getNumericCellValue();
                if (d < EXCEL_MIN_LONG || EXCEL_MAX_LONG < d) {
                    String msg = createExceptionMsg(cell, "表現可能な範囲外の数値(" + d + ")");
                    throw new NumberFormatException(msg);
                }
                long l = (long) d;
                if (l != d) {
                    String msg = createExceptionMsg(cell, "小数部を持つ実数をDECIMALに変換できません。");
                    throw new NumberFormatException(msg);
                }
                String str = Double.toString(d);
                str = str.replaceAll("\\.0*$", "");
                bigDecimal = new BigDecimal(str);
            }
            break;
        case Cell.CELL_TYPE_STRING:
            String str = cell.getStringCellValue();
            try {
                bigDecimal = new BigDecimal(str);
            } catch (NumberFormatException e) {
                String msg = createExceptionMsg(cell, "DECIMALに変換できない文字列");
                throw new NumberFormatException(msg);
            }
            break;
        case Cell.CELL_TYPE_ERROR:
        case Cell.CELL_TYPE_FORMULA:
        default:
            String msg = createCellTypeMismatchExceptionMsg(cell, "DECIMAL");
            throw new CellTypeMismatchException(msg);
        }
        DecimalOption decimalOption = new DecimalOption();
        decimalOption.modify(bigDecimal);
        return decimalOption;
    }

    /**
     * セルからjava.util.Data値を取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private Date getDate(HSSFCell cell) {
        Date date;
        switch(cell.getCellType()) {
        case Cell.CELL_TYPE_BLANK:
            date = null;
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                double d = cell.getNumericCellValue();
                date = DateUtil.getJavaDate(d);
            } else {
                String msg = createCellTypeMismatchExceptionMsg(cell, "日付");
                throw new CellTypeMismatchException(msg);
            }
            break;
        case Cell.CELL_TYPE_STRING:
            String str = cell.getStringCellValue();
            try {
                date = dateTimeFormat.parse(str);
            } catch (Exception e) {
                try {
                    date = dateFormat.parse(str);
                } catch (Exception e2) {
                    String msg = createCellTypeMismatchExceptionMsg(cell, "日付");
                    throw new CellTypeMismatchException(msg);
                }
            }
            break;
        case Cell.CELL_TYPE_BOOLEAN:
        case Cell.CELL_TYPE_ERROR:
        case Cell.CELL_TYPE_FORMULA:
        default:
            String msg = createCellTypeMismatchExceptionMsg(cell, "日付");
            throw new CellTypeMismatchException(msg);
        }
        return date;
    }

    /**
     * セルからLong値を取得する。
     * @param cell 対象のセル
     * @return 対応する値
     */
    private Long getLong(HSSFCell cell) {
        Long l;
        switch(cell.getCellType()) {
        case Cell.CELL_TYPE_BLANK:
            l = null;
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            if (cell.getBooleanCellValue()) {
                l = 1L;
            } else {
                l = 0L;
            }
            break;
        case Cell.CELL_TYPE_NUMERIC:
            double d = cell.getNumericCellValue();
            if (d < EXCEL_MIN_LONG || EXCEL_MAX_LONG < d) {
                String msg = createExceptionMsg(cell, "表現可能な範囲外の数値(" + d + ")");
                throw new NumberFormatException(msg);
            }
            l = (long) d;
            if ((double) l != d) {
                String msg = createExceptionMsg(cell, "小数部を持つ数値を整数型に変換できません。");
                throw new NumberFormatException(msg);
            }
            break;
        case Cell.CELL_TYPE_STRING:
            try {
                String str = cell.getStringCellValue();
                l = Long.parseLong(str);
            } catch (Exception e) {
                String msg = createCellTypeMismatchExceptionMsg(cell, "数値");
                throw new CellTypeMismatchException(msg);
            }
            break;
        case Cell.CELL_TYPE_ERROR:
        case Cell.CELL_TYPE_FORMULA:
        default:
            String msg = createCellTypeMismatchExceptionMsg(cell, "数値");
            throw new CellTypeMismatchException(msg);
        }
        return l;
    }

    /**
     * 特定のセルの処理に失敗した時のExceptionメッセージを作成する。
     * @param cell 対象のセル
     * @param msg エラーメッセージ
     * @return Exception用のメッセージ
     */
    private String createExceptionMsg(HSSFCell cell, String msg) {
        int col = cell.getColumnIndex();
        int rownum = cell.getRowIndex();
        String sheetName = cell.getSheet().getSheetName();
        String fmt = "%s, filename = %s, sheet = %s, row = %d, col = %d";
        String ret = String.format(fmt, msg, filename, sheetName, rownum + 1, col + 1);
        return ret;

    }


    /**
     * セルの型と、期待する型が異なる場合に生じるExceptionのメッセージを作成する。
     * @param cell 対象セル
     * @param expect 期待する型を表す文字列
     * @return Exception用のメッセージ
     */
    private String createCellTypeMismatchExceptionMsg(HSSFCell cell, String expect) {
        int col = cell.getColumnIndex();
        int rownum = cell.getRowIndex();
        String sheetName = cell.getSheet().getSheetName();
        String actual;
        switch(cell.getCellType()) {
        case Cell.CELL_TYPE_BLANK:
            actual = "ブランク";
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            actual = "真偽値";
            break;
        case Cell.CELL_TYPE_ERROR:
            actual = "エラー";
            break;
        case Cell.CELL_TYPE_FORMULA:
            actual = "数式";
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                actual = "日付";
            } else {
                actual = "数値";
            }
            break;
        case Cell.CELL_TYPE_STRING:
            actual = "文字列";
            break;
       default:
           actual = "不明";
           break;
        }
        String fmt = "Excel CELLのデータ型が期待した型と互換性がありません。"
            + "expect = %s, actual = %s,  filename = %s, sheet = %s, row = %d, col = %d";
        String ret = String.format(fmt, expect, actual, filename, sheetName, rownum + 1, col + 1);
        return ret;
    }

    /**
     * カラム情報のリストを取得します。
     * @return カラム情報のリスト
     */
    public List<ColumnInfo> getColumnInfos() {
        return columnInfos;
    }

    /**
     * TestDataHolderを作成する。
     * @return 現在のExcelブックに対応する{@link TestDataHolder}のオブジェクト
     */
    public TestDataHolder getTestDataHolder() {
        List<Writable> source = createDatalList(inputDataSheet);
        List<Writable> expect = createDatalList(outputDataSheet);
        Class<? extends Writable> modelClass = getModelClass();
        return new TestDataHolder(source, expect, columnInfos, modelClass, rowMatchingCondition);
    }
}
