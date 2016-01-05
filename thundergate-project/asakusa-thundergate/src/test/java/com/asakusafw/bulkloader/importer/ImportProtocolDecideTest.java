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
package com.asakusafw.bulkloader.importer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.modelgen.table.model.ImportTarget1;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.cache.LocalCacheInfo;
import com.asakusafw.bulkloader.cache.LocalCacheInfoRepository;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.exception.BulkLoaderReRunnableException;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;

/**
 * Test for {@link ImportProtocolDecide}.
 */
public class ImportProtocolDecideTest {

    private static List<String> properties = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    private static String testJobflowId = "JOB_FLOW01";
    private static String testExecutionId = "JOB_FLOW01-001";

    /**
     * set up.
     * @throws Exception if failed
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
    }

    /**
     * clean up.
     * @throws Exception if failed
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownEnv();
        UnitTestUtil.tearDownAfterClass();
    }

    /**
     * set up.
     * @throws Exception if failed
     */
    @Before
    public void setUp() throws Exception {
        BulkLoaderInitializer.initDBServer(testJobflowId, testExecutionId, properties, "target1");
        UnitTestUtil.setUpDB();
        UnitTestUtil.startUp();

        Connection connection = DBConnection.getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("DROP TABLE IF EXISTS __TG_TEST1");
            statement.execute("DROP TABLE IF EXISTS __TG_TEST2");
            statement.execute("CREATE TABLE __TG_TEST1 ( VALUE DATETIME ) ENGINE=InnoDB");
            statement.execute("CREATE TABLE __TG_TEST2 ( VALUE VARCHAR(200) ) ENGINE=InnoDB");
        } finally {
            DBConnection.closeStmt(statement);
            DBConnection.closeConn(connection);
        }
    }

    /**
     * only normal contents.
     * @throws Exception if failed
     */
    @Test
    public void contents() throws Exception {
        ImportBean bean = createBean();
        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId(null);
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide();
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.CONTENT));
        assertThat(tb1.getStartTimestamp(), is(nullValue()));
    }

    /**
     * Creates a new cache.
     * @throws Exception if failed
     */
    @Test
    public void create_cache() throws Exception {
        ImportBean bean = createBean();

        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.emptyMap();
            }
        };
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.CREATE_CACHE));
        assertThat(tb1.getImportProtocol().getLocation(), is(tb1.getDfsFilePath()));
        assertThat(tb1.getStartTimestamp(), is(nullValue()));
        CacheInfo info = tb1.getImportProtocol().getInfo();
        assertThat(info, is(notNullValue()));

        assertThat(info.getId(), is("tb1"));
        assertThat(info.getFeatureVersion(), is(CacheInfo.FEATURE_VERSION));
        assertThat(info.getTimestamp(), is(not(nullValue())));
        assertThat(info.getTableName(), is("__TG_TEST1"));
        assertThat(info.getColumnNames(), is((Object) new HashSet<String>(tb1.getImportTargetColumns())));
        assertThat(info.getModelClassName(), is(ImportTarget1.class.getName()));
        assertThat(info.getModelClassVersion(), is(new ImportTarget1().__tgc__DataModelVersion()));
    }

    /**
     * Updates a cache.
     * @throws Exception if failed
     */
    @Test
    public void update_cache() throws Exception {
        ImportBean bean = createBean();

        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        final ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        Connection conn = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(conn);
            repo.putCacheInfo(new LocalCacheInfo(
                    tb1.getCacheId(),
                    null,
                    null,
                    "__TG_TEST1",
                    tb1.getDfsFilePath()));

        } finally {
            DBConnection.closeConn(conn);
        }

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.singletonMap("tb1", new CacheInfo(
                        CacheInfo.FEATURE_VERSION,
                        tb1.getCacheId(),
                        offset(-1),
                        "__TG_TEST1",
                        tb1.getImportTargetColumns(),
                        tb1.getImportTargetType().getName(),
                        new ImportTarget1().__tgc__DataModelVersion()));
            }
        };
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.UPDATE_CACHE));
        assertThat(tb1.getImportProtocol().getLocation(), is(tb1.getDfsFilePath()));
        assertThat(tb1.getStartTimestamp(), is(notNullValue()));
        CacheInfo info = tb1.getImportProtocol().getInfo();
        assertThat(info, is(notNullValue()));

        assertThat(info.getId(), is("tb1"));
        assertThat(info.getFeatureVersion(), is(CacheInfo.FEATURE_VERSION));
        assertThat(info.getTimestamp(), is(not(nullValue())));
        assertThat(info.getTableName(), is("__TG_TEST1"));
        assertThat(info.getColumnNames(), is((Object) new HashSet<String>(tb1.getImportTargetColumns())));
        assertThat(info.getModelClassName(), is(ImportTarget1.class.getName()));
        assertThat(info.getModelClassVersion(), is(new ImportTarget1().__tgc__DataModelVersion()));
    }

    /**
     * Updates a cache.
     * @throws Exception if failed
     */
    @Test
    public void update_cache_rebuild() throws Exception {
        ImportBean bean = createBean();

        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        final ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        Connection conn = DBConnection.getConnection();
        final Calendar last = offset(-1);
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(conn);
            repo.putCacheInfo(new LocalCacheInfo(
                    tb1.getCacheId(),
                    null,
                    last,
                    "__TG_TEST1",
                    tb1.getDfsFilePath()));

        } finally {
            DBConnection.closeConn(conn);
        }

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.singletonMap("tb1", new CacheInfo(
                        CacheInfo.FEATURE_VERSION,
                        tb1.getCacheId(),
                        last,
                        "__TG_TEST1",
                        tb1.getImportTargetColumns(),
                        tb1.getImportTargetType().getName(),
                        new ImportTarget1().__tgc__DataModelVersion()));
            }
        };
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.UPDATE_CACHE));
        assertThat(tb1.getImportProtocol().getLocation(), is(tb1.getDfsFilePath()));
        assertThat(tb1.getStartTimestamp(), is(notNullValue()));
        CacheInfo info = tb1.getImportProtocol().getInfo();
        assertThat(info, is(notNullValue()));

        assertThat(info.getId(), is("tb1"));
        assertThat(info.getFeatureVersion(), is(CacheInfo.FEATURE_VERSION));
        assertThat(info.getTimestamp(), is(not(nullValue())));
        assertThat(info.getTableName(), is("__TG_TEST1"));
        assertThat(info.getColumnNames(), is((Object) new HashSet<String>(tb1.getImportTargetColumns())));
        assertThat(info.getModelClassName(), is(ImportTarget1.class.getName()));
        assertThat(info.getModelClassVersion(), is(new ImportTarget1().__tgc__DataModelVersion()));
    }

    /**
     * Updates a cache (DB may be rollbacked).
     * @throws Exception if failed
     */
    @Test
    public void update_cache_db_rollback() throws Exception {
        ImportBean bean = createBean();

        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        final ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        Connection conn = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(conn);
            repo.putCacheInfo(new LocalCacheInfo(
                    tb1.getCacheId(),
                    null,
                    null,
                    "__TG_TEST1",
                    tb1.getDfsFilePath()));

        } finally {
            DBConnection.closeConn(conn);
        }

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.singletonMap("tb1", new CacheInfo(
                        CacheInfo.FEATURE_VERSION,
                        tb1.getCacheId(),
                        offset(+1),
                        "__TG_TEST1",
                        tb1.getImportTargetColumns(),
                        tb1.getImportTargetType().getName(),
                        new ImportTarget1().__tgc__DataModelVersion()));
            }
        };
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.CREATE_CACHE));
    }

    /**
     * Updates a cache (DFS may be rollbacked).
     * @throws Exception if failed
     */
    @Test
    public void update_cache_dfs_rollback() throws Exception {
        ImportBean bean = createBean();

        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        final ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        Connection conn = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(conn);
            repo.putCacheInfo(new LocalCacheInfo(
                    tb1.getCacheId(),
                    null,
                    offset(-1),
                    "__TG_TEST1",
                    tb1.getDfsFilePath()));

        } finally {
            DBConnection.closeConn(conn);
        }

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.singletonMap("tb1", new CacheInfo(
                        CacheInfo.FEATURE_VERSION,
                        tb1.getCacheId(),
                        offset(-2),
                        "__TG_TEST1",
                        tb1.getImportTargetColumns(),
                        tb1.getImportTargetType().getName(),
                        new ImportTarget1().__tgc__DataModelVersion()));
            }
        };
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.CREATE_CACHE));
    }

    /**
     * Updates a cache (local information was broken).
     * @throws Exception if failed
     */
    @Test
    public void update_cache_broken_local() throws Exception {
        ImportBean bean = createBean();

        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        final ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.singletonMap("tb1", new CacheInfo(
                        CacheInfo.FEATURE_VERSION,
                        tb1.getCacheId(),
                        offset(-1),
                        "__TG_TEST1",
                        tb1.getImportTargetColumns(),
                        tb1.getImportTargetType().getName(),
                        new ImportTarget1().__tgc__DataModelVersion()));
            }
        };
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.CREATE_CACHE));
    }

    /**
     * Updates a cache (remote information was broken).
     * @throws Exception if failed
     */
    @Test
    public void update_cache_broken_remote() throws Exception {
        ImportBean bean = createBean();

        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        final ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        Connection conn = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(conn);
            repo.putCacheInfo(new LocalCacheInfo(
                    tb1.getCacheId(),
                    null,
                    null,
                    "__TG_TEST1",
                    tb1.getDfsFilePath()));

        } finally {
            DBConnection.closeConn(conn);
        }

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.emptyMap();
            }
        };
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.CREATE_CACHE));
    }

    /**
     * Updates a cache (inconsistent feature).
     * @throws Exception if failed
     */
    @Test
    public void update_cache_feature_changed() throws Exception {
        ImportBean bean = createBean();

        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        final ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        Connection conn = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(conn);
            repo.putCacheInfo(new LocalCacheInfo(
                    tb1.getCacheId(),
                    null,
                    null,
                    "__TG_TEST1",
                    tb1.getDfsFilePath()));

        } finally {
            DBConnection.closeConn(conn);
        }

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.singletonMap("tb1", new CacheInfo(
                        CacheInfo.FEATURE_VERSION + "+",
                        tb1.getCacheId(),
                        offset(-1),
                        "__TG_TEST1",
                        tb1.getImportTargetColumns(),
                        tb1.getImportTargetType().getName(),
                        new ImportTarget1().__tgc__DataModelVersion()));
            }
        };
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.CREATE_CACHE));
    }

    /**
     * Updates a cache (dataomdel was inconsistent).
     * @throws Exception if failed
     */
    @Test
    public void update_cache_inconsistent_model() throws Exception {
        ImportBean bean = createBean();

        Map<String, ImportTargetTableBean> targetTable = new HashMap<String, ImportTargetTableBean>();

        final ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable.put("__TG_TEST1", tb1);

        Connection conn = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(conn);
            repo.putCacheInfo(new LocalCacheInfo(
                    tb1.getCacheId(),
                    null,
                    null,
                    "__TG_TEST1",
                    tb1.getDfsFilePath()));

        } finally {
            DBConnection.closeConn(conn);
        }

        bean.setTargetTable(targetTable);
        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.singletonMap("tb1", new CacheInfo(
                        CacheInfo.FEATURE_VERSION,
                        tb1.getCacheId(),
                        offset(-1),
                        "__TG_TEST1",
                        tb1.getImportTargetColumns(),
                        tb1.getImportTargetType().getName(),
                        new ImportTarget1().__tgc__DataModelVersion() + 1));
            }
        };
        service.execute(bean);

        assertThat(tb1.getImportProtocol().getKind(), is(FileProtocol.Kind.CREATE_CACHE));
    }

    /**
     * Cache lock is conflict.
     * @throws Exception if failed
     */
    @Test
    public void conflict_lock() throws Exception {
        ImportBean bean1 = createBean();
        Map<String, ImportTargetTableBean> targetTable1 = new HashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable1.put("__TG_TEST1", tb1);
        bean1.setTargetTable(targetTable1);

        ImportBean bean2 = createBean();
        Map<String, ImportTargetTableBean> targetTable2 = new HashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tb2 = new ImportTargetTableBean();
        tb2.setCacheId("tb1");
        tb2.setDfsFilePath("tb1");
        tb2.setImportTargetType(ImportTarget1.class);
        tb2.setImportTargetColumns(Arrays.asList("A"));
        tb2.setSearchCondition("");
        targetTable2.put("__TG_TEST1", tb2);
        bean2.setTargetTable(targetTable2);

        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.emptyMap();
            }
        };
        service.execute(bean1);
        try {
            service.execute(bean2);
            fail();
        } catch (BulkLoaderReRunnableException e) {
            // ok
        }
    }

    /**
     * Cache lock is conflict.
     * @throws Exception if failed
     */
    @Test
    public void release_lock() throws Exception {
        ImportBean bean1 = createBean();
        Map<String, ImportTargetTableBean> targetTable1 = new HashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tb1 = new ImportTargetTableBean();
        tb1.setCacheId("tb1");
        tb1.setDfsFilePath("tb1");
        tb1.setImportTargetType(ImportTarget1.class);
        tb1.setImportTargetColumns(Arrays.asList("A"));
        tb1.setSearchCondition("");
        targetTable1.put("__TG_TEST1", tb1);
        bean1.setTargetTable(targetTable1);

        ImportBean bean2 = createBean();
        Map<String, ImportTargetTableBean> targetTable2 = new HashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tb2 = new ImportTargetTableBean();
        tb2.setCacheId("tb1");
        tb2.setDfsFilePath("tb1");
        tb2.setImportTargetType(ImportTarget1.class);
        tb2.setImportTargetColumns(Arrays.asList("A"));
        tb2.setSearchCondition("");
        targetTable2.put("__TG_TEST1", tb2);
        bean2.setTargetTable(targetTable2);

        ImportProtocolDecide service = new ImportProtocolDecide() {
            @Override
            protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean ignored)
                    throws BulkLoaderSystemException {
                return Collections.emptyMap();
            }
        };
        service.execute(bean1);
        service.cleanUpForRetry(bean1);
        service.execute(bean2);
        // ok.
    }

    private ImportBean createBean() {
        ImportBean bean = new ImportBean();
        bean.setTargetName("target");
        bean.setBatchId("batch");
        bean.setJobflowId(testJobflowId);
        bean.setExecutionId(testExecutionId);
        return bean;
    }

    static Calendar offset(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, days);
        return c;
    }

    static Calendar calendar(String timeString) throws AssertionError {
        Calendar timestamp = Calendar.getInstance();
        try {
            timestamp.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeString));
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
        return timestamp;
    }

    class Mock extends ImportProtocolDecide {
        // empty
    }
}
