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

import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;

/**
 * コンパイル済みのジョブフローの情報。
 */
public class CompiledJobflow {

    private List<ExternalIoCommandProvider> commands;

    private List<CompiledStage> prologueStages;

    private List<CompiledStage> epilogueStages;

    /**
     * インスタンスを生成する。
     * @param commands 提供するコマンドの一覧
     * @param prologueStages プロローグステージの一覧
     * @param epilogueStages エピローグステージの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledJobflow(
            List<ExternalIoCommandProvider> commands,
            List<CompiledStage> prologueStages,
            List<CompiledStage> epilogueStages) {
        Precondition.checkMustNotBeNull(commands, "commands"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(prologueStages, "prologueStages"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(epilogueStages, "epilogueStages"); //$NON-NLS-1$
        this.commands = commands;
        this.prologueStages = prologueStages;
        this.epilogueStages = epilogueStages;
    }

    /**
     * 提供するコマンドの一覧を返す。
     * @return 提供するコマンドの一覧
     */
    public List<ExternalIoCommandProvider> getCommandProviders() {
        return commands;
    }

    /**
     * プロローグステージの一覧を返す。
     * @return プロローグステージの一覧
     */
    public List<CompiledStage> getPrologueStages() {
        return prologueStages;
    }

    /**
     * エピローグステージの一覧を返す。
     * @return エピローグステージの一覧
     */
    public List<CompiledStage> getEpilogueStages() {
        return epilogueStages;
    }
}
