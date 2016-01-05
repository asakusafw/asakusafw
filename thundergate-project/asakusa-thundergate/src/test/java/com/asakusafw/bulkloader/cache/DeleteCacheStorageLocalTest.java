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
import java.util.ArrayList;
import java.util.List;
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

import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileList.Reader;
import com.asakusafw.bulkloader.transfer.FileList.Writer;
import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.FileProtocol.Kind;
import com.asakusafw.bulkloader.transfer.StreamFileListProvider;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.thundergate.runtime.cache.CacheStorage;

/**
 * Test for {@link DeleteCacheStorageLocal}.
 */
public class DeleteCacheStorageLocalTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    final DeleteCacheStorageRemote remote = new DeleteCacheStorageRemote();

    LocalCacheInfo info1, info2, info3;

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
        remote.initialize("target", "tester");
        remote.setConf(new ConfigurationProvider().newInstance());
        ConfigurationLoader.getProperty().setProperty(
                Constants.PROP_KEY_BASE_PATH,
                folder.getRoot().getAbsoluteFile().toURI().toString());
        info1 = new LocalCacheInfo(
                "testing1",
                null,
                null,
                "__TG_TEST",
                "testing1");
        info2 = new LocalCacheInfo(
                "testing2",
                null,
                null,
                "__TG_TEST",
                "testing2");
        info3 = new LocalCacheInfo(
                "testing3",
                null,
                null,
                "__TG_TEST",
                "testing3");
    }

    /**
     * nothing to delete.
     * @throws Exception if failed
     */
    @Test(timeout =  5000L)
    public void nothing() throws Exception {
        List<LocalCacheInfo> list = new ArrayList<LocalCacheInfo>();
        Map<String, Kind> results = new Mock().delete(list, "dummy");
        assertThat(results.size(), is(0));
    }

    /**
     * delete an entry.
     * @throws Exception if failed
     */
    @Test(timeout =  5000L)
    public void delete() throws Exception {
        prepare(info1);

        List<LocalCacheInfo> list = new ArrayList<LocalCacheInfo>();
        list.add(info1);

        Map<String, Kind> results = new Mock().delete(list, "dummy");
        assertThat(results.size(), is(1));
        assertThat(results.get(info1.getPath()), is(Kind.RESPONSE_DELETED));
    }

    /**
     * delete an entry but is missing.
     * @throws Exception if failed
     */
    @Test(timeout =  5000L)
    public void delete_missing() throws Exception {
        List<LocalCacheInfo> list = new ArrayList<LocalCacheInfo>();
        list.add(info1);

        Map<String, Kind> results = new Mock().delete(list, "dummy");
        assertThat(results.size(), is(1));
        assertThat(results.get(info1.getPath()), is(Kind.RESPONSE_NOT_FOUND));
    }

    /**
     * delete an entry but is missing.
     * @throws Exception if failed
     */
    @Test(timeout =  5000L)
    public void delete_multiple() throws Exception {
        prepare(info1, info3);

        List<LocalCacheInfo> list = new ArrayList<LocalCacheInfo>();
        list.add(info1);
        list.add(info2);
        list.add(info3);

        Map<String, Kind> results = new Mock().delete(list, "dummy");
        assertThat(results.size(), is(3));
        assertThat(results.get(info1.getPath()), is(Kind.RESPONSE_DELETED));
        assertThat(results.get(info2.getPath()), is(Kind.RESPONSE_NOT_FOUND));
        assertThat(results.get(info3.getPath()), is(Kind.RESPONSE_DELETED));
    }

    private void prepare(LocalCacheInfo... caches) throws IOException {
        for (LocalCacheInfo info : caches) {
            CacheStorage storage = new CacheStorage(remote.getConf(), uri(info.getId()));
            try {
                storage.getFileSystem().create(storage.getHeadContents("0")).close();
            } finally {
                storage.close();
            }
        }
    }

    private URI uri(String string) {
        try {
            return FileNameUtil.createPath(remote.getConf(), string, "dummy", "dummy").toUri();
        } catch (BulkLoaderSystemException e) {
            throw new AssertionError(e);
        }
    }

    class Mock extends DeleteCacheStorageLocal {

        boolean fail = false;

        Mock willFail() {
            fail = true;
            return this;
        }

        @Override
        protected FileListProvider openFileList(String targetName) throws IOException {
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
