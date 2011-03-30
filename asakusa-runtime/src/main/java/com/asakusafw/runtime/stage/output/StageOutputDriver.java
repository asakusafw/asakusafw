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
package com.asakusafw.runtime.stage.output;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.ResultOutput;

/**
 * ステージ出力を設定するためのドライバ。
 * <p>
 * 現在のところ、{@link FileOutputFormat}およびそのサブクラスに関するもののみを取り扱う。
 * </p>
 */
public class StageOutputDriver {

    private final MultipleOutputs<?, ?> multipleOutputs;

    private final Map<String, Result<?>> resultSinks;

    /**
     * インスタンスを生成する。
     * @param context 現在のタスク試行コンテキスト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public StageOutputDriver(TaskInputOutputContext<?, ?, ?, ?> context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.multipleOutputs = ResultOutput.createMultipleOutputs(context);
        this.resultSinks = new HashMap<String, Result<?>>();
    }

    /**
     * 指定の名前を持つ出力のシンクオブジェクトを返す。
     * <p>
     * ここに指定する名前は、ジョブの起動時にあらかじめ
     * {@link #add(Job, String, Class, Class, Class)}で登録しておく必要がある。
     * </p>
     * @param <T> 出力の型
     * @param name 出力の名前
     * @return 対応するシンクオブジェクト
     * @throws IOException 出力の作成に失敗した場合
     * @throws InterruptedException 出力の作成時に割り込みが発行された場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public synchronized <T extends Writable> Result<T> getResultSink(
            String name) throws IOException, InterruptedException {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        @SuppressWarnings("unchecked")
        Result<T> sink = (Result<T>) resultSinks.get(name);
        if (sink != null) {
            return sink;
        }
        sink = new ResultOutput<T>(multipleOutputs, name);
        resultSinks.put(name, sink);
        return sink;
    }

    /**
     * 現在の出力を破棄する。
     * @throws IOException 出力のフラッシュに失敗した場合
     * @throws InterruptedException 出力の破棄に割り込みが発行された場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public void close() throws IOException, InterruptedException {
        multipleOutputs.close();
    }

    /**
     * 指定のジョブの出力先ディレクトリを指定する。
     * @param job 対象のジョブ
     * @param path 出力先ディレクトリ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static void setPath(Job job, Path path) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        FileOutputFormat.setOutputPath(job, path);
    }

    /**
     * 指定のジョブに出力の情報を追加する。
     * @param job 対象のジョブ
     * @param name 出力ファイルの接頭辞
     * @param formatClass 入力フォーマットクラス
     * @param keyClass 出力するキーの型
     * @param valueClass 出力する値の型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    @SuppressWarnings("rawtypes")
    public static void add(
            Job job,
            String name,
            Class<? extends OutputFormat> formatClass,
            Class<?> keyClass,
            Class<?> valueClass) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (keyClass == null) {
            throw new IllegalArgumentException("keyClass must not be null"); //$NON-NLS-1$
        }
        if (valueClass == null) {
            throw new IllegalArgumentException("valueClass must not be null"); //$NON-NLS-1$
        }
        MultipleOutputs.addNamedOutput(job, name, formatClass, keyClass, valueClass);
    }
}
