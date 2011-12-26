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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.asakusafw.runtime.stage.input.StageInputDriver.Input;
import com.asakusafw.runtime.stage.temporary.TemporaryInputFormat;

/**
 * ステージへの複数の入力を処理する{@link InputFormat}。
 * <p>
 * {@link StageInputDriver}に指定された実際のInputFormatに処理を移譲する。
 * </p>
 * <p>
 * 現在のところ、{@link FileInputFormat}およびそのサブクラスに関するもののみを取り扱う。
 * </p>
 */
@SuppressWarnings("rawtypes")
public class StageInputFormat extends InputFormat {

    static final Log LOG = LogFactory.getLog(StageInputFormat.class);

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        Map<FormatAndMapper, List<Input>> paths = getPaths(context);
        Map<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>> formats = instantiateFormats(paths.keySet());

        Job temporaryJob = new Job(context.getConfiguration());
        List<InputSplit> results = new ArrayList<InputSplit>();
        for (Map.Entry<FormatAndMapper, List<Input>> entry : paths.entrySet()) {
            FormatAndMapper formatAndMapper = entry.getKey();
            List<Input> current = entry.getValue();
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
            for (InputSplit split : splits) {
                StageInputSplit wrapped = new StageInputSplit(
                        split, formatAndMapper.formatClass, formatAndMapper.mapperClass);
                wrapped.setConf(context.getConfiguration());
                results.add(wrapped);
            }
        }
        return results;
    }

    private Path[] toPathArray(List<Input> inputs) {
        assert inputs != null;
        List<Path> paths = new ArrayList<Path>();
        for (Input input : inputs) {
            paths.add(input.getPath());
        }
        return paths.toArray(new Path[paths.size()]);
    }

    private Map<FormatAndMapper, List<Input>> getPaths(JobContext context) throws IOException {
        assert context != null;
        List<Input> inputs = StageInputDriver.getInputs(context.getConfiguration());
        Map<FormatAndMapper, List<Input>> paths = new HashMap<FormatAndMapper, List<Input>>();
        for (Input input : inputs) {
            FormatAndMapper fam = new FormatAndMapper(input.getFormatClass(), input.getMapperClass());
            List<Input> list = paths.get(fam);
            if (list == null) {
                list = new ArrayList<Input>();
                paths.put(fam, list);
            }
            list.add(input);
        }
        return paths;
    }

    private Map<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>> instantiateFormats(
            Set<FormatAndMapper> pairs) throws IOException {
        assert pairs != null;
        Map<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>> results =
            new HashMap<Class<? extends InputFormat<?, ?>>, InputFormat<?, ?>>();
        for (FormatAndMapper pair : pairs) {
            Class<? extends InputFormat<?, ?>> type = pair.formatClass;
            if (results.containsKey(type) == false) {
                try {
                    InputFormat<?, ?> instance = type.newInstance();
                    results.put(type, instance);
                } catch (Exception e) {
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
        StageInputSplit input = (StageInputSplit) split;
        InputFormat format = ReflectionUtils.newInstance(input.getFormatClass(), context.getConfiguration());
        RecordReader original = format.createRecordReader(input.getOriginal(), context);
        return new StageInputRecordReader(original);
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
