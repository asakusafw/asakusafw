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
package com.asakusafw.thundergate.runtime.cache.mapreduce;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.stage.input.StageInputDriver;
import com.asakusafw.runtime.stage.input.StageInputFormat;
import com.asakusafw.runtime.stage.input.StageInputMapper;
import com.asakusafw.runtime.stage.input.TemporaryInputFormat;
import com.asakusafw.runtime.stage.output.LegacyBridgeOutputCommitter;
import com.asakusafw.runtime.stage.output.TemporaryOutputFormat;
import com.asakusafw.thundergate.runtime.cache.CacheStorage;

/**
 * MapReduce job client for applying cache patch.
 * This requires following command line arguments:
 * <ol>
 * <li> subcommand:
 *   <ul>
 *   <li> {@code "create"} - create a new cache head from {@code <directory>/PATCH} </li>
 *   <li> {@code "update"} - update cache head with merging {@code <directory>/HEAD} and {@code <directory>/PATCH} </li>
 *   <li> </li>
 *   </ul>
 * </li>
 * <li> path to the cache directory </li>
 * <li> fully qualified data model class name </li>
 * </ol>
 * @since 0.2.3
 */
public class CacheBuildClient extends Configured implements Tool {

    /**
     * Subcommand to create a new cache.
     */
    public static final String SUBCOMMAND_CREATE = "create";

    /**
     * Subcommand to update a cache.
     */
    public static final String SUBCOMMAND_UPDATE = "update";

    private static final String NEXT_DIRECTORY_NAME = "NEXT";

    private static final String ESCAPE_DIRECTORY_NAME = "PREVIOUS";

    static final Log LOG = LogFactory.getLog(CacheBuildClient.class);

    private CacheStorage storage;

    private Class<?> modelClass;

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid arguments: {0}",
                    Arrays.toString(args)));
        }
        String subcommand = args[0];
        boolean create;
        if (subcommand.equals(SUBCOMMAND_CREATE)) {
            create = true;
        } else if (subcommand.equals(SUBCOMMAND_UPDATE)) {
            create = false;
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid arguments (unknown subcommand): {0}",
                    Arrays.toString(args)));
        }

        Path cacheDirectory = new Path(args[1]);
        modelClass = getConf().getClassByName(args[2]);
        this.storage = new CacheStorage(getConf(), cacheDirectory.toUri());
        try {
            clearNext();
            if (create) {
                create();
            } else {
                update();
            }
            switchHead();
        } finally {
            storage.close();
        }
        return 0;
    }

    private void clearNext() throws IOException {
        LOG.info(MessageFormat.format("Cleaning cache output directory: {0}",
                getNextDirectory()));
        storage.getFileSystem().delete(getNextDirectory(), true);
    }

    private void update() throws IOException, InterruptedException {
        Job job = JobCompatibility.newJob(getConf());
        job.setJobName("TGC-UPDATE-" + storage.getPatchDirectory());

        List<StageInput> inputList = new ArrayList<StageInput>();
        inputList.add(new StageInput(
                storage.getHeadContents("*").toString(),
                TemporaryInputFormat.class,
                BaseMapper.class));
        inputList.add(new StageInput(
                storage.getPatchContents("*").toString(),
                TemporaryInputFormat.class,
                PatchMapper.class));
        StageInputDriver.set(job, inputList);
        job.setInputFormatClass(StageInputFormat.class);
        job.setMapperClass(StageInputMapper.class);
        job.setMapOutputKeyClass(PatchApplyKey.class);
        job.setMapOutputValueClass(modelClass);

        // combiner may have no effect in normal cases
        job.setReducerClass(PatchApplyReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(modelClass);
        job.setPartitionerClass(PatchApplyKey.Partitioner.class);
        job.setSortComparatorClass(PatchApplyKey.SortComparator.class);
        job.setGroupingComparatorClass(PatchApplyKey.GroupComparator.class);

        TemporaryOutputFormat.setOutputPath(job, getNextDirectory());
        job.setOutputFormatClass(TemporaryOutputFormat.class);
        job.getConfiguration().setClass(
                "mapred.output.committer.class",
                LegacyBridgeOutputCommitter.class,
                org.apache.hadoop.mapred.OutputCommitter.class);

        LOG.info(MessageFormat.format("Applying patch: {0} / {1} -> {2}",
                storage.getPatchContents("*"),
                storage.getHeadContents("*"),
                getNextContents()));
        try {
            boolean succeed = job.waitForCompletion(true);
            LOG.info(MessageFormat.format("Applied patch: succeed={0}, {1} / {2} -> {3}",
                    succeed,
                    storage.getPatchContents("*"),
                    storage.getHeadContents("*"),
                    getNextContents()));
            if (succeed == false) {
                throw new IOException(MessageFormat.format("Failed to apply patch: {0} / {1} -> {2}",
                        storage.getPatchContents("*"),
                        storage.getHeadContents("*"),
                        getNextContents()));
            }
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }

        LOG.info(MessageFormat.format("Setting patched properties: {0} -> {1}",
                storage.getPatchProperties(),
                getNextDirectory()));
        FileUtil.copy(
                storage.getFileSystem(),
                storage.getPatchProperties(),
                storage.getFileSystem(),
                getNextProperties(),
                false,
                storage.getConfiguration());
    }

    private void create() throws InterruptedException, IOException {
        Job job = JobCompatibility.newJob(getConf());
        job.setJobName("TGC-CREATE-" + storage.getPatchDirectory());

        List<StageInput> inputList = new ArrayList<StageInput>();
        inputList.add(new StageInput(
                storage.getPatchContents("*").toString(),
                TemporaryInputFormat.class,
                DeleteMapper.class));
        StageInputDriver.set(job, inputList);
        job.setInputFormatClass(StageInputFormat.class);
        job.setMapperClass(StageInputMapper.class);
        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(modelClass);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(modelClass);

        TemporaryOutputFormat.setOutputPath(job, getNextDirectory());
        job.setOutputFormatClass(TemporaryOutputFormat.class);
        job.getConfiguration().setClass(
                "mapred.output.committer.class",
                LegacyBridgeOutputCommitter.class,
                org.apache.hadoop.mapred.OutputCommitter.class);

        job.setNumReduceTasks(0);

        LOG.info(MessageFormat.format("Applying patch: {0} / (empty) -> {2}",
                storage.getPatchContents("*"),
                storage.getHeadContents("*"),
                getNextContents()));
        try {
            boolean succeed = job.waitForCompletion(true);
            LOG.info(MessageFormat.format("Applied patch: succeed={0}, {1} / (empty) -> {3}",
                    succeed,
                    storage.getPatchContents("*"),
                    storage.getHeadContents("*"),
                    getNextContents()));
            if (succeed == false) {
                throw new IOException(MessageFormat.format("Failed to apply patch: {0} / (empty) -> {2}",
                        storage.getPatchContents("*"),
                        storage.getHeadContents("*"),
                        getNextContents()));
            }
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }

        LOG.info(MessageFormat.format("Setting patched properties: {0} -> {1}",
                storage.getPatchProperties(),
                getNextDirectory()));
        FileUtil.copy(
                storage.getFileSystem(),
                storage.getPatchProperties(),
                storage.getFileSystem(),
                getNextProperties(),
                false,
                storage.getConfiguration());
    }

    private void switchHead() throws IOException {
        boolean hasHead = storage.getFileSystem().exists(storage.getHeadDirectory());
        if (hasHead) {
            LOG.info(MessageFormat.format(
                    "Escaping previous cache: {0} -> {1}",
                    storage.getHeadDirectory(),
                    getEscapeDir()));
            storage.getFileSystem().delete(getEscapeDir(), true);
            storage.getFileSystem().rename(storage.getHeadDirectory(), getEscapeDir());
        }

        LOG.info(MessageFormat.format(
                "Switching patched as HEAD: {0} -> {1}",
                getNextDirectory(),
                storage.getHeadDirectory()));
        storage.getFileSystem().rename(getNextDirectory(), storage.getHeadDirectory());

        if (hasHead) {
            LOG.info(MessageFormat.format(
                    "Cleaning previous cache: {0}",
                    storage.getHeadDirectory()));
            storage.getFileSystem().delete(getEscapeDir(), true);
        }
    }

    private Path getNextDirectory() {
        return new Path(storage.getTempoaryDirectory(), NEXT_DIRECTORY_NAME);
    }

    private Path getNextProperties() {
        return new Path(getNextDirectory(), CacheStorage.META_FILE_NAME);
    }

    private Path getNextContents() {
        return new Path(getNextDirectory(), CacheStorage.CONTENT_FILE_GLOB);
    }

    private Path getEscapeDir() {
        return new Path(storage.getTempoaryDirectory(), ESCAPE_DIRECTORY_NAME);
    }
}
