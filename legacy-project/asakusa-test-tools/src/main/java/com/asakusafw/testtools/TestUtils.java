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
package com.asakusafw.testtools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.testtools.db.DbUtils;
import com.asakusafw.testtools.excel.ExcelUtils;
import com.asakusafw.testtools.inspect.Cause;
import com.asakusafw.testtools.inspect.DefaultInspector;
import com.asakusafw.testtools.inspect.Inspector;

/**
 * テストツールのエントリ。
 */
public class TestUtils {

    /**
     * テーブル名をキーに、テストデータを保持するマップ。
     */
    private final Map<String, TestDataHolder> dataHolderMap = new HashMap<String, TestDataHolder>();


    /**
     * テーブル名をキーに、検査に使用するクラスを保持するマップ。
     */
    private final Map<String, Inspector> inspectorMap = new HashMap<String, Inspector>();

    /**
     * テストがNGだったとき原因。
     */
    private final List<Cause> causes = new ArrayList<Cause>();

    /**
     * オブジェクトの生成時刻。
     */
    long startTime;

    /**
     * コンストラクタ、 Excelファイルを含むディレクトリを指定する。
     * @param dir 対象のディレクトリ
     * @throws IOException ファイルの読み出しに失敗した場合
     */
    public TestUtils(File dir) throws IOException {
        if (!dir.isDirectory()) {
            throw new IOException(MessageFormat.format(
                    "{0} is not a directory",
                    dir.getAbsolutePath()));
        }
        List<File> excelFileList = collectExcelFileList(dir);
        init(excelFileList);
    }

    private List<File> collectExcelFileList(File dir) {
        File[] files = dir.listFiles();
        List<File> excelFileList = new ArrayList<File>();
        for (File file : files) {
            String filename = file.getAbsolutePath();
            String lowcaseFilename = filename.toLowerCase();
            if (lowcaseFilename.endsWith(".xls")) {
                excelFileList.add(file);
            }
        }
        Collections.sort(excelFileList);
        return excelFileList;
    }

    /**
     * コンストラクタ、Excelファイルのリストを指定する。
     * @param excelFileList Excelファイルの一覧
     * @throws IOException Excelファイルの読み出しに失敗した場合
     */
    public TestUtils(List<File> excelFileList) throws IOException {
        init(excelFileList);
    }


    /**
     * 指定のExcelファイルを読み込み、初期化する。
     * @param excelFileList Excelファイルの一覧
     * @throws IOException Excelファイルの読み出しに失敗した場合
     */
    private void init(List<File> excelFileList) throws IOException {
        for (File file : excelFileList) {
            String filename = file.getAbsolutePath();
            String lowcaseFilename = filename.toLowerCase();
            if (!lowcaseFilename.endsWith(".xls")) {
                throw new IOException(MessageFormat.format(
                        "Excelファイルではありません({0})",
                        file.getAbsolutePath()));
            }
            ExcelUtils excelUtils = new ExcelUtils(filename);
            TestDataHolder dataHolder = excelUtils.getTestDataHolder();
            dataHolderMap.put(dataHolder.getTablename(), dataHolder);
        }
        startTime = System.currentTimeMillis();
    }

    /**
     * テストデータをデータベースに書き込みます。
     * @param createTable trueのときテーブルをdrop/createし、falseのときtruncateする
     */
    public void storeToDatabase(boolean createTable) {
        Connection conn = null;
        try {
            conn = DbUtils.getConnection();
            clearCache(conn);
            for (TestDataHolder dataHolder : dataHolderMap.values()) {
                dataHolder.storeToDatabase(conn, createTable);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    private void clearCache(Connection connection) {
        assert connection != null;
        try {
            Statement statement = connection.createStatement();
            try {
                statement.execute("TRUNCATE TABLE __TG_CACHE_INFO");
                statement.execute("TRUNCATE TABLE __TG_CACHE_LOCK");
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            System.err.println("キャッシュの削除に失敗しました");
            e.printStackTrace();
        }
    }

    /**
     * 実行結果をデータベースから取り込みます。
     */
    public void loadFromDatabase() {
        Connection conn = null;
        try {
            conn = DbUtils.getConnection();
            for (TestDataHolder dataHolder : dataHolderMap.values()) {
                dataHolder.loadFromDatabase(conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     * テストデータをシーケンスファイルに書き込みます。
     * @param tablename 対象テストデータのテーブル名
     * @param writer 書き込み先
     * @deprecated Use {@link #storeToTemporary(String, Configuration, Path)} instead
     */
    @Deprecated
    public void storeToSequenceFile(String tablename, SequenceFile.Writer writer) {
        TestDataHolder dataHolder = dataHolderMap.get(tablename);
        try {
            dataHolder.store(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 実行結果をシーケンスファイルから取り込みます。
     * @param tablename 対象実行データのテーブル名
     * @param reader 取り込み元
     * @deprecated Use {@link #loadFromTemporary(String, Configuration, Path)} instead
     */
    @Deprecated
    public void loadFromSequenceFile(String tablename, SequenceFile.Reader reader) {
        TestDataHolder dataHolder = dataHolderMap.get(tablename);
        try {
            dataHolder.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads dataset from the temporary area.
     * @param tableName corresponded table name
     * @param conf current configuration
     * @param path source path
     * @throws IOException if failed to load dataset
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    public void loadFromTemporary(String tableName, Configuration conf, Path path) throws IOException {
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        TestDataHolder dataHolder = dataHolderMap.get(tableName);
        ModelInput<?> input = TemporaryStorage.openInput(conf, dataHolder.getModelClass(), path);
        try {
            dataHolder.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            input.close();
        }
    }

    /**
     * Stores dataset into the temporary area.
     * @param tableName corresponded table name
     * @param conf current configuration
     * @param path source path
     * @throws IOException if failed to store dataset
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    public void storeToTemporary(String tableName, Configuration conf, Path path) throws IOException {
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        TestDataHolder dataHolder = dataHolderMap.get(tableName);
        ModelOutput<?> output = TemporaryStorage.openOutput(conf, dataHolder.getModelClass(), path);
        try {
            dataHolder.store(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            output.close();
        }
    }

    /**
     * テストがNGだったときの理由を表す文字列を取得します。
     * @return テストがNGだったときの理由を表す文字列
     */
    public String getCauseMessage() {
        StringBuilder sb = new StringBuilder(String.format("%n"));
        for (Cause cause : causes) {
            sb.append(String.format("%s%n", cause.getMessage()));
        }
        return sb.toString();
    }

    /**
     * テスト対象のテーブル名のセットを返す。
     * @return テーブル名の一覧
     */
    public Set<String> getTablenames() {
        return dataHolderMap.keySet();
    }

    /**
     * テーブル名に対応するモデルクラスを返す。
     * @param tablename 対象のテーブル名
     * @return 対応するモデルクラス
     */
    public Class<?> getClassByTablename(String tablename) {
        TestDataHolder dataHolder = dataHolderMap.get(tablename);
        if (dataHolder == null) {
            throw new RuntimeException("No such tablename: " + tablename);
        }
        return dataHolder.getModelClass();
    }

    /**
     * モデルクラスに対応するテーブル名を返す。
     * @param modelClass 対象のモデルクラス
     * @return 対応するテーブル名
     */
    public String getTablenameByClass(Class<?> modelClass) {
        for (Map.Entry<String, TestDataHolder> entry : dataHolderMap.entrySet()) {
            if (entry.getValue().getModelClass() == modelClass) {
                return entry.getKey();
            }
        }
        throw new RuntimeException(MessageFormat.format(
                "Unknown model class: {0}",
                modelClass.getName()));
    }

    /**
     * 検査に使用するInspectorを指定する。
     * 指定しなかった場合、{@link DefaultInspector}が使用される。
     * @param tablename テーブル名
     * @param inspector 使用するInspector
     */
    public void setInspector(String tablename, Inspector inspector) {
        inspectorMap.put(tablename, inspector);
    }

    /**
     * テストがNGだったとき原因を取得します。
     * @return テストがNGだったとき原因
     */
    public List<Cause> getCauses() {
        return causes;
    }

    /**
     * 実行結果が期待したものかを確認します。
     * @return OKの場合true
     */
    public boolean inspect() {
        boolean success = true;
        for (String tablename : dataHolderMap.keySet()) {
            TestDataHolder dataHolder = dataHolderMap.get(tablename);
            Inspector inspector = inspectorMap.get(tablename);
            if (inspector == null) {
                inspector = new DefaultInspector();
            }
            inspector.setColumnInfos(dataHolder.getColumnInfos());
            inspector.setStartTime(startTime);
            inspector.setFinishTime(System.currentTimeMillis());
            inspector.inspect(dataHolder);
            if (!inspector.isSuccess()) {
                success = false;
                causes.addAll(inspector.getCauses());
            }
        }
        return success;
    }
}
