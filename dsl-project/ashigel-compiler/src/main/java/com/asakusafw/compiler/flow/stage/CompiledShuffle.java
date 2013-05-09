/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * {@link ShuffleModel}をコンパイルした結果の、シャッフルフェーズで利用される情報。
 */
public class CompiledShuffle {

    private Name keyTypeName;

    private Name valueTypeName;

    private Name groupComparatorTypeName;

    private Name sortComparatorTypeName;

    private Name partitionerTypeName;

    /**
     * インスタンスを生成する。
     * @param keyTypeName キークラスの完全限定名
     * @param valueTypeName 値クラスの完全限定名
     * @param groupComparatorTypeName グループ比較器クラスの完全限定名
     * @param sortComparatorTypeName 順序比較器クラスの完全限定名
     * @param partitionerTypeName パーティショナークラスの完全限定名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledShuffle(
            Name keyTypeName,
            Name valueTypeName,
            Name groupComparatorTypeName,
            Name sortComparatorTypeName,
            Name partitionerTypeName) {
        Precondition.checkMustNotBeNull(keyTypeName, "keyTypeName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(valueTypeName, "valueTypeName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(groupComparatorTypeName, "groupComparatorTypeName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(sortComparatorTypeName, "sortComparatorTypeName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(partitionerTypeName, "partitionerTypeName"); //$NON-NLS-1$
        this.keyTypeName = keyTypeName;
        this.valueTypeName = valueTypeName;
        this.groupComparatorTypeName = groupComparatorTypeName;
        this.sortComparatorTypeName = sortComparatorTypeName;
        this.partitionerTypeName = partitionerTypeName;
    }

    /**
     * シャッフル時に利用するキークラスの完全限定名を返す。
     * @return キークラスの完全限定名
     */
    public Name getKeyTypeName() {
        return keyTypeName;
    }

    /**
     * シャッフル時に利用する値クラスの完全限定名を返す。
     * @return 値クラスの完全限定名
     */
    public Name getValueTypeName() {
        return valueTypeName;
    }

    /**
     * シャッフル時に利用するグループ化比較器クラスの完全限定名を返す。
     * @return グループ化比較器クラスの完全限定名
     */
    public Name getGroupComparatorTypeName() {
        return groupComparatorTypeName;
    }

    /**
     * シャッフル時に利用する順序比較器クラスの完全限定名を返す。
     * @return 順序比較器クラスの完全限定名
     */
    public Name getSortComparatorTypeName() {
        return sortComparatorTypeName;
    }

    /**
     * シャッフル時に利用するパーティショナークラスの完全限定名を返す。
     * @return パーティショナークラスの完全限定名
     */
    public Name getPartitionerTypeName() {
        return partitionerTypeName;
    }
}
