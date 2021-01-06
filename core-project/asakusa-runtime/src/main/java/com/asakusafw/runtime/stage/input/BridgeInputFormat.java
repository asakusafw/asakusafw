/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.input;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataDefinition;
import com.asakusafw.runtime.directio.DataFilter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceConstants;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.SimpleDataDefinition;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.util.VariableTable;

/**
 * A bridge implementation for Hadoop {@link InputFormat}.
 * @since 0.2.5
 * @version 0.7.3
 */
public final class BridgeInputFormat extends InputFormat<NullWritable, Object> {

    static final Log LOG = LogFactory.getLog(BridgeInputFormat.class);

    @Override
    @Deprecated
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Direct access to getSplits() is not supported.");
    }

    /**
     * Computes and returns splits for the specified inputs.
     * @param context current job context
     * @param inputList target input list
     * @return the computed splits
     * @throws IOException if failed to compute splits
     * @throws InterruptedException if interrupted while computing inputs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<InputSplit> getSplits(
            JobContext context,
            List<StageInput> inputList) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (inputList == null) {
            throw new IllegalArgumentException("inputList must not be null"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start computing splits for Direct I/O: input={0}", //$NON-NLS-1$
                    inputList.size()));
        }
        long t0 = -1L;
        if (LOG.isInfoEnabled()) {
            t0 = System.currentTimeMillis();
        }
        DirectDataSourceRepository repo = getDataSourceRepository(context);
        List<InputSplit> results = new ArrayList<>();
        Map<DirectInputGroup, List<InputPath>> patternGroups = extractInputList(context, repo, inputList);
        long totalSize = 0;
        for (Map.Entry<DirectInputGroup, List<InputPath>> entry : patternGroups.entrySet()) {
            DirectInputGroup group = entry.getKey();
            List<InputPath> paths = entry.getValue();
            DirectDataSource dataSource = repo.getRelatedDataSource(group.containerPath);
            DataDefinition<?> definition = createDataDefinition(context.getConfiguration(), group);
            for (InputPath path : paths) {
                List<DirectInputFragment> fragments = getFragments(repo, group, path, definition, dataSource);
                for (DirectInputFragment fragment : fragments) {
                    totalSize += fragment.getSize();
                    results.add(new BridgeInputSplit(group, fragment));
                }
            }
        }
        if (results.isEmpty()) {
            // Execute this job even if there are no input fragments.
            // It will create empty output files required by successive jobs.
            results.add(new NullInputSplit());
        }
        if (LOG.isInfoEnabled()) {
            String type = "(unknown)"; //$NON-NLS-1$
            if (patternGroups.isEmpty() == false) {
                type = patternGroups.keySet().iterator().next().dataType.getName();
            }
            long t1 = System.currentTimeMillis();
            LOG.info(MessageFormat.format(
                    "found Direct I/O input splits: primary-type={0}, fragments={1}, size={2}bytes, elapsed={3}ms",
                    type,
                    results.size(),
                    totalSize,
                    t1 - t0));
        }
        return results;
    }

    private DataDefinition<?> createDataDefinition(Configuration configuration, DirectInputGroup group) {
        DataFormat<?> format = ReflectionUtils.newInstance(group.formatClass, configuration);
        DataFilter<?> filter = createFilter(group.filterClass, configuration);
        DataDefinition<?> definition = SimpleDataDefinition.newInstance(group.dataType, format, filter);
        return definition;
    }

    private <T> List<DirectInputFragment> getFragments(
            DirectDataSourceRepository repo,
            DirectInputGroup group,
            InputPath path,
            DataDefinition<T> definition,
            DirectDataSource dataSource) throws IOException, InterruptedException {
        assert group != null;
        assert path != null;
        assert definition != null;
        assert dataSource != null;
        List<DirectInputFragment> fragments =
            dataSource.findInputFragments(definition, path.componentPath, path.pattern);
        if (fragments.isEmpty()) {
            String id = repo.getRelatedId(group.containerPath);
            String pathString = dataSource.path(path.componentPath, path.pattern);
            if (path.optional) {
                LOG.info(MessageFormat.format(
                        "Skipped optional input (datasource={0}, path=\"{1}\", type={2})",
                        id,
                        pathString,
                        definition.getDataFormat().getSupportedType().getName()));
            } else {
                throw new IOException(MessageFormat.format(
                        "Input not found (datasource={0}, path=\"{1}\", type={2})",
                        id,
                        pathString,
                        definition.getDataFormat().getSupportedType().getName()));
            }
        }
        return fragments;
    }

    private Map<DirectInputGroup, List<InputPath>> extractInputList(
            JobContext context,
            DirectDataSourceRepository repo,
            List<StageInput> inputList) throws IOException {
        assert context != null;
        assert repo != null;
        assert inputList != null;
        VariableTable variables = createBatchArgumentsTable(context.getConfiguration());
        Map<DirectInputGroup, List<InputPath>> results = new HashMap<>();
        for (StageInput input : inputList) {
            String fullBasePath = variables.parse(extractBasePath(input));
            String basePath = repo.getComponentPath(fullBasePath);
            FilePattern pattern = extractSearchPattern(context, variables, input);
            Class<?> dataClass = extractDataClass(context, input);
            Class<? extends DataFormat<?>> formatClass = extractFormatClass(context, input);
            Class<? extends DataFilter<?>> filterClass = extractFilterClass(context, input);
            DirectInputGroup group = new DirectInputGroup(fullBasePath, dataClass, formatClass, filterClass);
            List<InputPath> paths = results.get(group);
            if (paths == null) {
                paths = new ArrayList<>();
                results.put(group, paths);
            }
            paths.add(new InputPath(basePath, pattern, extractOptional(input)));
        }
        return results;
    }

    private String extractBasePath(StageInput input) throws IOException {
        assert input != null;
        return extract(input, DirectDataSourceConstants.KEY_BASE_PATH);
    }

    private FilePattern extractSearchPattern(
            JobContext context,
            VariableTable variables,
            StageInput input) throws IOException {
        assert context != null;
        assert input != null;
        String value = extract(input, DirectDataSourceConstants.KEY_RESOURCE_PATH);
        value = variables.parse(value);
        try {
            FilePattern compiled = FilePattern.compile(value);
            if (compiled.containsVariables()) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Search pattern contains variables: {0}",
                        value));
            }
            return compiled;
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Invalid resource path pattern: \"{1}\" (path={0})",
                    extractBasePath(input),
                    value), e);
        }
    }

    private boolean extractOptional(StageInput input) {
        assert input != null;
        String value = input.getAttributes().get(DirectDataSourceConstants.KEY_OPTIONAL);
        if (value == null) {
            value = DirectDataSourceConstants.DEFAULT_OPTIONAL;
        }
        return value.equals("true"); //$NON-NLS-1$
    }

    private Class<?> extractDataClass(JobContext context, StageInput input) throws IOException {
        assert context != null;
        assert input != null;
        String value = extract(input, DirectDataSourceConstants.KEY_DATA_CLASS);
        try {
            return Class.forName(value, false, context.getConfiguration().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IOException(MessageFormat.format(
                    "Invalid data class: \"{1}\" (path={0})",
                    extractBasePath(input),
                    value), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DataFormat<?>> extractFormatClass(
            JobContext context,
            StageInput input) throws IOException {
        assert context != null;
        assert input != null;
        String value = extract(input, DirectDataSourceConstants.KEY_FORMAT_CLASS);
        try {
            Class<?> aClass = Class.forName(value, false, context.getConfiguration().getClassLoader());
            return (Class<? extends DataFormat<?>>) aClass.asSubclass(DataFormat.class);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Invalid format class: \"{1}\" (path={0})",
                    extractBasePath(input),
                    value), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DataFilter<?>> extractFilterClass(
            JobContext context,
            StageInput input) throws IOException {
        assert context != null;
        assert input != null;
        String value = input.getAttributes().get(DirectDataSourceConstants.KEY_FILTER_CLASS);
        if (value == null) {
            return null;
        }
        try {
            Class<?> aClass = Class.forName(value, false, context.getConfiguration().getClassLoader());
            return (Class<? extends DataFilter<?>>) aClass.asSubclass(DataFilter.class);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Invalid format class: \"{1}\" (path={0})",
                    extractBasePath(input),
                    value), e);
        }
    }

    private String extract(StageInput input, String key) throws IOException {
        String value = input.getAttributes().get(key);
        if (value == null) {
            throw new IOException(MessageFormat.format(
                    "A mandatory attribute \"{1}\" is not defined (path={0})",
                    input.getPathString(),
                    key));
        }
        return value;
    }

    @Override
    public RecordReader<NullWritable, Object> createRecordReader(
            InputSplit split,
            TaskAttemptContext context) throws IOException, InterruptedException {
        if (split instanceof BridgeInputSplit) {
            BridgeInputSplit bridgeInfo = (BridgeInputSplit) split;
            DataDefinition<?> definition = createDataDefinition(context.getConfiguration(), bridgeInfo.group);
            return createRecordReader(definition, bridgeInfo, context);
        } else if (split instanceof NullInputSplit) {
            return createNullRecordReader(context);
        } else {
            throw new IOException(MessageFormat.format(
                    "Unknown input split: {0}",
                    split));
        }
    }

    private DataFilter<?> createFilter(Class<? extends DataFilter<?>> filterClass, Configuration configuration) {
        if (filterClass == null) {
            return null;
        }
        DataFilter<?> result = ReflectionUtils.newInstance(filterClass, configuration);
        Map<String, String> batchArguments = createBatchArgumentsTable(configuration).getVariables();
        DataFilter.Context context = new DataFilter.Context(batchArguments);
        result.initialize(context);
        return result;
    }

    private VariableTable createBatchArgumentsTable(Configuration configuration) {
        String arguments = configuration.get(StageConstants.PROP_ASAKUSA_BATCH_ARGS, ""); //$NON-NLS-1$
        VariableTable variables = new VariableTable(VariableTable.RedefineStrategy.IGNORE);
        variables.defineVariables(arguments);
        return variables;
    }

    private <T> RecordReader<NullWritable, Object> createRecordReader(
            DataDefinition<T> definition,
            BridgeInputSplit split,
            TaskAttemptContext context) throws IOException, InterruptedException {
        assert definition != null;
        assert split != null;
        assert context != null;
        Configuration conf = context.getConfiguration();
        T buffer = ReflectionUtils.newInstance(definition.getDataClass(), conf);
        Counter counter = new Counter();
        ModelInput<T> input = createInput(context, split.group.containerPath, definition, counter, split.fragment);
        return new BridgeRecordReader<>(input, buffer, counter, split.fragment.getSize());
    }

    private RecordReader<NullWritable, Object> createNullRecordReader(TaskAttemptContext context) {
        assert context != null;
        return new NullRecordReader<>();
    }

    private <T> ModelInput<T> createInput(
            TaskAttemptContext context,
            String containerPath,
            DataDefinition<T> definition,
            Counter counter,
            DirectInputFragment fragment) throws IOException, InterruptedException {
        assert context != null;
        assert containerPath != null;
        assert definition != null;
        assert counter != null;
        assert fragment != null;
        DirectDataSourceRepository repo = getDataSourceRepository(context);
        DirectDataSource ds = repo.getRelatedDataSource(containerPath);
        return ds.openInput(definition, fragment, counter);
    }

    private static DirectDataSourceRepository getDataSourceRepository(JobContext context) {
        assert context != null;
        return HadoopDataSourceUtil.loadRepository(context.getConfiguration());
    }

    private static class DirectInputGroup {

        final String containerPath;

        final Class<?> dataType;

        final Class<? extends DataFormat<?>> formatClass;

        final Class<? extends DataFilter<?>> filterClass;

        DirectInputGroup(
                String containerPath,
                Class<?> dataType,
                Class<? extends DataFormat<?>> formatClass,
                Class<? extends DataFilter<?>> filterClass) {
            assert containerPath != null;
            assert dataType != null;
            assert formatClass != null;
            this.containerPath = containerPath;
            this.dataType = dataType;
            this.formatClass = formatClass;
            this.filterClass = filterClass;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + containerPath.hashCode();
            result = prime * result + dataType.hashCode();
            result = prime * result + formatClass.hashCode();
            result = prime * result + ((filterClass == null) ? 0 : filterClass.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DirectInputGroup other = (DirectInputGroup) obj;
            if (!containerPath.equals(other.containerPath)) {
                return false;
            }
            if (!dataType.equals(other.dataType)) {
                return false;
            }
            if (!formatClass.equals(other.formatClass)) {
                return false;
            }
            if (filterClass == null) {
                if (other.filterClass != null) {
                    return false;
                }
            } else if (!filterClass.equals(other.filterClass)) {
                return false;
            }
            return true;
        }
    }

    private static class InputPath {

        final String componentPath;

        final FilePattern pattern;

        final boolean optional;

        InputPath(String componentPath, FilePattern pattern, boolean optional) {
            assert componentPath != null;
            assert pattern != null;
            this.componentPath = componentPath;
            this.pattern = pattern;
            this.optional = optional;
        }
    }

    /**
     * A bridge implementation for Hadoop {@link InputSplit}.
     * @since 0.2.5
     */
    public static class BridgeInputSplit extends InputSplit implements Writable, Configurable {

        volatile Configuration conf;

        volatile DirectInputGroup group;

        volatile DirectInputFragment fragment;

        /**
         * Creates a new instance for {@link Writable} facilities.
         */
        public BridgeInputSplit() {
            return;
        }

        BridgeInputSplit(DirectInputGroup group, DirectInputFragment fragment) {
            this.group = group;
            this.fragment = fragment;
        }

        @Override
        public void setConf(Configuration conf) {
            this.conf = conf;
        }

        @Override
        public Configuration getConf() {
            return conf;
        }

        @Override
        public long getLength() throws IOException, InterruptedException {
            return fragment.getSize();
        }

        @Override
        public String[] getLocations() throws IOException, InterruptedException {
            List<String> locations = fragment.getOwnerNodeNames();
            return locations.toArray(new String[locations.size()]);
        }

        @Override
        public void write(DataOutput out) throws IOException {
            DirectInputGroup groupCopy = group;
            WritableUtils.writeString(out, groupCopy.containerPath);
            WritableUtils.writeString(out, groupCopy.dataType.getName());
            WritableUtils.writeString(out, groupCopy.formatClass.getName());
            if (groupCopy.filterClass == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                WritableUtils.writeString(out, groupCopy.filterClass.getName());
            }

            DirectInputFragment fragmentCopy = fragment;
            WritableUtils.writeString(out, fragmentCopy.getPath());
            WritableUtils.writeVLong(out, fragmentCopy.getOffset());
            WritableUtils.writeVLong(out, fragmentCopy.getSize());
            List<String> ownerNodeNames = fragmentCopy.getOwnerNodeNames();
            WritableUtils.writeStringArray(out, ownerNodeNames.toArray(new String[ownerNodeNames.size()]));
            Map<String, String> attributes = fragmentCopy.getAttributes();
            WritableUtils.writeVInt(out, attributes.size());
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                WritableUtils.writeString(out, entry.getKey());
                WritableUtils.writeString(out, entry.getValue());
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void readFields(DataInput in) throws IOException {
            String containerPath = WritableUtils.readString(in);
            String dataTypeName = WritableUtils.readString(in);
            String formatTypeName = WritableUtils.readString(in);
            String filterTypeName = null;
            if (in.readBoolean()) {
                filterTypeName = WritableUtils.readString(in);
            }
            String path = WritableUtils.readString(in);
            long offset = WritableUtils.readVLong(in);
            long length = WritableUtils.readVLong(in);
            String[] locations = WritableUtils.readStringArray(in);
            Map<String, String> attributes;
            int attributeCount = WritableUtils.readVInt(in);
            if (attributeCount == 0) {
                attributes = Collections.emptyMap();
            } else {
                attributes = new HashMap<>();
                for (int i = 0; i < attributeCount; i++) {
                    String key = WritableUtils.readString(in);
                    String value = WritableUtils.readString(in);
                    attributes.put(key, value);
                }
            }
            this.fragment = new DirectInputFragment(path, offset, length, Arrays.asList(locations), attributes);

            try {
                Class<? extends DataFormat<?>> formatClass = (Class<? extends DataFormat<?>>) conf
                        .getClassByName(formatTypeName)
                        .asSubclass(DataFormat.class);
                Class<? extends DataFilter<?>> filterClass = null;
                if (filterTypeName != null) {
                    filterClass = (Class<? extends DataFilter<?>>) conf
                            .getClassByName(filterTypeName)
                            .asSubclass(DataFilter.class);
                }
                Class<?> dataType = conf.getClassByName(dataTypeName);
                this.group = new DirectInputGroup(containerPath, dataType, formatClass, filterClass);
            } catch (ClassNotFoundException e) {
                throw new IOException("Failed to restore split", e);
            }
        }
    }

    /**
     * A bridge implementation for Hadoop {@link RecordReader}.
     * @param <T> input type
     * @since 0.2.5
     */
    private static final class BridgeRecordReader<T> extends RecordReader<NullWritable, Object> {

        private static final NullWritable KEY = NullWritable.get();

        private final ModelInput<T> input;

        private final T buffer;

        private final Counter sizeCounter;

        private final double fragmentSize;

        private boolean closed = false;

        BridgeRecordReader(
                ModelInput<T> input,
                T buffer,
                Counter sizeCounter,
                long fragmentSize) {
            assert input != null;
            assert buffer != null;
            assert sizeCounter != null;
            this.sizeCounter = sizeCounter;
            this.input = input;
            this.buffer = buffer;
            if (fragmentSize < 0) {
                this.fragmentSize = Double.POSITIVE_INFINITY;
            } else {
                this.fragmentSize = fragmentSize;
            }
        }

        @Override
        public void initialize(
                InputSplit split,
                TaskAttemptContext context) throws IOException, InterruptedException {
            assert split instanceof BridgeInputSplit;
        }

        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            if (closed) {
                return false;
            }
            boolean exists = input.readTo(buffer);
            if (exists == false) {
                return false;
            }
            return exists;
        }

        @Override
        public NullWritable getCurrentKey() throws IOException, InterruptedException {
            return KEY;
        }

        @Override
        public Object getCurrentValue() throws IOException, InterruptedException {
            return buffer;
        }

        @Override
        public float getProgress() throws IOException, InterruptedException {
            if (closed) {
                return 1.0f;
            }
            float progress = (float) (sizeCounter.get() / fragmentSize);
            return Math.min(progress, 0.99f);
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            input.close();
        }
    }

    /**
     * Empty implementation for Hadoop {@link InputSplit}.
     * @since 0.6.1
     */
    public static final class NullInputSplit extends InputSplit implements Writable, Configurable {

        volatile Configuration conf;

        /**
         * Creates a new instance for {@link Writable} facilities.
         */
        public NullInputSplit() {
            return;
        }

        @Override
        public Configuration getConf() {
            return conf;
        }

        @Override
        public void setConf(Configuration conf) {
            this.conf = conf;
        }

        @Override
        public long getLength() throws IOException, InterruptedException {
            return 0;
        }

        @Override
        public String[] getLocations() throws IOException, InterruptedException {
            return new String[0];
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            return;
        }

        @Override
        public void write(DataOutput out) throws IOException {
            return;
        }
    }

    /**
     * Empty implementation for Hadoop {@link RecordReader}.
     * @param <KEYIN> the key type
     * @param <VALUEIN> the value type
     */
    public static final class NullRecordReader<KEYIN, VALUEIN> extends RecordReader<KEYIN, VALUEIN> {

        /**
         * Creates a new instance.
         */
        public NullRecordReader() {
            return;
        }

        @Override
        public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
            return;
        }

        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            return false;
        }

        @Override
        public KEYIN getCurrentKey() throws IOException, InterruptedException {
            throw new NoSuchElementException();
        }

        @Override
        public VALUEIN getCurrentValue() throws IOException, InterruptedException {
            throw new NoSuchElementException();
        }

        @Override
        public float getProgress() throws IOException, InterruptedException {
            return 1.0f;
        }

        @Override
        public void close() throws IOException {
            return;
        }
    }
}
