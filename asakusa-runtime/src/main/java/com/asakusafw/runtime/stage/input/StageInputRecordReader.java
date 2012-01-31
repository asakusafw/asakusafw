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

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * {@link StageInputSplit}を処理する{@link RecordReader}の実装。
 */
@SuppressWarnings("rawtypes")
public class StageInputRecordReader extends RecordReader {

    private final RecordReader<?, ?> original;

    /**
     * インスタンスを生成する。
     * @param original {@link StageInputSplit#getOriginal()}を処理する{@link RecordReader}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public StageInputRecordReader(RecordReader<?, ?> original) {
        if (original == null) {
            throw new IllegalArgumentException("original must not be null"); //$NON-NLS-1$
        }
        this.original = original;
    }

    /**
     * 実際の処理を行う{@link RecordReader}を返す。
     * @return 実際の処理を行う{@link RecordReader}
     */
    public RecordReader<?, ?> getOriginal() {
        return original;
    }

    @Override
    public void initialize(
            InputSplit split,
            TaskAttemptContext context) throws IOException, InterruptedException {
        assert split instanceof StageInputSplit;
        original.initialize(((StageInputSplit) split).getOriginal(), context);
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        return original.nextKeyValue();
    }

    @Override
    public Object getCurrentKey() throws IOException, InterruptedException {
        return original.getCurrentKey();
    }

    @Override
    public Object getCurrentValue() throws IOException, InterruptedException {
        return original.getCurrentValue();
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        return original.getProgress();
    }

    @Override
    public void close() throws IOException {
        original.close();
    }
}
