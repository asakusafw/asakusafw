/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.mock;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.counters.GenericCounter;
import org.apache.hadoop.mapreduce.task.TaskInputOutputContextImpl;

import com.asakusafw.runtime.core.Result;

/**
 * Mock output.
 */
public class MockOutput {

    private MockOutput() {
        return;
    }

    /**
     * Creates a new instance.
     * @param <K> the key type
     * @param <V> the value type
     * @param keyOut the key output
     * @param valueOut the value output
     * @return the created instance
     */
    public static <K, V> TaskInputOutputContext<?, ?, K, V> create(
            Result<K> keyOut,
            Result<V> valueOut) {
        Configuration conf = new Configuration(false);
        TaskAttemptID id = new TaskAttemptID();
        return new MockTaskInputOutputContext<>(conf, id, keyOut, valueOut);
    }

    private static class MockTaskInputOutputContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
            extends TaskInputOutputContextImpl<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

        private final Result<? super KEYOUT> keyOut;

        private final Result<? super VALUEOUT> valueOut;

        MockTaskInputOutputContext(
                Configuration conf, TaskAttemptID taskId,
                Result<? super KEYOUT> keyOut, Result<? super VALUEOUT> valueOut) {
            super(conf, taskId, null, null, new MockStatusReporter());
            this.keyOut = keyOut;
            this.valueOut = valueOut;
        }

        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            return false;
        }

        @Override
        public KEYIN getCurrentKey() throws IOException, InterruptedException {
            return null;
        }

        @Override
        public VALUEIN getCurrentValue() throws IOException, InterruptedException {
            return null;
        }

        @Override
        public void write(KEYOUT key, VALUEOUT value) {
            keyOut.add(key);
            valueOut.add(value);
        }
    }

    private static final class MockStatusReporter extends StatusReporter {

        MockStatusReporter() {
            return;
        }

        @Override
        public Counter getCounter(Enum<?> name) {
            return getCounter(name.getDeclaringClass().getName(), name.name());
        }

        @Override
        public Counter getCounter(String group, String name) {
            return new GenericCounter() {
                // empty
            };
        }

        @Override
        public void progress() {
            return;
        }

        @Override
        public void setStatus(String status) {
            return;
        }

        @Override
        public float getProgress() {
            return 0;
        }
    }
}
