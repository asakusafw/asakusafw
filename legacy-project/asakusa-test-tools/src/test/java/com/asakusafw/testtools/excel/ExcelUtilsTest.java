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


import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import org.apache.hadoop.io.Writable;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.modelgen.source.MySqlDataType;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.testtools.ColumnInfo;
import com.asakusafw.testtools.ColumnMatchingCondition;
import com.asakusafw.testtools.Constants;
import com.asakusafw.testtools.NullValueCondition;

public class ExcelUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    /**
     * コンストラクタの処理が成功すること
     * @throws IOException
     */
    @Test
    public void testConstractor01() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/FOO.xls";
       new ExcelUtils(filename);
    }

    /**
     * コンストラクタのテスト
     * 不正なファイル名が指定された場合
     * @throws IOException
     */
    @Test(expected = java.io.FileNotFoundException.class)
    public void testConstractor02() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NOT_EXIST_FILE";
        new ExcelUtils(filename);
    }

    /**
     * コンストラクタのテスト
     * 入力データのシートがない場合
     * @throws IOException
     */
    @Test
    public void testConstractor03() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_INPUT_DATA.xls";
        try {
            new ExcelUtils(filename);
        } catch (IOException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイル: src/test/data/Excel/ExcelUtils/NO_INPUT_DATA.xlsに入力データのシートが存在しません";
            assertEquals(expected, actual);
        }
    }

    /**
     * コンストラクタのテスト
     * 出力データのシートがない場合
     * @throws IOException
     */
    @Test
    public void testConstractor04() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_OUTPUT_DATA.xls";
        try {
            new ExcelUtils(filename);
        } catch (IOException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイル: src/test/data/Excel/ExcelUtils/NO_OUTPUT_DATA.xlsに出力データのシートが存在しません";
            assertEquals(expected, actual);
        }
    }

    /**
     * コンストラクタのテスト
     * テスト条件のシートがない場合
     * @throws IOException
     */
    @Test
    public void testConstractor05() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_TEST_CONDITON.xls";
        try {
            new ExcelUtils(filename);
        } catch (IOException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイル: src/test/data/Excel/ExcelUtils/NO_TEST_CONDITON.xlsにテスト条件データのシートが存在しません";
            assertEquals(expected, actual);
        }

    }


    /**
     * コンストラクタのテスト
     * テーブル名が未設定
     * @throws IOException
     */
    @Test
    public void testConstractor06() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/BROKEN_TABLENAME1.xls";
        try {
            new ExcelUtils(filename);
        } catch (IOException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイル: src/test/data/Excel/ExcelUtils/BROKEN_TABLENAME1.xlsにテーブル名が定義されていません";
            assertEquals(expected, actual);
        }
    }

    /**
     * コンストラクタのテスト
     * テーブル名のセルが存在しない
     * @throws IOException
     */
    @Test
    public void testConstractor07() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/BROKEN_TABLENAME2.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です(空セル), file = src/test/data/Excel/ExcelUtils/BROKEN_TABLENAME2.xls, sheet = テスト条件, row = 1, col = 3";
            assertEquals(expected, actual);
        }
    }



    /**
     * getColumnInfos()のテスト
     * テーブル名の行が存在しない
     * @throws IOException
     */
    @Test
    public void testConstractor08() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/EMPTY_SEETS.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です(空行), file = src/test/data/Excel/ExcelUtils/EMPTY_SEETS.xls, sheet = テスト条件, row = 0";
            assertEquals(expected, actual);
        }

    }

    /**
     * getColumnInfos()のテスト
     * Decimalカラムの桁数が未設定
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos01() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_WIDTH1.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。テスト条件のシートの定義が不正です。 file = src/test/data/Excel/ExcelUtils/NO_WIDTH1.xls, 行 = 32, 項目 = 桁数";
            assertEquals(expected, actual);
        }
    }

    /**
     * getColumnInfos()のテスト
     * Decimalカラムの精度が未設定
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos02() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_WIDTH2.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。テスト条件のシートの定義が不正です。 file = src/test/data/Excel/ExcelUtils/NO_WIDTH2.xls, 行 = 32, 項目 = 精度";
            assertEquals(expected, actual);
        }
    }

    /**
     * getColumnInfos()のテスト
     * VARCHARカラムの桁数が未設定
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos03() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_WIDTH3.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。テスト条件のシートの定義が不正です。 file = src/test/data/Excel/ExcelUtils/NO_WIDTH3.xls, 行 = 5, 項目 = 桁数";
            assertEquals(expected, actual);
        }
    }

    /**
     * getColumnInfos()のテスト
     * カラム名が未設定
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos04() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_COLUMN_NAME.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。テスト条件のシートの定義が不正です。 file = src/test/data/Excel/ExcelUtils/NO_COLUMN_NAME.xls, 行 = 19, 項目 = カラム名";
            assertEquals(expected, actual);
        }
    }

    /**
     * getColumnInfos()のテスト
     * データ型が未設定
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos05() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_DATA_TYPE.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。テスト条件のシートの定義が不正です。 file = src/test/data/Excel/ExcelUtils/NO_DATA_TYPE.xls, 行 = 13, 項目 = データ型";
            assertEquals(expected, actual);
        }
    }

    /**
     * getColumnInfos()のテスト
     * テスト条件が未設定
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos06() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_TEST_CONDITION.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。テスト条件のシートの定義が不正です。 file = src/test/data/Excel/ExcelUtils/NO_TEST_CONDITION.xls, 行 = 26, 項目 = 比較条件";
            assertEquals(expected, actual);
        }
    }

    /**
     * getColumnInfos()のテスト
     * NULL値の取り扱いが未設定
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos07() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/NO_NULL_VALUE_CONDITION.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。テスト条件のシートの定義が不正です。 file = src/test/data/Excel/ExcelUtils/NO_NULL_VALUE_CONDITION.xls, 行 = 31, 項目 = NULL値";
            assertEquals(expected, actual);
        }
    }

    /**
     * getColumnInfos()のテスト
     * 正常系
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos08() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/FOO.xls";
        ExcelUtils excelUtils =  new ExcelUtils(filename);
        List<ColumnInfo> list = excelUtils.getColumnInfos();
        // カラム数の確認
        assertEquals(37, list.size());
        // 最初のカラムの確認
        ColumnInfo info;
        info = list.get(0);
        assertEquals("FOO", info.getTableName());
        assertEquals("PK", info.getColumnName());
        assertEquals("", info.getColumnComment());
        assertEquals(MySqlDataType.LONG, info.getDataType());
        assertTrue(info.isKey());
        assertFalse(info.isNullable());
        assertEquals(ColumnMatchingCondition.NONE, info.getColumnMatchingCondition());
        assertEquals(NullValueCondition.NORMAL, info.getNullValueCondition());

        // VARCHARのカラム(2番目)を確認
        info = list.get(2 - 1); // カラム番号は1オリジンなので、-1する
        assertEquals("FOO", info.getTableName());
        assertEquals("DETAIL_GROUP_ID", info.getColumnName());
        assertEquals("", info.getColumnComment());
        assertEquals(MySqlDataType.VARCHAR, info.getDataType());
        assertEquals(256, info.getCharacterMaximumLength());
        assertFalse(info.isKey());
        assertFalse(info.isNullable());
        assertEquals(ColumnMatchingCondition.PARTIAL, info.getColumnMatchingCondition());
        assertEquals(NullValueCondition.NULL_IS_NG, info.getNullValueCondition());

        // DECIMALのカラム(29番目)を確認
        info = list.get(29 - 1); // カラム番号は1オリジンなので、-1する
        assertEquals("FOO", info.getTableName());
        assertEquals("DEC_COL", info.getColumnName());
        assertEquals("DEC_COL_C", info.getColumnComment());
        assertEquals(MySqlDataType.DECIMAL, info.getDataType());
        assertEquals(10, info.getNumericPrecision());
        assertEquals(4, info.getNumericScale());
        assertFalse(info.isKey());
        assertTrue(info.isNullable());
        assertEquals(ColumnMatchingCondition.EXACT, info.getColumnMatchingCondition());
        assertEquals(NullValueCondition.NULL_IS_OK, info.getNullValueCondition());
    }


    /**
     * getColumnInfos()のテスト
     * 正常系 - 数値が文字列として入っている場合、文字列が数値と
     *          して入っている場合でも正しく動作することの確認
     */
    @Test
    public void testgetColumnInfos09() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/BAR.xls";
        ExcelUtils excelUtils =  new ExcelUtils(filename);
        List<ColumnInfo> list = excelUtils.getColumnInfos();
        // カラム数の確認
        assertEquals(37, list.size());
        // 最初のカラムの確認
        ColumnInfo info;
        info = list.get(0);
        assertEquals("BAR", info.getTableName());
        assertEquals("PK", info.getColumnName());
        assertEquals("", info.getColumnComment());
        assertEquals(MySqlDataType.LONG, info.getDataType());
        assertTrue(info.isKey());
        assertFalse(info.isNullable());
        assertEquals(ColumnMatchingCondition.NONE, info.getColumnMatchingCondition());
        assertEquals(NullValueCondition.NORMAL, info.getNullValueCondition());

        // VARCHARのカラム(2番目)を確認
        info = list.get(2 - 1); // カラム番号は1オリジンなので、-1する
        assertEquals("BAR", info.getTableName());
        assertEquals("DETAIL_GROUP_ID", info.getColumnName());
        assertEquals("52", info.getColumnComment());
        assertEquals(MySqlDataType.VARCHAR, info.getDataType());
        assertEquals(256, info.getCharacterMaximumLength());
        assertFalse(info.isKey());
        assertFalse(info.isNullable());
        assertEquals(ColumnMatchingCondition.PARTIAL, info.getColumnMatchingCondition());
        assertEquals(NullValueCondition.NULL_IS_NG, info.getNullValueCondition());

        // DECIMALのカラム(29番目)を確認
        info = list.get(29 - 1); // カラム番号は1オリジンなので、-1する
        assertEquals("BAR", info.getTableName());
        assertEquals("DEC_COL", info.getColumnName());
        assertEquals("123456", info.getColumnComment());
        assertEquals(MySqlDataType.DECIMAL, info.getDataType());
        assertEquals(10, info.getNumericPrecision());
        assertEquals(4, info.getNumericScale());
        assertFalse(info.isKey());
        assertTrue(info.isNullable());
        assertEquals(ColumnMatchingCondition.EXACT, info.getColumnMatchingCondition());
        assertEquals(NullValueCondition.NULL_IS_OK, info.getNullValueCondition());
    }

    /**
     * getColumnInfos()のテスト
     * 数値のカラムに数値に変換できない文字列が設定されている
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos10() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/INVALID_NUM1.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。数値のセルに数値以外の値が設定されています。 file = src/test/data/Excel/ExcelUtils/INVALID_NUM1.xls, sheet = テスト条件, row = 5, col = 5";
            assertEquals(expected, actual);
        }
    }

    /**
     * getColumnInfos()のテスト
     * 数値のカラムに真偽値が設定されている
     * @throws IOException
     */
    @Test
    public void testgetColumnInfos11() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/INVALID_NUM2.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。数値のセルに数値以外の値が設定されています。 file = src/test/data/Excel/ExcelUtils/INVALID_NUM2.xls, sheet = テスト条件, row = 5, col = 5";
            assertEquals(expected, actual);
        }
    }

    /**
     * getColumnInfos()のテスト
     * 文字列のカラムに数式が設定されている
     * @throws IOException
     */
    @Test
    public void testGetColumnInfos12() throws IOException {
        String filename = "src/test/data/Excel/ExcelUtils/INVALID_STR.xls";
        try {
            new ExcelUtils(filename);
        } catch (InvalidExcelBookException e) {
            String actual = e.getLocalizedMessage();
            String expected = "Excelファイルが異常です。文字列のセルに文字列以外の値が設定されています。 file = src/test/data/Excel/ExcelUtils/INVALID_STR.xls, sheet = テスト条件, row = 4, col = 3";
            assertEquals(expected, actual);
        }
    }


    /**
     * getXXXoption()のテスト
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetXXXOption() throws Exception {
        String filename = "src/test/data/Excel/ExcelUtils/ALLT_TYPES.xls";

        // テスト対象のEXCELシートを取得
        InputStream is = new FileInputStream(filename);
        HSSFWorkbook workbook = new HSSFWorkbook(is);
        HSSFSheet sheet = workbook.getSheet(Constants.OUTPUT_DATA_SHEET_NAME);

        // テスト対象のExcelUtilsオブジェクトを生成
        ExcelUtils excelUtils =  new ExcelUtils(filename);


        // getLongOptionのテスト

        LongOption longOption = new LongOption();
        longOption.modify(0);
        testGetXXXOptionDo(excelUtils, sheet,  DATA.NUMERIC_0, TYPES.BIGINT, longOption);
        longOption.modify(1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_1, TYPES.BIGINT, longOption);
        longOption.modify(-1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MINUS1, TYPES.BIGINT, longOption);
        longOption.modify(ExcelUtils.EXCEL_MAX_LONG);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MAX, TYPES.BIGINT, longOption);
        longOption.modify(ExcelUtils.EXCEL_MIN_LONG);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MIN, TYPES.BIGINT, longOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DECIMAL, TYPES.BIGINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_OVER_MAX, TYPES.BIGINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_UNDER_MIN, TYPES.BIGINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_BIG_VALUE, TYPES.BIGINT, new NumberFormatException("表現可能な範囲外の数値"));
        longOption.modify(0);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_0, TYPES.BIGINT, longOption);
        longOption.modify(1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_1, TYPES.BIGINT, longOption);
        longOption.modify(-1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MINUS1, TYPES.BIGINT, longOption);
        longOption.modify(Long.MAX_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MAX, TYPES.BIGINT, longOption);
        longOption.modify(Long.MIN_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MIN, TYPES.BIGINT, longOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DECIMAL, TYPES.BIGINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_OVER_MAX, TYPES.BIGINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_UNDER_MIN, TYPES.BIGINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_BIG_VALUE, TYPES.BIGINT, new CellTypeMismatchException("数値"));
        longOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.BLANK, TYPES.BIGINT, longOption);
        longOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.NULL_STRING, TYPES.BIGINT, new CellTypeMismatchException("数値"));
        longOption.modify(1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_TRUE, TYPES.BIGINT, longOption);
        longOption.modify(0);
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_FALSE, TYPES.BIGINT, longOption);
        longOption.modify(40179);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATE, TYPES.BIGINT, longOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT1, TYPES.BIGINT, longOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT2, TYPES.BIGINT, longOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT1, TYPES.BIGINT, longOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT2, TYPES.BIGINT, longOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATE, TYPES.BIGINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATETIME, TYPES.BIGINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT1, TYPES.BIGINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT2, TYPES.BIGINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT1, TYPES.BIGINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT2, TYPES.BIGINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATETIME, TYPES.BIGINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.ERROR, TYPES.BIGINT,  new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.FORMULA, TYPES.BIGINT, new CellTypeMismatchException("数値"));

        // getIntOptionのテスト

        IntOption intOption = new IntOption();
        intOption.modify(0);
        testGetXXXOptionDo(excelUtils, sheet,  DATA.NUMERIC_0, TYPES.INT, intOption);
        intOption.modify(1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_1, TYPES.INT, intOption);
        intOption.modify(-1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MINUS1, TYPES.INT, intOption);
        intOption.modify(Integer.MAX_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MAX, TYPES.INT, intOption);
        intOption.modify(Integer.MIN_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MIN, TYPES.INT, intOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DECIMAL, TYPES.INT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_OVER_MAX, TYPES.INT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_UNDER_MIN, TYPES.INT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_BIG_VALUE, TYPES.INT, new NumberFormatException("表現可能な範囲外の数値"));
        intOption.modify(0);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_0, TYPES.INT, intOption);
        intOption.modify(1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_1, TYPES.INT, intOption);
        intOption.modify(-1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MINUS1, TYPES.INT, intOption);
        intOption.modify(Integer.MAX_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MAX, TYPES.INT, intOption);
        intOption.modify(Integer.MIN_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MIN, TYPES.INT, intOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DECIMAL, TYPES.INT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_OVER_MAX, TYPES.INT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_UNDER_MIN, TYPES.INT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_BIG_VALUE, TYPES.INT, new CellTypeMismatchException("数値"));
        intOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.BLANK, TYPES.INT, intOption);
        longOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.NULL_STRING, TYPES.INT, new CellTypeMismatchException("数値"));
        intOption.modify(1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_TRUE, TYPES.INT, intOption);
        intOption.modify(0);
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_FALSE, TYPES.INT, intOption);
        intOption.modify(40179);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATE, TYPES.INT, intOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT1, TYPES.INT, intOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT2, TYPES.INT, intOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT1, TYPES.INT, intOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT2, TYPES.INT, intOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATE, TYPES.INT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATETIME, TYPES.INT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT1, TYPES.INT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT2, TYPES.INT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT1, TYPES.INT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT2, TYPES.INT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATETIME, TYPES.INT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.ERROR, TYPES.INT,  new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.FORMULA, TYPES.INT, new CellTypeMismatchException("数値"));

        // getShortOptionのテスト

        ShortOption shortOption = new ShortOption();
        shortOption.modify((short) 0);
        testGetXXXOptionDo(excelUtils, sheet,  DATA.NUMERIC_0, TYPES.SMALLINT, shortOption);
        shortOption.modify((short) 1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_1, TYPES.SMALLINT, shortOption);
        shortOption.modify((short) -1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MINUS1, TYPES.SMALLINT, shortOption);
        shortOption.modify(Short.MAX_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MAX, TYPES.SMALLINT, shortOption);
        shortOption.modify(Short.MIN_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MIN, TYPES.SMALLINT, shortOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DECIMAL, TYPES.SMALLINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_OVER_MAX, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_UNDER_MIN, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_BIG_VALUE, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        shortOption.modify((short)0);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_0, TYPES.SMALLINT, shortOption);
        shortOption.modify((short)1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_1, TYPES.SMALLINT, shortOption);
        shortOption.modify((short)-1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MINUS1, TYPES.SMALLINT, shortOption);
        shortOption.modify(Short.MAX_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MAX, TYPES.SMALLINT, shortOption);
        shortOption.modify(Short.MIN_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MIN, TYPES.SMALLINT, shortOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DECIMAL, TYPES.SMALLINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_OVER_MAX, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_UNDER_MIN, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_BIG_VALUE, TYPES.SMALLINT, new CellTypeMismatchException("数値"));
        shortOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.BLANK, TYPES.SMALLINT, shortOption);
        longOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.NULL_STRING, TYPES.SMALLINT, new CellTypeMismatchException("数値"));
        shortOption.modify((short)1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_TRUE, TYPES.SMALLINT, shortOption);
        shortOption.modify((short)0);
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_FALSE, TYPES.SMALLINT, shortOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATE, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT1, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT2, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT1, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT2, TYPES.SMALLINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATE, TYPES.SMALLINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATETIME, TYPES.SMALLINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT1, TYPES.SMALLINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT2, TYPES.SMALLINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT1, TYPES.SMALLINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT2, TYPES.SMALLINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATETIME, TYPES.SMALLINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.ERROR, TYPES.SMALLINT,  new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.FORMULA, TYPES.SMALLINT, new CellTypeMismatchException("数値"));

        // getByteOptionのテスト
        ByteOption byteOption = new ByteOption();
        byteOption.modify((byte) 0);
        testGetXXXOptionDo(excelUtils, sheet,  DATA.NUMERIC_0, TYPES.TINYINT, byteOption);
        byteOption.modify((byte) 1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_1, TYPES.TINYINT, byteOption);
        byteOption.modify((byte) -1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MINUS1, TYPES.TINYINT, byteOption);
        byteOption.modify(Byte.MAX_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MAX, TYPES.TINYINT, byteOption);
        byteOption.modify(Byte.MIN_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MIN, TYPES.TINYINT, byteOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DECIMAL, TYPES.TINYINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_OVER_MAX, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_UNDER_MIN, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_BIG_VALUE, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        byteOption.modify((byte)0);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_0, TYPES.TINYINT, byteOption);
        byteOption.modify((byte)1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_1, TYPES.TINYINT, byteOption);
        byteOption.modify((byte)-1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MINUS1, TYPES.TINYINT, byteOption);
        byteOption.modify(Byte.MAX_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MAX, TYPES.TINYINT, byteOption);
        byteOption.modify(Byte.MIN_VALUE);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MIN, TYPES.TINYINT, byteOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DECIMAL, TYPES.TINYINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_OVER_MAX, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_UNDER_MIN, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_BIG_VALUE, TYPES.TINYINT, new CellTypeMismatchException("数値"));
        byteOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.BLANK, TYPES.TINYINT, byteOption);
        longOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.NULL_STRING, TYPES.TINYINT, new CellTypeMismatchException("数値"));
        byteOption.modify((byte)1);
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_TRUE, TYPES.TINYINT, byteOption);
        byteOption.modify((byte)0);
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_FALSE, TYPES.TINYINT, byteOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATE, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT1, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT2, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT1, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT2, TYPES.TINYINT, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATE, TYPES.TINYINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATETIME, TYPES.TINYINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT1, TYPES.TINYINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT2, TYPES.TINYINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT1, TYPES.TINYINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT2, TYPES.TINYINT, new NumberFormatException("小数部を持つ数値を整数型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATETIME, TYPES.TINYINT, new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.ERROR, TYPES.TINYINT,  new CellTypeMismatchException("数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.FORMULA, TYPES.TINYINT, new CellTypeMismatchException("数値"));

        // getStringOptionのテスト

        StringOption stringOption = new StringOption();
        stringOption.modify("0");
        testGetXXXOptionDo(excelUtils, sheet,  DATA.NUMERIC_0, TYPES.CHAR, stringOption);
        stringOption.modify("1");
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_1, TYPES.CHAR, stringOption);
        stringOption.modify("-1");
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MINUS1, TYPES.CHAR, stringOption);
        stringOption.modify("6.54321");
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DECIMAL, TYPES.CHAR, stringOption);
        stringOption.modify("9.2233720368547697E18");
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_OVER_MAX, TYPES.CHAR, stringOption);
        stringOption.modify("-9.2233720368547697E18");
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_UNDER_MIN, TYPES.CHAR, stringOption);
        stringOption.modify("1.23456789012345E19");
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_BIG_VALUE, TYPES.CHAR, stringOption);
        stringOption.modify("0");
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_0, TYPES.CHAR, stringOption);
        stringOption.modify("1");
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_1, TYPES.CHAR, stringOption);
        stringOption.modify("-1");
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MINUS1, TYPES.CHAR, stringOption);
        stringOption.modify("6.54321");
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DECIMAL, TYPES.CHAR, stringOption);
        stringOption.modify("9223372036854775808");
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_OVER_MAX, TYPES.CHAR, stringOption);
        stringOption.modify("-9223372036854775809");
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_UNDER_MIN, TYPES.CHAR, stringOption);
        stringOption.modify("12345678901234567890");
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_BIG_VALUE, TYPES.CHAR, stringOption);
        stringOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.BLANK, TYPES.CHAR, stringOption);
        stringOption.modify("");
        testGetXXXOptionDo(excelUtils, sheet, DATA.NULL_STRING, TYPES.CHAR, stringOption);
        stringOption.modify("1");
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_TRUE, TYPES.CHAR, stringOption);
        stringOption.modify("0");
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_FALSE, TYPES.CHAR, stringOption);
        stringOption.modify("40179");
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATE, TYPES.CHAR, stringOption);
        stringOption.modify("2010-01-01 00:00:00");
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT1, TYPES.CHAR, stringOption);
        stringOption.modify("2010-01-01 00:00:00");
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT2, TYPES.CHAR, stringOption);
        stringOption.modify("2010-01-01 00:00:00");
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT1, TYPES.CHAR, stringOption);
        stringOption.modify("2010-01-01 00:00:00");
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT2, TYPES.CHAR, stringOption);
        stringOption.modify("2010-01-01");
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATE, TYPES.CHAR, stringOption);
        stringOption.modify("40452.50090277778");
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATETIME, TYPES.CHAR, stringOption);
        stringOption.modify("2010-10-01 12:01:18");
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT1, TYPES.CHAR, stringOption);
        stringOption.modify("2010-10-01 12:01:18");
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT2, TYPES.CHAR, stringOption);
        stringOption.modify("2010-10-01 12:01:18");
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT1, TYPES.CHAR, stringOption);
        stringOption.modify("2010-10-01 12:01:18");
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT2, TYPES.CHAR, stringOption);
        stringOption.modify("2010-10-01 12:01:18");
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATETIME, TYPES.CHAR, stringOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.ERROR, TYPES.CHAR,  new CellTypeMismatchException("文字列"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.FORMULA, TYPES.CHAR, new CellTypeMismatchException("文字列"));

        // getDateOptionのテスト

        DateOption dateOption = new DateOption();

        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_0, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_1, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MINUS1, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DECIMAL, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_OVER_MAX, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_UNDER_MIN, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_BIG_VALUE, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_0, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_1, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MINUS1, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DECIMAL, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_OVER_MAX, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_UNDER_MIN, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_BIG_VALUE, TYPES.DATE, new CellTypeMismatchException("日付"));
        dateOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.BLANK, TYPES.DATE, dateOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NULL_STRING, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_TRUE, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_FALSE, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATE, TYPES.DATE, new CellTypeMismatchException("日付"));
        dateOption.modify(DateUtil.getDayFromDate(2010, 1, 1));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT1, TYPES.DATE, dateOption);
        dateOption.modify(DateUtil.getDayFromDate(2010, 1, 1));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT2, TYPES.DATE, dateOption);
        dateOption.modify(DateUtil.getDayFromDate(2010, 1, 1));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT1, TYPES.DATE, dateOption);
        dateOption.modify(DateUtil.getDayFromDate(2010, 1, 1));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT2, TYPES.DATE, dateOption);
        dateOption.modify(DateUtil.getDayFromDate(2010, 1, 1));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATE, TYPES.DATE, dateOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATETIME, TYPES.DATE, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT1, TYPES.DATE, new CellTypeMismatchException("時分秒が0でない値は日付型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT2, TYPES.DATE, new CellTypeMismatchException("時分秒が0でない値は日付型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT1, TYPES.DATE, new CellTypeMismatchException("時分秒が0でない値は日付型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT2, TYPES.DATE, new CellTypeMismatchException("時分秒が0でない値は日付型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATETIME, TYPES.DATE, new CellTypeMismatchException("時分秒が0でない値は日付型に変換できません"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.ERROR, TYPES.DATE,  new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.FORMULA, TYPES.DATE, new CellTypeMismatchException("日付"));

        // getDateTimeOptionのテスト

        DateTimeOption dateTimeOption = new DateTimeOption();
        DateTime dateTime = new DateTime();

        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_0, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_1, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MINUS1, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DECIMAL, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_OVER_MAX, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_UNDER_MIN, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_BIG_VALUE, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_0, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_1, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MINUS1, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DECIMAL, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_OVER_MAX, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_UNDER_MIN, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_BIG_VALUE, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        dateTimeOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.BLANK, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NULL_STRING, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_TRUE, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_FALSE, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATE, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        dateTime.setElapsedSeconds(DateUtil.getDayFromDate(2010, 1, 1) * 86400L);
        dateTimeOption.modify(dateTime);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT1, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT2, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT1, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT2, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATE, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATETIME, TYPES.DATETIME, new CellTypeMismatchException("日付"));
        dateTime.setElapsedSeconds(DateUtil.getDayFromDate(2010, 10, 1) * 86400L + DateUtil.getSecondFromTime(12, 1, 18));
        dateTimeOption.modify(dateTime);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT1, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT2, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT1, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT2, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATETIME, TYPES.DATETIME, dateTimeOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.ERROR, TYPES.DATETIME,  new CellTypeMismatchException("日付"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.FORMULA, TYPES.DATETIME, new CellTypeMismatchException("日付"));


        // getDecimalOptionのテスト

        DecimalOption decimalOption = new DecimalOption();
        decimalOption.modify(new BigDecimal("0"));
        testGetXXXOptionDo(excelUtils, sheet,  DATA.NUMERIC_0, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("1"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_1, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("-1"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MINUS1, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("999999999999999"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MAX, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("-999999999999999"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_MIN, TYPES.DECIMAL, decimalOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DECIMAL, TYPES.DECIMAL, new NumberFormatException("小数部を持つ実数をDECIMALに変換できません。"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_OVER_MAX, TYPES.DECIMAL, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_UNDER_MIN, TYPES.DECIMAL, new NumberFormatException("表現可能な範囲外の数値"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_BIG_VALUE, TYPES.DECIMAL, new NumberFormatException("表現可能な範囲外の数値"));
        decimalOption.modify(new BigDecimal("0"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_0, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("1"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_1, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("-1"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_MINUS1, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("6.54321"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DECIMAL, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("9223372036854775808"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_OVER_MAX, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("-9223372036854775809"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_UNDER_MIN, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("12345678901234567890"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_BIG_VALUE, TYPES.DECIMAL, decimalOption);
        decimalOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.BLANK, TYPES.DECIMAL, decimalOption);
        longOption.setNull();
        testGetXXXOptionDo(excelUtils, sheet, DATA.NULL_STRING, TYPES.DECIMAL, new NumberFormatException("DECIMALに変換できない文字列"));
        decimalOption.modify(new BigDecimal("1"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_TRUE, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("0"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.BOOL_FALSE, TYPES.DECIMAL, decimalOption);
        decimalOption.modify(new BigDecimal("40179"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATE, TYPES.DECIMAL, decimalOption);
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT1, TYPES.DECIMAL, new CellTypeMismatchException("DECIMAL"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATE_FMT2, TYPES.DECIMAL, new CellTypeMismatchException("DECIMAL"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT1, TYPES.DECIMAL, new CellTypeMismatchException("DECIMAL"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATE_DATETIME_FIMT2, TYPES.DECIMAL, new CellTypeMismatchException("DECIMAL"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATE, TYPES.DECIMAL, new NumberFormatException("DECIMALに変換できない文字列"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.NUMERIC_DATETIME, TYPES.DECIMAL, new NumberFormatException("小数部を持つ実数をDECIMALに変換できません。"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT1, TYPES.DECIMAL, new CellTypeMismatchException("DECIMAL"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATE_FMT2, TYPES.DECIMAL, new CellTypeMismatchException("DECIMAL"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT1, TYPES.DECIMAL, new CellTypeMismatchException("DECIMAL"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.DATETIME_DATETIME_FIMT2, TYPES.DECIMAL, new CellTypeMismatchException("DECIMAL"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.STRING_DATETIME, TYPES.DECIMAL, new NumberFormatException("DECIMALに変換できない文字列"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.ERROR, TYPES.DECIMAL,  new CellTypeMismatchException("DECIMAL"));
        testGetXXXOptionDo(excelUtils, sheet, DATA.FORMULA, TYPES.DECIMAL, new CellTypeMismatchException("DECIMAL"));

    }



    public void testGetXXXOptionDo(ExcelUtils excelUtils, HSSFSheet sheet, DATA data, TYPES types, Object expected) throws Exception {
        int rownum = data.getRownum();
        int colpos = types.getColpos();
        HSSFCell cell = getCell(sheet, rownum, colpos);
        String methodName = types.getMethodName();
        Method method = excelUtils.getClass().getDeclaredMethod(methodName, HSSFCell.class);
        method.setAccessible(true);

        Throwable t = null;
        Object actual = null;
        try {
            actual = method.invoke(excelUtils, cell);
        } catch (InvocationTargetException e) {
            t = e.getCause();
        }

        String fmt = "テスト対象メソッド: %s, セルの値[%s]";
        String assertMsg = String.format(fmt, methodName, data.getComment());

        if (t instanceof RuntimeException) {
            if (expected instanceof RuntimeException) {
            // Exceptionの型が同じ
            assertEquals(assertMsg, expected.getClass(), t.getClass());
            String expectedMsg = ((RuntimeException) expected).getMessage();
            String actualMsg = t.getMessage();
            // Exception中のメッセージがマッチすることの確認
            assertTrue(assertMsg, actualMsg.matches(".*" + expectedMsg + ".*"));
            } else {
                throw (RuntimeException)t;
            }
        } else if (expected instanceof Writable) {
            assertEquals(assertMsg, expected, actual);
        } else if (expected instanceof Exception) {
            throw new RuntimeException("Expected exception is not cought (" + expected.getClass().toString() +")");
        } else {
            throw new RuntimeException("Unexpected object class:" + expected.getClass().toString());
        }

    }

    /**
     * シート、行位置、カラム位置を指定してセルを取得する
     * @param sheet
     * @param rownum
     * @param col
     * @return
     */
    private HSSFCell getCell(HSSFSheet sheet, int rownum, int col) {
        HSSFRow row = sheet.getRow(rownum);
        HSSFCell cell = row.getCell(col);
        return cell;
    }


    /**
     * テスト対象のデータ型が定義されている列挙型
     */
    private enum TYPES {
        BIGINT(2, "getLongOption"),
        INT(3, "getIntOption"),
        SMALLINT(4, "getShortOption"),
        TINYINT(5, "getByteOption"),
        CHAR(6, "getStringOption"),
        DATETIME(7, "getDateTimeOption"),
        DATE(8, "getDateOption"),
        DECIMAL(9, "getDecimalOption");

        /**
         * EXCELシートのカラム位置
         */
        private int colpos;

        /**
         * テストに使用するメソッド名
         */
        private String methodName;

        public int getColpos() {
            return colpos;
        }

        public String getMethodName() {
            return methodName;
        }

        private TYPES(int colpos, String methodName) {
            this.colpos = colpos;
            this.methodName = methodName;
        }
    }


    /**
     * 各行にどのようなテストデータが定義されているかを
     * 表す列挙型
     */
    private enum DATA {
        NUMERIC_0("数値の0", 1),
        NUMERIC_1("数値の1", 2),
        NUMERIC_MINUS1("数値の-1", 3),
        NUMERIC_MAX("表現可能な最大値(数値)", 4),
        NUMERIC_MIN("表現可能な最小値(数値)", 5),
        NUMERIC_DECIMAL("小数値(数値)", 6),
        NUMERIC_OVER_MAX("表現可能な最大値+1(数値)", 7),
        NUMERIC_UNDER_MIN("表現可能な最小値-1(数値)", 8),
        NUMERIC_BIG_VALUE("大きな値(数値)", 9),
        STRING_0("文字列の0", 10),
        STRING_1("文字列の1", 11),
        STRING_MINUS1("文字列の-1", 12),
        STRING_MAX("表現可能な最大値(文字列)", 13),
        STRING_MIN("表現可能な最小値(文字列)", 14),
        STRING_DECIMAL("小数値(文字列)", 15),
        STRING_OVER_MAX("表現可能な最大値+1(文字列)", 16),
        STRING_UNDER_MIN("表現可能な最小値-1(文字列)", 17),
        STRING_BIG_VALUE("大きな値(数値)", 18),
        BLANK("ブランク", 19),
        NULL_STRING("空文字列", 20),
        BOOL_TRUE("BOOL値(TRUE)", 21),
        BOOL_FALSE("BOOL値(FALSE)", 22),
        NUMERIC_DATE("日付(数値フォーマット)", 23),
        DATE_DATE_FMT1("日付(日付フォーマット1)", 24),
        DATE_DATE_FMT2("日付(日付フォーマット2)", 25),
        DATE_DATETIME_FIMT1("日付(時刻フォーマット1)", 26),
        DATE_DATETIME_FIMT2("日付(時刻フォーマット2)", 27),
        STRING_DATE("日付(文字列)", 28),
        NUMERIC_DATETIME("時刻(数値フォーマット)", 29),
        DATETIME_DATE_FMT1("時刻(日付フォーマット1)", 30),
        DATETIME_DATE_FMT2("時刻(日付フォーマット2)", 31),
        DATETIME_DATETIME_FIMT1("時刻(時刻フォーマット1)", 32),
        DATETIME_DATETIME_FIMT2("時刻(時刻フォーマット2)", 33),
        STRING_DATETIME("時刻(文字列)", 34),
        ERROR("エラーセル", 35),
        FORMULA("数式セル", 36);


        /**
         * コメント
         */
        private String comment;

        /**
         * EXCELシートの行位置
         */
        private int rownum;

        private DATA(String comment, int rownum) {
            this.comment = comment;
            this.rownum = rownum;
        }

        public String getComment() {
            return comment;
        }

        public int getRownum() {
            return rownum;
        }
    }


}
