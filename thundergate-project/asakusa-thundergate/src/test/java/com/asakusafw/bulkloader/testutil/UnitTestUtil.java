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
package com.asakusafw.bulkloader.testutil;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.transfer.FileList;


/**
 * ThunderGate単体テスト用のユーティリティクラス。
 * @author akira.kawaguchi
 *
 */
public class UnitTestUtil {
    private static final String DELIM = ";";
    private static final String SQLFILE_ENCODING = "UTF-8";
    private static final String PATH_DIST_MAIN = "src/main/dist/bulkloader";
    private static final String PATH_DIST_TEST = "src/test/dist/bulkloader";
    private static final File targetDir = new File("target/asakusa-thundergate/");

    public static void setUpEnv() throws Exception {
        Properties p = System.getProperties();
        p.setProperty(Constants.ASAKUSA_HOME, new File(".").getCanonicalPath());
        p.setProperty(Constants.THUNDER_GATE_HOME, new File(PATH_DIST_TEST).getCanonicalPath());
        ConfigurationLoader.setSysProp(p);
        System.setProperties(p);
    }
    public static void tearDownEnv() throws Exception {
        Properties p = System.getProperties();
        p.remove(Constants.ASAKUSA_HOME);
        p.remove(Constants.THUNDER_GATE_HOME);
        ConfigurationLoader.setSysProp(p);
        System.setProperties(p);
    }
    public static void setUpBeforeClass() throws Exception {
        targetDir.mkdir();
        if (!SystemUtils.IS_OS_WINDOWS) {
            ProcessBuilder pb = new ProcessBuilder("chmod", "777", targetDir.getAbsolutePath());
            pb.start();
        }
    }
    public static void tearDownAfterClass() throws Exception {
        FileUtils.deleteDirectory(targetDir);
    }

    private static File getHomeDir() {
        return new File(ConfigurationLoader.getEnvProperty(Constants.ASAKUSA_HOME));
    }

    private static File getHomeFile(String path) {
        File home = getHomeDir();
        return new File(home, path);
    }

    public static void setUpDB() throws Exception {
        File mainDir = getHomeFile(PATH_DIST_MAIN);
        File testDir = getHomeFile(PATH_DIST_TEST);
        File createSysTableSql = new File(mainDir, "sql/create_table.sql");
        File insertImportTableLockSql = new File(testDir, "sql/insert_import_table_lock.sql");
        File createUtestTableSql = new File(testDir, "sql/create_utest_table.sql");
        File dropSysTableSql = new File(mainDir, "sql/drop_table.sql");
        File dropUtestTableSql = new File(testDir, "sql/drop_utest_table.sql");

        // UT用のテーブルを削除
        executeWithFile(dropUtestTableSql.getAbsolutePath());
        executeWithFile(dropSysTableSql.getAbsolutePath());

        // UT用のテーブルを作成
        executeWithFile(createSysTableSql.getAbsolutePath());
        executeWithFile(insertImportTableLockSql.getAbsolutePath());
        executeWithFile(createUtestTableSql.getAbsolutePath());
    }
    public static void tearDownDB() throws Exception {
        File mainDir = getHomeFile(PATH_DIST_MAIN);
        File testDir = getHomeFile(PATH_DIST_TEST);
        File dropSysTableSql = new File(mainDir, "sql/drop_table.sql");
        File dropUtestTableSql = new File(testDir, "sql/drop_utest_table.sql");

        // UT用のテーブルを削除
        executeWithFile(dropUtestTableSql.getAbsolutePath());
        executeWithFile(dropSysTableSql.getAbsolutePath());
    }
    public static void startUp() throws Exception {
    }
    public static void tearDown() throws Exception {
    }

    public static boolean assertFile(File expected, File actual) throws FileNotFoundException, IOException {
        byte[] b1 = new byte[(int)expected.length()];
        byte[] b2 = new byte[(int)actual.length()];
        new FileInputStream(expected).read(b1);
        new FileInputStream(actual).read(b2);

        if (b1.length != b2.length) {
            return false;
        }

        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create file list from Zip file.
     * @param originalZipFile source Zip file
     * @param targetFileList target file list file
     */
    public static void createFileList(File originalZipFile, File targetFileList) throws IOException {
        ZipFile zip = new ZipFile(originalZipFile);
        try {
            FileOutputStream output = new FileOutputStream(targetFileList);
            try {
                FileList.Writer writer = FileList.createWriter(output, true);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry next = entries.nextElement();
                    InputStream input = zip.getInputStream(next);
                    try {
                        OutputStream target = writer.openNext(FileList.content(next.getName().replace('\\', '/')));
                        try {
                            IOUtils.pipingAndClose(input, target);
                        } finally {
                            target.close();
                        }
                    } finally {
                        input.close();
                    }
                }
                writer.close();
            } finally {
                output.close();
            }
        } finally {
            zip.close();
        }
    }

    /**
     * Checks whether the target file list has expected contents.
     * @param fileList target file list file
     * @param expected expected contents
     * @throws IOException if failed
     */
    public static void assertSameFileList(File fileList, File... expected) throws IOException {
        FileInputStream source = new FileInputStream(fileList);
        try {
            FileList.Reader reader = FileList.createReader(source);
            for (File file : expected) {
                assertThat("expected: " + file, reader.next(), is(true));
                InputStream expectedInput = null;
                InputStream actualInput = null;
                try {
                    expectedInput = new FileInputStream(file);
                    actualInput = reader.openContent();
                } finally {
                    StreamCloseLogic.closeGently(expectedInput);
                    StreamCloseLogic.closeGently(actualInput);
                }
            }
            assertThat("unexpected file", reader.next(), is(false));
            reader.close();
        } finally {
            source.close();
        }
    }

    public static boolean assertZipFile(File[] expectedFile, File zipFile) throws IOException {

        ZipInputStream zipIs = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = null;
        int i = 0;
        while((zipEntry = zipIs.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) {
                continue;
            }
            i++;
            File tempFile = new File("target/asakusa-thundergate/tempdata" + String.valueOf(i));
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] b = new byte[1024];
            while (true) {
                int read = zipIs.read(b);
                if (read == -1) {
                    break;
                }
                fos.write(b, 0, read);
            }

            // ファイルの中身を比較
            if (!assertFile(expectedFile[i - 1], tempFile)) {
                tempFile.delete();
                return false;
            }

            // tempファイルを削除
            tempFile.delete();
        }
        return true;
    }
    /**
     * SQLの実行を行う
     * @param sql
     */
    public static int executeUpdate(String sql) throws Exception {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            printLog("executeUpdateを実行します。SQL：" + sql, "executeUpdate");
            conn =DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            int result = stmt.executeUpdate();
            printLog("executeUpdateを実行しました。結果：" + result, "executeUpdate");
            return result;
        } finally {
            DBConnection.closePs(stmt);
            DBConnection.closeConn(conn);
        }
    }
    /**
     * テーブルが存在するか確認する
     * @param tableName
     * @return
     */
    public static boolean isExistTable(String tableName) throws Exception {
        String url = ConfigurationLoader.getProperty(Constants.PROP_KEY_DB_URL);
        String schema = url.substring(url.lastIndexOf("/") + 1, url.length());
        String sql = "SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=? AND TABLE_SCHEMA=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            printLog("executeQueryを実行します。SQL：" + sql + "パラメータ：" + tableName + "," + schema, "isExistTable");
            conn =DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, tableName);
            stmt.setString(2, schema);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                if (count > 0) {
                    printLog("テーブルが存在します。テーブル名：" + tableName, "isExistTable");
                    return true;
                } else {
                    printLog("テーブルが存在しません。テーブル名：" + tableName, "isExistTable");
                    return false;
                }
            } else {
                printLog("テーブルが存在しません。テーブル名：" + tableName, "isExistTable");
                return false;
            }
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
            DBConnection.closeConn(conn);
        }
    }
    /**
     * テーブルのレコード件数を検証する
     * @param tableName
     * @param expected
     * @return
     */
    public static boolean countAssert(String tableName, int expected) throws Exception {
        String sql = "SELECT count(*) FROM " + tableName;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            printLog("executeQueryを実行します。SQL：" + sql, "countAssert");
            conn =DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count(*)");
                if (expected == count) {
                    printLog("レコードが存在します。レコード件数：" + count, "countAssert");
                    return true;
                } else {
                    printLog("レコードが存在しません。テーブル名：" + tableName, "countAssert");
                    return false;
                }
            } else {
                printLog("レコードが存在しません。テーブル名：" + tableName, "countAssert");
                return false;
            }
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
            DBConnection.closeConn(conn);
        }
    }
    /**
     * DDLや更新系DMLなどの結果を返さないSQLが記述されたSQLファイルを実行する。
     * @param sqlFile SQLファイル
     * @throws Exception
     */
    public static void executeWithFile(String sqlFile) throws Exception {
        BufferedReader reader = null;
        FileInputStream fs = new FileInputStream(sqlFile);
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(fs,SQLFILE_ENCODING));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("//")) {
                    continue;
                }
                if (line.startsWith("--")) {
                    continue;
                }
                sb.append(line);
            }
        } finally {
            fs.close();
            if (reader != null) {
                reader.close();
            }
        }
        String sql = sb.toString();
        int beginIndex = 0;
        int endIndex = 0;
        String workSql = complementDelimiter(sql);
        while ((endIndex = workSql.indexOf(DELIM, beginIndex)) != -1) {
            String oneStatement = workSql.substring(beginIndex, endIndex);
            if ("".equals(oneStatement.trim())) {
                continue;
            }
            executeUpdate(oneStatement);
            beginIndex = endIndex + 1;
        }
    }
    private static String complementDelimiter(String sql) {
        if (!(sql.trim().endsWith(DELIM))) {
            return sql + DELIM;
        } else {
            return sql;
        }
    }
    /**
     * ログを出力する。
     * @param message ログメッセージ
     * @param method メソッド
     */
    public static void printLog(String message, String method) {
//        String strDate = getDate();
//        System.err.println("[LOG] [com.asakusafw.bulkloader.testutil.UnitTestUtil#" + method +  "] [" + strDate + "]：" + message);
    }
    /**
     * 現在の日付時刻を表す文字列を返す。
     * @return 現在の日付時刻を表す文字列
     */
    private static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        return sdf.format(new Date());
    }
}
