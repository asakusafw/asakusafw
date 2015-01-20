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
package com.asakusafw.compiler.common;

import java.util.Set;

import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;

/**
 * 衝突しない名前を作成する。
 */
public class NameGenerator {

    private final ModelFactory factory;

    private final Set<String> used = Sets.create();

    /**
     * インスタンスを生成する。
     * @param factory DOMを生成するファクトリー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public NameGenerator(ModelFactory factory) {
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        this.factory = factory;
    }

    /**
     * 指定の名前を利用済みの名前として登録する。
     * @param name 登録する名前
     * @return 引数の値
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public String reserve(String name) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        used.add(name);
        return name;
    }

    /**
     * 指定の要素とヒント名を利用して、まだ利用していない単純名を生成する。
     * @param hint ヒント名
     * @return まだ利用していない名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public SimpleName create(String hint) {
        Precondition.checkMustNotBeNull(hint, "hint"); //$NON-NLS-1$
        int initial = 0;
        String name = hint;
        if (used.contains(name)) {
            int number = initial;
            String current;
            do {
                current = name + number;
                number++;
            } while (used.contains(current));
            name = current;
        }
        used.add(name);
        return factory.newSimpleName(name);
    }
}
