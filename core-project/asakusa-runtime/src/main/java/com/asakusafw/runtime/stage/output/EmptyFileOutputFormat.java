/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.output;

import java.io.IOException;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * An empty implementation of {@link FileOutputFormat}.
 * @since 0.2.5
 */
public final class EmptyFileOutputFormat extends FileOutputFormat<Object, Object> {

    @Override
    public RecordWriter<Object, Object> getRecordWriter(
            TaskAttemptContext job) throws IOException, InterruptedException {
        return new RecordWriter<Object, Object>() {
            @Override
            public void write(Object key, Object value) throws IOException, InterruptedException {
                return;
            }
            @Override
            public void close(TaskAttemptContext context) throws IOException, InterruptedException {
                return;
            }
        };
    }
}
