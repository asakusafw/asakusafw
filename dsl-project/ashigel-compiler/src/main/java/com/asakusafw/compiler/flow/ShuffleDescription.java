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
package com.asakusafw.compiler.flow;

import java.lang.reflect.Type;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;


/**
 * シャッフル操作に関する情報。
 */
public class ShuffleDescription {

    private Type outputType;

    private ShuffleKey keyInfo;

    private LinePartProcessor converter;

    /**
     * インスタンスを生成する。
     * @param outputType レデュースに入力される型
     * @param keyInfo シャッフルで利用するキーの情報
     * @param converter シャッフル前に適用される変換機
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ShuffleDescription(
            Type outputType,
            ShuffleKey keyInfo,
            LinePartProcessor converter) {
        Precondition.checkMustNotBeNull(outputType, "outputType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(keyInfo, "keyInfo"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(converter, "converter"); //$NON-NLS-1$
        this.outputType = outputType;
        this.keyInfo = keyInfo;
        this.converter = converter;
    }

    /**
     * シャッフルフェーズで転送されるデータの種類を返す。
     * @return 出力するデータの種類
     */
    public Type getOutputType() {
        return outputType;
    }

    /**
     * シャッフル時のキーの情報を返す。
     * @return シャッフル時のキーの情報
     */
    public ShuffleKey getKeyInfo() {
        return keyInfo;
    }

    /**
     * シャッフルフェーズの先頭 (マップブロックの末尾)で適用される変換器を返す。
     * @return 適用される変換器
     */
    public LinePartProcessor getConverter() {
        return converter;
    }
}
