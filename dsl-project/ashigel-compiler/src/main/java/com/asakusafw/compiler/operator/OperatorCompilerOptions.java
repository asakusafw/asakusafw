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
package com.asakusafw.compiler.operator;

import java.text.MessageFormat;
import java.util.Map;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.repository.SpiDataModelMirrorRepository;

/**
 * Operator DSLコンパイラのオプション一覧。
 */
public final class OperatorCompilerOptions {

    private ClassLoader serviceClassLoader;
    private DataModelMirrorRepository dataModelRepository;

    private OperatorCompilerOptions() {
        return;
    }

    /**
     * 注釈プロセッサのオプションを解析してこのオブジェクトを返す。
     * @param options 注釈プロセッサのオプション一覧
     * @return 解析結果
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     * @throws OperatorCompilerException 引数の解析に失敗した場合
     */
    public static OperatorCompilerOptions parse(Map<String, String> options) {
        Precondition.checkMustNotBeNull(options, "options"); //$NON-NLS-1$
        OperatorCompilerOptions result = new OperatorCompilerOptions();
        result.serviceClassLoader = OperatorCompilerOptions.class.getClassLoader();
        result.dataModelRepository = new SpiDataModelMirrorRepository(result.serviceClassLoader);
        return result;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}'{''}'",
                getClass().getSimpleName());
    }

    /**
     * Returns the class loader to load compiler plug-ins.
     * @return a compiler plug-ins loader
     */
    public ClassLoader getServiceClassLoader() {
        return serviceClassLoader;
    }

    /**
     * Returns the data model repository.
     * @return the data model repository
     */
    public DataModelMirrorRepository getDataModelRepository() {
        return dataModelRepository;
    }
}
