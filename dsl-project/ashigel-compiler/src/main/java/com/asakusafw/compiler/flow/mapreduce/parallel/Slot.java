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
package com.asakusafw.compiler.flow.mapreduce.parallel;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.hadoop.mapreduce.InputFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.Location;

/**
 * ソートする出力のスロット。
 */
public class Slot {

    private String outputName;

    private Type type;

    private List<String> propertyNames;

    private List<Slot.Input> inputs;

    private Class<?> outputFormatType;

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
            List<Input> inputs,
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
    public List<Slot.Input> getInputs() {
        return inputs;
    }

    /**
     * このスロットからの出力形式を返す。
     * @return このスロットからの出力形式
     */
    public Class<?> getOutputFormatType() {
        return outputFormatType;
    }

    /**
     * スロットへの入力。
     */
    public static class Input {

        private Location location;

        private Class<? extends InputFormat<?, ?>> formatType;

        /**
         * インスタンスを生成する。
         * @param location この入力が配置された位置
         * @param formatType この入力のフォーマットを表す型
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Input(Location location, Class<? extends InputFormat> formatType) {
            Precondition.checkMustNotBeNull(location, "location"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(formatType, "formatType"); //$NON-NLS-1$
            this.location = location;
            this.formatType = (Class<? extends InputFormat<?, ?>>) formatType;
        }

        /**
         * この入力が配置された位置を返す。
         * @return この入力が配置された位置
         */
        public Location getLocation() {
            return location;
        }

        /**
         * この入力のフォーマットを表す型を返す。
         * @return この入力のフォーマットを表す型
         */
        public Class<? extends InputFormat<?, ?>> getFormatType() {
            return formatType;
        }
    }
}
