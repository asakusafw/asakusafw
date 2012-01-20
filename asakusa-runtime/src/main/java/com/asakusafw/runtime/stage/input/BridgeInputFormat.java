/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceConstants;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.SearchPattern;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.util.VariableTable;

/**
 * A bridge implementation for Hadoop {@link InputFormat}.
 * @since 0.2.5
 */
public final class BridgeInputFormat extends InputFormat<NullWritable, Object> {

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
        DirectDataSourceRepository repo = getDataSourceRepository(context);
        List<InputSplit> results = new ArrayList<InputSplit>();
        Map<DirectInputGroup, List<InputPath>> patternGroups = extractInputList(context, repo, inputList);
        for (Map.Entry<DirectInputGroup, List<InputPath>> entry : patternGroups.entrySet()) {
            DirectInputGroup group = entry.getKey();
            List<InputPath> paths = entry.getValue();
            DataFormat<?> format = ReflectionUtils.newInstance(group.formatClass, context.getConfiguration());
            DirectDataSource dataSource = repo.getRelatedDataSource(group.containerPath);
            for (InputPath path : paths) {
                List<DirectInputFragment> fragments = getFragments(repo, group, path, format, dataSource);
                for (DirectInputFragment fragment : fragments) {
                    results.add(new BridgeInputSplit(group, fragment));
                }
            }
        }
        return results;
    }

    private <T> List<DirectInputFragment> getFragments(
            DirectDataSourceRepository repo,
            DirectInputGroup group,
            InputPath path,
            DataFormat<T> format,
            DirectDataSource dataSource) throws IOException, InterruptedException {
        assert group != null;
        assert path != null;
        assert format != null;
        assert dataSource != null;
        Class<? extends T> dataType = group.dataType.asSubclass(format.getSupportedType());
        List<DirectInputFragment> fragments =
            dataSource.findInputFragments(dataType, format, path.componentPath, path.pattern);
        if (fragments.isEmpty()) {
            String id = repo.getRelatedId(group.containerPath);
            throw new IOException(MessageFormat.format(
                    "Input not found (datasource={0}, basePath=\"{1}\", resourcePattern=\"{2}\")",
                    id,
                    path.originalBasePath,
                    path.pattern));
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
        String arguments = context.getConfiguration().get(StageConstants.PROP_ASAKUSA_BATCH_ARGS, "");
        VariableTable variables = new VariableTable(VariableTable.RedefineStrategy.IGNORE);
        variables.defineVariables(arguments);

        Map<DirectInputGroup, List<InputPath>> results = new HashMap<DirectInputGroup, List<InputPath>>();
        for (StageInput input : inputList) {
            String fullBasePath = input.getPathString();
            String basePath = variables.parse(repo.getComponentPath(fullBasePath));
            SearchPattern pattern = extractSearchPattern(context, variables, input);
            Class<?> dataClass = extractDataClass(context, input);
            Class<? extends DataFormat<?>> formatClass = extractFormatClass(context, input);
            DirectInputGroup group = new DirectInputGroup(fullBasePath, dataClass, formatClass);
            List<InputPath> paths = results.get(group);
            if (paths == null) {
                paths = new ArrayList<InputPath>();
                results.put(group, paths);
            }
            paths.add(new InputPath(fullBasePath, basePath, pattern));
        }
        return results;
    }

    private SearchPattern extractSearchPattern(
            JobContext context,
            VariableTable variables,
            StageInput input) throws IOException {
        assert context != null;
        assert input != null;
        String value = extract(input, DirectDataSourceConstants.KEY_RESOURCE_PATH);
        value = variables.parse(value);
        try {
            SearchPattern compiled = SearchPattern.compile(value);
            if (compiled.containsVariables()) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Search pattern contains variables: {0}",
                        value));
            }
            return compiled;
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Invalid resource path pattern: \"{1}\" (path={0})",
                    input.getPathString(),
                    value), e);
        }
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
                    input.getPathString(),
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
                    input.getPathString(),
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
        assert split instanceof BridgeInputSplit;
        BridgeInputSplit bridgeInfo = (BridgeInputSplit) split;
        assert bridgeInfo != null;
        DataFormat<?> format = ReflectionUtils.newInstance(bridgeInfo.group.formatClass, context.getConfiguration());
        return createRecordReader(format, bridgeInfo, context);
    }

    private <T> RecordReader<NullWritable, Object> createRecordReader(
            DataFormat<T> format,
            BridgeInputSplit split,
            TaskAttemptContext context) throws IOException, InterruptedException {
        assert format != null;
        assert split != null;
        assert context != null;
        Configuration conf = context.getConfiguration();
        Class<? extends T> type = split.group.dataType.asSubclass(format.getSupportedType());
        T buffer = ReflectionUtils.newInstance(type, conf);
        Counter counter = new Counter();
        ModelInput<T> input = createInput(context, split.group.containerPath, type, format, counter, split.fragment);
        return new BridgeRecordReader<T>(input, buffer, counter, split.fragment.getSize());
    }

    private <T> ModelInput<T> createInput(
            TaskAttemptContext context,
            String containerPath,
            Class<? extends T> dataType,
            DataFormat<T> format,
            Counter counter,
            DirectInputFragment fragment) throws IOException, InterruptedException {
        assert context != null;
        assert containerPath != null;
        assert dataType != null;
        assert format != null;
        assert counter != null;
        assert fragment != null;
        DirectDataSourceRepository repo = getDataSourceRepository(context);
        DirectDataSource ds = repo.getRelatedDataSource(containerPath);
        return ds.openInput(dataType, format, fragment, counter);
    }

    private static DirectDataSourceRepository getDataSourceRepository(JobContext context) {
        assert context != null;
        return HadoopDataSourceUtil.loadRepository(context.getConfiguration());
    }

    private static class DirectInputGroup {

        final String containerPath;

        final Class<?> dataType;

        final Class<? extends DataFormat<?>> formatClass;

        DirectInputGroup(
                String containerPath,
                Class<?> dataType,
                Class<? extends DataFormat<?>> formatClass) {
            assert containerPath != null;
            assert dataType != null;
            assert formatClass != null;
            this.containerPath = containerPath;
            this.dataType = dataType;
            this.formatClass = formatClass;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + containerPath.hashCode();
            result = prime * result + dataType.hashCode();
            result = prime * result + formatClass.hashCode();
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
            return true;
        }
    }

    private static class InputPath {

        final String originalBasePath;

        final String componentPath;

        final SearchPattern pattern;

        InputPath(String originalBasePath, String componentPath, SearchPattern pattern) {
            assert originalBasePath != null;
            assert componentPath != null;
            assert pattern != null;
            this.originalBasePath = originalBasePath;
            this.componentPath = componentPath;
            this.pattern = pattern;
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

            DirectInputFragment fragmentCopy = fragment;
            WritableUtils.writeString(out, fragmentCopy.getPath());
            WritableUtils.writeVLong(out, fragmentCopy.getOffset());
            WritableUtils.writeVLong(out, fragmentCopy.getSize());
            List<String> ownerNodeNames = fragmentCopy.getOwnerNodeNames();
            WritableUtils.writeStringArray(out, ownerNodeNames.toArray(new String[ownerNodeNames.size()]));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void readFields(DataInput in) throws IOException {
            String containerPath = WritableUtils.readString(in);
            String dataTypeName = WritableUtils.readString(in);
            String supportTypeName = WritableUtils.readString(in);
            String path = WritableUtils.readString(in);
            long offset = WritableUtils.readVLong(in);
            long length = WritableUtils.readVLong(in);
            String[] locations = WritableUtils.readStringArray(in);
            this.fragment = new DirectInputFragment(path, offset, length, Arrays.asList(locations));

            try {
                Class<? extends DataFormat<?>> formatClass = (Class<? extends DataFormat<?>>) conf
                        .getClassByName(supportTypeName)
                        .asSubclass(DataFormat.class);
                Class<?> dataType = conf.getClassByName(dataTypeName);
                this.group = new DirectInputGroup(containerPath, dataType, formatClass);
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
    private static class BridgeRecordReader<T> extends RecordReader<NullWritable, Object> {

        private static final NullWritable KEY = NullWritable.get();

        private final ModelInput<T> input;

        private final T buffer;

        private final Counter counter;

        private final double fragmentSize;

        private boolean closed = false;

        public BridgeRecordReader(ModelInput<T> input, T buffer, Counter counter, long fragmentSize) {
            assert counter != null;
            assert input != null;
            assert buffer != null;
            this.counter = counter;
            this.input = input;
            this.buffer = buffer;
            if (fragmentSize < 0) {
                this.fragmentSize = Double.POSITIVE_INFINITY;
            } else {
                this.fragmentSize = fragmentSize;
            }
        }

        @Override
        public final void initialize(
                InputSplit split,
                TaskAttemptContext context) throws IOException, InterruptedException {
            assert split instanceof BridgeInputSplit;
        }

        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            if (closed) {
                return false;
            }
            return input.readTo(buffer);
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
            float progress = (float) (counter.get() / fragmentSize);
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
}
