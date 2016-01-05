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
package com.asakusafw.bulkloader.exporter;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;


/**
 * ExportFileDeleteのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ExportFileDeleteTest {
    /** Importerで読み込むプロパティファイル */
    private static List<String> propertys = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    /** ジョブフローID */
    private static String jobflowId = "JOB_FLOW01";
    /** ジョブフロー実行ID */
    private static String executionId = "JOB_FLOW01-001";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys, "target1");
        UnitTestUtil.setUpDB();
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownDB();
        UnitTestUtil.tearDownAfterClass();
    }
    @Before
    public void setUp() throws Exception {
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys, "target1");
        UnitTestUtil.startUp();
    }
    @After
    public void tearDown() throws Exception {
        UnitTestUtil.tearDown();
    }
    /**
     *
     * <p>
     * 正常系：複数のファイルを削除するケース
     * 詳細の設定は以下の通り
     * ・Importファイル1：target/asakusa-thundergate/deltest1/load/EXP_IMPORT_TARGET1.tsv
     * ・Importファイル2：target/asakusa-thundergate/deltest1/load/EXP_IMPORT_TARGET2.tsv
     * ・Importファイル3：target/asakusa-thundergate/deltest1/load/EXP_IMPORT_TARGET3.tsv
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteFileTest01() throws Exception {
        // ディレクトリ生成
        File dumpDir = new File("target/asakusa-thundergate");

        // ファイル生成
        File importFile1 = new File(dumpDir, "EXP_IMPORT_TARGET1.tsv");
        File importFile2 = new File(dumpDir, "EXP_IMPORT_TARGET2.tsv");
        File importFile3 = new File(dumpDir, "EXP_IMPORT_TARGET3.tsv");
        importFile1.createNewFile();
        importFile2.createNewFile();
        importFile3.createNewFile();

        // ExporterBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean bean1 = new ExportTargetTableBean();
        bean1.addExportFile(importFile1);
        bean1.addExportFile(importFile2);
        targetTable.put("IMPORT_TARGET1", bean1);
        ExportTargetTableBean bean2 = new ExportTargetTableBean();
        bean2.addExportFile(importFile3);
        targetTable.put("IMPORT_TARGET2", bean2);

        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);

        // テスト対象クラス実行
        ExportFileDelete delete = new ExportFileDelete();
        delete.deleteFile(bean);

        // 実行結果の検証
       assertFalse(importFile1.exists());
       assertFalse(importFile2.exists());
       assertFalse(importFile3.exists());
       assertTrue(dumpDir.exists());
    }
    /**
     *
     * <p>
     * 正常系：削除対象ファイルが存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteFileTest02() throws Exception {
        // ディレクトリ生成
        File dumpDir = new File("target/asakusa-thundergate");

        // ファイル生成
        File importFile1 = new File(dumpDir, "EXP_IMPORT_TARGET1.tsv");
        File importFile2 = new File(dumpDir, "EXP_IMPORT_TARGET2.tsv");
        File importFile3 = new File(dumpDir, "EXP_IMPORT_TARGET3.tsv");

        // ExporterBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean bean1 = new ExportTargetTableBean();
        bean1.addExportFile(importFile1);
        bean1.addExportFile(importFile2);
        targetTable.put("IMPORT_TARGET1", bean1);
        ExportTargetTableBean bean2 = new ExportTargetTableBean();
        bean2.addExportFile(importFile3);
        targetTable.put("IMPORT_TARGET2", bean2);

        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);

        // テスト対象クラス実行
        ExportFileDelete delete = new ExportFileDelete();
        delete.deleteFile(bean);

        // 実行結果の検証
       assertFalse(importFile1.exists());
       assertFalse(importFile2.exists());
       assertFalse(importFile3.exists());
       assertTrue(dumpDir.exists());
    }
    /**
     *
     * <p>
     * 正常系：削除対象ファイルがない(null)のケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteFileTest03() throws Exception {
        // ファイル生成
        File importFile1 = null;
        File importFile2 = null;
        File importFile3 = null;

        // ExporterBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean bean1 = new ExportTargetTableBean();
        bean1.addExportFile(importFile1);
        bean1.addExportFile(importFile2);
        targetTable.put("IMPORT_TARGET1", bean1);
        ExportTargetTableBean bean2 = new ExportTargetTableBean();
        bean2.addExportFile(importFile3);
        targetTable.put("IMPORT_TARGET2", bean2);

        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);

        // テスト対象クラス実行
        ExportFileDelete delete = new ExportFileDelete();
        delete.deleteFile(bean);
    }
}
