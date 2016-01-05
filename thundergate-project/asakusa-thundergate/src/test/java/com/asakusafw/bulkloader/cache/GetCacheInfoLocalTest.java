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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileList.Reader;
import com.asakusafw.bulkloader.transfer.FileList.Writer;
import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.StreamFileListProvider;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;
import com.asakusafw.thundergate.runtime.cache.CacheStorage;

/**
 * Test for {@link GetCacheInfoLocal}.
 */
public class GetCacheInfoLocalTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    final GetCacheInfoRemote remote = new GetCacheInfoRemote();

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
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {
        UnitTestUtil.startUp();
        remote.initialize("target", "batch", "flow", "exec", "tester");
        remote.setConf(new ConfigurationProvider().newInstance());
        ConfigurationLoader.getProperty().setProperty(
                Constants.PROP_KEY_BASE_PATH,
                folder.getRoot().getAbsoluteFile().toURI().toString());
    }

    /**
     * no local cache information.
     * @throws Exception if failed
     */
    @Test(timeout =  5000L)
    public void withoutCache() throws Exception {
        ImportBean bean = createBean();
        Map<String, ImportTargetTableBean> map = new HashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean table = new ImportTargetTableBean();
        table.setDfsFilePath("normal");
        map.put("normal", table);
        bean.setTargetTable(map);

        GetCacheInfoLocal service = new Mock();
        Map<String, CacheInfo> results = service.get(bean);
        assertThat(results.size(), is(0));
    }

    /**
     * no remote cache information.
     * @throws Exception if failed
     */
    @Test(timeout =  5000L)
    public void nothing() throws Exception {
        ImportBean bean = createBean();
        Map<String, ImportTargetTableBean> map = new HashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean table = new ImportTargetTableBean();
        table.setDfsFilePath("nothing");
        table.setCacheId("nothing");
        map.put("nothing", table);
        bean.setTargetTable(map);

        GetCacheInfoLocal service = new Mock();
        Map<String, CacheInfo> results = service.get(bean);
        assertThat(results.size(), is(0));
    }

    /**
     * cache information exists.
     * @throws Exception if failed
     */
    @Test(timeout =  5000L)
    public void found() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "available",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);

        CacheStorage storage = new CacheStorage(remote.getConf(), uri("available"));
        try {
            storage.putHeadCacheInfo(info);
        } finally {
            storage.close();
        }

        ImportBean bean = createBean();
        Map<String, ImportTargetTableBean> map = new HashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean table = new ImportTargetTableBean();
        table.setDfsFilePath("available");
        table.setCacheId("available");
        map.put("available", table);
        bean.setTargetTable(map);

        GetCacheInfoLocal service = new Mock();
        Map<String, CacheInfo> results = service.get(bean);
        assertThat(results.size(), is(1));
        assertThat(results.get("available"), is(info));
    }

    /**
     * multiple requests.
     * @throws Exception if failed
     */
    @Test(timeout =  5000L)
    public void mixed() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "available",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);

        CacheStorage storage = new CacheStorage(remote.getConf(), uri("available"));
        try {
            storage.putHeadCacheInfo(info);
        } finally {
            storage.close();
        }

        ImportBean bean = createBean();
        Map<String, ImportTargetTableBean> map = new HashMap<String, ImportTargetTableBean>();

        ImportTargetTableBean table1 = new ImportTargetTableBean();
        table1.setDfsFilePath("nothing1");
        table1.setCacheId("nothing1");
        map.put("nothing1", table1);
        bean.setTargetTable(map);

        ImportTargetTableBean table2 = new ImportTargetTableBean();
        table2.setDfsFilePath("available");
        table2.setCacheId("available");
        map.put("available", table2);
        bean.setTargetTable(map);


        ImportTargetTableBean table3 = new ImportTargetTableBean();
        table3.setDfsFilePath("nothing3");
        table3.setCacheId("nothing3");
        map.put("nothing3", table3);
        bean.setTargetTable(map);

        ImportTargetTableBean table4 = new ImportTargetTableBean();
        table4.setDfsFilePath("nocache");
        map.put("nocache", table4);
        bean.setTargetTable(map);

        GetCacheInfoLocal service = new Mock();
        Map<String, CacheInfo> results = service.get(bean);
        assertThat(results.size(), is(1));
        assertThat(results.get("available"), is(info));
    }

    private ImportBean createBean() {
        ImportBean bean = new ImportBean();
        bean.setTargetName("target");
        bean.setBatchId("batch");
        bean.setJobflowId("flow");
        bean.setExecutionId("exec");
        return bean;
    }

    /**
     * execution will be failed.
     * @throws Exception if failed
     */
    @Test(timeout =  5000L)
    public void fail() throws Exception {
        ImportBean bean = createBean();
        Map<String, ImportTargetTableBean> map = new HashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean table = new ImportTargetTableBean();
        table.setDfsFilePath("nothing");
        table.setCacheId("nothing");
        map.put("nothing", table);
        bean.setTargetTable(map);

        GetCacheInfoLocal service = new Mock().willFail();
        try {
            service.get(bean);
            org.junit.Assert.fail();
        } catch (Exception e) {
            // ok.
        }
    }

    private URI uri(String string) {
        try {
            return FileNameUtil.createPath(remote.getConf(), string, "dummy", "dummy").toUri();
        } catch (BulkLoaderSystemException e) {
            throw new AssertionError(e);
        }
    }

    private Calendar calendar(String string) {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string);
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    class Mock extends GetCacheInfoLocal {

        boolean fail = false;

        Mock willFail() {
            fail = true;
            return this;
        }

        @Override
        protected FileListProvider openFileList(
                String targetName,
                String batchId,
                String jobflowId,
                String executionId) throws IOException {
            final PipedInputStream remoteStdin = new PipedInputStream();
            final PipedOutputStream remoteStdout = new PipedOutputStream();
            final PipedOutputStream upstream = new PipedOutputStream(remoteStdin);
            final PipedInputStream downstream = new PipedInputStream(remoteStdout);

            final Future<Void> future = Executors.newFixedThreadPool(1).submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Writer writer = FileList.createWriter(remoteStdout, false);
                    Reader reader = FileList.createReader(remoteStdin);
                    try {
                        remote.execute(reader, writer);
                    } finally {
                        // closes quitetly for piped streams
                        IOUtils.closeQuietly(writer);
                        IOUtils.closeQuietly(reader);
                    }
                    return null;
                }
            });

            return new StreamFileListProvider() {

                @Override
                protected OutputStream getOutputStream() throws IOException {
                    return upstream;
                }

                @Override
                protected InputStream getInputStream() throws IOException {
                    return downstream;
                }

                @Override
                protected void waitForDone() throws IOException, InterruptedException {
                    try {
                        future.get();
                    } catch (Exception e) {
                        throw new AssertionError(e.getCause());
                    }
                    if (fail) {
                        throw new IOException();
                    }
                }

                @Override
                public void close() throws IOException {
                    remoteStdin.close();
                    remoteStdout.close();
                }
            };
        }
    }
}
