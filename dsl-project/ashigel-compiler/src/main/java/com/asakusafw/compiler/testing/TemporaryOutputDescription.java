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
package com.asakusafw.compiler.testing;

import java.text.MessageFormat;

import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * An {@link ExporterDescription} to output result to temporary storage.
 * @since 0.2.5
 */
public abstract class TemporaryOutputDescription implements ExporterDescription {

    /**
     * エクスポート先のファイルへのパスの接頭辞を返す。
     * <p>
     * パスの各セグメントは{@code /}で区切り、末尾には{@code -*}を付与すること。
     * また、末尾のセグメント(ファイル名の接頭辞)には数字とアルファベットのみ指定できる。
     * </p>
     * <p>
     * パスには必ずひとつ以上のディレクトリを含めなければならない。
     * また、同ディレクトリがエクスポートの実行時に存在していた場合、
     * エクスポート処理が失敗する可能性がある。
     * </p>
     * @return エクスポート先のファイルが出力されるパスの接頭辞
     */
    public abstract String getPathPrefix();

    @Override
    public String toString() {
        return MessageFormat.format(
                "TemporaryExporter({0})",
                getPathPrefix());
    }
}
