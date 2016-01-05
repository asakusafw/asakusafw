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
package com.asakusafw.bulkloader.collector;

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

import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;


/**
 * Collectorのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class CollectorTest {
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
        args[0] = "batch01";
        args[1] = "11";
        args[2] = "1";
        args[3] = "11-1";
        args[4] = "hadoop";
        Collector collector = new StubCollector();
        int result = collector.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 異常系：ExportファイルのDBサーバへの送信失敗するケース
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
        Collector collector = new StubCollector() {
            @Override
            protected ExportFileSend createExportFileSend() {
                return new StubExportFileSend(false);
            }
        };
        int result = collector.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：処理中に例外が発生するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest03() throws Exception {
        // 処理の実行
        String[] args = new String[5];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        args[4] = "hadoop";
        Collector collector = new StubCollector() {
            @Override
            protected ExportFileSend createExportFileSend() {
                throw new NullPointerException();
            }
        };
        int result = collector.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：引数の個数が不正なケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest04() throws Exception {
        // 処理の実行
        String[] args = new String[1];
        args[0] = "target1";
        Collector collector = new StubCollector();
        int result = collector.execute(args);

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
    public void executeTest05() throws Exception {
        // 処理の実行
        String[] args = new String[5];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        args[4] = "hadoop";
        Collector collector = new StubCollector() {

            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    public boolean loadExportParam(String targetName, String batchId, String jobflowId) {
                        return false;
                    }

                };
                return loder;
            }

        };
        int result = collector.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }

}
class StubCollector extends Collector {
    @Override
    protected JobFlowParamLoader createJobFlowParamLoader() {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getExportProp(File file, String targetName) throws IOException {
                File propFile = new File("src/test/data/common/export1.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }

        };
        return loder;
    }
    @Override
    protected ExportFileSend createExportFileSend() {
        return new StubExportFileSend();
    }
}
class StubExportFileSend extends ExportFileSend {
    boolean result = true;
    public StubExportFileSend() {

    }
    public StubExportFileSend(boolean result) {
        this.result = result;
    }
    @Override
    public boolean sendExportFile(ExporterBean bean, String user) {
        return result;
    }
}