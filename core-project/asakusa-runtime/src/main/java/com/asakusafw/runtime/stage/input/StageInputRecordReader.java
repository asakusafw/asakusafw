/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.stage.input.StageInputSplit.Source;

/**
 * {@link StageInputSplit}を処理する{@link RecordReader}の実装。
 * @since 0.1.0
 * @version 0.2.6
 */
@SuppressWarnings("rawtypes")
public class StageInputRecordReader extends RecordReader {

    private static final RecordReader<?, ?> VOID = new RecordReader<Object, Object>() {

        @Override
        public void initialize(InputSplit split, TaskAttemptContext ctxt) {
            return;
        }

        @Override
        public boolean nextKeyValue() {
            return false;
        }

        @Override
        public Object getCurrentKey() {
            throw new IllegalStateException();
        }

        @Override
        public Object getCurrentValue() {
            throw new IllegalStateException();
        }

        @Override
        public float getProgress() {
            return 0f;
        }

        @Override
        public void close() {
            return;
        }
    };

    private Iterator<Source> sources;

    private TaskAttemptContext context;

    private RecordReader<?, ?> current;

    private boolean eof;

    private float progressPerSource;

    private float baseProgress;

    @Override
    public void initialize(
            InputSplit split,
            TaskAttemptContext taskContext) throws IOException, InterruptedException {
        assert split instanceof StageInputSplit;
        List<Source> sourceList = ((StageInputSplit) split).getSources();
        this.sources = sourceList.iterator();
        this.context = taskContext;
        this.progressPerSource = sourceList.isEmpty() ? 1f : 1f / sourceList.size();
        this.baseProgress = 0f;
        prepare();
    }

    private void prepare() throws IOException, InterruptedException {
        if (current != null) {
            baseProgress += progressPerSource;
            current.close();
        }
        if (sources.hasNext()) {
            Source next = sources.next();
            InputFormat<?, ?> format = ReflectionUtils.newInstance(next.getFormatClass(), context.getConfiguration());
            current = format.createRecordReader(next.getSplit(), context);
            current.initialize(next.getSplit(), context);
        } else {
            eof = true;
            current = VOID;
        }
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        while (eof == false) {
            if (current.nextKeyValue()) {
                return true;
            }
            prepare();
        }
        return false;
    }

    @Override
    public Object getCurrentKey() throws IOException, InterruptedException {
        return current.getCurrentKey();
    }

    @Override
    public Object getCurrentValue() throws IOException, InterruptedException {
        return current.getCurrentValue();
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        float progress = current.getProgress();
        return baseProgress + progress * progressPerSource;
    }

    @Override
    public void close() throws IOException {
        current.close();
    }
}
