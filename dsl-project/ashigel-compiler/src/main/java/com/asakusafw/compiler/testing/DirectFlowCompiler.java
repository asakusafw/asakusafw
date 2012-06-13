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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.ResourceRepository;
import com.asakusafw.compiler.common.FileRepository;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.ZipRepository;
import com.asakusafw.compiler.flow.FlowCompiler;
import com.asakusafw.compiler.flow.FlowCompilerConfiguration;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.packager.FilePackager;
import com.asakusafw.compiler.repository.SpiDataClassRepository;
import com.asakusafw.compiler.repository.SpiExternalIoDescriptionProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowElementProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowGraphRewriterRepository;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.Models;

/**
 * フロー部品やジョブフローを直接コンパイルして、JARのパッケージを作成する。
 */
public final class DirectFlowCompiler {

    static final Logger LOG = LoggerFactory.getLogger(DirectFlowCompiler.class);

    /**
     * フロー部品やジョブフローををコンパイルして、JARのパッケージを作成する。
     * @param flowGraph 対象フローの演算子グラフ
     * @param batchId 対象バッチの識別子
     * @param flowId 対象フローの識別子
     * @param basePackageName 対象フローのプログラムを出力する既定のパッケージ名
     * @param clusterWorkingDirectory 対象フローのプログラムが利用するクラスター上のディレクトリ
     * @param localWorkingDirectory コンパイル時に利用するローカル環境のワーキングディレクトリ
     * @param extraResources 追加リソースのディレクトリまたはZIPアーカイブの一覧
     * @param serviceClassLoader サービス情報をロードするためのクラスローダ
     * @param flowCompilerOptions フローDSLコンパイラのオプション設定一覧
     * @return コンパイル結果
     * @throws IOException コンパイルに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static JobflowInfo compile(
            FlowGraph flowGraph,
            String batchId,
            String flowId,
            String basePackageName,
            Location clusterWorkingDirectory,
            File localWorkingDirectory,
            List<File> extraResources,
            ClassLoader serviceClassLoader,
            FlowCompilerOptions flowCompilerOptions) throws IOException {
        Precondition.checkMustNotBeNull(flowGraph, "flowGraph"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(batchId, "batchId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(flowId, "flowId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(clusterWorkingDirectory, "clusterWorkingDirectory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(localWorkingDirectory, "localWorkingDirectory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(extraResources, "extraResources"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(serviceClassLoader, "serviceClassLoader"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(flowCompilerOptions, "flowCompilerOptions"); //$NON-NLS-1$

        if (localWorkingDirectory.exists()) {
            clean(localWorkingDirectory);
        }
        List<ResourceRepository> repositories = createRepositories(extraResources);
        FlowCompilerConfiguration config = createConfig(
                batchId,
                flowId,
                basePackageName,
                clusterWorkingDirectory,
                localWorkingDirectory,
                repositories,
                serviceClassLoader,
                flowCompilerOptions);

        FlowCompiler compiler = new FlowCompiler(config);
        JobflowModel jobflow = compiler.compile(flowGraph);

        File jobflowSources = new File(
                localWorkingDirectory,
                Naming.getJobflowSourceBundleName(flowId));
        File jobflowPackage = new File(
                localWorkingDirectory,
                Naming.getJobflowClassPackageName(flowId));
        compiler.collectSources(jobflowSources);
        compiler.buildSources(jobflowPackage);

        return toInfo(jobflow, jobflowSources, jobflowPackage);
    }

    /**
     * コンパイル済みのジョブフローを簡易実行計画に変換して返す。
     * @param jobflow 対象のジョブフロー
     * @param sourceBundle ソースバンドルファイル
     * @param packageFile パッケージアーカイブ
     * @return 簡易実行計画
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static JobflowInfo toInfo(
            JobflowModel jobflow,
            File sourceBundle,
            File packageFile) {
        Precondition.checkMustNotBeNull(jobflow, "jobflow"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(sourceBundle, "sourceBundle"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(packageFile, "packageFile"); //$NON-NLS-1$
        List<StageInfo> stages = new ArrayList<StageInfo>();
        for (CompiledStage compiled : jobflow.getCompiled().getPrologueStages()) {
            stages.add(toInfo(compiled));
        }
        Graph<JobflowModel.Stage> depenedencies = jobflow.getDependencyGraph();
        for (JobflowModel.Stage stage : Graphs.sortPostOrder(depenedencies)) {
            stages.add(toInfo(stage.getCompiled()));
        }
        for (CompiledStage compiled : jobflow.getCompiled().getEpilogueStages()) {
            stages.add(toInfo(compiled));
        }
        return new JobflowInfo(jobflow, packageFile, sourceBundle, stages);
    }

    private static void clean(File localWorkingDirectory) {
        assert localWorkingDirectory != null;
        LOG.info("Cleaning local working directory: {}", localWorkingDirectory);
        delete(localWorkingDirectory);
    }

    private static boolean delete(File target) {
        assert target != null;
        boolean success = true;
        if (target.isDirectory()) {
            for (File child : target.listFiles()) {
                success &= delete(child);
            }
        }
        success &= target.delete();
        return success;
    }

    static List<ResourceRepository> createRepositories(
            List<File> extraResources) throws IOException {
        assert extraResources != null;
        List<ResourceRepository> results = new ArrayList<ResourceRepository>();
        for (File file : extraResources) {
            if (file.isDirectory()) {
                results.add(new FileRepository(file));
            } else if (file.isFile() && file.getName().endsWith(".zip")) {
                results.add(new ZipRepository(file));
            } else if (file.isFile() && file.getName().endsWith(".jar")) {
                results.add(new ZipRepository(file));
            } else {
                LOG.warn("{}は不明な形式のため、無視されます", file);
            }
        }
        return results;
    }

    private static FlowCompilerConfiguration createConfig(
            String batchId,
            String flowId,
            String basePackageName,
            Location baseLocation,
            File workingDirectory,
            List<? extends ResourceRepository> repositories,
            ClassLoader serviceClassLoader,
            FlowCompilerOptions flowCompilerOptions) {
        assert batchId != null;
        assert flowId != null;
        assert basePackageName != null;
        assert baseLocation != null;
        assert workingDirectory != null;
        assert repositories != null;
        assert serviceClassLoader != null;
        assert flowCompilerOptions != null;
        FlowCompilerConfiguration config = new FlowCompilerConfiguration();
        ModelFactory factory = Models.getModelFactory();
        config.setBatchId(batchId);
        config.setFlowId(flowId);
        config.setFactory(factory);
        config.setProcessors(new SpiFlowElementProcessorRepository());
        config.setExternals(new SpiExternalIoDescriptionProcessorRepository());
        config.setDataClasses(new SpiDataClassRepository());
        config.setGraphRewriters(new SpiFlowGraphRewriterRepository());
        config.setPackager(new FilePackager(workingDirectory, repositories));
        config.setRootPackageName(basePackageName);
        config.setRootLocation(baseLocation);
        config.setServiceClassLoader(serviceClassLoader);
        config.setOptions(flowCompilerOptions);
        return config;
    }

    private static StageInfo toInfo(CompiledStage stage) {
        assert stage != null;
        String className = stage.getQualifiedName().toNameString();
        return new StageInfo(className);
    }

    /**
     * 指定のクラスを含むライブラリへのパスを返す。
     * @param memberClass 対象のライブラリパス下のクラス
     * @return 対応するライブラリへのパス、不明の場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static File toLibraryPath(Class<?> memberClass) {
        Precondition.checkMustNotBeNull(memberClass, "memberClass"); //$NON-NLS-1$
        return findLibraryPathFromClass(memberClass);
    }

    private static File findLibraryPathFromClass(Class<?> aClass) {
        assert aClass != null;
        int start = aClass.getName().lastIndexOf('.') + 1;
        String name = aClass.getName().substring(start);
        URL resource = aClass.getResource(name + ".class");
        if (resource == null) {
            LOG.warn("Failed to locate the class file: {}", aClass.getName());
            return null;
        }
        String protocol = resource.getProtocol();
        if (protocol.equals("file")) {
            File file = new File(resource.getPath());
            return toClassPathRoot(aClass, file);
        }
        if (protocol.equals("jar")) {
            String path = resource.getPath();
            return toClassPathRoot(aClass, path);
        } else {
            LOG.warn("Failed to locate the library path (unsupported protocol {}): {}",
                    resource,
                    aClass.getName());
            return null;
        }
    }

    private static File toClassPathRoot(Class<?> aClass, File classFile) {
        assert aClass != null;
        assert classFile != null;
        assert classFile.isFile();
        String name = aClass.getName();
        File current = classFile.getParentFile();
        assert current != null && current.isDirectory() : classFile;
        for (int i = name.indexOf('.'); i >= 0; i = name.indexOf('.', i + 1)) {
            current = current.getParentFile();
            assert current != null && current.isDirectory() : classFile;
        }
        return current;
    }

    private static File toClassPathRoot(Class<?> aClass, String uriQualifiedPath) {
        assert aClass != null;
        assert uriQualifiedPath != null;
        int entry = uriQualifiedPath.lastIndexOf('!');
        String qualifier;
        if (entry >= 0) {
            qualifier = uriQualifiedPath.substring(0, entry);
        } else {
            qualifier = uriQualifiedPath;
        }
        URI archive;
        try {
            archive = new URI(qualifier);
        } catch (URISyntaxException e) {
            LOG.warn(MessageFormat.format(
                    "Failed to locate the JAR library file {}: {}",
                    qualifier,
                    aClass.getName()),
                    e);
            throw new UnsupportedOperationException(qualifier, e);
        }
        if (archive.getScheme().equals("file") == false) {
            LOG.warn("Failed to locate the library path (unsupported protocol {}): {}",
                    archive,
                    aClass.getName());
            return null;
        }
        File file = new File(archive);
        assert file.isFile() : file;
        return file;
    }

    private DirectFlowCompiler() {
        return;
    }
}
