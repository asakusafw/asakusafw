/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.util.Set;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;

/**
 * ファイルからのインポーターの処理内容を記述するクラスの基底クラス。
 */
public abstract class FileImporterDescription implements ImporterDescription {

    @Override
    public DataSize getDataSize() {
        return DataSize.UNKNOWN;
    }

    /**
     * インポート対象のファイルへのパス一覧を返す。
     * <p>
     * パスは絶対パスとして指定し、パス内にはワイルドカードとして末尾に{@code -*}を付与することができる。
     * </p>
     * @return インポート対象のファイルへのパス一覧
     */
    public abstract Set<String> getPaths();

    /**
     * インポート時に利用する入力フォーマットのクラスを返す。
     * <p>
     * 同クラスは、ファイルを読み出してキーに{@link NullWritable}、
     * 値に{@link ExporterDescription#getModelType()}の型の値を生成する形式である必要がある。
     * なお、デフォルトでは{@link SequenceFileInputFormat}を利用する設定となっている。
     * </p>
     * @return インポート時に利用する入力フォーマットのクラス
     */
    @SuppressWarnings("rawtypes")
    public Class<? extends FileInputFormat> getInputFormat() {
        return SequenceFileInputFormat.class;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "FileImporter({1}, {0})",
                getPaths(),
                getInputFormat().getSimpleName());
    }
}
