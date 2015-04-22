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

import java.lang.reflect.Type;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.SourceInfo;

/**
 * ソートする出力のスロット。
 */
public class Slot {

    private final String outputName;

    private final Type type;

    private final List<String> propertyNames;

    private final List<SourceInfo> inputs;

    private final Class<?> outputFormatType;

    /**
     * インスタンスを生成する。
     * @param outputName このスロットに関する出力先の名前
     * @param type このスロットに関連する出力の型
     * @param propertyNames このスロットに関連する整列プロパティ名の一覧
     * @param inputs このスロットへの入力の一覧
     * @param outputFormatType このスロットからの出力形式
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Slot(
            String outputName,
            Type type,
            List<String> propertyNames,
            List<SourceInfo> inputs,
            Class<?> outputFormatType) {
        Precondition.checkMustNotBeNull(outputName, "outputName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(propertyNames, "propertyNames"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputFormatType, "outputFormatType"); //$NON-NLS-1$
        this.outputName = outputName;
        this.type = type;
        this.propertyNames = propertyNames;
        this.inputs = inputs;
        this.outputFormatType = outputFormatType;
    }

    /**
     * このスロットに関連する出力の識別子を返す。
     * @return このスロットに関連する出力の識別子
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * このスロットに関連する出力の型を返す。
     * @return このスロットに関連する出力の型
     */
    public Type getType() {
        return type;
    }

    /**
     * このスロットに関連する整列プロパティ名の一覧を返す。
     * @return このスロットに関連する整列プロパティ名の一覧
     */
    public List<String> getSortPropertyNames() {
        return propertyNames;
    }

    /**
     * このスロットへの入力の一覧を返す。
     * @return このスロットへの入力の一覧
     */
    public List<SourceInfo> getInputs() {
        return inputs;
    }

    /**
     * このスロットからの出力形式を返す。
     * @return このスロットからの出力形式
     */
    public Class<?> getOutputFormatType() {
        return outputFormatType;
    }
}
