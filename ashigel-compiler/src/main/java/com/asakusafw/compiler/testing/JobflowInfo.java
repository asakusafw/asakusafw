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

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;


/**
 * テストで利用するジョブフローの構成情報。
 */
public class JobflowInfo {

    private File packageFile;

    private File sourceArchive;

    private List<StageInfo> stages;

    private JobflowModel jobflow;

    /**
     * インスタンスを生成する。
     * @param jobflow 元となったジョブフロー
     * @param packageArchive 構築したパッケージアーカイブ
     * @param sourceArchive 構築したソースアーカイブ
     * @param stages ステージの実行順序
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public JobflowInfo(
            JobflowModel jobflow,
            File packageArchive,
            File sourceArchive,
            List<StageInfo> stages) {
        Precondition.checkMustNotBeNull(jobflow, "jobflow"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(packageArchive, "packageArchive"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(sourceArchive, "sourceArchive"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stages, "stages"); //$NON-NLS-1$
        this.jobflow = jobflow;
        this.packageFile = packageArchive;
        this.sourceArchive = sourceArchive;
        this.stages = stages;
    }

    /**
     * 元となったジョブフローを返す。
     * @return 元となったジョブフロー
     */
    public JobflowModel getJobflow() {
        return jobflow;
    }

    /**
     * 構築したパッケージファイルを返す。
     * @return 構築したパッケージファイル
     */
    public File getPackageFile() {
        return packageFile;
    }

    /**
     * 生成したソースプログラムのアーカイブファイルを返す。
     * @return 生成したソースプログラムのアーカイブファイル
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public File getSourceArchive() {
        return sourceArchive;
    }

    /**
     * 各ステージの実行順序を返す。
     * @return ステージの実行順序
     */
    public List<StageInfo> getStages() {
        return stages;
    }
}
