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
import com.asakusafw.runtime.io.ModelOutput;

/**
 * {@link SequenceFile}にモデルオブジェクトを書き出す{@link ModelOutput}の実装。
 * @param <T> モデルオブジェクトの型
 */
public class SequenceFileModelOutput<T extends Writable> implements ModelOutput<T> {

    private SequenceFile.Writer writer;

    /**
     * インスタンスを生成する。
     * @param writer 出力先
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public SequenceFileModelOutput(SequenceFile.Writer writer) {
        Precondition.checkMustNotBeNull(writer, "writer"); //$NON-NLS-1$
        this.writer = writer;
    }

    @Override
    public void write(T model) throws IOException {
        writer.append(NullWritable.get(), model);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
