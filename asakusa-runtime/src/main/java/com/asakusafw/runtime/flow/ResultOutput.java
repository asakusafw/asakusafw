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
package com.asakusafw.runtime.flow;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import com.asakusafw.runtime.core.Result;

/**
 * 結果を出力する。
 * @param <T> 結果の型
 */
public class ResultOutput<T extends Writable> implements Result<T> {

    static final Log LOG = LogFactory.getLog(ResultOutput.class);

    private MultipleOutputs<?, ?> collector;

    private String outputName;

    /**
     * インスタンスを生成する。
     * @param outputs 結果の出力先
     * @param outputName 出力先の名前
     * @throws IOException 初期化に失敗した場合
     * @throws InterruptedException 初期化に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ResultOutput(
            MultipleOutputs<?, ?> outputs,
            String outputName) throws IOException, InterruptedException {
        if (outputs == null) {
            throw new IllegalArgumentException("outputs must not be null"); //$NON-NLS-1$
        }
        if (outputName == null) {
            throw new IllegalArgumentException("outputName must not be null"); //$NON-NLS-1$
        }
        this.outputName = outputName;
        this.collector = outputs;
        initialize();
    }

    private void initialize() throws IOException, InterruptedException {
        LOG.info(MessageFormat.format("出力{0}を初期化しています", outputName));
        try {
            collector.write(outputName, null, null);
        } catch (RuntimeException e) {
            // FIXME force initializing MultipleOutputs
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Ignore RuntimeException while initializing {0}",
                        outputName));
            }
        }
    }

    @Override
    public void add(T result) {
        try {
            collector.write(outputName, NullWritable.get(), result);
        } catch (Exception e) {
            throw new Result.OutputException(e);
        }
    }

    /**
     * {@link MultipleOutputs}を安全に生成する。
     * @param <K> キーの型
     * @param <V> 値の型
     * @param context コンテキストオブジェクト
     * @return このコンテキストで利用可能な{@link MultipleOutputs}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static <K, V> MultipleOutputs<K, V> createMultipleOutputs(
            TaskInputOutputContext<?, ?, K, V> context) {
        return new MultipleOutputs<K, V>(context);
    }
}
