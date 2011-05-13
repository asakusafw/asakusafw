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
package com.asakusafw.vocabulary.external;

import java.text.MessageFormat;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

/**
 * ファイルへのエクスポーターの処理内容を記述するクラスの基底クラス。
 */
public abstract class FileExporterDescription implements ExporterDescription {

    /**
     * エクスポート先のファイルへのパスの接頭辞を返す。
     * <p>
     * パスの各セグメントは{@code /}で区切り、末尾には{@code -*}を付与すること
     * (付与しなかった場合にはエラーとなる)。
     * </p>
     * <p>
     * なお、末尾のセグメントには数字とアルファベットのみ指定できる。
     * </p>
     * @return エクスポート先のファイルが出力されるパスの接頭辞
     */
    public abstract String getPathPrefix();

    /**
     * エクスポート時に利用する出力フォーマットのクラスを返す。
     * <p>
     * 同クラスは、キーに{@link NullWritable}、値に{@link ExporterDescription#getModelType()}の
     * 型を出力できる形式である必要がある。
     * なお、デフォルトでは{@link SequenceFileOutputFormat}を利用する設定となっている。
     * </p>
     * @return エクスポート時に利用する出力フォーマットのクラス
     */
    @SuppressWarnings("rawtypes")
    public Class<? extends FileOutputFormat> getOutputFormat() {
        return SequenceFileOutputFormat.class;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "FileExporter({1}, {0})",
                getPathPrefix(),
                getOutputFormat().getClass().getSimpleName());
    }
}
