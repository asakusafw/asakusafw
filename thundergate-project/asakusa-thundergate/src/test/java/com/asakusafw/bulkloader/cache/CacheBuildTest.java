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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.ProcessFileListProvider;
import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.runtime.configuration.HadoopEnvironmentChecker;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;
import com.asakusafw.thundergate.runtime.cache.CacheStorage;
import com.asakusafw.thundergate.runtime.cache.mapreduce.CacheBuildClient;

/**
 * Test for building caches ({@link CacheBuildClient}).
 */
public class CacheBuildTest {

    /**
     * This test class requires Hadoop is installed.
     */
    @Rule
    public HadoopEnvironmentChecker check = new HadoopEnvironmentChecker(false);

    /**
     * Deploys framework.
     */
    @Rule
    public final FrameworkDeployer framework = new FrameworkDeployer() {

        @Override
        protected void deploy() throws IOException {
            deployLibrary(CacheInfo.class, "core/lib/asakusa-thundergate.jar");
        }
    };

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        URI uri = getTargetUri();
        FileSystem fs = FileSystem.get(uri, getConfiguration());
        fs.delete(new Path(uri), true);
    }

    /**
     * Creates a new cache.
     * @throws Exception if failed
     */
    @Test
    public void create() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "EXAMPLE",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);
        framework.deployLibrary(TestDataModel.class, "batchapps/tbatch/lib/jobflow-tflow.jar");
        CacheStorage storage = new CacheStorage(getConfiguration(), getTargetUri());
        try {
            storage.putPatchCacheInfo(info);
            ModelOutput<TestDataModel> output = create(storage, storage.getPatchContents("0"));
            try {
                TestDataModel model = new TestDataModel();
                model.systemId.set(100);
                model.value.set("Hello, world!");
                model.deleted.set(false);
                output.write(model);
            } finally {
                output.close();
            }

            execute(CacheBuildClient.SUBCOMMAND_CREATE);
            assertThat(storage.getHeadCacheInfo(), is(info));

            List<TestDataModel> results = collect(storage, storage.getHeadContents("*"));
            assertThat(results.size(), is(1));
            assertThat(results.get(0).systemId.get(), is(100L));
            assertThat(results.get(0).value.toString(), is("Hello, world!"));
        } finally {
            storage.close();
        }
    }

    /**
     * Creates a new cache with some deleted values.
     * @throws Exception if failed
     */
    @Test
    public void create_deleted() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "EXAMPLE",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);
        framework.deployLibrary(TestDataModel.class, "batchapps/tbatch/lib/jobflow-tflow.jar");
        CacheStorage storage = new CacheStorage(getConfiguration(), getTargetUri());
        try {
            storage.putPatchCacheInfo(info);
            ModelOutput<TestDataModel> output = create(storage, storage.getPatchContents("0"));
            try {
                TestDataModel model = new TestDataModel();
                for (int i = 0; i < 100; i++) {
                    model.systemId.set(i);
                    model.deleted.set(i % 10 != 0);
                    output.write(model);
                }
            } finally {
                output.close();
            }

            execute(CacheBuildClient.SUBCOMMAND_CREATE);
            assertThat(storage.getHeadCacheInfo(), is(info));

            List<TestDataModel> results = collect(storage, storage.getHeadContents("*"));
            assertThat(results.size(), is(10));
            for (int i = 0; i < 10; i++) {
                assertThat(results.get(i).systemId.get(), is(i * 10L));
            }
        } finally {
            storage.close();
        }
    }

    /**
     * Update a cache.
     * @throws Exception if failed
     */
    @Test
    public void update() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "EXAMPLE",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);
        framework.deployLibrary(TestDataModel.class, "batchapps/tbatch/lib/jobflow-tflow.jar");
        CacheStorage storage = new CacheStorage(getConfiguration(), getTargetUri());
        try {
            storage.putPatchCacheInfo(info);
            ModelOutput<TestDataModel> head = create(storage, storage.getHeadContents("0"));
            try {
                TestDataModel model = new TestDataModel();
                model.systemId.set(1);
                model.value.set("HEAD");
                model.deleted.set(false);
                head.write(model);

                model.systemId.set(2);
                model.value.set("HEAD");
                model.deleted.set(false);
                head.write(model);
            } finally {
                head.close();
            }
            ModelOutput<TestDataModel> patch = create(storage, storage.getPatchContents("0"));
            try {
                TestDataModel model = new TestDataModel();
                model.systemId.set(1);
                model.value.set("NEXT");
                model.deleted.set(false);
                patch.write(model);

                model.systemId.set(3);
                model.value.set("NEXT");
                model.deleted.set(false);
                patch.write(model);
            } finally {
                patch.close();
            }

            execute(CacheBuildClient.SUBCOMMAND_UPDATE);
            assertThat(storage.getHeadCacheInfo(), is(info));

            List<TestDataModel> results = collect(storage, storage.getHeadContents("*"));
            assertThat(results.size(), is(3));
            assertThat(results.get(0).systemId.get(), is(1L));
            assertThat(results.get(0).value.toString(), is("NEXT"));
            assertThat(results.get(1).systemId.get(), is(2L));
            assertThat(results.get(1).value.toString(), is("HEAD"));
            assertThat(results.get(2).systemId.get(), is(3L));
            assertThat(results.get(2).value.toString(), is("NEXT"));
        } finally {
            storage.close();
        }
    }

    /**
     * Update a cache.
     * @throws Exception if failed
     */
    @Test
    public void update_delete() throws Exception {
        CacheInfo info = new CacheInfo(
                "a",
                "id",
                calendar("2011-12-13 14:15:16"),
                "EXAMPLE",
                Collections.singleton("COL"),
                "com.example.Model",
                123L);
        framework.deployLibrary(TestDataModel.class, "batchapps/tbatch/lib/jobflow-tflow.jar");
        CacheStorage storage = new CacheStorage(getConfiguration(), getTargetUri());
        try {
            storage.putPatchCacheInfo(info);
            ModelOutput<TestDataModel> head = create(storage, storage.getHeadContents("0"));
            try {
                TestDataModel model = new TestDataModel();
                for (int i = 0; i < 10; i++) {
                    model.systemId.set(i);
                    model.value.set("HEAD");
                    model.deleted.set(false);
                    head.write(model);
                }
            } finally {
                head.close();
            }
            ModelOutput<TestDataModel> patch = create(storage, storage.getPatchContents("0"));
            try {
                TestDataModel model = new TestDataModel();
                for (int i = 0; i < 10; i += 2) {
                    model.systemId.set(i);
                    model.value.set("NEXT");
                    model.deleted.set(i % 4 == 0);
                    patch.write(model);
                }
            } finally {
                patch.close();
            }

            execute(CacheBuildClient.SUBCOMMAND_UPDATE);
            assertThat(storage.getHeadCacheInfo(), is(info));

            List<TestDataModel> results = collect(storage, storage.getHeadContents("*"));
            assertThat(results.size(), is(7));
            assertThat(results.get(0).systemId.get(), is(1L));
            assertThat(results.get(0).value.toString(), is("HEAD"));
            assertThat(results.get(1).systemId.get(), is(2L));
            assertThat(results.get(1).value.toString(), is("NEXT"));
            assertThat(results.get(2).systemId.get(), is(3L));
            assertThat(results.get(2).value.toString(), is("HEAD"));
            assertThat(results.get(3).systemId.get(), is(5L));
            assertThat(results.get(3).value.toString(), is("HEAD"));
            assertThat(results.get(4).systemId.get(), is(6L));
            assertThat(results.get(4).value.toString(), is("NEXT"));
            assertThat(results.get(5).systemId.get(), is(7L));
            assertThat(results.get(5).value.toString(), is("HEAD"));
            assertThat(results.get(6).systemId.get(), is(9L));
            assertThat(results.get(6).value.toString(), is("HEAD"));
        } finally {
            storage.close();
        }
    }

    private void execute(String subcommand) throws IOException, InterruptedException {
        FileListProvider provider = execute(
                Constants.PATH_REMOTE_ROOT + Constants.PATH_LOCAL_CACHE_BUILD,
                subcommand,
                "tbatch",
                "tflow",
                "testing",
                getTargetUri().toString(),
                TestDataModel.class.getName());
        try {
            provider.discardReader();
            provider.discardWriter();
            provider.waitForComplete();
        } finally {
            provider.close();
        }
    }

    private FileListProvider execute(String scriptPath, String... arguments) throws IOException {
        List<String> command = new ArrayList<String>();
        command.add(new File(framework.getHome(), scriptPath).getAbsolutePath());
        Collections.addAll(command, arguments);

        Map<String, String> env = new HashMap<String, String>();
        env.put("ASAKUSA_HOME", framework.getHome().getAbsolutePath());

        return new ProcessFileListProvider(command, env);
    }

    private URI getTargetUri() {
        try {
            return FileNameUtil.createPath(
                    getConfiguration(),
                    "target/testing/" + getClass().getSimpleName(), "dummy", "dummy").toUri();
        } catch (BulkLoaderSystemException e) {
            throw new AssertionError(e);
        }
    }

    private Configuration getConfiguration() {
        return new ConfigurationProvider().newInstance();
    }

    private List<TestDataModel> collect(CacheStorage storage, Path contents) throws IOException {
        List<TestDataModel> results = new ArrayList<TestDataModel>();
        FileSystem fs = storage.getFileSystem();
        for (FileStatus status : fs.globStatus(contents)) {
            results.addAll(collectContent(fs, status));
        }
        Collections.sort(results);
        return results;
    }

    private Collection<TestDataModel> collectContent(FileSystem fs, FileStatus status) throws IOException {
        Collection<TestDataModel> results = new ArrayList<TestDataModel>();
        ModelInput<TestDataModel> input = TemporaryStorage.openInput(
                fs.getConf(), TestDataModel.class, status.getPath());
        try {
            TestDataModel model = new TestDataModel();
            while (input.readTo(model)) {
                results.add(model.copy());
            }
        } finally {
            input.close();
        }
        return results;
    }

    private ModelOutput<TestDataModel> create(CacheStorage storage, Path path) throws IOException {
        return TemporaryStorage.openOutput(storage.getFileSystem().getConf(), TestDataModel.class, path);
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
