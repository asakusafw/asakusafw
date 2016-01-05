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
package com.asakusafw.modelgen.source;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.modelgen.Constants;
import com.asakusafw.modelgen.ModelMatcher;
import com.asakusafw.modelgen.model.Attribute;
import com.asakusafw.modelgen.model.DecimalType;
import com.asakusafw.modelgen.model.ModelDescription;
import com.asakusafw.modelgen.model.ModelRepository;
import com.asakusafw.modelgen.model.PropertyTypeKind;
import com.asakusafw.modelgen.model.StringType;
import com.asakusafw.modelgen.util.TableModelBuilder;
import com.asakusafw.modelgen.view.ViewAnalyzer;
import com.asakusafw.modelgen.view.ViewDefinition;
import com.asakusafw.modelgen.view.ViewParser;
import com.asakusafw.modelgen.view.model.CreateView;

/**
 * データベースからテーブル情報を収集する。
 */
public class DatabaseSource implements Closeable {

    @SuppressWarnings("unused")
    private static final String STR_IS_PK = "PRI"; // PKを表す文字列
    private static final String STR_NOT_NULL = "NO"; // NOT NULLを表す文字列

    static final Logger LOG = LoggerFactory.getLogger(DatabaseSource.class);

    private Connection conn;

    private String databaseName;

    /**
     * インスタンスを生成する。
     * @param jdbcDriver ドライバ名
     * @param jdbcUrl 接続のためのJDBC URL
     * @param user ユーザ名
     * @param password パスワード
     * @param databaseName データベース名
     * @throws IOException 初期化に失敗した場合
     * @throws SQLException DBコネクションの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public DatabaseSource(
            String jdbcDriver,
            String jdbcUrl,
            String user,
            String password,
            String databaseName) throws IOException, SQLException {
        if (jdbcDriver == null) {
            throw new IllegalArgumentException("jdbcDriver must not be null"); //$NON-NLS-1$
        }
        if (jdbcUrl == null) {
            throw new IllegalArgumentException("jdbcUrl must not be null"); //$NON-NLS-1$
        }
        if (user == null) {
            throw new IllegalArgumentException("user must not be null"); //$NON-NLS-1$
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null"); //$NON-NLS-1$
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("databaseName must not be null"); //$NON-NLS-1$
        }
        this.databaseName = databaseName;
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new IOException("JDBC Drivr not found", e);
        }
        conn = DriverManager.getConnection(jdbcUrl, user, password);
    }

    /**
     * データベースからテーブル情報を収集し、モデルの情報一覧を返す。
     * @param filter 対象に含める情報
     * @return テーブル情報を元に生成したモデル情報の一覧
     * @throws IOException モデル情報の収集に失敗した場合
     * @throws SQLException DBアクセスに失敗した場合
     */
    public List<ModelDescription> collectTables(
            ModelMatcher filter) throws IOException, SQLException {
        String sql = ""
            + "SELECT"
            + "   columns.TABLE_NAME,"
            + "   columns.COLUMN_NAME,"
            + "   columns.COLUMN_COMMENT,"
            + "   columns.DATA_TYPE,"
            + "   columns.CHARACTER_MAXIMUM_LENGTH,"
            + "   columns.NUMERIC_PRECISION,"
            + "   columns.NUMERIC_SCALE,"
            + "   columns.IS_NULLABLE,"
            + "   columns.COLUMN_KEY"
            + " FROM INFORMATION_SCHEMA.COLUMNS AS columns"
            + " JOIN INFORMATION_SCHEMA.TABLES AS tables"
            + " WHERE columns.TABLE_SCHEMA = tables.TABLE_SCHEMA"
            + " AND columns.TABLE_NAME = tables.TABLE_NAME"
            + " AND columns.TABLE_SCHEMA = ?"
            + " AND tables.TABLE_TYPE = 'BASE TABLE'"
            + " ORDER BY TABLE_NAME, ORDINAL_POSITION";

        List<ModelDescription> results = new ArrayList<ModelDescription>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, databaseName);
            rs = ps.executeQuery();

            String prevTableName = null;
            TableModelBuilder builder = null;
            while (rs.next()) {
                // カラム情報の取り出し
                String tableName = rs.getString(1);
                String columnName = rs.getString(2);
                String columnComment = rs.getString(3);
                String dataType = rs.getString(4);
                long characterMaximumLength = rs.getLong(5);
                int numericPrecision = rs.getInt(6);
                int numericScale = rs.getInt(7);
                String isNullable = rs.getString(8);
                String columnKey = rs.getString(9);

                if (filter.acceptModel(tableName) == false) {
                    if (tableName.equals(prevTableName) == false) {
                        LOG.info("テーブル{}はユーザの指定によりスキップされます", tableName);
                        prevTableName = tableName;
                    }
                    continue;
                }

                // 対象テーブルが変わったかを確認
                if (builder == null
                        || prevTableName == null
                        || prevTableName.equals(tableName) == false) {
                    if (builder != null) {
                        results.add(builder.toDescription());
                    }
                    builder = new TableModelBuilder(tableName);
                    builder.namespace(Constants.SOURCE_TABLE);
                    prevTableName = tableName;
                }

                // データ型からプロパティの型を得る
                PropertyTypeKind propertyType = MySqlDataType.getPropertyTypeByString(dataType);
                if (propertyType == null) {
                    LOG.error("データ型{}は未サポートのため、無視されます({}:{})", new Object[] {
                            dataType,
                            tableName,
                            columnName,
                    });
                    continue;
                }

                // Attributeに設定すべき項目があるか調べる
                // 現状は、NOT NULL制約と、PRIMARY KEY制約にのみ対応
                ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
                if (isNullable != null && isNullable.equals(STR_NOT_NULL)) {
                    attributeList.add(Attribute.NOT_NULL);
                }
                if (columnKey != null && columnKey.equals(MySQLConstants.STR_IS_PK)) {
                    attributeList.add(Attribute.PRIMARY_KEY);
                }

                Attribute[] attributes = attributeList.toArray(new Attribute[attributeList.size()]);
                switch (propertyType) {
                case BIG_DECIMAL:
                    DecimalType decimalType = new DecimalType(numericPrecision, numericScale);
                    builder.add(columnComment, columnName, decimalType, attributes);
                    break;
                case STRING:
                    StringType stringType = new StringType((int) characterMaximumLength);
                    builder.add(columnComment, columnName, stringType, attributes);
                    break;
                default:
                    builder.add(columnComment, columnName, propertyType, attributes);
                    break;
                }
            }
            if (builder != null) {
                results.add(builder.toDescription());
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    // ignored
                    LOG.debug("Failed to close ResultSet", e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    // ignored
                    LOG.debug("Failed to close PreparedStatement", e);
                }
            }
        }
        return results;
    }

    /**
     * データベースからView情報を収集し、モデルの情報一覧を返す。
     * @param repository ここまでの情報を含むリポジトリ
     * @param filter 利用するフィルタ
     * @return View情報を元に生成したモデル情報の一覧
     * @throws IOException モデル情報の収集に失敗した場合
     * @throws SQLException View情報の取得に失敗した場合
     */
    public List<ModelDescription> collectViews(
            ModelRepository repository,
            ModelMatcher filter) throws IOException, SQLException {

        List<ViewDefinition> definitions = collectViewDefinitions(filter);
        LOG.info("{}個のビュー定義を読み込みました", definitions.size());

        ViewAnalyzer analyzer = new ViewAnalyzer();
        for (ViewDefinition definition : definitions) {
            LOG.info("ビュー\"{}\"を解析しています", definition.name);
            CreateView tree = ViewParser.parse(definition);
            analyzer.add(Collections.singletonList(Constants.SOURCE_VIEW), tree);
        }

        List<ModelDescription> results = analyzer.analyze(repository);
        return results;
    }

    /**
     * データベースからビューの定義情報を収集し、ビュー名とSELECT文に対応する文字列のペアを返す。
     * @param filter 利用するフィルタ
     * @return ビューの定義情報
     * @throws IOException ビューの定義情報の収集に失敗した場合
     * @throws SQLException SQLの発行に失敗した場合
     */
    private List<ViewDefinition> collectViewDefinitions(
            ModelMatcher filter) throws IOException, SQLException {
        String sql = "SELECT TABLE_NAME, VIEW_DEFINITION"
            + " FROM INFORMATION_SCHEMA.VIEWS"
            + " WHERE TABLE_SCHEMA = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<ViewDefinition> results = new ArrayList<ViewDefinition>();

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, databaseName);
            rs = ps.executeQuery();

            while (rs.next()) {
                // カラム情報の取り出し
                String viewName = rs.getString(1);
                if (filter.acceptModel(viewName) == false) {
                    LOG.info("ビュー{}はユーザの指定によりスキップされます", viewName);
                    continue;
                }
                String statement = rs.getString(2);
                results.add(new ViewDefinition(viewName, statement));
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    // ignored
                    LOG.debug("Failed to close ResultSet", e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    // ignored
                    LOG.debug("Failed to close PreparedStatement", e);
                }
            }
        }
        return results;
    }

    @Override
    public void close() throws IOException {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                // ignored
                LOG.debug("Failed to close Connection", e);
            }
        }
    }
}
