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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Export;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Import;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;


/**
 * テストで利用するジョブフローの構成情報。
 */
public class JobflowInfo {

    private final File packageFile;

    private final File sourceArchive;

    private final List<StageInfo> stages;

    private final JobflowModel jobflow;

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
     * Returns a {@link ImporterDescription} object in this jobflow which has the specified {@code Input ID}.
     * @param inputId target input ID
     * @return the corresponded {@link ImporterDescription}, or {@code null} if no such input exists
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ImporterDescription findImporter(String inputId) {
        if (inputId == null) {
            throw new IllegalArgumentException("inputId must not be null"); //$NON-NLS-1$
        }
        for (Import importer : jobflow.getImports()) {
            if (inputId.equals(importer.getId())) {
                return importer.getDescription().getImporterDescription();
            }
        }
        return null;
    }

    /**
     * Returns pairs of input ID and its {@link ImporterDescription} in this jobflow.
     * @return pairs of input ID and its {@link ImporterDescription}
     */
    public Map<String, ImporterDescription> getImporterMap() {
        Map<String, ImporterDescription> results = new HashMap<String, ImporterDescription>();
        for (Import importer : jobflow.getImports()) {
            results.put(importer.getId(), importer.getDescription().getImporterDescription());
        }
        return results;
    }

    /**
     * Returns a {@link ExporterDescription} object in this jobflow which has the specified {@code Output ID}.
     * @param outputId target output ID
     * @return the corresponded {@link ExporterDescription}, or {@code null} if no such output exists
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExporterDescription findExporter(String outputId) {
        if (outputId == null) {
            throw new IllegalArgumentException("outputId must not be null"); //$NON-NLS-1$
        }
        for (Export exporter : jobflow.getExports()) {
            if (outputId.equals(exporter.getId())) {
                return exporter.getDescription().getExporterDescription();
            }
        }
        return null;
    }

    /**
     * Returns pairs of output ID and its {@link ExporterDescription} in this jobflow.
     * @return pairs of output ID and its {@link ExporterDescription}
     */
    public Map<String, ExporterDescription> getExporterMap() {
        Map<String, ExporterDescription> results = new HashMap<String, ExporterDescription>();
        for (Export exporter : jobflow.getExports()) {
            results.put(exporter.getId(), exporter.getDescription().getExporterDescription());
        }
        return results;
    }

    /**
     * Returns {@link ExternalIoCommandProvider}s for this jobflow.
     * @return {@link ExternalIoCommandProvider}s
     */
    public List<ExternalIoCommandProvider> getCommandProviders() {
        return jobflow.getCompiled().getCommandProviders();
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
