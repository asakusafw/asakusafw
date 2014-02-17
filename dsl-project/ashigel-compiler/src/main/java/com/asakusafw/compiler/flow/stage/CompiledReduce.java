/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
public class CompiledReduce {

    private CompiledType reducerType;

    private CompiledType combinerTypeOrNull;

    /**
     * インスタンスを生成する。
     * @param reducerType Reducerの型
     * @param combinerTypeOrNull Combinerの型、利用しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledReduce(CompiledType reducerType, CompiledType combinerTypeOrNull) {
        if (reducerType == null) {
            throw new IllegalArgumentException("reducerType must not be null"); //$NON-NLS-1$
        }
        this.reducerType = reducerType;
        this.combinerTypeOrNull = combinerTypeOrNull;
    }

    /**
     * Reducerの型情報を返す。
     * @return Reducerの型情報
     */
    public CompiledType getReducerType() {
        return reducerType;
    }

    /**
     * Combinerの型情報を返す。
     * @return Combinerの型情報、利用しない場合は{@code null}
     */
    public CompiledType getCombinerTypeOrNull() {
        return combinerTypeOrNull;
    }
}
