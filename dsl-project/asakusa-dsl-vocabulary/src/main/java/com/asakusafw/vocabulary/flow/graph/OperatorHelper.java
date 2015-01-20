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
package com.asakusafw.vocabulary.flow.graph;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

/**
 * 補助演算子を表す。
 */
public final class OperatorHelper implements FlowElementAttribute {

    private String name;

    private List<Class<?>> parameterTypes;

    /**
     * インスタンスを生成する。
     * @param name 補助演算子メソッドの名前
     * @param parameterTypes 補助演算子メソッドの引数型(消去型)一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public OperatorHelper(String name, List<Class<?>> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public Class<? extends FlowElementAttribute> getDeclaringClass() {
        return OperatorHelper.class;
    }

    /**
     * 補助演算子メソッドの名前を返す。
     * @return 補助演算子メソッドの名前
     */
    public String getName() {
        return name;
    }

    /**
     * 補助演算子メソッドのパラメーター型一覧を返す。
     * <p>
     * これらの型は、消去型として返される。
     * </p>
     * @return 補助演算子メソッドのパラメーター型一覧
     */
    public List<Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    /**
     * この宣言に対する実行時のメソッド表現を返す。
     * @param owner 補助対象の演算子
     * @return 実行時のメソッド表現、対応するものが存在しない場合は{@code null}
     */
    public Method toMethod(OperatorDescription.Declaration owner) {
        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null"); //$NON-NLS-1$
        }
        Class<?>[] params = parameterTypes.toArray(new Class<?>[parameterTypes.size()]);
        try {
            return owner.getDeclaring().getMethod(name, params);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "#{0}({1})",
                name,
                parameterTypes);
    }
}
