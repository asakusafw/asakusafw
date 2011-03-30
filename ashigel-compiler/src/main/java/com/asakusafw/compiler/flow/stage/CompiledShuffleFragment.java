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
package com.asakusafw.compiler.flow.stage;

/**
 * コンパイルされたReduceフェーズの処理。
 */
public class CompiledShuffleFragment {

    private CompiledType mapOutputType;

    private CompiledType combineOutputType;

    /**
     * インスタンスを生成する。
     * @param mapOutput Mapの出力型
     * @param combineOutput Combineの出力型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledShuffleFragment(CompiledType mapOutput, CompiledType combineOutput) {
        if (mapOutput == null) {
            throw new IllegalArgumentException("reducerType must not be null"); //$NON-NLS-1$
        }
        if (combineOutput == null) {
            throw new IllegalArgumentException("combineOutput must not be null"); //$NON-NLS-1$
        }
        this.mapOutputType = mapOutput;
        this.combineOutputType = combineOutput;
    }

    /**
     * Mapの出力型の情報を返す。
     * @return Reducerの型情報
     */
    public CompiledType getMapOutputType() {
        return mapOutputType;
    }

    /**
     * Combineの出力型の情報を返す。
     * @return Combineの出力型の情報
     */
    public CompiledType getCombineOutputType() {
        return combineOutputType;
    }
}
