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
package com.asakusafw.bulkloader.cache;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.bulkloader.transfer.FileProtocol.Kind;

/**
 * Test for {@link GcCacheStorage}.
 */
public class GcCacheStorageTest {

    private static List<String> properties = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    private static String testJobflowId = "JOB_FLOW01";
    private static String testExecutionId = "JOB_FLOW01-001";

    static final LocalCacheInfo INFO1 = new LocalCacheInfo(
            "testing1",
            null,
            null,
            "__TG_TEST",
            "/test/path1");

    static final LocalCacheInfo INFO2 = new LocalCacheInfo(
            "testing2",
            null,
            null,
            "__TG_TEST",
            "/test/path2");

    static final LocalCacheInfo INFO3 = new LocalCacheInfo(
            "testing3",
            null,
            null,
            "__TG_TEST",
            "/test/path3");


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
            statement.execute("DROP TABLE IF EXISTS __TG_TEST");
            statement.execute("CREATE TABLE __TG_TEST ( VALUE DATETIME ) ENGINE=InnoDB");

            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
            repo.putCacheInfo(INFO1);
            repo.putCacheInfo(INFO2);
            repo.putCacheInfo(INFO3);
        } finally {
            DBConnection.closeStmt(statement);
            DBConnection.closeConn(connection);
        }
    }

    /**
     * clean up.
     * @throws Exception if failed
     */
    @After
    public void tearDown() throws Exception {
        UnitTestUtil.tearDown();
    }

    /**
     * nothing to delete.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        int code = new Mock()
            .execute("dummy", testExecutionId);
        assertThat(code, is(Constants.EXIT_CODE_SUCCESS));
    }

    /**
     * delete an entry.
     * @throws Exception if failed
     */
    @Test
    public void delete() throws Exception {
        Connection connection = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
            repo.deleteCacheInfo(INFO1.getId());

            assertThat(repo.listDeletedCacheInfo().size(), is(1));

            int code = new Mock()
                .put(INFO1, Kind.RESPONSE_DELETED)
                .execute("dummy", testExecutionId);

            assertThat(code, is(Constants.EXIT_CODE_SUCCESS));
            assertThat(repo.listDeletedCacheInfo().size(), is(0));
        } finally {
            DBConnection.closeConn(connection);
        }
    }

    /**
     * delete an entry but is already deleted.
     * @throws Exception if failed
     */
    @Test
    public void delete_already() throws Exception {
        Connection connection = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
            repo.deleteCacheInfo(INFO1.getId());

            assertThat(repo.listDeletedCacheInfo().size(), is(1));

            int code = new Mock()
                .put(INFO1, Kind.RESPONSE_NOT_FOUND)
                .execute("dummy", testExecutionId);

            assertThat(code, is(Constants.EXIT_CODE_SUCCESS));
            assertThat(repo.listDeletedCacheInfo().size(), is(0));
        } finally {
            DBConnection.closeConn(connection);
        }
    }

    /**
     * delete an entry but failed.
     * @throws Exception if failed
     */
    @Test
    public void delete_error() throws Exception {
        Connection connection = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
            repo.deleteCacheInfo(INFO1.getId());

            assertThat(repo.listDeletedCacheInfo().size(), is(1));

            int code = new Mock()
                .put(INFO1, Kind.RESPONSE_ERROR)
                .execute("dummy", testExecutionId);

            assertThat(code, not(Constants.EXIT_CODE_SUCCESS));
            assertThat(repo.listDeletedCacheInfo().size(), is(1));
        } finally {
            DBConnection.closeConn(connection);
        }
    }

    /**
     * delete an entry but is omitted because the remote program was something wrong.
     * @throws Exception if failed
     */
    @Test
    public void delete_omitted() throws Exception {
        Connection connection = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
            repo.deleteCacheInfo(INFO1.getId());

            assertThat(repo.listDeletedCacheInfo().size(), is(1));

            int code = new Mock()
                .execute("dummy", testExecutionId);

            assertThat(code, not(Constants.EXIT_CODE_SUCCESS));
            assertThat(repo.listDeletedCacheInfo().size(), is(1));
        } finally {
            DBConnection.closeConn(connection);
        }
    }

    /**
     * delete an entry but was locked.
     * @throws Exception if failed
     */
    @Test
    public void delete_locked() throws Exception {
        Connection connection = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
            repo.tryLock(testExecutionId + "-other", INFO1.getId(), INFO1.getTableName());
            repo.deleteCacheInfo(INFO1.getId());

            assertThat(repo.listDeletedCacheInfo().size(), is(1));

            int code = new Mock()
                .put(INFO1, Kind.RESPONSE_DELETED)
                .execute("dummy", testExecutionId);

            assertThat(code, not(Constants.EXIT_CODE_SUCCESS));
            assertThat(repo.listDeletedCacheInfo().size(), is(1));
        } finally {
            DBConnection.closeConn(connection);
        }
    }

    /**
     * delete entries.
     * @throws Exception if failed
     */
    @Test
    public void delete_mixed() throws Exception {
        Connection connection = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
            repo.deleteCacheInfo(INFO1.getId());
            repo.deleteCacheInfo(INFO2.getId());
            repo.deleteCacheInfo(INFO3.getId());

            assertThat(repo.listDeletedCacheInfo().size(), is(3));

            int code = new Mock()
                .put(INFO1, Kind.RESPONSE_DELETED)
                .put(INFO2, Kind.RESPONSE_NOT_FOUND)
                .put(INFO3, Kind.RESPONSE_ERROR)
                .execute("dummy", testExecutionId);

            assertThat(code, not(Constants.EXIT_CODE_SUCCESS));
            assertThat(repo.listDeletedCacheInfo().size(), is(1));
        } finally {
            DBConnection.closeConn(connection);
        }
    }

    /**
     * delete an entry but remote program was crashed.
     * @throws Exception if failed
     */
    @Test
    public void delete_crash() throws Exception {
        Connection connection = DBConnection.getConnection();
        try {
            LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
            repo.deleteCacheInfo(INFO1.getId());

            assertThat(repo.listDeletedCacheInfo().size(), is(1));

            int code = new Mock() {
                @Override
                protected DeleteCacheStorageLocal getClient() {
                    return new DeleteCacheStorageLocal() {
                        @Override
                        public Map<String, Kind> delete(List<LocalCacheInfo> list, String targetName)
                                throws BulkLoaderSystemException {
                            throw new BulkLoaderSystemException(getClass(), "TG-GCCACHE-03008", "testing");
                        }
                    };
                }
            }.execute("dummy", testExecutionId);

            assertThat(code, is(Constants.EXIT_CODE_ERROR));
        } finally {
            DBConnection.closeConn(connection);
        }
    }

    static class Mock extends GcCacheStorage {

        final Map<String, FileProtocol.Kind> results = new LinkedHashMap<String, FileProtocol.Kind>();

        Mock put(LocalCacheInfo info, FileProtocol.Kind kind) {
            results.put(info.getPath(), kind);
            return this;
        }

        @Override
        protected DeleteCacheStorageLocal getClient() {
            return new DeleteCacheStorageLocal() {
                @Override
                public Map<String, Kind> delete(List<LocalCacheInfo> list, String targetName)
                        throws BulkLoaderSystemException {
                    return results;
                }
            };
        }
    }
}
