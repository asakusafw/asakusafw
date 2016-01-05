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
package com.asakusafw.testtools.inspect;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.io.Writable;
import org.junit.Test;

import test.modelgen.model.AllTypesWNoerr;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.testtools.RowMatchingCondition;
import com.asakusafw.testtools.TestDataHolder;
import com.asakusafw.testtools.db.DbUtils;
import com.asakusafw.testtools.excel.ExcelUtils;
import com.asakusafw.testtools.inspect.Cause.Type;

public class DefaultInspectorTest {


     // テストファイル名
    private static final String TEST_FILE = "src/test/data/Excel/ExcelUtils/INSPECTOR.xls";
    private static final String TEST_FILE_NULL_NORMAL =  "src/test/data/Excel/ExcelUtils/INSPECTOR_NULL_NORMAL.xls";
    private static final String TEST_FILE_NULL_OK =  "src/test/data/Excel/ExcelUtils/INSPECTOR_NULL_OK.xls";
    private static final String TEST_FILE_NULL_NG =  "src/test/data/Excel/ExcelUtils/INSPECTOR_NULL_NG.xls";
    private static final String TEST_FILE_NOT_NULL_OK =  "src/test/data/Excel/ExcelUtils/INSPECTOR_NOT_NULL_OK.xls";
    private static final String TEST_FILE_NOT_NULL_NG =  "src/test/data/Excel/ExcelUtils/INSPECTOR_NOT_NULL_NG.xls";
    private static final String TEST_FILE_INSPECT_NONE =  "src/test/data/Excel/ExcelUtils/INSPECTOR_INSPECT_NONE.xls";
    private static final String TEST_FILE_INSPECT_NOW =  "src/test/data/Excel/ExcelUtils/INSPECTOR_INSPECT_NOW.xls";
    private static final String TEST_FILE_INSPECT_TODAY =  "src/test/data/Excel/ExcelUtils/INSPECTOR_INSPECT_TODAY.xls";
    private static final String TEST_FILE_INSPECT_PARTIAL = "src/test/data/Excel/ExcelUtils/INSPECTOR_INSPECT_PARTIAL.xls";
    private static final String TEST_FILE_INSPECT_PARTIAL2 = "src/test/data/Excel/ExcelUtils/INSPECTOR_INSPECT_PARTIAL2.xls";

    /**
     * テストファイルに含まれる行数
     */
    private static final int ROWNS_IN_TEST_FILE = 34;


    /**
     * setUpメソッドで初期化されるテスト対象のデータホルダ
     * このデータホルダのsource, expectの値を書き換えて
     * テストを実行する
     */
    private TestDataHolder dataHolder;



    /**
     * 指定のExcelファイルを読み込みdetaHolderを初期化する
     * @param filename Excelファイルのファイル名
     * @throws Exception
     */
    private void initDataHolder(String filename) throws Exception {
        ExcelUtils excelUtils = new ExcelUtils(filename);
        dataHolder = excelUtils.getTestDataHolder();
        // DBへのストアとDBからのロード
        Connection conn = null;
        try {
            conn = DbUtils.getConnection();
            dataHolder.storeToDatabase(conn, true);
            dataHolder.loadFromDatabase(conn);
        } finally {
            DbUtils.closeQuietly(conn);
        }

    }


    /**
     * 期待値と実値が同一である場合
     * @throws Exception
     */
    @Test
    public void testNormal() throws Exception {
        initDataHolder(TEST_FILE);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        assertEquals ("エラー原因のリストの要素数", 0, inspector.getCauses().size());
    }

    /**
     * 期待値のレコードのリストが空の場合
     * @throws Exception
     */
    @Test
    public void testNullExcepctList() throws Exception {
        initDataHolder(TEST_FILE);
        List<Writable> expect = dataHolder.getExpect();
        expect.clear();

        // エラーになることの確認
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        assertEquals ("エラー原因のリストの要素数", ROWNS_IN_TEST_FILE, inspector.getCauses().size());
        for(Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因のチェック", Type.NO_EXPECT_RECORD, cause.getType());
        }

        // テーブル毎の行の比較条件を部分一致に設定して比較
        dataHolder.setRowMatchingCondition(RowMatchingCondition.PARTIAL);
        inspector.clear();
        inspector.inspect(dataHolder);
        assertEquals ("エラー原因のリストの要素数", 0, inspector.getCauses().size());

        // テーブル毎の行の比較条件を「検査しない」に設定して比較
        dataHolder.setRowMatchingCondition(RowMatchingCondition.PARTIAL);
        inspector.clear();
        inspector.inspect(dataHolder);
        assertEquals ("エラー原因のリストの要素数", 0, inspector.getCauses().size());
    }


    /**
     * 実値のレコードのリストが空の場合
     * @throws Exception
     */
    @Test
    public void testNullActualList() throws Exception {
        initDataHolder(TEST_FILE);
        List<Writable> actual = dataHolder.getActual();
        actual.clear();

        // エラーになることの確認
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        assertEquals ("エラー原因のリストの要素数", ROWNS_IN_TEST_FILE, inspector.getCauses().size());
        for(Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因のチェック", Type.NO_ACTUAL_RECORD, cause.getType());
        }


        // テーブル毎の行の比較条件を部分一致に設定して比較 => エラーとなることの確認
        dataHolder.setRowMatchingCondition(RowMatchingCondition.PARTIAL);
        inspector.clear();
        inspector.inspect(dataHolder);

        assertEquals ("エラー原因のリストの要素数", ROWNS_IN_TEST_FILE, inspector.getCauses().size());
        for(Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因のチェック", Type.NO_ACTUAL_RECORD, cause.getType());
        }

        // テーブル毎の行の比較条件を「検査しない」に設定して比較
        dataHolder.setRowMatchingCondition(RowMatchingCondition.NONE);
        inspector.clear();
        inspector.inspect(dataHolder);
        assertEquals ("エラー原因のリストの要素数", 0, inspector.getCauses().size());

    }


    /**
     * 期待値のリストと、実値のリストの重複検出のテスト
     * @throws Exception
     */
    @Test
    public void testDuplicatedRecords() throws Exception {
        initDataHolder(TEST_FILE);
        dataHolder.sort(); // Inspector実行時のソートによりリストの順番が変わらないように
                           // 予めソートする
        List<Writable> expect = dataHolder.getExpect();
        List<Writable> actual = dataHolder.getActual();

        // 期待値のリストの４番目のレコードと５番目のレコードを重複させる
        expect.set(5, expect.get(4));

        // 期待値のリストの９番目～１１番目のレコードを重複させる
        actual.set(10,actual.get(9));
        actual.set(11,actual.get(9));

        // 期待値のリストの１５番目のレコードと１６番目のレコードを重複させる
        expect.set(15, expect.get(16));

        // エラーになることの確認
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        assertEquals ("エラー原因のリストの要素数", 4, inspector.getCauses().size());

        // 重複レコードが正しくcausesに記録されていることの確認
        Set<String> actualTags = new HashSet<String>();
        Set<String> expectTags = new HashSet<String>();

        for(Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            AllTypesWNoerr actualModelObject = (AllTypesWNoerr) cause.getActual();
            AllTypesWNoerr expectModelObject = (AllTypesWNoerr) cause.getExpect();
            if (actualModelObject == null) {
                assertEquals("エラー原因のチェック", Type.DUPLICATEED_KEY_IN_EXPECT_RECORDS, cause.getType());
                assertNotNull("actualのみがNULLであることの確認", expectModelObject);
                expectTags.add(expectModelObject.getCTagAsString());
            }
            if (expectModelObject == null) {
                assertEquals("エラー原因のチェック", Type.DUPLICATEED_KEY_IN_ACTUALT_RECORDS, cause.getType());
                assertNotNull("actualのみがNULLであることの確認", actualModelObject);
                actualTags.add(actualModelObject.getCTagAsString());
            }
        }
        assertEquals("期待値のリストで重複しているタグの数", 2, expectTags.size());
        assertEquals("実値のリストで重複しているタグの数",1, actualTags.size());
        assertTrue("重複しているタグの確認", expectTags.contains(
                ((AllTypesWNoerr)expect.get(4)).getCTagAsString()));
        assertTrue("重複しているタグの確認", expectTags.contains(
                ((AllTypesWNoerr)expect.get(16)).getCTagAsString()));
        assertTrue("重複しているタグの確認", actualTags.contains(
                ((AllTypesWNoerr)actual.get(9)).getCTagAsString()));
    }



    /**
     * 期待値のレコードの一部の不足をテスト
     * @throws Exception
     */
    @Test
    public void testLackOfExpectRecord() throws Exception {
        initDataHolder(TEST_FILE);
        dataHolder.sort(); // Inspector実行時のソートによりリストの順番が変わらないように
                           // 予めソートする
        List<Writable> expect = dataHolder.getExpect();
        List<String> removedRecordTags = new ArrayList<String>();
        // 下のレコードをリストから削除する(逆順に定義する必要がある)
        int[] removeIndexs = {(expect.size() -1), (expect.size() - 2), 19, 18, 15, 1, 0};
        for(int index: removeIndexs) {
            removedRecordTags.add(((AllTypesWNoerr) expect.get(index)).getCTagAsString());
            expect.remove(index);
        }

        // エラーになることの確認
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        for (Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因の確認", Type.NO_EXPECT_RECORD, cause.getType());
            AllTypesWNoerr model = (AllTypesWNoerr) cause.getActual();
            String tag = model.getCTagAsString();
            assertTrue("エラーが報告されたレコードが削除されたレコードと同じタグであること", removedRecordTags.contains(tag));
            removedRecordTags.remove(tag);
        }
        assertEquals ("エラー原因のリストの要素数", 7, inspector.getCauses().size());
        assertTrue("削除したレコードとエラーが発生したレコードの数が同じ", removedRecordTags.size() == 0);
    }

    /**
     * 実値のレコードの一部の不足をテスト
     * @throws Exception
     */
    @Test
    public void testLackOfActualRecord() throws Exception {
        initDataHolder(TEST_FILE);
        dataHolder.sort(); // Inspector実行時のソートによりリストの順番が変わらないように
                           // 予めソートする
        List<Writable> actual = dataHolder.getActual();
        List<String> removedRecordTags = new ArrayList<String>();
        // 下のレコードをリストから削除する(逆順に定義する必要がある)
        int[] removeIndexs = {(actual.size() -1), (actual.size() - 2), 15, 11, 6, 2, 0};
        for(int index: removeIndexs) {
            removedRecordTags.add(((AllTypesWNoerr) actual.get(index)).getCTagAsString());
            actual.remove(index);
        }

        // エラーになることの確認
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        for (Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因の確認", Type.NO_ACTUAL_RECORD, cause.getType());
            AllTypesWNoerr model = (AllTypesWNoerr) cause.getExpect();
            String tag = model.getCTagAsString();
            assertTrue("エラーが報告されたレコードが削除されたレコードと同じタグであること", removedRecordTags.contains(tag));
            removedRecordTags.remove(tag);
        }
        assertEquals ("エラー原因のリストの要素数", 7, inspector.getCauses().size());
        assertTrue("削除したレコードとエラーが発生したレコードの数が同じ", removedRecordTags.size() == 0);
    }

    /**
     * NULL値の取り扱い指定が「通常比較」の場合のテスト
     * @throws Exception
     */
    @Test
    public void testNullNormal() throws Exception {
        initDataHolder(TEST_FILE_NULL_NORMAL);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);


        // エラー原因を確認しながら、エラーとなったレコードのCTAGとエラーと
        // なったカラムのセットを作成する
        Set<String> actualErrorSet = new HashSet<String>();
        for (Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因の確認", Type.COLUMN_VALUE_MISSMATCH, cause.getType());
            AllTypesWNoerr expect = (AllTypesWNoerr) cause.getExpect();
            AllTypesWNoerr actual = (AllTypesWNoerr) cause.getActual();
            String ctag = expect.getCTagAsString();
            String columnName = cause.getColumnInfo().getColumnName();
            actualErrorSet.add(ctag + ":" + columnName);
            if ("3".equals(ctag)) {
                assertFalse("期待値がNULLでない", actual.getCBigintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCIntOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCSmallintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCTinyintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCCharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCVcharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDateOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDatetimeOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal200Option().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal255Option().isNull());
                assertTrue("実値がNULL", expect.getCBigintOption().isNull());
                assertTrue("実値がNULL", expect.getCIntOption().isNull());
                assertTrue("実値がNULL", expect.getCSmallintOption().isNull());
                assertTrue("実値がNULL", expect.getCTinyintOption().isNull());
                assertTrue("実値がNULL", expect.getCCharOption().isNull());
                assertTrue("実値がNULL", expect.getCVcharOption().isNull());
                assertTrue("実値がNULL", expect.getCDateOption().isNull());
                assertTrue("実値がNULL", expect.getCDatetimeOption().isNull());
                assertTrue("実値がNULL", expect.getCDecimal200Option().isNull());
                assertTrue("実値がNULL", expect.getCDecimal255Option().isNull());
            } else if ("4".equals(ctag)){
                assertTrue("期待値がNULL", actual.getCBigintOption().isNull());
                assertTrue("期待値がNULL", actual.getCIntOption().isNull());
                assertTrue("期待値がNULL", actual.getCSmallintOption().isNull());
                assertTrue("期待値がNULL", actual.getCTinyintOption().isNull());
                assertTrue("期待値がNULL", actual.getCCharOption().isNull());
                assertTrue("期待値がNULL", actual.getCVcharOption().isNull());
                assertTrue("期待値がNULL", actual.getCDateOption().isNull());
                assertTrue("期待値がNULL", actual.getCDatetimeOption().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal200Option().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal255Option().isNull());
                assertFalse("実値がNULLでない", expect.getCBigintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCIntOption().isNull());
                assertFalse("実値がNULLでない", expect.getCSmallintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCTinyintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCCharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCVcharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDateOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDatetimeOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal200Option().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal255Option().isNull());
            }
        }

        Set<String> expectErrorSet = new HashSet<String>();
        expectErrorSet.add("3:C_BIGINT");
        expectErrorSet.add("3:C_INT");
        expectErrorSet.add("3:C_SMALLINT");
        expectErrorSet.add("3:C_TINYINT");
        expectErrorSet.add("3:C_CHAR");
        expectErrorSet.add("3:C_DATETIME");
        expectErrorSet.add("3:C_DATE");
        expectErrorSet.add("3:C_DECIMAL20_0");
        expectErrorSet.add("3:C_DECIMAL25_5");
        expectErrorSet.add("3:C_VCHAR");
        expectErrorSet.add("4:C_BIGINT");
        expectErrorSet.add("4:C_INT");
        expectErrorSet.add("4:C_SMALLINT");
        expectErrorSet.add("4:C_TINYINT");
        expectErrorSet.add("4:C_CHAR");
        expectErrorSet.add("4:C_DATETIME");
        expectErrorSet.add("4:C_DATE");
        expectErrorSet.add("4:C_DECIMAL20_0");
        expectErrorSet.add("4:C_DECIMAL25_5");
        expectErrorSet.add("4:C_VCHAR");
        assertTrue("期待したエラーが出力されている", expectErrorSet.equals(actualErrorSet));
    }

    /**
     * NULL値の取り扱い指定が「通常比較」の場合のテスト
     * @throws Exception
     */
    @Test
    public void testNullOk() throws Exception {
        initDataHolder(TEST_FILE_NULL_OK);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);


        // エラー原因を確認しながら、エラーとなったレコードのCTAGとエラーと
        // なったカラムのセットを作成する
        Set<String> actualErrorSet = new HashSet<String>();
        for (Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因の確認", Type.COLUMN_VALUE_MISSMATCH, cause.getType());
            AllTypesWNoerr expect = (AllTypesWNoerr) cause.getExpect();
            AllTypesWNoerr actual = (AllTypesWNoerr) cause.getActual();
            String ctag = expect.getCTagAsString();
            String columnName = cause.getColumnInfo().getColumnName();
            actualErrorSet.add(ctag + ":" + columnName);
            if ("3".equals(ctag)){
                assertFalse("期待値がNULLでない", actual.getCBigintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCIntOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCSmallintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCTinyintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCCharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCVcharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDateOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDatetimeOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal200Option().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal255Option().isNull());
                assertTrue("実値がNULL", expect.getCBigintOption().isNull());
                assertTrue("実値がNULL", expect.getCIntOption().isNull());
                assertTrue("実値がNULL", expect.getCSmallintOption().isNull());
                assertTrue("実値がNULL", expect.getCTinyintOption().isNull());
                assertTrue("実値がNULL", expect.getCCharOption().isNull());
                assertTrue("実値がNULL", expect.getCVcharOption().isNull());
                assertTrue("実値がNULL", expect.getCDateOption().isNull());
                assertTrue("実値がNULL", expect.getCDatetimeOption().isNull());
                assertTrue("実値がNULL", expect.getCDecimal200Option().isNull());
                assertTrue("実値がNULL", expect.getCDecimal255Option().isNull());
            }
        }

        Set<String> expectErrorSet = new HashSet<String>();
        expectErrorSet.add("3:C_BIGINT");
        expectErrorSet.add("3:C_INT");
        expectErrorSet.add("3:C_SMALLINT");
        expectErrorSet.add("3:C_TINYINT");
        expectErrorSet.add("3:C_CHAR");
        expectErrorSet.add("3:C_DATETIME");
        expectErrorSet.add("3:C_DATE");
        expectErrorSet.add("3:C_DECIMAL20_0");
        expectErrorSet.add("3:C_DECIMAL25_5");
        expectErrorSet.add("3:C_VCHAR");
        assertTrue("期待したエラーが出力されている", expectErrorSet.equals(actualErrorSet));
    }

    /**
     * NULL値の取り扱い指定が「常にNG」の場合のテスト
     * @throws Exception
     */
    @Test
    public void testNullNg() throws Exception {
        initDataHolder(TEST_FILE_NULL_NG);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);


        // エラー原因を確認しながら、エラーとなったレコードのCTAGとエラーと
        // なったカラムのセットを作成する
        Set<String> actualErrorSet = new HashSet<String>();
        for (Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因の確認", Type.COLUMN_VALUE_MISSMATCH, cause.getType());
            AllTypesWNoerr expect = (AllTypesWNoerr) cause.getExpect();
            AllTypesWNoerr actual = (AllTypesWNoerr) cause.getActual();
            String ctag = expect.getCTagAsString();
            String columnName = cause.getColumnInfo().getColumnName();
            actualErrorSet.add(ctag + ":" + columnName);
            if ("2".equals(ctag)) {
                assertTrue("期待値がNULL", actual.getCBigintOption().isNull());
                assertTrue("期待値がNULL", actual.getCIntOption().isNull());
                assertTrue("期待値がNULL", actual.getCSmallintOption().isNull());
                assertTrue("期待値がNULL", actual.getCTinyintOption().isNull());
                assertTrue("期待値がNULL", actual.getCCharOption().isNull());
                assertTrue("期待値がNULL", actual.getCVcharOption().isNull());
                assertTrue("期待値がNULL", actual.getCDateOption().isNull());
                assertTrue("期待値がNULL", actual.getCDatetimeOption().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal200Option().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal255Option().isNull());
                assertTrue("実値がNULL", expect.getCBigintOption().isNull());
                assertTrue("実値がNULL", expect.getCIntOption().isNull());
                assertTrue("実値がNULL", expect.getCSmallintOption().isNull());
                assertTrue("実値がNULL", expect.getCTinyintOption().isNull());
                assertTrue("実値がNULL", expect.getCCharOption().isNull());
                assertTrue("実値がNULL", expect.getCVcharOption().isNull());
                assertTrue("実値がNULL", expect.getCDateOption().isNull());
                assertTrue("実値がNULL", expect.getCDatetimeOption().isNull());
                assertTrue("実値がNULL", expect.getCDecimal200Option().isNull());
                assertTrue("実値がNULL", expect.getCDecimal255Option().isNull());
            } else if ("3".equals(ctag)) {
                assertFalse("期待値がNULLでない", actual.getCBigintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCIntOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCSmallintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCTinyintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCCharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCVcharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDateOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDatetimeOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal200Option().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal255Option().isNull());
                assertTrue("実値がNULL", expect.getCBigintOption().isNull());
                assertTrue("実値がNULL", expect.getCIntOption().isNull());
                assertTrue("実値がNULL", expect.getCSmallintOption().isNull());
                assertTrue("実値がNULL", expect.getCTinyintOption().isNull());
                assertTrue("実値がNULL", expect.getCCharOption().isNull());
                assertTrue("実値がNULL", expect.getCVcharOption().isNull());
                assertTrue("実値がNULL", expect.getCDateOption().isNull());
                assertTrue("実値がNULL", expect.getCDatetimeOption().isNull());
                assertTrue("実値がNULL", expect.getCDecimal200Option().isNull());
                assertTrue("実値がNULL", expect.getCDecimal255Option().isNull());
            } else if ("4".equals(ctag)){
                assertTrue("期待値がNULL", actual.getCBigintOption().isNull());
                assertTrue("期待値がNULL", actual.getCIntOption().isNull());
                assertTrue("期待値がNULL", actual.getCSmallintOption().isNull());
                assertTrue("期待値がNULL", actual.getCTinyintOption().isNull());
                assertTrue("期待値がNULL", actual.getCCharOption().isNull());
                assertTrue("期待値がNULL", actual.getCVcharOption().isNull());
                assertTrue("期待値がNULL", actual.getCDateOption().isNull());
                assertTrue("期待値がNULL", actual.getCDatetimeOption().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal200Option().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal255Option().isNull());
                assertFalse("実値がNULLでない", expect.getCBigintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCIntOption().isNull());
                assertFalse("実値がNULLでない", expect.getCSmallintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCTinyintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCCharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCVcharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDateOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDatetimeOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal200Option().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal255Option().isNull());
            }
        }

        Set<String> expectErrorSet = new HashSet<String>();
        expectErrorSet.add("2:C_BIGINT");
        expectErrorSet.add("2:C_INT");
        expectErrorSet.add("2:C_SMALLINT");
        expectErrorSet.add("2:C_TINYINT");
        expectErrorSet.add("2:C_CHAR");
        expectErrorSet.add("2:C_DATETIME");
        expectErrorSet.add("2:C_DATE");
        expectErrorSet.add("2:C_DECIMAL20_0");
        expectErrorSet.add("2:C_DECIMAL25_5");
        expectErrorSet.add("2:C_VCHAR");
        expectErrorSet.add("3:C_BIGINT");
        expectErrorSet.add("3:C_INT");
        expectErrorSet.add("3:C_SMALLINT");
        expectErrorSet.add("3:C_TINYINT");
        expectErrorSet.add("3:C_CHAR");
        expectErrorSet.add("3:C_DATETIME");
        expectErrorSet.add("3:C_DATE");
        expectErrorSet.add("3:C_DECIMAL20_0");
        expectErrorSet.add("3:C_DECIMAL25_5");
        expectErrorSet.add("3:C_VCHAR");
        expectErrorSet.add("4:C_BIGINT");
        expectErrorSet.add("4:C_INT");
        expectErrorSet.add("4:C_SMALLINT");
        expectErrorSet.add("4:C_TINYINT");
        expectErrorSet.add("4:C_CHAR");
        expectErrorSet.add("4:C_DATETIME");
        expectErrorSet.add("4:C_DATE");
        expectErrorSet.add("4:C_DECIMAL20_0");
        expectErrorSet.add("4:C_DECIMAL25_5");
        expectErrorSet.add("4:C_VCHAR");
        assertTrue("期待したエラーが出力されている", expectErrorSet.equals(actualErrorSet));
    }

    /**
     * NULL値の取り扱い指定が「常にNULLでないとき常にOK」の場合のテスト
     * @throws Exception
     */
    @Test
    public void testNotNullOk() throws Exception {
        initDataHolder(TEST_FILE_NOT_NULL_OK);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);


        // エラー原因を確認しながら、エラーとなったレコードのCTAGとエラーと
        // なったカラムのセットを作成する
        Set<String> actualErrorSet = new HashSet<String>();
        for (Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因の確認", Type.COLUMN_VALUE_MISSMATCH, cause.getType());
            AllTypesWNoerr expect = (AllTypesWNoerr) cause.getExpect();
            AllTypesWNoerr actual = (AllTypesWNoerr) cause.getActual();
            String ctag = expect.getCTagAsString();
            String columnName = cause.getColumnInfo().getColumnName();
            actualErrorSet.add(ctag + ":" + columnName);
            if ("1".equals(ctag)) {
                assertFalse("期待値がNULLでない", actual.getCBigintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCIntOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCSmallintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCTinyintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCCharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCVcharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDateOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDatetimeOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal200Option().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal255Option().isNull());
                assertFalse("実値がNULLでない", expect.getCBigintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCIntOption().isNull());
                assertFalse("実値がNULLでない", expect.getCSmallintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCTinyintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCCharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCVcharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDateOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDatetimeOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal200Option().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal255Option().isNull());

            } else if ("2".equals(ctag)) {
                assertTrue("期待値がNULL", actual.getCBigintOption().isNull());
                assertTrue("期待値がNULL", actual.getCIntOption().isNull());
                assertTrue("期待値がNULL", actual.getCSmallintOption().isNull());
                assertTrue("期待値がNULL", actual.getCTinyintOption().isNull());
                assertTrue("期待値がNULL", actual.getCCharOption().isNull());
                assertTrue("期待値がNULL", actual.getCVcharOption().isNull());
                assertTrue("期待値がNULL", actual.getCDateOption().isNull());
                assertTrue("期待値がNULL", actual.getCDatetimeOption().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal200Option().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal255Option().isNull());
                assertTrue("実値がNULL", expect.getCBigintOption().isNull());
                assertTrue("実値がNULL", expect.getCIntOption().isNull());
                assertTrue("実値がNULL", expect.getCSmallintOption().isNull());
                assertTrue("実値がNULL", expect.getCTinyintOption().isNull());
                assertTrue("実値がNULL", expect.getCCharOption().isNull());
                assertTrue("実値がNULL", expect.getCVcharOption().isNull());
                assertTrue("実値がNULL", expect.getCDateOption().isNull());
                assertTrue("実値がNULL", expect.getCDatetimeOption().isNull());
                assertTrue("実値がNULL", expect.getCDecimal200Option().isNull());
                assertTrue("実値がNULL", expect.getCDecimal255Option().isNull());
            } else if ("3".equals(ctag)) {
                assertFalse("期待値がNULLでない", actual.getCBigintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCIntOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCSmallintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCTinyintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCCharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCVcharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDateOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDatetimeOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal200Option().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal255Option().isNull());
                assertTrue("実値がNULL", expect.getCBigintOption().isNull());
                assertTrue("実値がNULL", expect.getCIntOption().isNull());
                assertTrue("実値がNULL", expect.getCSmallintOption().isNull());
                assertTrue("実値がNULL", expect.getCTinyintOption().isNull());
                assertTrue("実値がNULL", expect.getCCharOption().isNull());
                assertTrue("実値がNULL", expect.getCVcharOption().isNull());
                assertTrue("実値がNULL", expect.getCDateOption().isNull());
                assertTrue("実値がNULL", expect.getCDatetimeOption().isNull());
                assertTrue("実値がNULL", expect.getCDecimal200Option().isNull());
                assertTrue("実値がNULL", expect.getCDecimal255Option().isNull());
            } else if ("4".equals(ctag)){
                assertTrue("期待値がNULL", actual.getCBigintOption().isNull());
                assertTrue("期待値がNULL", actual.getCIntOption().isNull());
                assertTrue("期待値がNULL", actual.getCSmallintOption().isNull());
                assertTrue("期待値がNULL", actual.getCTinyintOption().isNull());
                assertTrue("期待値がNULL", actual.getCCharOption().isNull());
                assertTrue("期待値がNULL", actual.getCVcharOption().isNull());
                assertTrue("期待値がNULL", actual.getCDateOption().isNull());
                assertTrue("期待値がNULL", actual.getCDatetimeOption().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal200Option().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal255Option().isNull());
                assertFalse("実値がNULLでない", expect.getCBigintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCIntOption().isNull());
                assertFalse("実値がNULLでない", expect.getCSmallintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCTinyintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCCharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCVcharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDateOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDatetimeOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal200Option().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal255Option().isNull());
            }
        }

        Set<String> expectErrorSet = new HashSet<String>();
        expectErrorSet.add("4:C_BIGINT");
        expectErrorSet.add("4:C_INT");
        expectErrorSet.add("4:C_SMALLINT");
        expectErrorSet.add("4:C_TINYINT");
        expectErrorSet.add("4:C_CHAR");
        expectErrorSet.add("4:C_DATETIME");
        expectErrorSet.add("4:C_DATE");
        expectErrorSet.add("4:C_DECIMAL20_0");
        expectErrorSet.add("4:C_DECIMAL25_5");
        expectErrorSet.add("4:C_VCHAR");
        assertTrue("期待したエラーが出力されている", expectErrorSet.equals(actualErrorSet));
    }



    /**
     * NULL値の取り扱い指定が「常にNULLでないとき常にNG」の場合のテスト
     * @throws Exception
     */
    @Test
    public void testNotNullNg() throws Exception {
        initDataHolder(TEST_FILE_NOT_NULL_NG);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);


        // エラー原因を確認しながら、エラーとなったレコードのCTAGとエラーと
        // なったカラムのセットを作成する
        Set<String> actualErrorSet = new HashSet<String>();
        for (Cause cause: inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因の確認", Type.COLUMN_VALUE_MISSMATCH, cause.getType());
            AllTypesWNoerr expect = (AllTypesWNoerr) cause.getExpect();
            AllTypesWNoerr actual = (AllTypesWNoerr) cause.getActual();
            String ctag = expect.getCTagAsString();
            String columnName = cause.getColumnInfo().getColumnName();
            actualErrorSet.add(ctag + ":" + columnName);
            if ("1".equals(ctag)) {
                assertFalse("期待値がNULLでない", actual.getCBigintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCIntOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCSmallintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCTinyintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCCharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCVcharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDateOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDatetimeOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal200Option().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal255Option().isNull());
                assertFalse("実値がNULLでない", expect.getCBigintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCIntOption().isNull());
                assertFalse("実値がNULLでない", expect.getCSmallintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCTinyintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCCharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCVcharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDateOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDatetimeOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal200Option().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal255Option().isNull());

            } else if ("2".equals(ctag)) {
                assertTrue("期待値がNULL", actual.getCBigintOption().isNull());
                assertTrue("期待値がNULL", actual.getCIntOption().isNull());
                assertTrue("期待値がNULL", actual.getCSmallintOption().isNull());
                assertTrue("期待値がNULL", actual.getCTinyintOption().isNull());
                assertTrue("期待値がNULL", actual.getCCharOption().isNull());
                assertTrue("期待値がNULL", actual.getCVcharOption().isNull());
                assertTrue("期待値がNULL", actual.getCDateOption().isNull());
                assertTrue("期待値がNULL", actual.getCDatetimeOption().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal200Option().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal255Option().isNull());
                assertTrue("実値がNULL", expect.getCBigintOption().isNull());
                assertTrue("実値がNULL", expect.getCIntOption().isNull());
                assertTrue("実値がNULL", expect.getCSmallintOption().isNull());
                assertTrue("実値がNULL", expect.getCTinyintOption().isNull());
                assertTrue("実値がNULL", expect.getCCharOption().isNull());
                assertTrue("実値がNULL", expect.getCVcharOption().isNull());
                assertTrue("実値がNULL", expect.getCDateOption().isNull());
                assertTrue("実値がNULL", expect.getCDatetimeOption().isNull());
                assertTrue("実値がNULL", expect.getCDecimal200Option().isNull());
                assertTrue("実値がNULL", expect.getCDecimal255Option().isNull());
            } else if ("3".equals(ctag)) {
                assertFalse("期待値がNULLでない", actual.getCBigintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCIntOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCSmallintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCTinyintOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCCharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCVcharOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDateOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDatetimeOption().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal200Option().isNull());
                assertFalse("期待値がNULLでない", actual.getCDecimal255Option().isNull());
                assertTrue("実値がNULL", expect.getCBigintOption().isNull());
                assertTrue("実値がNULL", expect.getCIntOption().isNull());
                assertTrue("実値がNULL", expect.getCSmallintOption().isNull());
                assertTrue("実値がNULL", expect.getCTinyintOption().isNull());
                assertTrue("実値がNULL", expect.getCCharOption().isNull());
                assertTrue("実値がNULL", expect.getCVcharOption().isNull());
                assertTrue("実値がNULL", expect.getCDateOption().isNull());
                assertTrue("実値がNULL", expect.getCDatetimeOption().isNull());
                assertTrue("実値がNULL", expect.getCDecimal200Option().isNull());
                assertTrue("実値がNULL", expect.getCDecimal255Option().isNull());
            } else if ("4".equals(ctag)){
                assertTrue("期待値がNULL", actual.getCBigintOption().isNull());
                assertTrue("期待値がNULL", actual.getCIntOption().isNull());
                assertTrue("期待値がNULL", actual.getCSmallintOption().isNull());
                assertTrue("期待値がNULL", actual.getCTinyintOption().isNull());
                assertTrue("期待値がNULL", actual.getCCharOption().isNull());
                assertTrue("期待値がNULL", actual.getCVcharOption().isNull());
                assertTrue("期待値がNULL", actual.getCDateOption().isNull());
                assertTrue("期待値がNULL", actual.getCDatetimeOption().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal200Option().isNull());
                assertTrue("期待値がNULL", actual.getCDecimal255Option().isNull());
                assertFalse("実値がNULLでない", expect.getCBigintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCIntOption().isNull());
                assertFalse("実値がNULLでない", expect.getCSmallintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCTinyintOption().isNull());
                assertFalse("実値がNULLでない", expect.getCCharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCVcharOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDateOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDatetimeOption().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal200Option().isNull());
                assertFalse("実値がNULLでない", expect.getCDecimal255Option().isNull());
            }
        }

        Set<String> expectErrorSet = new HashSet<String>();
        expectErrorSet.add("1:C_BIGINT");
        expectErrorSet.add("1:C_INT");
        expectErrorSet.add("1:C_SMALLINT");
        expectErrorSet.add("1:C_TINYINT");
        expectErrorSet.add("1:C_CHAR");
        expectErrorSet.add("1:C_DATETIME");
        expectErrorSet.add("1:C_DATE");
        expectErrorSet.add("1:C_DECIMAL20_0");
        expectErrorSet.add("1:C_DECIMAL25_5");
        expectErrorSet.add("1:C_VCHAR");

        expectErrorSet.add("3:C_BIGINT");
        expectErrorSet.add("3:C_INT");
        expectErrorSet.add("3:C_SMALLINT");
        expectErrorSet.add("3:C_TINYINT");
        expectErrorSet.add("3:C_CHAR");
        expectErrorSet.add("3:C_DATETIME");
        expectErrorSet.add("3:C_DATE");
        expectErrorSet.add("3:C_DECIMAL20_0");
        expectErrorSet.add("3:C_DECIMAL25_5");
        expectErrorSet.add("3:C_VCHAR");

        expectErrorSet.add("4:C_BIGINT");
        expectErrorSet.add("4:C_INT");
        expectErrorSet.add("4:C_SMALLINT");
        expectErrorSet.add("4:C_TINYINT");
        expectErrorSet.add("4:C_CHAR");
        expectErrorSet.add("4:C_DATETIME");
        expectErrorSet.add("4:C_DATE");
        expectErrorSet.add("4:C_DECIMAL20_0");
        expectErrorSet.add("4:C_DECIMAL25_5");
        expectErrorSet.add("4:C_VCHAR");
        assertTrue("期待したエラーが出力されている", expectErrorSet.equals(actualErrorSet));
    }



    /**
     * カラムの比較条件に「検査対象外」を指定した場合のテスト
     * @throws Exception
     */
    @Test
    public void tetstInspectNone() throws Exception {
        initDataHolder(TEST_FILE_INSPECT_NONE);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
        }
        assertTrue("検査成功", inspector.isSuccess());
        assertTrue("エラー原因のリストが空", inspector.getCauses().size() == 0);
    }

    /**
     * カラムの比較条件に「現在時刻」を指定した場合のテスト
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testInspectNow() throws Exception {
        // NGとなるケース(実値が過去)
        initDataHolder(TEST_FILE_INSPECT_NOW);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            //            System.out.println(cause.getMessage());
            String columnName = cause.getColumnInfo().getColumnName();
            if ("C_DATETIME".equals(columnName)) {
                assertEquals("エラー原因のチェック", Type.NOT_IN_TESTING_TIME, cause.getType());
            } else {
                assertEquals("エラー原因のチェック", Type.CONDITION_NOW_ON_INVALID_COLUMN, cause.getType());
            }
        }
        assertEquals("エラー原因のリストの要素数", 40, inspector.getCauses().size());
        assertFalse("検査失敗", inspector.isSuccess());

        // OKとなるケース(実値に現在時刻を指定して、inspect()を実行)
        for (AllTypesWNoerr model : getActualList()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int s = cal.get(Calendar.SECOND);
            int days = DateUtil.getDayFromDate(y, m + 1, d);
            int secs = DateUtil.getSecondFromTime(h, min, s);
            DateTime dt = new DateTime();
            dt.setElapsedSeconds((long) days * 86400 + secs);
            model.getCDatetimeOption().modify(dt);

            Date date = new Date();
            date.setElapsedDays(days);
            model.getCDateOption().modify(date);
        }
        inspector.clear();
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            //            System.out.println(cause.getMessage());
            if (cause.getType().equals(Type.NOT_IN_TESTING_TIME)) {
                System.out.println(cause.getMessage());
            }
            assertEquals("エラー原因のチェック", Type.CONDITION_NOW_ON_INVALID_COLUMN, cause.getType());
        }
        assertEquals("エラー原因のリストの要素数", 36, inspector.getCauses().size()); // 4件成功するのでNG件数が40-4=36(件)となる。
        assertFalse("検査失敗", inspector.isSuccess());

        // NGとなるケース(実値に未来時刻を指定して、inspect()を実行)
        for (AllTypesWNoerr model : getActualList()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int s = cal.get(Calendar.SECOND);
            int days = DateUtil.getDayFromDate(y + 1, m + 1, d); // 1年後を指定
            int secs = DateUtil.getSecondFromTime(h, min, s);
            DateTime dt = new DateTime();
            dt.setElapsedSeconds((long) days * 86400 + secs);
            model.getCDatetimeOption().modify(dt);

            Date date = new Date();
            date.setElapsedDays(days);
            model.getCDateOption().modify(date);
        }
        inspector.clear();
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            String columnName = cause.getColumnInfo().getColumnName();
            if ("C_DATETIME".equals(columnName)) {
                assertEquals("エラー原因のチェック", Type.NOT_IN_TESTING_TIME, cause.getType());
            } else {
                assertEquals("エラー原因のチェック", Type.CONDITION_NOW_ON_INVALID_COLUMN, cause.getType());
            }
        }
        assertEquals("エラー原因のリストの要素数", 40, inspector.getCauses().size());
        assertFalse("検査失敗", inspector.isSuccess());
    }

    /**
     * カラムの比較条件に「現在日」を指定した場合のテスト
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testInspectToday() throws Exception {
        // NGとなるケース(実値が過去)
        initDataHolder(TEST_FILE_INSPECT_TODAY);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            String columnName = cause.getColumnInfo().getColumnName();
            if ("C_DATETIME".equals(columnName) || "C_DATE".equals(columnName)) {
                assertEquals("エラー原因のチェック", Type.NOT_IN_TEST_DAY, cause.getType());
            } else {
                assertEquals("エラー原因のチェック", Type.CONDITION_TODAY_ON_INVALID_COLUMN, cause.getType());
            }
        }
        assertEquals("エラー原因のリストの要素数", 40, inspector.getCauses().size());
        assertFalse("検査失敗", inspector.isSuccess());

        // OKとなるケース(実値に現在時刻を指定して、inspect()を実行)
        for (AllTypesWNoerr model : getActualList()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int s = cal.get(Calendar.SECOND);
            int days = DateUtil.getDayFromDate(y, m + 1, d);
            int secs = DateUtil.getSecondFromTime(h, min, s);
            DateTime dt = new DateTime();
            dt.setElapsedSeconds((long) days * 86400 + secs);
            model.getCDatetimeOption().modify(dt);

            Date date = new Date();
            date.setElapsedDays(days);
            model.getCDateOption().modify(date);
        }
        inspector.clear();
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因のチェック", Type.CONDITION_TODAY_ON_INVALID_COLUMN, cause.getType());
        }
        assertEquals("エラー原因のリストの要素数", 32, inspector.getCauses().size()); // 8件成功するのでNG件数が40-8=36(件)となる。
        assertFalse("検査失敗", inspector.isSuccess());

        // OKとなるケース(実値に現在日の00:00:00を指定して、inspect()を実行)
        for (AllTypesWNoerr model : getActualList()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int days = DateUtil.getDayFromDate(y, m + 1, d);
            DateTime dt = new DateTime();
            dt.setElapsedSeconds((long) days * 86400);
            model.getCDatetimeOption().modify(dt);

            Date date = new Date();
            date.setElapsedDays(days);
            model.getCDateOption().modify(date);
        }
        inspector.clear();
        // テストが日をまたいで実行された場合に期待したテストができなくなるので、
        // finishTimeにstartTimeと同じ値を入れ、日をまたがないでテストが終了
        // した状況を作り出す。
        inspector.setFinishTime(inspector.getStartTime());
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因のチェック", Type.CONDITION_TODAY_ON_INVALID_COLUMN, cause.getType());
        }
        assertEquals("エラー原因のリストの要素数", 32, inspector.getCauses().size()); // 8件成功するのでNG件数が40-8=36(件)となる。
        assertFalse("検査失敗", inspector.isSuccess());

        // NGとなるケース(実値に翌日の00:00:00を指定して、inspect()を実行)
        for (AllTypesWNoerr model : getActualList()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int days = DateUtil.getDayFromDate(y, m + 1, d) + 1;
            DateTime dt = new DateTime();
            dt.setElapsedSeconds((long) days * 86400);
            model.getCDatetimeOption().modify(dt);

            Date date = new Date();
            date.setElapsedDays(days);
            model.getCDateOption().modify(date);
        }
        inspector.clear();

        // テストが日をまたいで実行された場合に期待したテストができなくなるので、
        // finishTimeにstartTimeと同じ値を入れ、日をまたがないでテストが終了
        // した状況を作り出す。
        inspector.setFinishTime(inspector.getStartTime());
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            String columnName = cause.getColumnInfo().getColumnName();
            if ("C_DATETIME".equals(columnName) || "C_DATE".equals(columnName)) {
                assertEquals("エラー原因のチェック", Type.NOT_IN_TEST_DAY, cause.getType());
            } else {
                assertEquals("エラー原因のチェック", Type.CONDITION_TODAY_ON_INVALID_COLUMN, cause.getType());
            }
        }
        assertEquals("エラー原因のリストの要素数", 40, inspector.getCauses().size());
        assertFalse("検査失敗", inspector.isSuccess());

        // OKとなるケース(実値に翌日の00:00:00を指定して、inspect()を実行、テストが日を跨ぐケース)
        for (AllTypesWNoerr model : getActualList()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int days = DateUtil.getDayFromDate(y, m + 1, d) + 1;
            DateTime dt = new DateTime();
            dt.setElapsedSeconds((long) days * 86400);
            model.getCDatetimeOption().modify(dt);

            Date date = new Date();
            date.setElapsedDays(days);
            model.getCDateOption().modify(date);
        }
        inspector.clear();
        // テストが日をまたいで実行された状況を作り出す。
        inspector.setFinishTime(inspector.getStartTime() + 86400 * 1000 );
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            String columnName = cause.getColumnInfo().getColumnName();
            if ("C_DATETIME".equals(columnName) || "C_DATE".equals(columnName)) {
                assertEquals("エラー原因のチェック", Type.NOT_IN_TEST_DAY, cause.getType());
            } else {
                assertEquals("エラー原因のチェック", Type.CONDITION_TODAY_ON_INVALID_COLUMN, cause.getType());
            }
        }


        // NGとなるケース(実値に未来時刻を指定して、inspect()を実行)
        for (AllTypesWNoerr model : getActualList()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int s = cal.get(Calendar.SECOND);
            int days = DateUtil.getDayFromDate(y + 1, m + 1, d); // 1年後を指定
            int secs = DateUtil.getSecondFromTime(h, min, s);
            DateTime dt = new DateTime();
            dt.setElapsedSeconds((long) days * 86400 + secs);
            model.getCDatetimeOption().modify(dt);

            Date date = new Date();
            date.setElapsedDays(days);
            model.getCDateOption().modify(date);
        }
        inspector.clear();
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            String columnName = cause.getColumnInfo().getColumnName();
            if ("C_DATETIME".equals(columnName) || "C_DATE".equals(columnName)) {
                assertEquals("エラー原因のチェック", Type.NOT_IN_TEST_DAY, cause.getType());
            } else {
                assertEquals("エラー原因のチェック", Type.CONDITION_TODAY_ON_INVALID_COLUMN, cause.getType());
            }
        }
        assertEquals("エラー原因のリストの要素数", 40, inspector.getCauses().size());
        assertFalse("検査失敗", inspector.isSuccess());

        // NGとなるケース(実値に翌日の00:00:00を指定して、inspect()を実行)
        for (AllTypesWNoerr model : getActualList()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int days = DateUtil.getDayFromDate(y, m + 1, d);
            DateTime dt = new DateTime();
            dt.setElapsedSeconds((long) days * 86400);
            model.getCDatetimeOption().modify(dt);

            Date date = new Date();
            date.setElapsedDays(days);
            model.getCDateOption().modify(date);
        }
        inspector.clear();
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            assertEquals("エラー原因のチェック", Type.CONDITION_TODAY_ON_INVALID_COLUMN, cause.getType());
        }
        assertEquals("エラー原因のリストの要素数", 32, inspector.getCauses().size()); // 8件成功するのでNG件数が40-8=36(件)となる。
        assertFalse("検査失敗", inspector.isSuccess());
    }


    /**
     * カラムの比較条件に「部分一致」を指定した場合のテスト
     * @throws Exception
     */
    @Test
    public void tetstInspectPartial() throws Exception {
        // NULL値と非NULL値の組み合わせ
        initDataHolder(TEST_FILE_INSPECT_PARTIAL);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            if (cause.getColumnInfo().getColumnName().equals("C_CHAR") || cause.getColumnInfo().getColumnName().equals("C_VCHAR")) {
                assertEquals("エラー原因のチェック", Type.COLUMN_VALUE_MISSMATCH, cause.getType());
            } else {
                assertEquals("エラー原因のチェック", Type.CONDITION_PARTIAL_ON_INVALID_COLUMN, cause.getType());
            }
        }
        assertFalse("検査失敗", inspector.isSuccess());
        assertEquals("エラー原因のリストが要素数", 36, inspector.getCauses().size());
    }

    /**
     * カラムの比較条件に「部分一致」を指定した場合のテスト2
     * @throws Exception
     */
    @Test
    public void tetstInspectPartial2() throws Exception {
        // 部分文字列、NULL文字列の組み合わせ、および実データが期待データの部分文字列の場合
        initDataHolder(TEST_FILE_INSPECT_PARTIAL2);
        DefaultInspector inspector = new DefaultInspector();
        inspector.setColumnInfos(dataHolder.getColumnInfos());
        inspector.setStartTime(System.currentTimeMillis());
        inspector.inspect(dataHolder);
        for (Cause cause : inspector.getCauses()) {
            System.out.println(cause.getMessage());
            if (cause.getColumnInfo().getColumnName().equals("C_CHAR") || cause.getColumnInfo().getColumnName().equals("C_VCHAR")) {
                assertEquals("エラー原因のチェック", Type.COLUMN_VALUE_MISSMATCH, cause.getType());
                AllTypesWNoerr model = (AllTypesWNoerr) cause.getActual();
                int tagNo = Integer.parseInt(model.getCTagAsString());
                assertTrue("エラーが発生したレコードのタグを確認", (2 <= tagNo && tagNo <=4));
            } else {
                fail("文字列型以外のカラムではエラーは発生しないはず");
            }
        }
        assertFalse("検査失敗", inspector.isSuccess());
        assertEquals("エラー原因のリストが要素数", 6, inspector.getCauses().size());
    }


    /**
     * 実値のモデルオブジェクトのリストを返す
     * @return 実値のモデルオブジェクトのリスト
     */
    private List<AllTypesWNoerr> getActualList() {
        List<AllTypesWNoerr> list = new ArrayList<AllTypesWNoerr>();
        for(Writable model: dataHolder.getActual()) {
            list.add((AllTypesWNoerr) model);
        }
        return list;
    }

    //TODO rev.8702でバグとなったケースのテストを追加する。


}
