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
package com.asakusafw.cleaner.main;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.cleaner.common.ConfigurationLoader;
import com.asakusafw.cleaner.testutil.UnitTestUtil;

/**
 * HDFSCleanerのテストクラス
 *
 *
 * @author yuta.shirai
 *
 */
public class HDFSCleanerTest {
    // プロパティファイル
    private static final String propFile = "clean-hdfs-conf.properties";
    private static final String propFile1 = "clean-hdfs-conf1.properties";
    private static final String propFile2 = "clean-hdfs-conf2.properties";
    private static final String propFile3 = "clean-hdfs-conf3.properties";
    private static final String propFile4 = "clean-hdfs-conf4.properties";
    private static final String propFile5 = "clean-hdfs-conf5.properties";
    private static final String propFile6 = "clean-hdfs-conf6.properties";

    // ディレクトリ１
    private static final File cleanDir01 = new File("target/asakusa-cleaner/HDFSCleaner01");
    // 第1階層
    private File tempDir = null;
    private File logDir = null;
    private File confFile = null;
    private File readmeFile = null;
    // 第2階層(tempDir配下)
    private File dir11_1 = null;
    private File dir11_2 = null;
    private File file11_3 = null;
    // 第3階層(dir11_2配下)
    private File fileData1 = null;
    private File fileData2 = null;
    private File dirData3 = null;

    // ディレクトリ２
    private static final File cleanDir02 = new File("target/asakusa-cleaner/HDFSCleaner02");
    // 第1階層
    private File fileImportData1 = null;
    private File fileImportData2 = null;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownAfterClass();
    }

    @Before
    public void setUp() throws Exception {
        UnitTestUtil.startUp();
        cleanDir01.mkdir();
        cleanDir02.mkdir();
        Properties p = ConfigurationLoader.getProperty();
        p.clear();
    }

    @After
    public void tearDown() throws Exception {
        UnitTestUtil.tearDown();
        cleanDir01.delete();
        cleanDir02.delete();
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：クリーニングが正常に終了するケース
     * 　　　　・動作モード：0
     * 　　　　・コンフィグレーションファイル
     * 　　　　　- 保持期間：0
     * 　　　　　- クリーニング対象：単一指定
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest01() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir01();

        // 処理の実行
        String[] args = new String[]{"normal", "asakusa-cleaner", propFile};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // 結果を検証
        // 第0階層
        assertTrue(cleanDir01.exists());
        // 第1階層
        assertTrue(tempDir.exists());
        assertTrue(logDir.exists());
        assertFalse(confFile.exists());
        // 第2階層(tempDir配下)
        assertTrue(dir11_1.exists());
        assertTrue(dir11_2.exists());
        assertTrue(file11_3.exists());
        // 第3階層(dir11_2配下)
        assertTrue(fileData1.exists());
        assertTrue(fileData2.exists());
        assertTrue(dirData3.exists());

        // ディレクトリを削除
        cleanDir(cleanDir01);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：クリーニングが正常に終了するケース
     * 　　　　・動作モード：1
     * 　　　　・コンフィグレーションファイル
     * 　　　　　- 保持期間：0
     * 　　　　　- クリーニング対象：単一指定
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest02() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir01();

        // 処理の実行
        String[] args = new String[]{"recursive", "asakusa-cleaner", propFile};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // 結果を検証
        // 第0階層
        assertTrue(cleanDir01.exists());
        // 第1階層
        assertFalse(tempDir.exists());
        assertFalse(logDir.exists());
        assertFalse(confFile.exists());
        // 第2階層(tempDir配下)
        assertFalse(dir11_1.exists());
        assertFalse(dir11_2.exists());
        assertFalse(file11_3.exists());
        // 第3階層(dir11_2配下)
        assertFalse(fileData1.exists());
        assertFalse(fileData2.exists());
        assertFalse(dirData3.exists());

        // ディレクトリを削除
        cleanDir(cleanDir01);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：クリーニングが正常に終了するケース
     * 　　　　・動作モード：1
     * 　　　　・コンフィグレーションファイル
     * 　　　　　- 保持期間：3(保持期間を超えるファイルと超えないファイルが存在する)
     * 　　　　　- クリーニング対象：複数指定
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest03() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir01();
        createCleanDir02();

        // ファイルの更新日時を更新
        long now = new Date().getTime();// 削除されないファイル
        long past = now - (5L * 24L * 60L * 60L * 1000L); // 削除されるファイル
        confFile.setLastModified(now);
        file11_3.setLastModified(past);
        fileData1.setLastModified(past);
        fileData2.setLastModified(now);
        fileImportData1.setLastModified(past);
        fileImportData2.setLastModified(past);
        tempDir.setLastModified(past);
        logDir.setLastModified(past);
        dir11_1.setLastModified(past);
        dir11_2.setLastModified(past);
        dirData3.setLastModified(past);

        // 処理の実行
        String[] args = new String[]{"recursive", "asakusa-cleaner", propFile1};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // 結果を検証
        // クリーニング対象ディレクトリ1
        assertTrue(cleanDir01.exists());
        // 第1階層
        assertTrue(tempDir.exists());
        assertFalse(logDir.exists());
        assertTrue(confFile.exists());
        // 第2階層(tempDir配下)
        assertFalse(dir11_1.exists());
        assertTrue(dir11_2.exists());
        assertFalse(file11_3.exists());
        // 第3階層(dir11_2配下)
        assertFalse(fileData1.exists());
        assertTrue(fileData2.exists());
        assertFalse(dirData3.exists());

        // クリーニング対象ディレクトリ2
        assertTrue(cleanDir02.exists());
        // 第1階層
        assertFalse(fileImportData1.exists());
        assertFalse(fileImportData2.exists());

        // ディレクトリを削除
        cleanDir(cleanDir01);
        cleanDir(cleanDir02);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：クリーニングが正常に終了するケース
     * 　　　　・動作モード：1
     * 　　　　・コンフィグレーションファイル
     * 　　　　　- 保持期間：3(保持期間を超えるディレクトリと超えないディレクトリが存在する)
     * 　　　　　- クリーニング対象：複数指定
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest04() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir01();
        createCleanDir02();

        // ファイルの更新日時を更新
        long now = new Date().getTime();// 削除されないファイル
        long past = now - (5L * 24L * 60L * 60L * 1000L); // 削除されるファイル
        confFile.setLastModified(past);
        file11_3.setLastModified(past);
        fileData1.setLastModified(past);
        fileData2.setLastModified(past);
        fileImportData1.setLastModified(past);
        fileImportData2.setLastModified(past);
        tempDir.setLastModified(past);
        logDir.setLastModified(now);
        dir11_1.setLastModified(now);
        dir11_2.setLastModified(past);
        dirData3.setLastModified(past);

        // 処理の実行
        String[] args = new String[]{"recursive", "asakusa-cleaner", propFile1};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // 結果を検証
        // クリーニング対象ディレクトリ1
        assertTrue(cleanDir01.exists());
        // 第1階層
        assertTrue(tempDir.exists());
        assertTrue(logDir.exists());
        assertFalse(confFile.exists());
        // 第2階層(tempDir配下)
        assertTrue(dir11_1.exists());
        assertFalse(dir11_2.exists());
        assertFalse(file11_3.exists());
        // 第3階層(dir11_2配下)
        assertFalse(fileData1.exists());
        assertFalse(fileData2.exists());
        assertFalse(dirData3.exists());

        // クリーニング対象ディレクトリ2
        assertTrue(cleanDir02.exists());
        // 第1階層
        assertFalse(fileImportData1.exists());
        assertFalse(fileImportData2.exists());

        // ディレクトリを削除
        cleanDir(cleanDir01);
        cleanDir(cleanDir02);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：引数の数が不正なケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest05() throws Exception {
        // 処理の実行
        String[] args = new String[]{"recursive", "asakusa-cleaner", propFile, "1"};
        HDFSCleaner cleaner = new HDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);

        // 処理の実行
        args = new String[]{};
        result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：動作モードが不正なケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest06() throws Exception {
        // 処理の実行
        String[] args = new String[]{"2", "test", propFile};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：クリーニング対象ディレクトリの指定が不正なケース
     * 　　　　（存在しない、ディレクトリでない）
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest07() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir02();

        // 処理の実行
        String[] args = new String[]{"recursive", "asakusa-cleaner", propFile2};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // ディレクトリを削除
        cleanDir(cleanDir02);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：クリーニング対象ディレクトリの指定が不正なケース
     * 　　　　（ファイルシステムの取得に失敗するケース）
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest08() throws Exception {
        // 処理の実行
        String[] args = new String[]{"recursive", "asakusa-cleaner", propFile};
        HDFSCleaner cleaner = new StubHDFSCleaner(){
            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.cleaner.StubHDFSCleaner#createPath(java.lang.String)
             */
            @Override
            protected Path createPath(String strCleanPath) {
                return new Path("hdf://localhost:1127");
            }
        };
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(2, result);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：クリーニング中に予期しない例外が発生するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest09() throws Exception {
        // 処理の実行
        String[] args = new String[]{"recursive", "asakusa-cleaner", propFile};
        HDFSCleaner cleaner = new StubHDFSCleaner(){
            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.cleaner.StubHDFSCleaner#createPath(java.lang.String)
             */
            @Override
            protected Path createPath(String strCleanPath) {
                throw new NullPointerException();
            }
        };
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：クリーニングが正常に終了するケース
     * 　　　　・動作モード：1
     * 　　　　・コンフィグレーションファイル
     * 　　　　　- 保持期間：0
     * 　　　　　- クリーニング対象：複数指定
     * 　　　　・処理中のディレクトリを含む
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest10() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir01();
        createCleanDir02();

        // 処理の実行
        String[] args = new String[]{"recursive", "asakusa-cleaner", propFile3};
        StubHDFSCleaner cleaner = new StubHDFSCleaner(true);
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(0, result);
        ArrayList<String> instanceId = cleaner.getInstanceId();
        assertEquals(2, instanceId.size());
        assertEquals("11_1", instanceId.get(0));
        assertEquals("11_2", instanceId.get(1));

        // 結果を検証
        // クリーニング対象ディレクトリ1
        assertTrue(cleanDir01.exists());
        // 第1階層
        assertTrue(tempDir.exists());
        assertTrue(logDir.exists());
        assertTrue(confFile.exists());
        // 第2階層(tempDir配下)
        assertTrue(dir11_1.exists());
        assertTrue(dir11_2.exists());
        assertFalse(file11_3.exists());
        // 第3階層(dir11_2配下)
        assertTrue(fileData1.exists());
        assertTrue(fileData2.exists());
        assertTrue(dirData3.exists());

        // クリーニング対象ディレクトリ2
        assertTrue(cleanDir02.exists());
        // 第1階層
        assertFalse(fileImportData1.exists());
        assertFalse(fileImportData2.exists());

        // ディレクトリを削除
        cleanDir(cleanDir01);
        cleanDir(cleanDir02);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：削除パターンを指定するケース
     * 　　　　・動作モード：normal
     * 　　　　・コンフィグレーションファイル
     * 　　　　　- 保持期間：0
     * 　　　　　- パターン：.*\.txt
     * 　　　　　- クリーニング対象：単一指定
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest11() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir01();

        // 処理の実行
        String[] args = new String[]{"normal", "asakusa-cleaner", propFile4};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // 結果を検証
        // 第0階層
        assertTrue(cleanDir01.exists());
        // 第1階層
        assertTrue(tempDir.exists());
        assertTrue(logDir.exists());
        assertTrue(confFile.exists());
        assertFalse(readmeFile.exists());
        // 第2階層(tempDir配下)
        assertTrue(dir11_1.exists());
        assertTrue(dir11_2.exists());
        assertTrue(file11_3.exists());
        // 第3階層(dir11_2配下)
        assertTrue(fileData1.exists());
        assertTrue(fileData2.exists());
        assertTrue(dirData3.exists());

        // ディレクトリを削除
        cleanDir(cleanDir01);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：削除パターンを指定するケース
     * 　　　　・動作モード：recursive
     * 　　　　・コンフィグレーションファイル
     * 　　　　　- 保持期間：0
     * 　　　　　- パターン：.*\.txt
     * 　　　　　- クリーニング対象：単一指定
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest12() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir01();

        // 処理の実行
        String[] args = new String[]{"recursive", "asakusa-cleaner", propFile4};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // 結果を検証
        // 第0階層
        assertTrue(cleanDir01.exists());
        // 第1階層
        assertTrue(tempDir.exists());
        assertFalse(logDir.exists());
        assertTrue(confFile.exists());
        assertFalse(readmeFile.exists());
        // 第2階層(tempDir配下)
        assertFalse(dir11_1.exists());
        assertFalse(dir11_2.exists());
        assertTrue(file11_3.exists());
        // 第3階層(dir11_2配下)
        assertFalse(fileData1.exists());
        assertFalse(fileData2.exists());
        assertFalse(dirData3.exists());

        // ディレクトリを削除
        cleanDir(cleanDir01);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：パターン指定が不正なケース
     * 　　　　・動作モード：normal
     * 　　　　・コンフィグレーションファイル
     * 　　　　　- 保持期間：0
     * 　　　　　- パターン：*.txt
     * 　　　　　- クリーニング対象：単一指定
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest13() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir01();

        // 処理の実行
        String[] args = new String[]{"normal", "test", propFile5};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // 結果を検証
        // 第0階層
        assertTrue(cleanDir01.exists());
        // 第1階層
        assertTrue(tempDir.exists());
        assertTrue(logDir.exists());
        assertTrue(confFile.exists());
        assertTrue(readmeFile.exists());
        // 第2階層(tempDir配下)
        assertTrue(dir11_1.exists());
        assertTrue(dir11_2.exists());
        assertTrue(file11_3.exists());
        // 第3階層(dir11_2配下)
        assertTrue(fileData1.exists());
        assertTrue(fileData2.exists());
        assertTrue(dirData3.exists());

        // ディレクトリを削除
        cleanDir(cleanDir01);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：削除パターンが指定されていないケース
     * 　　　　・動作モード：normal
     * 　　　　・コンフィグレーションファイル
     * 　　　　　- 保持期間：0
     * 　　　　　- パターン：*.txt
     * 　　　　　- クリーニング対象：単一指定
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest14() throws Exception {
        // ディレクトリ構成を作成
        createCleanDir01();

        // 処理の実行
        String[] args = new String[]{"normal", "test", propFile6};
        HDFSCleaner cleaner = new StubHDFSCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);

        // 結果を検証
        // 第0階層
        assertTrue(cleanDir01.exists());
        // 第1階層
        assertTrue(tempDir.exists());
        assertTrue(logDir.exists());
        assertTrue(confFile.exists());
        assertTrue(readmeFile.exists());
        // 第2階層(tempDir配下)
        assertTrue(dir11_1.exists());
        assertTrue(dir11_2.exists());
        assertTrue(file11_3.exists());
        // 第3階層(dir11_2配下)
        assertTrue(fileData1.exists());
        assertTrue(fileData2.exists());
        assertTrue(dirData3.exists());

        // ディレクトリを削除
        cleanDir(cleanDir01);
    }

    /**
     * クリーニング対象ディレクトリ1を作成する
     * @throws IOException
     */
    private void createCleanDir01() throws IOException {
        cleanDir01.mkdir();
        // 第1階層
        tempDir = new File(cleanDir01, "temp");
        tempDir.mkdir();
        logDir = new File(cleanDir01, "log");
        logDir.mkdir();
        confFile = new File(cleanDir01, "localfs-conf.properties");
        confFile.createNewFile();
        readmeFile = new File(cleanDir01, "readme.txt");
        readmeFile.createNewFile();

        // 第2階層(tempDir配下)
        dir11_1 = new File(tempDir, "11_1");
        dir11_1.mkdir();
        dir11_2 = new File(tempDir, "11_2");
        dir11_2.mkdir();
        file11_3 = new File(tempDir, "11_3.tsv");
        file11_3.createNewFile();

        // 第3階層(dir11_2配下)
        fileData1 = new File(dir11_2, "data1.txt");
        fileData1.createNewFile();
        fileData2 = new File(dir11_2, "data2.txt");
        fileData2.createNewFile();
        dirData3 = new File(dir11_2, "data3");
        dirData3.mkdir();
    }
    /**
     * クリーニング対象ディレクトリ2を作成する
     * @throws IOException
     */
    private void createCleanDir02() throws IOException {
        cleanDir02.mkdir();
        // 第1階層
        fileImportData1 = new File(cleanDir02, "importdata1.tsv");
        fileImportData1.createNewFile();
        fileImportData2 = new File(cleanDir02, "importdata2.tsv");
        fileImportData2.createNewFile();
    }
    /**
     * クリーニング対象ディレクトリ内のファイルを削除する
     * @param cleandir
     * @throws IOException
     */
    private void cleanDir(File cleandir) throws IOException {
        File[] listFiles = cleandir.listFiles();
        for (File file: listFiles) {
            if (file.isFile()) {
                file.delete();
            }
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            }
        }
        cleandir.delete();
    }
}
class StubHDFSCleaner extends HDFSCleaner {
    boolean exec = false;
    public StubHDFSCleaner() {
        super(new Configuration());
    }
    public StubHDFSCleaner(boolean exec) {
        super(new Configuration());
        this.exec = exec;
    }
    ArrayList<String> instanceId = new ArrayList<String>();
    /**
     * @see com.asakusafw.cleaner.main.HDFSCleaner#createPath(java.lang.String)
     */
    @Override
    protected Path createPath(String strCleanPath) {
        File file = new File(strCleanPath);
        URI uri = file.toURI();
        return new Path(uri.getPath());
    }
    /**
     * @see com.asakusafw.cleaner.main.HDFSCleaner#isRunningJobFlow(java.lang.String)
     */
    @Override
    protected boolean isRunningJobFlow(String executionId) {
        instanceId.add(executionId);
        return exec;
    }
    /**
     * @return instanceId
     */
    public ArrayList<String> getInstanceId() {
        Collections.sort(instanceId);
        return instanceId;
    }
}