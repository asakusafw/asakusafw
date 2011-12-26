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

import java.text.MessageFormat;
import java.util.Set;

import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * An {@link ImporterDescription} to input data from temporary storage.
 * @since 0.2.5
 */
public abstract class TemporaryInputDescription implements ImporterDescription {

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

    @Override
    public String toString() {
        return MessageFormat.format(
                "TemporaryImporter({0})",
                getPaths());
    }
}
