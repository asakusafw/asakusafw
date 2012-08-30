/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;

/**
 * バッチクラスの内容。
 */
public class BatchClass {

    private Batch config;

    private BatchDescription description;

    /**
     * インスタンスを生成する。
     * @param config このバッチの設定
     * @param description このバッチを記述するクラスのインスタンス (結線済み)
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public BatchClass(Batch config, BatchDescription description) {
        Precondition.checkMustNotBeNull(config, "config"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        this.config = config;
        this.description = description;
    }

    /**
     * このバッチの設定を返す。
     * @return このバッチの設定
     */
    public Batch getConfig() {
        return config;
    }

    /**
     * このバッチを記述するクラスのインスタンスを返す。
     * @return このバッチを記述するクラスのインスタンス
     */
    public BatchDescription getDescription() {
        return description;
    }
}
