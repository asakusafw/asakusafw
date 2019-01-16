/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.io.util.DataBuffer;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.stage.StageUtil;
import com.asakusafw.runtime.stage.input.StageInputSplit.Source;

/**
 * An implementation of Hadoop {@link InputFormat} for handling handling multiple Map operations.
 * @since 0.1.0
 * @version 0.7.1
 */
@SuppressWarnings("rawtypes")
public class StageInputFormat extends InputFormat {

    private static final String KEY_SPLIT_COMBINER = "com.asakusafw.input.combine"; //$NON-NLS-1$

    private static final String DEFAULT_SPLIT_COMBINER = "default"; //$NON-NLS-1$

    static final Log LOG = LogFactory.getLog(StageInputFormat.class);

    private static final Map<String, Class<? extends SplitCombiner>> SPLIT_COMBINERS;
    static {
        Map<String, Class<? extends SplitCombiner>> map = new HashMap<>();
        map.put(DEFAULT_SPLIT_COMBINER, DefaultSplitCombiner.class);
        map.put("disabled", IdentitySplitCombiner.class); //$NON-NLS-1$
        map.put("extreme", ExtremeSplitCombiner.class); //$NON-NLS-1$
        SPLIT_COMBINERS = Collections.unmodifiableMap(map);
    }

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        List<StageInputSplit> splits = computeSplits(context);
        SplitCombiner combiner = getSplitCombiner(context);
        if (LOG.isDebugEnabled()) {
            if ((combiner instanceof IdentitySplitCombiner) == false) {
                LOG.debug(MessageFormat.format(
                        "combines {0} splits: {1}", //$NON-NLS-1$
                        splits.size(),
                        combiner.getClass().getName()));
            }
        }
        List<StageInputSplit> combined = combiner.combine(context, splits);
        if (LOG.isDebugEnabled() && splits.size() != combined.size()) {
            LOG.debug(MessageFormat.format(
                    "input splits are combined: {0} -> {1}", //$NON-NLS-1$
                    splits.size(),
                    combined.size()));
        }
        return new ArrayList<>(combined);
    }

    static List<StageInputSplit> computeSplits(JobContext context) throws IOException, InterruptedException {
        assert context != null;
        List<StageInput> inputs = StageInputDriver.getInputs(context.getConfiguration());
        List<StageInputSplit> cached = Cache.find(context, inputs);
        if (cached != null) {
            return cached;
        }
        Map<FormatAndMapper, List<StageInput>> paths = groupByFormatAndMapper(inputs);
        Map<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>> formats =
            instantiateFormats(context, paths.keySet());
        Job temporaryJob = Job.getInstance(context.getConfiguration());
        List<StageInputSplit> results = new ArrayList<>();
        for (Map.Entry<FormatAndMapper, List<StageInput>> entry : paths.entrySet()) {
            FormatAndMapper formatAndMapper = entry.getKey();
            List<StageInput> current = entry.getValue();
            InputFormat<?, ?> format = formats.get(formatAndMapper.formatClass);
            List<? extends InputSplit> splits;
            if (format instanceof FileInputFormat<?, ?>) {
                FileInputFormat.setInputPaths(temporaryJob, toPathArray(current));
                splits = format.getSplits(temporaryJob);
            } else if (format instanceof BridgeInputFormat) {
                splits = ((BridgeInputFormat) format).getSplits(context, current);
            } else if (format instanceof TemporaryInputFormat<?>) {
                splits = ((TemporaryInputFormat<?>) format).getSplits(context, current);
            } else {
                splits = format.getSplits(temporaryJob);
            }
            assert format != null : formatAndMapper.formatClass.getName();
            Class<? extends Mapper<?, ?, ?, ?>> mapper = formatAndMapper.mapperClass;
            for (InputSplit split : splits) {
                Source source = new Source(split, formatAndMapper.formatClass);
                StageInputSplit wrapped = new StageInputSplit(mapper, Collections.singletonList(source));
                wrapped.setConf(context.getConfiguration());
                results.add(wrapped);
            }
        }
        Cache.put(context, inputs, results);
        return results;
    }

    private static SplitCombiner getSplitCombiner(JobContext context) {
        assert context != null;
        Class<? extends SplitCombiner> combinerClass = getSplitCombinerClass(context);
        return ReflectionUtils.newInstance(combinerClass, context.getConfiguration());
    }

    /**
     * Returns the {@link SplitCombiner} class used in the current job.
     * @param context the current job context
     * @return the {@link SplitCombiner} class
     * @since 0.7.1
     */
    public static Class<? extends SplitCombiner> getSplitCombinerClass(JobContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        Configuration conf = context.getConfiguration();
        String combinerType = conf.get(KEY_SPLIT_COMBINER, DEFAULT_SPLIT_COMBINER);
        if (StageUtil.isLocalMode(context) && combinerType.equals(DEFAULT_SPLIT_COMBINER)) {
            return ExtremeSplitCombiner.class;
        }
        Class<? extends SplitCombiner> defined = SPLIT_COMBINERS.get(combinerType);
        if (defined != null) {
            return defined;
        }
        try {
            return conf.getClassByName(combinerType).asSubclass(SplitCombiner.class);
        } catch (Exception e) {
            LOG.warn(MessageFormat.format(
                    "failed to load a combiner \"{0}\"",
                    combinerType), e);
            return IdentitySplitCombiner.class;
        }
    }

    /**
     * Sets the {@link SplitCombiner} class for the current job.
     * @param context the current job context
     * @param aClass the {@link SplitCombiner} class
     * @since 0.7.1
     */
    public static void setSplitCombinerClass(JobContext context, Class<? extends SplitCombiner> aClass) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (aClass == null) {
            throw new IllegalArgumentException("aClass must not be null"); //$NON-NLS-1$
        }
        context.getConfiguration().set(KEY_SPLIT_COMBINER, aClass.getName());
    }

    private static Path[] toPathArray(List<StageInput> inputs) {
        assert inputs != null;
        List<Path> paths = new ArrayList<>();
        for (StageInput input : inputs) {
            paths.add(new Path(input.getPathString()));
        }
        return paths.toArray(new Path[paths.size()]);
    }

    private static Map<FormatAndMapper, List<StageInput>> groupByFormatAndMapper(List<StageInput> inputs) {
        assert inputs != null;
        Map<FormatAndMapper, List<StageInput>> paths = new HashMap<>();
        for (StageInput input : inputs) {
            FormatAndMapper fam = new FormatAndMapper(input.getFormatClass(), input.getMapperClass());
            List<StageInput> list = paths.get(fam);
            if (list == null) {
                list = new ArrayList<>();
                paths.put(fam, list);
            }
            list.add(input);
        }
        return paths;
    }

    private static Map<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>> instantiateFormats(
            JobContext context,
            Set<FormatAndMapper> pairs) throws IOException {
        assert context != null;
        assert pairs != null;
        Configuration conf = context.getConfiguration();
        Map<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>> results = new HashMap<>();
        for (FormatAndMapper pair : pairs) {
            Class<? extends InputFormat<?, ?>> type = pair.formatClass;
            if (results.containsKey(type) == false) {
                try {
                    InputFormat<?, ?> instance = ReflectionUtils.newInstance(type, conf);
                    results.put(type, instance);
                } catch (RuntimeException e) {
                    throw new IOException(MessageFormat.format(
                            "Cannot instantiate {0}",
                            type.getName()), e);
                }
            }
        }
        return results;
    }

    @Override
    public RecordReader createRecordReader(
            InputSplit split,
            TaskAttemptContext context) throws IOException, InterruptedException {
        assert split instanceof StageInputSplit;
        return new StageInputRecordReader();
    }

    private static class FormatAndMapper {

        final Class<? extends InputFormat<?, ?>> formatClass;

        final Class<? extends Mapper<?, ?, ?, ?>> mapperClass;

        FormatAndMapper(
                Class<? extends InputFormat<?, ?>> formatClass,
                Class<? extends Mapper<?, ?, ?, ?>> mapperClass) {
            assert formatClass != null;
            assert mapperClass != null;
            this.formatClass = formatClass;
            this.mapperClass = mapperClass;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + formatClass.hashCode();
            result = prime * result + mapperClass.hashCode();
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
            FormatAndMapper other = (FormatAndMapper) obj;
            if (formatClass.equals(other.formatClass) == false) {
                return false;
            }
            if (mapperClass.equals(other.mapperClass) == false) {
                return false;
            }
            return true;
        }
    }

    private static final class Cache {

        private static final String KEY_CACHE_ID = Cache.class.getName() + ".id"; //$NON-NLS-1$

        private static Reference<Cache> data;

        private final String id;

        private final Set<StageInput> key;

        private final byte[] value;

        private Cache(
                Collection<? extends StageInput> key,
                List<StageInputSplit> splits) throws IOException {
            this.id = UUID.randomUUID().toString();
            this.key = new HashSet<>(key);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "saving input splits into cache: id={0}", //$NON-NLS-1$
                        id));
            }
            DataBuffer buffer = new DataBuffer();
            buffer.writeInt(splits.size());
            for (StageInputSplit split : splits) {
                split.write(buffer);
            }
            this.value = Arrays.copyOfRange(buffer.getData(), 0, buffer.getWritePosition());
        }

        private List<StageInputSplit> restore(Configuration conf) throws IOException {
            assert conf != null;
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "restoring input splits from cache: id={0}", //$NON-NLS-1$
                        id));
            }
            DataBuffer buffer = new DataBuffer();
            buffer.reset(value, 0, value.length);
            int count = buffer.readInt();
            List<StageInputSplit> results = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                StageInputSplit split = new StageInputSplit();
                split.setConf(conf);
                split.readFields(buffer);
                results.add(split);
            }
            return results;
        }

        static List<StageInputSplit> find(
                JobContext context,
                Collection<? extends StageInput> key) throws IOException {
            assert key != null;
            assert context != null;
            String id = context.getConfiguration().get(KEY_CACHE_ID);
            if (id == null) {
                return null;
            }
            Cache cached;
            synchronized (Cache.class) {
                cached = data == null ? null : data.get();
                if (cached == null || id.equals(cached.id) == false) {
                    return null;
                }
                Set<StageInput> k = new HashSet<>(key);
                if (k.equals(cached.key) == false) {
                    return null;
                }
            }
            return cached.restore(context.getConfiguration());
        }

        static void put(
                JobContext context,
                Collection<? extends StageInput> key,
                List<StageInputSplit> value) throws IOException {
            assert key != null;
            assert value != null;
            Cache cache = new Cache(key, value);
            synchronized (Cache.class) {
                data = new SoftReference<>(cache);
            }
            context.getConfiguration().set(KEY_CACHE_ID, cache.id);
        }
    }
}
