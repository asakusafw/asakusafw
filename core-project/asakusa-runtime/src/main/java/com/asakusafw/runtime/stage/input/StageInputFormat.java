/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.stage.input.StageInputSplit.Source;

/**
 * ステージへの複数の入力を処理する{@link InputFormat}。
 * <p>
 * {@link StageInputDriver}に指定された実際のInputFormatに処理を移譲する。
 * </p>
 * @since 0.1.0
 * @version 0.2.6
 */
@SuppressWarnings("rawtypes")
public class StageInputFormat extends InputFormat {

    private static final String DEFAULT = "default";

    static final Log LOG = LogFactory.getLog(StageInputFormat.class);

    private static final Map<String, Class<? extends SplitCombiner>> SPLIT_COMBINERS;
    static {
        Map<String, Class<? extends SplitCombiner>> map = new HashMap<String, Class<? extends SplitCombiner>>();
        map.put(DEFAULT, DefaultSplitCombiner.class);
        map.put("disabled", IdentitySplitCombiner.class);
        map.put("extreme", ExtremeSplitCombiner.class);
        SPLIT_COMBINERS = Collections.unmodifiableMap(map);
    }

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        List<StageInputSplit> splits = computeSplits(context);
        SplitCombiner combiner = getSplitCombiner(context);
        if (LOG.isDebugEnabled()) {
            if ((combiner instanceof IdentitySplitCombiner) == false) {
                LOG.debug(MessageFormat.format(
                        "Combines {0} splits: {1}",
                        splits.size(),
                        combiner.getClass().getName()));
            }
        }
        List<StageInputSplit> combined = combiner.combine(context, splits);
        if (LOG.isDebugEnabled() && splits.size() != combined.size()) {
            LOG.debug(MessageFormat.format(
                    "Input splits are combined: {0} -> {1}",
                    splits.size(),
                    combined.size()));
        }
        return new ArrayList<InputSplit>(combined);
    }

    private List<StageInputSplit> computeSplits(JobContext context) throws IOException, InterruptedException {
        assert context != null;
        Map<FormatAndMapper, List<StageInput>> paths = getPaths(context);
        Map<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>> formats =
            instantiateFormats(context, paths.keySet());
        Job temporaryJob = JobCompatibility.newJob(context.getConfiguration());
        List<StageInputSplit> results = new ArrayList<StageInputSplit>();
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
        return results;
    }

    private SplitCombiner getSplitCombiner(JobContext context) {
        assert context != null;
        Class<? extends SplitCombiner> combinerClass = getSplitCombinerClass(context);
        return ReflectionUtils.newInstance(combinerClass, context.getConfiguration());
    }

    private Class<? extends SplitCombiner> getSplitCombinerClass(JobContext context) {
        assert context != null;
        Configuration conf = context.getConfiguration();
        String combinerType = conf.get("com.asakusafw.input.combine", DEFAULT);
        if (isLocalMode(context) && combinerType.equals(DEFAULT)) {
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
                    "Failed to load a combiner \"{0}\"",
                    combinerType), e);
            return IdentitySplitCombiner.class;
        }
    }

    private boolean isLocalMode(JobContext context) {
        assert context != null;
        return context.getConfiguration().get("mapred.job.tracker", "unknown").equals("local");
    }

    private Path[] toPathArray(List<StageInput> inputs) {
        assert inputs != null;
        List<Path> paths = new ArrayList<Path>();
        for (StageInput input : inputs) {
            paths.add(new Path(input.getPathString()));
        }
        return paths.toArray(new Path[paths.size()]);
    }

    private Map<FormatAndMapper, List<StageInput>> getPaths(JobContext context) throws IOException {
        assert context != null;
        List<StageInput> inputs = StageInputDriver.getInputs(context.getConfiguration());
        Map<FormatAndMapper, List<StageInput>> paths = new HashMap<FormatAndMapper, List<StageInput>>();
        for (StageInput input : inputs) {
            FormatAndMapper fam = new FormatAndMapper(input.getFormatClass(), input.getMapperClass());
            List<StageInput> list = paths.get(fam);
            if (list == null) {
                list = new ArrayList<StageInput>();
                paths.put(fam, list);
            }
            list.add(input);
        }
        return paths;
    }

    private Map<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>> instantiateFormats(
            JobContext context,
            Set<FormatAndMapper> pairs) throws IOException {
        assert context != null;
        assert pairs != null;
        Configuration conf = context.getConfiguration();
        Map<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>> results =
            new HashMap<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>>();
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
}
