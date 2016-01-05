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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;

/**
 * ConfigurationLoaderのテストクラス
 * @author yuta.shirai
 *
 */
public class ConfigurationLoaderTest {

    static final Log LOG = new Log(ConfigurationLoaderTest.class);

    private static final String PATH_DB_PARAMETER = "src/test/dist/bulkloader/conf/db-param.properties";

    /** 読み込むプロパティファイル */
    private static List<String> properties_db = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    private static List<String> properties_hc = Arrays.asList(new String[]{"bulkloader-conf-hc.properties"});

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
        UnitTestUtil.setUpEnv();
        ConfigurationLoader.cleanProp();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * <p>
     * initのテストケース(doDBPropCheck:true)
     * 正常系：DBサーバの初期化が正常に終了するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initTest01() throws Exception {
        try {
            ConfigurationLoader.init(properties_db, true, false);
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * initのテストケース(doHCPropCheck:true)
     * 正常系：HudoopClusterの初期化が正常に終了するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initTest02() throws Exception {
        try {
            ConfigurationLoader.init(properties_hc, false, true);
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * initのテストケース(doDBPropCheck:true)
     * 異常系：プロパティファイルの読み込みに失敗するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void initTest03() throws Exception {
        try {
            ConfigurationLoader.init(Arrays.asList(new String[]{"io-conf-dummy.properties"}), true, false);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IOException);
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkEnvのテストケース
     * 異常系："BULKLOADER_HOME"が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkEnvTest01() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Map<String, String> m = new HashMap<String, String>();
        m.put(Constants.ASAKUSA_HOME, ConfigurationLoader.getEnvProperty(Constants.ASAKUSA_HOME));
        m.put(Constants.THUNDER_GATE_HOME, null);
        ConfigurationLoader.setEnv(m);

        UnitTestUtil.tearDownEnv();

        try {
            ConfigurationLoader.checkEnv();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof Exception);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkEnvのテストケース
     * 異常系："BULKLOADER_HOME"の設定が不正のケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkEnvTest02() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Map<String, String> m = new HashMap<String, String>();
        m.put(Constants.ASAKUSA_HOME, ConfigurationLoader.getEnvProperty(Constants.ASAKUSA_HOME));
        m.put(Constants.THUNDER_GATE_HOME, "J:\temp");
        ConfigurationLoader.setEnv(m);

        Properties p = System.getProperties();
        p.setProperty(Constants.ASAKUSA_HOME, "src");
        p.setProperty(Constants.THUNDER_GATE_HOME, "J:\temp");
        ConfigurationLoader.setSysProp(p);
        System.setProperties(p);

        try {
            ConfigurationLoader.checkEnv();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof Exception);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkEnvのテストケース
     * 異常系："ASAKUSA_HOME"が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkEnvTest03() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Map<String, String> m = new HashMap<String, String>();
        m.put(Constants.ASAKUSA_HOME, null);
        m.put(Constants.THUNDER_GATE_HOME, ConfigurationLoader.getEnvProperty(Constants.THUNDER_GATE_HOME));
        ConfigurationLoader.setEnv(m);

        UnitTestUtil.tearDownEnv();

        try {
            ConfigurationLoader.checkEnv();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof Exception);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkEnvのテストケース
     * 異常系："ASAKUSA_HOME"の設定が不正のケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkEnvTest04() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Map<String, String> m = new HashMap<String, String>();
        m.put(Constants.ASAKUSA_HOME, "J:\temp");
        m.put(Constants.THUNDER_GATE_HOME, ConfigurationLoader.getEnvProperty(Constants.THUNDER_GATE_HOME));
        ConfigurationLoader.setEnv(m);

        Properties p = System.getProperties();
        p.setProperty(Constants.ASAKUSA_HOME, "J:\temp");
        p.setProperty(Constants.THUNDER_GATE_HOME, "src/test");
        ConfigurationLoader.setSysProp(p);
        System.setProperties(p);

        try {
            ConfigurationLoader.checkEnv();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof Exception);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkAndSetParamHCのテストケース
     * 正常系：Exportファイルの圧縮有無が設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamHC01() throws Exception {
        ConfigurationLoader.init(properties_hc, false, true);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.zip-comp-type", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamHC();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }

        assertEquals(
                FileCompType.STORED,
                FileCompType.find(ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_FILE_COMP_TYPE)));

    }
    /**
     * <p>
     * checkAndSetParamHCのテストケース
     * 異常系：Exportファイルの圧縮有無が不正なケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamHC02() throws Exception {
        ConfigurationLoader.init(properties_hc, false, true);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.zip-comp-type", "2");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamHC();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamHCのテストケース
     * 正常系：ワーキングディレクトリベースが設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Ignore("hadoop-cluster.workingdir.use is obsoleted")
    @Test
    public void checkAndSetParamHC03() throws Exception {
        ConfigurationLoader.init(properties_hc, false, true);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("hadoop-cluster.workingdir.use", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamHC();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }

        assertEquals("false", ConfigurationLoader.getProperty(Constants.PROP_KEY_WORKINGDIR_USE));

    }
    /**
     * <p>
     * checkAndSetParamHCのテストケース
     * 異常系：HDFSのプロトコルとホスト名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Ignore("hdfs-protocol-host is obsoleted")
    @Test
    public void checkAndSetParamHC04() throws Exception {
        ConfigurationLoader.init(properties_hc, false, true);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("hdfs-protocol-host", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamHC();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamHCのテストケース
     * 正常系：エクスポートファイルをTSVファイルに変換する際のファイル分割サイズが設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamHC05() throws Exception {
        ConfigurationLoader.init(properties_hc, false, true);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.tsv-max-size", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamHC();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals("16777216", ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_LOAD_MAX_SIZE));
    }
    /**
     * <p>
     * checkAndSetParamHCのテストケース
     * 正常系：エクスポートファイルをTSVファイルに変換する際のファイル分割サイズが不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamHC06() throws Exception {
        ConfigurationLoader.init(properties_hc, false, true);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.tsv-max-size", "a");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamHC();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamHCのテストケース
     * 正常系：出力ファイルの圧縮有無が設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamHC07() throws Exception {
        ConfigurationLoader.init(properties_hc, false, true);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.seq-comp-type", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamHC();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }

        assertEquals("NONE", ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_SEQ_FILE_COMP_TYPE));

    }

    /**
     * <p>
     * checkAndSetParamのテストケース
     * 正常系：log4j.xmlのパスが設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParam01() throws Exception {
        ConfigurationLoader.init(properties_hc, false, true);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("log.conf-path", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParam();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }

        assertEquals(
                new File("bulkloader/conf/log4j.xml").getCanonicalPath(),
                ConfigurationLoader.getProperty(Constants.PROP_KEY_LOG_CONF_PATH));

    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Importファイルの圧縮有無が設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB02() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.zip-comp-type", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals(
                FileCompType.STORED,
                FileCompType.find(ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_FILE_COMP_TYPE)));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：Importファイルの圧縮有無が不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB03() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.zip-comp-type", "2");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Importファイルの圧縮時のバッファサイズが設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB04() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.zip-comp-buf-size", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals("32768", ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_FILE_COMP_BUFSIZE));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Importファイルの圧縮時のバッファサイズ不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB05() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.zip-comp-buf-size", "a");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Importerのリトライ回数が設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB06() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.retry-count", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals("3", ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_RETRY_COUNT));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Importerのリトライ回数が不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB07() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.retry-count", "-1");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Importerのリトライインターバルが設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB08() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.retry-interval", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals("10", ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_RETRY_INTERVAL));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Importerのリトライインターバルが不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB09() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.retry-interval", "10.5");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Exportファイルの圧縮時のバッファサイズが設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB10() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.zip-comp-buf-size", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals("32768", ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_FILE_COMP_BUFSIZE));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Exportファイルの圧縮時のバッファサイズが不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB11() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.zip-comp-buf-size", "-1");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Exporterのリトライ回数が設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB12() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.retry-count", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals("3", ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_RETRY_COUNT));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Exporterのリトライ回数が不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB13() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.retry-count", "a１１１");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Exporterのリトライインターバルが設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB14() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.retry-interval", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals("10", ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_RETRY_INTERVAL));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：Exporterのリトライインターバルが不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB15() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.retry-interval", "12.5");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：エクスポート処理でExport対象テーブルにデータをコピーする時の最大レコード数が設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB18() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.data-copy-max-count", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals("100000", ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_COPY_MAX_RECORD));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：エクスポート処理でExport対象テーブルにデータをコピーする時の最大レコード数が不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB19() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.data-copy-max-count", "-10");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：SSHのパスが設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB21() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("ssh.path", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof BulkLoaderSystemException);
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：HDFSのNameノードのIPアドレス又はホスト名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB22() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("hadoop-cluster.host", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof BulkLoaderSystemException);
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：HDFSのNameノードのユーザー名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB23() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("hadoop-cluster.user", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof BulkLoaderSystemException);
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：Importファイルを置くディレクトリのトップディレクトリが設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB24() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.tsv-create-dir", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof BulkLoaderSystemException);
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：Extractorのシェル名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Ignore(Constants.PROP_KEY_EXT_SHELL_NAME + " is obsoleted")
    @Test
    public void checkAndSetParamDB25() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXT_SHELL_NAME, "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof BulkLoaderSystemException);
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：エクスポートファイルを置くディレクトリのトップディレクトリが設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB26() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.tsv-create-dir", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof BulkLoaderSystemException);
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：Collectorのシェル名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Ignore(Constants.PROP_KEY_COL_SHELL_NAME + " is obsoleted")
    @Test
    public void checkAndSetParamDB27() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_COL_SHELL_NAME, "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof BulkLoaderSystemException);
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：インポート正常終了時のTSVファイル削除有無が設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB28() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.delete-tsv", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals(
                TsvDeleteType.find(Constants.PROP_DEFAULT_IMPORT_TSV_DELETE),
                TsvDeleteType.find(ConfigurationLoader.getProperty(Constants.PROP_KEY_IMPORT_TSV_DELETE)));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：インポート正常終了時のTSVファイル削除有無が不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB29() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("import.delete-tsv", "2");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 正常系：エクスポート正常終了時のTSVファイル削除有無が設定されていないケース(デフォルト値が設定される)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB30() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.delete-tsv", "");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }
        assertEquals(
                TsvDeleteType.find(Constants.PROP_DEFAULT_EXPORT_TSV_DELETE),
                TsvDeleteType.find(ConfigurationLoader.getProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE)));
    }
    /**
     * <p>
     * checkAndSetParamDBのテストケース
     * 異常系：エクスポート正常終了時のTSVファイル削除有無が不正なケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkAndSetParamDB31() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("export.delete-tsv", "2");
        ConfigurationLoader.setProperty(p);

        try {
            ConfigurationLoader.checkAndSetParamDB();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        }
    }
    /**
     * <p>
     * getPropStartWithStringのテストケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void getPropStartWithString01() throws Exception {
        ConfigurationLoader.init(properties_db, false, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("clean.hdfs-dir.0", "abc");
        p.setProperty("clean.hdfs-dir.1", "def");
        p.setProperty("clean.hdfs-dir.2", "ghi");
        p.setProperty("clean.hdfs-dir.3", "jkl");
        p.setProperty("clean.hdfs-dir1.0", "xxx");
        p.setProperty("clean.hdfs.0", "xxx");
        p.setProperty("clean.hdfs-dir", "xxx");
        ConfigurationLoader.setProperty(p);

        List<String> list = ConfigurationLoader.getPropStartWithString("clean.hdfs-dir.");
        assertEquals(4, list.size());
        assertEquals("clean.hdfs-dir.3", list.get(0));
        assertEquals("clean.hdfs-dir.2", list.get(1));
        assertEquals("clean.hdfs-dir.1", list.get(2));
        assertEquals("clean.hdfs-dir.0", list.get(3));
    }
    /**
     * <p>
     * getNoEmptyListのテストケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void getNoEmptyList01() throws Exception {
        ConfigurationLoader.init(properties_db, false, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("clean.hdfs-dir.0", "abc");
        p.setProperty("clean.hdfs-dir.1", "");
        p.setProperty("clean.hdfs-dir.3", "jkl");
        ConfigurationLoader.setProperty(p);

        List<String> list = new ArrayList<String>();
        list.add("clean.hdfs-dir.0");
        list.add("clean.hdfs-dir.1");
        list.add("clean.hdfs-dir.3");
        list.add("clean.hdfs-dir.4");
        List<String> resultList = ConfigurationLoader.getExistValueList(list);

        assertEquals(2, resultList.size());
        assertEquals("clean.hdfs-dir.0", resultList.get(0));
        assertEquals("clean.hdfs-dir.3", resultList.get(1));

        resultList = ConfigurationLoader.getExistValueList(null);
        assertEquals(0, resultList.size());

        resultList = ConfigurationLoader.getExistValueList(new ArrayList<String>());
        assertEquals(0, resultList.size());
    }
    /**
     * <p>
     * loadJDBCPropのテストケース
     * 正常系：DBMSの接続情報を記述したプロパティファイルを読み込むケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadJDBCProp01() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
        try {
            ConfigurationLoader.loadJDBCProp("target3");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        assertEquals("com.mysql.jdbc.Driver", ConfigurationLoader.getProperty("jdbc.driver"));
        assertEquals("jdbc:mysql://localhost/__asakusa_utest_thundergate", ConfigurationLoader.getProperty("jdbc.url"));
        assertEquals("__asakusa_ut_tg", ConfigurationLoader.getProperty("jdbc.user"));
        assertEquals("__asakusa_ut_tg", ConfigurationLoader.getProperty("jdbc.password"));
        assertEquals(PATH_DB_PARAMETER, ConfigurationLoader.getProperty("db.parameter"));

    }
    /**
     * <p>
     * loadJDBCPropのテストケース
     * 異常系：DBMSの接続情報を記述したプロパティファイルの読み込みに失敗するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadJDBCProp02() throws Exception {
        ConfigurationLoader.init(properties_db, true, false);
//        LogInitializer.execute(ConfigurationLoader.getProperty(Constants.PROP_KEY_LOG_CONF_PATH));
        try {
            ConfigurationLoader.loadJDBCProp("targetErr");
            fail();
        } catch (BulkLoaderSystemException e) {
            LOG.info(e.getCause(), e.getMessageId(), e.getMessageArgs());
        }
    }
    /**
     * <p>
     * loadJDBCPropのテストケース
     * 異常系：DBMSの接続情報のJDBCドライバが設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadJDBCProp03() throws Exception {
//        ConfigurationLoader.init(propertys_db, true, false);
//        LogInitializer.execute(ConfigurationLoader.getProperty(Constants.PROP_KEY_LOG_CONF_PATH));

        ConfigurationLoader.init(properties_db, false, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("jdbc.url", "jdbc:mysql://localhost/asakusa");
        p.setProperty("jdbc.user", "asakusa");
        p.setProperty("jdbc.password", "asakusa");
        p.setProperty("db.parameter", PATH_DB_PARAMETER);
        ConfigurationLoader.setProperty(p);
        try {
            ConfigurationLoader.loadJDBCProp("target2");
            fail();
        } catch (BulkLoaderSystemException e) {
            LOG.info(e.getCause(), e.getMessageId(), e.getMessageArgs());
        }
    }
    /**
     * <p>
     * loadJDBCPropのテストケース
     * 異常系：DBMSの接続URLが設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadJDBCProp04() throws Exception {
//        ConfigurationLoader.init(propertys_db, true, false);
//        LogInitializer.execute(ConfigurationLoader.getProperty(Constants.PROP_KEY_LOG_CONF_PATH));

        ConfigurationLoader.init(properties_db, false, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("jdbc.driver", "com.mysql.jdbc.Driver");
        p.setProperty("jdbc.user", "asakusa");
        p.setProperty("jdbc.password", "asakusa");
        p.setProperty("db.parameter", PATH_DB_PARAMETER);
        ConfigurationLoader.setProperty(p);
        try {
            ConfigurationLoader.loadJDBCProp("target2");
            fail();
        } catch (BulkLoaderSystemException e) {
            LOG.info(e.getCause(), e.getMessageId(), e.getMessageArgs());
        }
    }
    /**
     * <p>
     * loadJDBCPropのテストケース
     * 異常系：DBMSのユーザー名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadJDBCProp05() throws Exception {
//        ConfigurationLoader.init(propertys_db, true, false);
//        LogInitializer.execute(ConfigurationLoader.getProperty(Constants.PROP_KEY_LOG_CONF_PATH));

        ConfigurationLoader.init(properties_db, false, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("jdbc.driver", "com.mysql.jdbc.Driver");
        p.setProperty("jdbc.url", "jdbc:mysql://localhost/asakusa");
        p.setProperty("jdbc.password", "asakusa");
        p.setProperty("db.parameter", PATH_DB_PARAMETER);
        ConfigurationLoader.setProperty(p);
        try {
            ConfigurationLoader.loadJDBCProp("target2");
            fail();
        } catch (BulkLoaderSystemException e) {
            LOG.info(e.getCause(), e.getMessageId(), e.getMessageArgs());
        }
    }
    /**
     * <p>
     * loadJDBCPropのテストケース
     * 異常系：DBMSのパスワードが設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadJDBCProp06() throws Exception {
//        ConfigurationLoader.init(propertys_db, true, false);
//        LogInitializer.execute(ConfigurationLoader.getProperty(Constants.PROP_KEY_LOG_CONF_PATH));

        ConfigurationLoader.init(properties_db, false, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("jdbc.driver", "com.mysql.jdbc.Driver");
        p.setProperty("jdbc.url", "jdbc:mysql://localhost/asakusa");
        p.setProperty("jdbc.user", "asakusa");
        p.setProperty("db.parameter", PATH_DB_PARAMETER);
        ConfigurationLoader.setProperty(p);
        try {
            ConfigurationLoader.loadJDBCProp("target2");
            fail();
        } catch (BulkLoaderSystemException e) {
            LOG.info(e.getCause(), e.getMessageId(), e.getMessageArgs());
        }
    }
    /**
     * <p>
     * loadJDBCPropのテストケース
     * 異常系：DBMSのDB名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadJDBCProp07() throws Exception {
//        ConfigurationLoader.init(propertys_db, true, false);
//        LogInitializer.execute(ConfigurationLoader.getProperty(Constants.PROP_KEY_LOG_CONF_PATH));

        ConfigurationLoader.init(properties_db, false, false);
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty("jdbc.driver", "com.mysql.jdbc.Driver");
        p.setProperty("jdbc.url", "jdbc:mysql://localhost/asakusa");
        p.setProperty("jdbc.user", "asakusa");
        p.setProperty("jdbc.password", "asakusa");
        p.setProperty("db.parameter", PATH_DB_PARAMETER);
        ConfigurationLoader.setProperty(p);
        try {
            ConfigurationLoader.loadJDBCProp("target2");
        } catch (BulkLoaderSystemException e) {
            LOG.info(e.getCause(), e.getMessageId(), e.getMessageArgs());
        }
    }
}
