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
package com.asakusafw.compiler.flow.jobflow;

import com.asakusafw.compiler.common.Precondition;
import com.ashigeru.lang.java.model.syntax.Name;

/**
 * コンパイルされたステージの情報。
 */
public class CompiledStage {

    private Name qualifiedName;

    private String stageId;

    /**
     * インスタンスを生成する。
     * @param qualifiedName コンパイル結果型の完全限定名
     * @param stageId ステージ識別子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledStage(Name qualifiedName, String stageId) {
        Precondition.checkMustNotBeNull(qualifiedName, "qualifiedName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageId, "stageId"); //$NON-NLS-1$
        this.qualifiedName = qualifiedName;
        this.stageId = stageId;
    }

    /**
     * コンパイル結果型の完全限定名を返す。
     * @return コンパイル結果型の完全限定名
     */
    public Name getQualifiedName() {
        return qualifiedName;
    }

    /**
     * このステージのステージ識別子を返す。
     * @return このステージのステージ識別子
     */
    public String getStageId() {
        return stageId;
    }
}
