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
package com.asakusafw.thundergate.runtime.cache;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link CacheStorage}.
 */
public class CacheStorageTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * delete HEAD version.
     * @throws Exception if failed
     */
    @Test
    public void deleteHead() throws Exception {
        File dir = folder.newFolder("testing");
        dir.delete();
        CacheStorage storage = new CacheStorage(new Configuration(), dir.toURI());
        try {
            Path content = storage.getHeadContents("a");
            FSDataOutputStream output = storage.getFileSystem().create(content);
            try {
                IOUtils.copyBytes(
                        new ByteArrayInputStream("Hello, world".getBytes()),
                        output,
                        storage.getConfiguration());
            } finally {
                output.close();
            }

            assertThat(storage.getFileSystem().exists(storage.getHeadDirectory()), is(true));
            assertThat(storage.getFileSystem().exists(storage.getPatchDirectory()), is(false));

            storage.deleteHead();
            assertThat(storage.getFileSystem().exists(storage.getHeadDirectory()), is(false));
        } finally {
            storage.close();
        }
    }

    /**
     * delete PATCH version.
     * @throws Exception if failed
     */
    @Test
    public void deletePatch() throws Exception {
        File dir = folder.newFolder("testing");
        dir.delete();
        CacheStorage storage = new CacheStorage(new Configuration(), dir.toURI());
        try {
            Path content = storage.getPatchContents("a");
            FSDataOutputStream output = storage.getFileSystem().create(content);
            try {
                IOUtils.copyBytes(
                        new ByteArrayInputStream("Hello, world".getBytes()),
                        output,
                        storage.getConfiguration());
            } finally {
                output.close();
            }

            assertThat(storage.getFileSystem().exists(storage.getPatchDirectory()), is(true));
            assertThat(storage.getFileSystem().exists(storage.getHeadDirectory()), is(false));

            storage.deletePatch();
            assertThat(storage.getFileSystem().exists(storage.getPatchDirectory()), is(false));
        } finally {
            storage.close();
        }
    }

    /**
     * delete all.
     * @throws Exception if failed
     */
    @Test
    public void deleteAll() throws Exception {
        File dir = folder.newFolder("testing");
        dir.delete();
        CacheStorage storage = new CacheStorage(new Configuration(), dir.toURI());
        try {
            Path headContent = storage.getHeadContents("a");
            FSDataOutputStream headOutput = storage.getFileSystem().create(headContent);
            try {
                IOUtils.copyBytes(
                        new ByteArrayInputStream("Hello, world".getBytes()),
                        headOutput,
                        storage.getConfiguration());
            } finally {
                headOutput.close();
            }
            Path patchContent = storage.getPatchContents("a");
            FSDataOutputStream patchOutput = storage.getFileSystem().create(patchContent);
            try {
                IOUtils.copyBytes(
                        new ByteArrayInputStream("Hello, world".getBytes()),
                        patchOutput,
                        storage.getConfiguration());
            } finally {
                patchOutput.close();
            }

            assertThat(storage.getFileSystem().exists(storage.getHeadDirectory()), is(true));
            assertThat(storage.getFileSystem().exists(storage.getPatchDirectory()), is(true));

            assertThat(storage.deleteAll(), is(true));
            assertThat(storage.getFileSystem().exists(storage.getHeadDirectory()), is(false));
            assertThat(storage.getFileSystem().exists(storage.getPatchDirectory()), is(false));
        } finally {
            storage.close();
        }
    }

    /**
     * delete all but does not exist.
     * @throws Exception if failed
     */
    @Test
    public void deleteAll_missing() throws Exception {
        File dir = folder.newFolder("testing");
        Assume.assumeTrue(dir.delete());
        CacheStorage storage = new CacheStorage(new Configuration(), dir.toURI());
        try {
            assertThat(storage.deleteAll(), is(false));
        } finally {
            storage.close();
        }
    }

    /**
     * Save and restore cache meta information.
     * @throws Exception if failed
     */
    @Test
    public void putPatchCacheInfo() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "EXAMPLE",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);
        File dir = folder.newFolder("testing");
        dir.delete();
        CacheStorage storage = new CacheStorage(new Configuration(), dir.toURI());
        try {
            storage.putPatchCacheInfo(info);

            assertThat(storage.getFileSystem().exists(storage.getPatchDirectory()), is(true));
            assertThat(storage.getFileSystem().exists(storage.getHeadDirectory()), is(false));

            CacheInfo restored = storage.getPatchCacheInfo();
            assertThat(restored.getFeatureVersion(), is(info.getFeatureVersion()));
            assertThat(restored.getId(), is(info.getId()));
            assertThat(tos(restored.getTimestamp()), is(tos(info.getTimestamp())));
            assertThat(restored.getTableName(), is(info.getTableName()));
            assertThat(restored.getColumnNames(), is(info.getColumnNames()));
            assertThat(restored.getModelClassName(), is(info.getModelClassName()));
            assertThat(restored.getModelClassVersion(), is(info.getModelClassVersion()));
        } finally {
            storage.close();
        }
    }

    /**
     * Save and restore cache meta information.
     * @throws Exception if failed
     */
    @Test
    public void putHeadCacheInfo() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "EXAMPLE",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);
        File dir = folder.newFolder("testing");
        dir.delete();
        CacheStorage storage = new CacheStorage(new Configuration(), dir.toURI());
        try {
            storage.putHeadCacheInfo(info);

            assertThat(storage.getFileSystem().exists(storage.getPatchDirectory()), is(false));
            assertThat(storage.getFileSystem().exists(storage.getHeadDirectory()), is(true));

            CacheInfo restored = storage.getHeadCacheInfo();
            assertThat(restored.getFeatureVersion(), is(info.getFeatureVersion()));
            assertThat(restored.getId(), is(info.getId()));
            assertThat(tos(restored.getTimestamp()), is(tos(info.getTimestamp())));
            assertThat(restored.getTableName(), is(info.getTableName()));
            assertThat(restored.getColumnNames(), is(info.getColumnNames()));
            assertThat(restored.getModelClassName(), is(info.getModelClassName()));
            assertThat(restored.getModelClassVersion(), is(info.getModelClassVersion()));
        } finally {
            storage.close();
        }
    }

    private String tos(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
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
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
