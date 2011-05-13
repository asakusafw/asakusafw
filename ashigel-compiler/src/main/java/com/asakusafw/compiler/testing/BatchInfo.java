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
package com.asakusafw.compiler.testing;

import java.io.File;
import java.util.List;

import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.common.Precondition;


/**
 * テストで利用するバッチの構成情報。
 */
public class BatchInfo {

    private final Workflow workflow;

    private final List<JobflowInfo> jobflows;

    private final File output;

    /**
     * インスタンスを生成する。
     * @param workflow 元となったワークフロー
     * @param output 成果物の出力先ディレクトリ
     * @param jobflows バッチに含まれるジョブフローの実行順序
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public BatchInfo(
            Workflow workflow,
            File output,
            List<JobflowInfo> jobflows) {
        Precondition.checkMustNotBeNull(workflow, "workflow"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(jobflows, "jobflows"); //$NON-NLS-1$
        this.workflow = workflow;
        this.output = output;
        this.jobflows = jobflows;
    }

    /**
     * Returns a {@link JobflowInfo} object in this batch which has the specified {@code Flow ID}.
     * @param flowId target Flow ID
     * @return the found {@link JobflowInfo}, or {@code null} if no such a jobflow exists
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JobflowInfo findJobflow(String flowId) {
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        for (JobflowInfo info : jobflows) {
            if (info.getJobflow().getFlowId().equals(flowId)) {
                return info;
            }
        }
        return null;
    }

    /**
     * 成果物の出力先ディレクトリを返す。
     * @return 成果物の出力先ディレクトリ
     */
    public File getOutputDirectory() {
        return output;
    }

    /**
     * 元となったワークフローを返す。
     * @return 元となったワークフロー
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * バッチに含まれるジョブフローの実行順序を返す。
     * @return バッチに含まれるジョブフローの実行順序
     */
    public List<JobflowInfo> getJobflows() {
        return this.jobflows;
    }
}
