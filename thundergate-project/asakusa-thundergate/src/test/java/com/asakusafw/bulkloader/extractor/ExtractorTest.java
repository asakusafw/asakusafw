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
package com.asakusafw.bulkloader.extractor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;


/**
 * Extractorのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ExtractorTest {
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
    }
    @After
    public void tearDown() throws Exception {
    }

    /**
     *
     * <p>
     * 正常系：全ての処理が正常に終了するケース（引数4つ）
     *
     * @throws Exception
     */
    @Test
    public void executeTest01() throws Exception {
        // 処理の実行
        String[] args = new String[5];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        args[4] = "hadoop";
        Extractor extractor = new StubExtractor();
        int result = extractor.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 異常系：Import対象ファイルのHDFSへの書き出しに失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest02() throws Exception {
        // 処理の実行
        String[] args = new String[5];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        args[4] = "hadoop";
        Extractor extractor = new StubExtractor(){

            @Override
            protected DfsFileImport createDfsFileImport() {
                return new StubHdfsFileImport(false);
            }
        };
        int result = extractor.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：引数の数が不正なケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest03() throws Exception {
        // 処理の実行
        String[] args = new String[1];
        args[0] = "target1";
        Extractor extractor = new StubExtractor();
        int result = extractor.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：DSLプロパティの読み込みに失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest04() throws Exception {
        // 処理の実行
        String[] args = new String[5];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        args[4] = "hadoop";
        Extractor extractor = new StubExtractor(){
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    public boolean loadImportParam(String targetName, String batchId, String jobflowId, boolean isPrimary) {
                        return false;
                    }

                };
                return loder;
            }
        };
        int result = extractor.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
}
class StubExtractor extends Extractor {
    @Override
    protected DfsFileImport createDfsFileImport() {
        return new StubHdfsFileImport();
    }
    @Override
    protected JobFlowParamLoader createJobFlowParamLoader() {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File file, String targetName) throws IOException {
                System.out.println(file);
                File propFile = new File("src/test/data/common/import1.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        return loder;
    }

}
class StubHdfsFileImport extends DfsFileImport {
    boolean result = true;
    public StubHdfsFileImport() {

    }
    public StubHdfsFileImport(boolean result) {
        this.result = result;
    }
    @Override
    public boolean importFile(ImportBean bean, String user) {
        return result;
    }
}