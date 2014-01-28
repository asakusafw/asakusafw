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
package com.asakusafw.compiler.testing;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import com.asakusafw.compiler.flow.Packager;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.packager.FilePackager;
import com.asakusafw.compiler.repository.SpiDataClassRepository;
import com.asakusafw.compiler.repository.SpiExternalIoDescriptionProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowElementProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowGraphRewriterRepository;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * フロー部品やジョブフローを直接コンパイルして、JARのパッケージを作成する。
 * @since 0.1.0
 * @version 0.4.0
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
        List<ResourceRepository> repositories = createRepositories(serviceClassLoader, extraResources);
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
        List<StageInfo> stages = Lists.create();
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
        if (localWorkingDirectory.exists()) {
            LOG.info("Cleaning local working directory: {}", localWorkingDirectory);
        }
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
            ClassLoader classLoader,
            List<File> extraResources) throws IOException {
        assert classLoader != null;
        assert extraResources != null;
        List<File> targets = Lists.create();
        targets.addAll(collectLibraryPathsFromMarker(classLoader));
        targets.addAll(extraResources);
        List<ResourceRepository> results = Lists.create();
        Set<File> saw = Sets.create();
        for (File file : targets) {
            LOG.debug("Preparing fragment resource: {}", file);
            File canonical = file.getAbsoluteFile().getCanonicalFile();
            if (saw.contains(canonical)) {
                LOG.debug("Skipped duplicated Fragment resource: {}", file);
                continue;
            }
            saw.add(file);
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
        config.setBuildId(UUID.randomUUID().toString());
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

    private static List<File> collectLibraryPathsFromMarker(ClassLoader classLoader) throws IOException {
        assert classLoader != null;
        String path = Packager.FRAGMENT_MARKER_PATH.toPath('/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> results = Lists.create();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            LOG.debug("Fragment marker found: {}", url);
            File library = findLibraryFromUrl(url, path);
            if (library != null) {
                LOG.info(MessageFormat.format(
                        "フラグメントクラスライブラリを取り込みます: {0}",
                        library));
                results.add(library);
            }
        }
        return results;
    }

    private static File findLibraryPathFromClass(Class<?> aClass) {
        assert aClass != null;
        String className = aClass.getName();
        int start = className.lastIndexOf('.') + 1;
        String name = className.substring(start);
        URL resource = aClass.getResource(name + ".class");
        if (resource == null) {
            LOG.warn("Failed to locate the class file: {}", aClass.getName());
            return null;
        }
        String resourcePath = className.replace('.', '/') + ".class";
        return findLibraryFromUrl(resource, resourcePath);
    }

    private static File findLibraryFromUrl(URL resource, String resourcePath) {
        assert resource != null;
        assert resourcePath != null;
        String protocol = resource.getProtocol();
        if (protocol.equals("file")) {
            File file = new File(resource.getPath());
            return toClassPathRoot(file, resourcePath);
        }
        if (protocol.equals("jar")) {
            String path = resource.getPath();
            return toClassPathRoot(path, resourcePath);
        } else {
            LOG.warn("Failed to locate the library path (unsupported protocol {}): {}",
                    resource,
                    resourcePath);
            return null;
        }
    }

    private static File toClassPathRoot(File resourceFile, String resourcePath) {
        assert resourceFile != null;
        assert resourcePath != null;
        assert resourceFile.isFile();
        File current = resourceFile.getParentFile();
        assert current != null && current.isDirectory() : resourceFile;
        for (int start = resourcePath.indexOf('/'); start >= 0; start = resourcePath.indexOf('/', start + 1)) {
            current = current.getParentFile();
            if (current == null || current.isDirectory() == false) {
                LOG.warn("Failed to locate the library path: {} ({})",
                        resourceFile,
                        resourcePath);
                return null;
            }
        }
        return current;
    }

    private static File toClassPathRoot(String uriQualifiedPath, String resourceName) {
        assert uriQualifiedPath != null;
        assert resourceName != null;
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
                    resourceName),
                    e);
            throw new UnsupportedOperationException(qualifier, e);
        }
        if (archive.getScheme().equals("file") == false) {
            LOG.warn("Failed to locate the library path (unsupported protocol {}): {}",
                    archive,
                    resourceName);
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
