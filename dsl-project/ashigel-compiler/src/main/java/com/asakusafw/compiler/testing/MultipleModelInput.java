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
package com.asakusafw.compiler.testing;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.io.ModelInput;

/**
 * 複数の入力を取り扱う{@link ModelInput}の実装。
 * @param <T> 入力の種類
 */
public class MultipleModelInput<T> implements ModelInput<T> {

    private LinkedList<ModelInput<T>> inputs;

    /**
     * インスタンスを生成する。
     * @param inputs 入力の一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public MultipleModelInput(List<? extends ModelInput<T>> inputs) {
        Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
        this.inputs = new LinkedList<ModelInput<T>>(inputs);
    }

    @Override
    public boolean readTo(T model) throws IOException {
        while (inputs.isEmpty() == false) {
            ModelInput<T> input = inputs.getFirst();
            if (input.readTo(model)) {
                return true;
            }
            inputs.removeFirst().close();
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        IOException first = null;
        while (inputs.isEmpty() == false) {
            ModelInput<T> input = inputs.removeFirst();
            try {
                input.close();
            } catch (IOException e) {
                if (first == null) {
                    first = e;
                }
            }
        }
        if (first != null) {
            throw first;
        }
    }
}
