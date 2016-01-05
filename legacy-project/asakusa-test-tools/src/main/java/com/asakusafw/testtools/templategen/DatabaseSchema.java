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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.modelgen.source.MySQLConstants;
import com.asakusafw.modelgen.source.MySqlDataType;
import com.asakusafw.testtools.ColumnInfo;

/**
 * データベース上のスキーマを分析する。
 */
public final class DatabaseSchema {

    /**
     * データベース上のINFORMATION_SCHEMAテーブルを分析し、指定のテーブルのカラム情報を返す。
     * @param conn 利用するコネクション
     * @param databaseName データベース名
     * @param tableName テーブル名
     * @return カラム情報
     * @throws SQLException DBアクセスに失敗した場合
     */
    public static ColumnInfo[] collectColumns(
            Connection conn,
            String databaseName,
            String tableName) throws SQLException {
        String sql = ""
            + " SELECT"
            + "   COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE,"
            + "   CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE,"
            + "   IS_NULLABLE, COLUMN_KEY"
            + " FROM INFORMATION_SCHEMA.COLUMNS"
            + " WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?"
            + " ORDER BY ORDINAL_POSITION";

        List<ColumnInfo> list = new ArrayList<ColumnInfo>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, databaseName);
            ps.setString(2, tableName);
            rs = ps.executeQuery();

            while (rs.next()) {
                // カラム情報の取り出し
                String columnName = rs.getString(1);
                String columnComment = rs.getString(2);
                String dataTypeStr = rs.getString(3);
                long characterMaximumLength = rs.getLong(4);
                int numericPrecision = rs.getInt(5);
                int numericScale = rs.getInt(6);
                String isNullableStr = rs.getString(7);
                String columnKeyStr = rs.getString(8);

                // MySQLのデータ型
                MySqlDataType dataType = MySqlDataType.getDataTypeByString(dataTypeStr);
                if (dataType == null) {
                    throw new RuntimeException("MySQLのデータ型(" + dataTypeStr + ")" + "は未サポートです");
                }

                // NULL可
                boolean nullable = true;
                if (isNullableStr != null && isNullableStr.equals(MySQLConstants.STR_NOT_NULL)) {
                    nullable = false;
                }

                // Primary Key
                boolean pk = false;
                if (columnKeyStr != null && columnKeyStr.equals(MySQLConstants.STR_IS_PK)) {
                    pk = true;
                }

                ColumnInfo info = new ColumnInfo(tableName, columnName,
                        columnComment, dataType, characterMaximumLength,
                        numericPrecision, numericScale, nullable, pk, null, null);
                list.add(info);
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
        if (list.size() == 0) {
            throw new RuntimeException("Cannot find a table: '" + tableName + "' on database: '" + databaseName + "'");
        }

        return list.toArray(new ColumnInfo[list.size()]);
    }

    private DatabaseSchema() {
        return;
    }
}
