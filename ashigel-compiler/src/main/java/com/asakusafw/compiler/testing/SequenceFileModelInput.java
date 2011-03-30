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
package com.asakusafw.compiler.testing;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.io.ModelInput;

/**
 * {@link SequenceFile}からモデルオブジェクトを読み出す{@link ModelInput}の実装。
 * @param <T> モデルオブジェクトの型
 */
public class SequenceFileModelInput<T extends Writable> implements ModelInput<T> {

    private SequenceFile.Reader reader;

    /**
     * インスタンスを生成する。
     * @param reader 入力元
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public SequenceFileModelInput(SequenceFile.Reader reader) {
        Precondition.checkMustNotBeNull(reader, "reader"); //$NON-NLS-1$
        this.reader = reader;
    }

    @Override
    public boolean readTo(T model) throws IOException {
        return reader.next(NullWritable.get(), model);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
