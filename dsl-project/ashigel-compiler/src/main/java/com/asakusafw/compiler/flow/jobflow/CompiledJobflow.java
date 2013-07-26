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
package com.asakusafw.compiler.flow.jobflow;

import java.util.Collection;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.IoContext;
import com.asakusafw.utils.collections.Lists;

/**
 * コンパイル済みのジョブフローの情報。
 * @since 0.1.0
 * @version 0.5.1
 */
public class CompiledJobflow {

    private static final String UNKNOWN_MODULE_NAME = "unknown";

    private final List<ExternalIoCommandProvider> commands;

    private final List<ExternalIoStage> prologueStages;

    private final List<ExternalIoStage> epilogueStages;

    /**
     * インスタンスを生成する。
     * @param commands 提供するコマンドの一覧
     * @param prologueStages プロローグステージの一覧
     * @param epilogueStages エピローグステージの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @deprecated Use {@link #CompiledJobflow(Collection, Collection, Collection)} instead
     */
    @Deprecated
    public CompiledJobflow(
            List<ExternalIoCommandProvider> commands,
            List<CompiledStage> prologueStages,
            List<CompiledStage> epilogueStages) {
        Precondition.checkMustNotBeNull(commands, "commands"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(prologueStages, "prologueStages"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(epilogueStages, "epilogueStages"); //$NON-NLS-1$
        this.commands = commands;
        this.prologueStages = blessIoContext(UNKNOWN_MODULE_NAME, prologueStages, IoContext.EMPTY);
        this.epilogueStages = blessIoContext(UNKNOWN_MODULE_NAME, epilogueStages, IoContext.EMPTY);
    }

    /**
     * Creates a new instance.
     * @param commands I/O command provides for this jobflow
     * @param prologueStages prologue stages in this jobflow
     * @param epilogueStages epilogue stages in this jobflow
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public CompiledJobflow(
            Collection<? extends ExternalIoCommandProvider> commands,
            Collection<? extends ExternalIoStage> prologueStages,
            Collection<? extends ExternalIoStage> epilogueStages) {
        Precondition.checkMustNotBeNull(commands, "commands"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(prologueStages, "prologueStages"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(epilogueStages, "epilogueStages"); //$NON-NLS-1$
        this.commands = Lists.from(commands);
        this.prologueStages = Lists.from(prologueStages);
        this.epilogueStages = Lists.from(epilogueStages);
    }

    /**
     * 提供するコマンドの一覧を返す。
     * @return 提供するコマンドの一覧
     */
    public List<ExternalIoCommandProvider> getCommandProviders() {
        return commands;
    }

    /**
     * Returns the prologue stages information in this jobflow.
     * @return the prologue stages information
     * @since 0.5.1
     */
    public List<ExternalIoStage> getPrologueIoStages() {
        return prologueStages;
    }

    /**
     * Returns the epilogue stages information in this jobflow.
     * @return the epilogue stages information
     * @since 0.5.1
     */
    public List<ExternalIoStage> getEpilogueIoStages() {
        return epilogueStages;
    }

    /**
     * プロローグステージの一覧を返す。
     * @return プロローグステージの一覧
     */
    public List<CompiledStage> getPrologueStages() {
        return unblessIoContext(prologueStages);
    }

    /**
     * エピローグステージの一覧を返す。
     * @return エピローグステージの一覧
     */
    public List<CompiledStage> getEpilogueStages() {
        return unblessIoContext(epilogueStages);
    }

    private List<ExternalIoStage> blessIoContext(String moduleName, List<CompiledStage> stages, IoContext context) {
        List<ExternalIoStage> results = Lists.create();
        for (CompiledStage stage : stages) {
            results.add(new ExternalIoStage(moduleName, stage, context));
        }
        return results;
    }

    private List<CompiledStage> unblessIoContext(List<ExternalIoStage> stages) {
        List<CompiledStage> results = Lists.create();
        for (ExternalIoStage stage : stages) {
            results.add(stage.getCompiledStage());
        }
        return results;
    }
}
