/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.runtime.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.runtime.stage.AbstractStageClient;
import com.asakusafw.runtime.util.VariableTable;

/**
 * バッチの文脈情報。
 * <p>
 * このクラスは<b>演算子の内部でのみ</b>利用できる。
 * インポータ記述などはコンパイル時に参照するため、このクラスを利用しようとするとエラーが発生する。
 * 一部のインポータやエクスポータでは、この文脈情報と同じ情報を、特殊な方法で取得するための
 * 機能が用意されているものもある。
 * </p>
 */
public class BatchContext {

    static final ThreadLocal<BatchContext> CONTEXTS = new ThreadLocal<BatchContext>() {
        @Override
        protected BatchContext initialValue() {
            throw new IllegalStateException("文脈情報が初期化されていません");
        }
    };

    private Map<String, String> variables = new HashMap<String, String>();

    /**
     * インスタンスを生成する。
     * @param variables 変数表
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    protected BatchContext(Map<String, String> variables) {
        if (variables == null) {
            throw new IllegalArgumentException("variables must not be null"); //$NON-NLS-1$
        }
        this.variables = new HashMap<String, String>(variables);
    }

    /**
     * 指定の名前に関連する変数の内容を返す。
     * @param name 変数名
     * @return 対応する内容、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return CONTEXTS.get().variables.get(name);
    }

    /**
     * {@link BatchContext}の初期化を行う。
     */
    public static class Initializer implements RuntimeResource {

        @Override
        public void setup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            String arguments = configuration.get(AbstractStageClient.PROP_ASAKUSA_BATCH_ARGS, "");
            VariableTable variables = new VariableTable();
            variables.defineVariables(arguments);
            BatchContext context = new BatchContext(variables.getVariables());
            CONTEXTS.set(context);
        }

        @Override
        public void cleanup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            CONTEXTS.remove();
        }
    }
}
