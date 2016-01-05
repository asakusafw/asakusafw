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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;


/**
 * DBConnectionのテストクラス
 * @author yuta.shirai
 *
 */
public class DBConnectionTest {
    /** ターゲット名 */
    private static String targetName = "target1";
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
    }
    @After
    public void tearDown() throws Exception {
    }

    /**
     * <p>
     * 正常系：パラメータ用プロパティが指定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void getConnectionTest01() throws Exception {
        Connection conn = null;
        try {
            BulkLoaderInitializer.initDBServer(jobflowId, executionId, Arrays.asList(new String[]{"bulkloader-conf-db.properties"}), targetName);
            UnitTestUtil.startUp();
            conn = DBConnection.getConnection();
            DBConnection.closePs(null);
            DBConnection.closeRs(null);
            DBConnection.closeConn(null);
            DBConnection.closeConn(conn);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            DBConnection.closeConn(conn);
        }
    }
    /**
     * <p>
     * 正常系：パラメータ用プロパティが指定されているケース(相対パス指定)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void getConnectionTest02() throws Exception {
        Connection conn = null;
        try {
            BulkLoaderInitializer.initDBServer(jobflowId, executionId, Arrays.asList(new String[]{"bulkloader-conf-db.properties"}), targetName);
            Properties p = ConfigurationLoader.getProperty();
            p.setProperty(Constants.PROP_KEY_NAME_DB_PRAM, "src/test/dist/bulkloader/conf/db-param.properties");
            ConfigurationLoader.setProperty(p);
            conn = DBConnection.getConnection();
            DBConnection.closeConn(conn);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            DBConnection.closeConn(conn);
        }

    }
    /**
     * <p>
     * 正常系：パラメータ用プロパティが指定されているケース(絶対パス指定)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void getConnectionTest03() throws Exception {
        Connection conn = null;
        try {
            String appHome = System.getProperty(Constants.THUNDER_GATE_HOME);
            String propDir = appHome + "/conf/db-param.properties";

            BulkLoaderInitializer.initDBServer(jobflowId, executionId, Arrays.asList(new String[]{"bulkloader-conf-db.properties"}), targetName);
            Properties p = ConfigurationLoader.getProperty();
            p.setProperty(Constants.PROP_KEY_NAME_DB_PRAM, propDir);
            ConfigurationLoader.setProperty(p);
            conn = DBConnection.getConnection();
            DBConnection.closeConn(conn);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            DBConnection.closeConn(conn);
        }
    }
    /**
     * <p>
     * 異常系：Connection取得中にSQLExceptionが発生するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void getConnectionTest04() throws Exception {
        Connection conn = null;
        try {
            BulkLoaderInitializer.initDBServer(jobflowId, executionId, Arrays.asList(new String[]{"bulkloader-conf-db.properties"}), targetName);
            Properties p = ConfigurationLoader.getProperty();
            p.setProperty(Constants.PROP_KEY_DB_USER, "n");
            p.setProperty(Constants.PROP_KEY_DB_PASSWORD, "n");
            ConfigurationLoader.setProperty(p);
            conn = DBConnection.getConnection();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
            assertTrue(e.getCause() instanceof SQLException);
        } finally {
            DBConnection.closeConn(conn);
        }

    }
}
