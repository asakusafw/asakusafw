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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.fs.FSDataOutputStream;
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
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;
import com.asakusafw.thundergate.runtime.cache.CacheStorage;

/**
 * Test for {@link GetCacheInfoRemote}.
 */
public class GetCacheInfoRemoteTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final GetCacheInfoRemote service = new GetCacheInfoRemote();

    private final ByteArrayOutputStream writerBuffer = new ByteArrayOutputStream();

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
        service.initialize("target", "batch", "flow", "exec", "tester");
        service.setConf(new ConfigurationProvider().newInstance());
        ConfigurationLoader.getProperty().setProperty(
                Constants.PROP_KEY_BASE_PATH,
                folder.getRoot().getAbsoluteFile().toURI().toString());
    }

    /**
     * no cache information.
     * @throws Exception if failed
     */
    @Test
    public void nothing() throws Exception {
        FileList.Reader reader = prepare("nothing");
        FileList.Writer writer = FileList.createWriter(writerBuffer, false);
        service.execute(reader, writer);
        writer.close();
        List<FileProtocol> results = collect(writerBuffer.toByteArray());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getLocation(), endsWith("nothing"));
        assertThat(results.get(0).getKind(), is(FileProtocol.Kind.RESPONSE_NOT_FOUND));
    }

    /**
     * cache information exists.
     * @throws Exception if failed
     */
    @Test
    public void found() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "EXAMPLE",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);

        CacheStorage storage = new CacheStorage(service.getConf(), uri("available"));
        try {
            storage.putHeadCacheInfo(info);
        } finally {
            storage.close();
        }

        FileList.Reader reader = prepare("available");
        FileList.Writer writer = FileList.createWriter(writerBuffer, false);
        service.execute(reader, writer);
        writer.close();
        List<FileProtocol> results = collect(writerBuffer.toByteArray());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getLocation(), endsWith("available"));
        assertThat(results.get(0).getKind(), is(FileProtocol.Kind.RESPONSE_CACHE_INFO));
        assertThat(results.get(0).getInfo(), is(info));
    }

    /**
     * multiple requests.
     * @throws Exception if failed
     */
    @Test
    public void mixed() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "EXAMPLE",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);

        CacheStorage storage = new CacheStorage(service.getConf(), uri("available"));
        try {
            storage.putHeadCacheInfo(info);
        } finally {
            storage.close();
        }

        FileList.Reader reader = prepare("nothing1", "nothing2", "available", "nothing3");
        FileList.Writer writer = FileList.createWriter(writerBuffer, false);
        service.execute(reader, writer);
        writer.close();
        List<FileProtocol> results = collect(writerBuffer.toByteArray());
        assertThat(results.size(), is(4));
        assertThat(results.get(0).getLocation(), endsWith("nothing1"));
        assertThat(results.get(0).getKind(), is(FileProtocol.Kind.RESPONSE_NOT_FOUND));
        assertThat(results.get(1).getLocation(), endsWith("nothing2"));
        assertThat(results.get(1).getKind(), is(FileProtocol.Kind.RESPONSE_NOT_FOUND));
        assertThat(results.get(2).getLocation(), endsWith("available"));
        assertThat(results.get(2).getKind(), is(FileProtocol.Kind.RESPONSE_CACHE_INFO));
        assertThat(results.get(2).getInfo(), is(info));
        assertThat(results.get(3).getLocation(), endsWith("nothing3"));
        assertThat(results.get(3).getKind(), is(FileProtocol.Kind.RESPONSE_NOT_FOUND));
    }

    /**
     * cache information exists but was broken.
     * @throws Exception if failed
     */
    @Test
    public void broken() throws Exception {
        CacheStorage storage = new CacheStorage(service.getConf(), uri("available"));
        try {
            // empty file
            FSDataOutputStream file = storage.getFileSystem().create(storage.getHeadProperties());
            file.close();
        } finally {
            storage.close();
        }

        FileList.Reader reader = prepare("available");
        FileList.Writer writer = FileList.createWriter(writerBuffer, false);
        service.execute(reader, writer);
        writer.close();
        List<FileProtocol> results = collect(writerBuffer.toByteArray());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getLocation(), endsWith("available"));
        assertThat(results.get(0).getKind(), is(FileProtocol.Kind.RESPONSE_NOT_FOUND));
    }

    private URI uri(String string) {
        try {
            return FileNameUtil.createPath(service.getConf(), string, "dummy", "dummy").toUri();
        } catch (BulkLoaderSystemException e) {
            throw new AssertionError(e);
        }
    }

    private FileList.Reader prepare(String... locations) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileList.Writer writer = FileList.createWriter(output, false);
        for (String location : locations) {
            writer.openNext(new FileProtocol(FileProtocol.Kind.GET_CACHE_INFO, location, null)).close();
        }
        writer.close();
        return FileList.createReader(new ByteArrayInputStream(output.toByteArray()));
    }

    private List<FileProtocol> collect(byte[] byteArray) throws IOException {
        List<FileProtocol> results = new ArrayList<FileProtocol>();
        ByteArrayInputStream input = new ByteArrayInputStream(byteArray);
        FileList.Reader reader = FileList.createReader(input);
        while (reader.next()) {
            results.add(reader.getCurrentProtocol());
            reader.openContent().close();
        }
        return results;
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
}
