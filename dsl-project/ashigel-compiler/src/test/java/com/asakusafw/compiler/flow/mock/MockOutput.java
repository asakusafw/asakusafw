/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import static com.asakusafw.runtime.compatibility.JobCompatibility.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

import com.asakusafw.runtime.compatibility.hadoop.KeyValueConsumer;
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
        TaskAttemptID id = newTaskAttemptId(newTaskId(newJobId()));
        return newTaskOutputContext(new Configuration(false), id, new ResultBridge<K, V>(keyOut, valueOut));
    }

    private static class ResultBridge<K, V> implements KeyValueConsumer<K, V> {

        private final Result<K> keyOut;

        private final Result<V> valueOut;

        public ResultBridge(Result<K> keyOut, Result<V> valueOut) {
            this.keyOut = keyOut;
            this.valueOut = valueOut;
        }

        @Override
        public void consume(K key, V value) {
            keyOut.add(key);
            valueOut.add(value);
        }
    }
}
