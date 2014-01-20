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
package com.asakusafw.dmdl.thundergate.util;

import com.asakusafw.dmdl.thundergate.model.ModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelReference;

/**
 * {@code *ModelBuilder}の基底。
 * @param <T> 実装クラスの型
 */
public abstract class ModelBuilder<T extends ModelBuilder<T>> {

    private String simpleName;

    /**
     * インスタンスを生成する。
     * @param simpleName 生成するモデルの単純名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ModelBuilder(String simpleName) {
        if (simpleName == null) {
            throw new IllegalArgumentException("simpleName must not be null"); //$NON-NLS-1$
        }
        this.simpleName = simpleName;
    }

    /**
     * 対象のモデルに対する参照を返す。
     * @return 対象のモデルに対する参照
     */
    public ModelReference getReference() {
        return new ModelReference(simpleName);
    }

    /**
     * ここまでの情報を元に、{@link ModelDescription}を構築して返す。
     * @return 構築したモデル
     * @throws IllegalStateException 構築に必要な情報が揃っていない場合
     */
    public abstract ModelDescription toDescription();
}
