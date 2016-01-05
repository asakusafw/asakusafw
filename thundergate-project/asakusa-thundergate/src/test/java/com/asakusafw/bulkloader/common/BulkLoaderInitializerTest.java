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
package com.asakusafw.bulkloader.common;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.testutil.UnitTestUtil;

/**
 * IOInitializerのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class BulkLoaderInitializerTest {
    /** ターゲット名 */
    private static String targetName = "target1";
    /** 読み込むプロパティファイル */
    private static List<String> propertys_db = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    private static List<String> propertys_hc = Arrays.asList(new String[]{"bulkloader-conf-hc.properties"});
    private static List<String> propertys_er1 = Arrays.asList(new String[]{"bulkloader-conf-er1.properties"});
    private static List<String> propertys_er2 = Arrays.asList(new String[]{"bulkloader-conf-er2.properties"});
    /** ジョブフローID */
    private static String jobflowId = "JOB_FLOW01";
    /** ジョブフロー実行ID */
    private static String executionId = "JOB_FLOW01-001";

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
        ConfigurationLoader.setProperty(new Properties());
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * <p>
     * initHCのテストケース
     * 正常系：初期化に成功するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initHCTest01() throws Exception {
        boolean result = BulkLoaderInitializer.initHadoopCluster(jobflowId, executionId, propertys_hc);
        assertTrue(result);
    }
    /**
     * <p>
     * initDBのテストケース
     * 正常系：初期化に成功するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initDBTest01() throws Exception {
        boolean result = BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys_db, targetName);
        assertTrue(result);
    }
    /**
     * <p>
     * initDBのテストケース
     * 異常系：コンフィグレーションの読み込みでプロパティの内容が不正なケース（ログ初期化に成功）
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initDBTest02() throws Exception {
        boolean result = BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys_hc, targetName);
        assertFalse(result);
    }
    /**
     * <p>
     * initDBのテストケース
     * 異常系：コンフィグレーションの読み込みでプロパティの内容が不正なケース（ログ初期化に失敗）
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initDBTest03() throws Exception {
        boolean result = BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys_er1, targetName);
        assertFalse(result);
    }
    /**
     * <p>
     * initDBのテストケース
     * 異常系：コンフィグレーションの読み込みでプロパティの読み込みに失敗するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initDBTest04() throws Exception {
        boolean result = BulkLoaderInitializer.initDBServer(jobflowId, executionId, Arrays.asList(new String[]{"io-conf-dummy.properties"}), targetName);
        assertFalse(result);
    }
    /**
     * <p>
     * initDBのテストケース
     * 異常系：ログの初期化に失敗するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initDBTest05() throws Exception {
        boolean result = BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys_er2, targetName);
        assertFalse(result);
    }
    /**
     * <p>
     * initDBのテストケース
     * 異常系：DBConnectionの初期化に失敗するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initDBTest07() throws Exception {
        boolean result = BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys_db, "targeterr");
        assertFalse(result);
    }
}
