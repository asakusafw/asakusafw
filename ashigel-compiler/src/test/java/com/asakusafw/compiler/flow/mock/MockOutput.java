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
package com.asakusafw.compiler.flow.mock;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

import com.asakusafw.runtime.core.Result;

/**
 * ダミーの出力先。
 * @param <KEYOUT> キーの型
 * @param <VALUEOUT> 値の型
 */
public class MockOutput<KEYOUT, VALUEOUT> extends
        TaskInputOutputContext<Object, Object, KEYOUT, VALUEOUT> {

    private final Result<? super KEYOUT> keyOut;

    private final Result<? super VALUEOUT> valueOut;

    /**
     * インスタンスを生成する。
     * @param <K> キーの型
     * @param <V> 値の型
     * @param keyOut キーの出力先
     * @param valueOut 値の出力先
     * @return 生成したインスタンス
     */
    public static <K, V> MockOutput<K, V> create(
            Result<K> keyOut,
            Result<V> valueOut) {
        return new MockOutput<K, V>(keyOut, valueOut);
    }

    /**
     * インスタンスを生成する。
     * @param keyOut キーの出力先
     * @param valueOut 値の出力先
     */
    public MockOutput(Result<? super KEYOUT> keyOut, Result<? super VALUEOUT> valueOut) {
        super(
                new Configuration(false),
                new TaskAttemptID(),
                null,
                null,
                null);
        this.keyOut = keyOut;
        this.valueOut = valueOut;
    }

    @Override
    public void write(KEYOUT key, VALUEOUT value) {
        keyOut.add(key);
        valueOut.add(value);
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getCurrentKey() throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getCurrentValue() throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }
}
