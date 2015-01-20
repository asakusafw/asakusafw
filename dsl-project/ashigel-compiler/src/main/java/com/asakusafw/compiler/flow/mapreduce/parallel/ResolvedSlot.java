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
package com.asakusafw.compiler.flow.mapreduce.parallel;

import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;

/**
 * 型に関連する情報を解決したスロット。
 */
public class ResolvedSlot {

    private Slot source;

    private int slotNumber;

    private DataClass valueClass;

    private List<Property> sortProperties;

    /**
     * インスタンスを生成する。
     * @param source コンパイルしたソース
     * @param slotNumber このスロットのスロット番号
     * @param valueClass スロットの値を表すクラス
     * @param sortProperties スロットのソート順序に関連するプロパティ一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ResolvedSlot(Slot source, int slotNumber, DataClass valueClass, List<Property> sortProperties) {
        Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(valueClass, "valueClass"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(sortProperties, "sortProperties"); //$NON-NLS-1$
        this.source = source;
        this.slotNumber = slotNumber;
        this.valueClass = valueClass;
        this.sortProperties = sortProperties;
    }

    /**
     * コンパイルしたソースを返す。
     * @return コンパイルしたソース
     */
    public Slot getSource() {
        return source;
    }

    /**
     * このスロットのスロット番号を返す。
     * @return このスロットのスロット番号
     */
    public int getSlotNumber() {
        return slotNumber;
    }

    /**
     * スロットの値を表すクラスを返す。
     * @return スロットの値を表すクラス
     */
    public DataClass getValueClass() {
        return valueClass;
    }

    /**
     * スロットのソート順序に関連するプロパティ一覧を返す。
     * @return スロットのソート順序に関連するプロパティ一覧
     */
    public List<DataClass.Property> getSortProperties() {
        return sortProperties;
    }
}
