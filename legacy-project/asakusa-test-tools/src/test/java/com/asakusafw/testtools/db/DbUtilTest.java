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
package com.asakusafw.testtools.db;


import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.asakusafw.testtools.ColumnInfo;
import com.asakusafw.testtools.excel.ExcelUtils;

public class DbUtilTest {
    /**
     * テーブル作成のテスト
     * @throws Exception
     */
    @Test
    public void testCreateTable01() throws Exception {
        // カラム情報のリストを生成
        String filename = "src/test/data/Excel/ExcelUtils/ALLT_TYPES_W_NOERR.xls";
        ExcelUtils excelUtils =  new ExcelUtils(filename);
        List<ColumnInfo> list = excelUtils.getColumnInfos();

        Connection conn = null;
        try {
            conn = DbUtils.getConnection();
            // テーブル作成前にテーブルがあれば削除する
            String tablename = list.get(0).getTableName();
            DbUtils.dropTable(conn, tablename);
            // テーブル作成前を削除
            DbUtils.createTable(conn, list);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }


    /**
     * テーブル作成のテスト(空のリストを渡したときエラー)
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testCreateTable02() throws Exception {
        // カラム情報のリストを生成
        List<ColumnInfo> list = new ArrayList<ColumnInfo>();
        Connection conn = null;
        try {
            conn = DbUtils.getConnection();
            DbUtils.createTable(conn, list);
        } catch (RuntimeException e) {
            assertEquals("カラム情報のリストが空", e.getMessage());
            throw e;
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     * テーブル削除のテスト
     * @throws Exception
     */
    @Test
    public void testDropTable() throws Exception {
        // カラム情報のリストを生成
        String filename = "src/test/data/Excel/ExcelUtils/BAR.xls";
        ExcelUtils excelUtils =  new ExcelUtils(filename);
        List<ColumnInfo> list = excelUtils.getColumnInfos();

        Connection conn = null;
        try {
            // テーブル作成前にテーブルがあれば削除する
            String tablename = list.get(0).getTableName();
            conn = DbUtils.getConnection();
            DbUtils.dropTable(conn, tablename);

            // テーブルを作成
            DbUtils.createTable(conn, list);

            // 作成したテーブルを削除
            DbUtils.dropTable(conn, tablename);

            // もう一度削除(テーブルがなくても削除に失敗しない
            DbUtils.dropTable(conn, tablename);

        } finally {
            DbUtils.closeQuietly(conn);
        }
    }



}
