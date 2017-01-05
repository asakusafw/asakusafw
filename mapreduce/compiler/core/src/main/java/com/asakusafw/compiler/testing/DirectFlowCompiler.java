/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * Compiles jobflow/flow-part classes and generates batch applications.
 * @since 0.1.0
 * @version 0.6.0
 */
public final class DirectFlowCompiler {

    static final Logger LOG = LoggerFactory.getLogger(DirectFlowCompiler.class);

    /**
     * Compiles the target batch class and returns its structural information.
     * @param flowGraph the target flow graph
     * @param batchId the target batch ID
     * @param flowId the target flow ID
     * @param basePackageName the base package name of generated Java source files
     * @param clusterWorkingDirectory the runtime working directory
     * @param localWorkingDirectory the working directory for compiler
     * @param extraResources the extra resources for embedding contents into each jobflow package file
     * @param serviceClassLoader the class loader for loading compiler services
     * @param flowCompilerOptions the compiler options for flow DSL
     * @return the compile results
     * @throws IOException if failed to compile
     * @throws IllegalArgumentException if the parameters are {@code null}
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
            delete(localWorkingDirectory);
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
     * Returns the structural information of the jobflow from a compiled one.
     * @param jobflow the target jobflow (compiled)
     * @param sourceBundle the jobflow source package
     * @param packageFile the jobflow package
     * @return the structural information
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static JobflowInfo toInfo(
            JobflowModel jobflow,
            File sourceBundle,
            File packageFile) {
        Precondition.checkMustNotBeNull(jobflow, "jobflow"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(sourceBundle, "sourceBundle"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(packageFile, "packageFile"); //$NON-NLS-1$
        List<StageInfo> stages = new ArrayList<>();
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

    private static boolean delete(File target) {
        assert target != null;
        boolean success = true;
        if (target.isDirectory()) {
            for (File child : list(target)) {
                success &= delete(child);
            }
        }
        success &= target.delete();
        return success;
    }

    private static List<File> list(File file) {
        return Optional.ofNullable(file.listFiles())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    static List<ResourceRepository> createRepositories(
            ClassLoader classLoader,
            List<File> extraResources) throws IOException {
        assert classLoader != null;
        assert extraResources != null;
        List<File> targets = new ArrayList<>();
        targets.addAll(collectLibraryPathsFromMarker(classLoader));
        targets.addAll(extraResources);
        List<ResourceRepository> results = new ArrayList<>();
        Set<File> saw = new HashSet<>();
        for (File file : targets) {
            LOG.debug("Preparing fragment resource: {}", file); //$NON-NLS-1$
            File canonical = file.getAbsoluteFile().getCanonicalFile();
            if (saw.contains(canonical)) {
                LOG.debug("Skipped duplicated Fragment resource: {}", file); //$NON-NLS-1$
                continue;
            }
            saw.add(file);
            if (file.isDirectory()) {
                results.add(new FileRepository(file));
            } else if (file.isFile() && file.getName().endsWith(".zip")) { //$NON-NLS-1$
                results.add(new ZipRepository(file));
            } else if (file.isFile() && file.getName().endsWith(".jar")) { //$NON-NLS-1$
                results.add(new ZipRepository(file));
            } else {
                LOG.warn(MessageFormat.format(
                        Messages.getString("DirectFlowCompiler.warnIgnoredUnknownFormat"), //$NON-NLS-1$
                        file));
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
     * Returns the library path which contains the target class.
     * @param memberClass the target class
     * @return the related library path, or {@code null} if it is not found
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static File toLibraryPath(Class<?> memberClass) {
        Precondition.checkMustNotBeNull(memberClass, "memberClass"); //$NON-NLS-1$
        return findLibraryPathFromClass(memberClass);
    }

    private static List<File> collectLibraryPathsFromMarker(ClassLoader classLoader) throws IOException {
        assert classLoader != null;
        String path = Packager.FRAGMENT_MARKER_PATH.toPath('/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> results = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            LOG.debug("Fragment marker found: {}", url); //$NON-NLS-1$
            File library = findLibraryFromUrl(url, path);
            if (library != null) {
                LOG.info(MessageFormat.format(
                        Messages.getString("DirectFlowCompiler.infoLoadFragmentClassLibrary"), //$NON-NLS-1$
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
        URL resource = aClass.getResource(name + ".class"); //$NON-NLS-1$
        if (resource == null) {
            LOG.warn(MessageFormat.format(
                    Messages.getString("DirectFlowCompiler.warnFailedToLocateClassFile"), //$NON-NLS-1$
                    aClass.getName()));
            return null;
        }
        String resourcePath = className.replace('.', '/') + ".class"; //$NON-NLS-1$
        return findLibraryFromUrl(resource, resourcePath);
    }

    private static File findLibraryFromUrl(URL resource, String resourcePath) {
        assert resource != null;
        assert resourcePath != null;
        String protocol = resource.getProtocol();
        if (protocol.equals("file")) { //$NON-NLS-1$
            try {
                File file = new File(resource.toURI());
                return toClassPathRoot(file, resourcePath);
            } catch (URISyntaxException e) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("DirectFlowCompiler.warnInvalidLibraryUri"), //$NON-NLS-1$
                        resource), e);
                return null;
            }
        }
        if (protocol.equals("jar")) { //$NON-NLS-1$
            String path = resource.getPath();
            return toClassPathRoot(path, resourcePath);
        } else {
            LOG.warn(MessageFormat.format(
                    Messages.getString("DirectFlowCompiler.warnUnsupportedLibraryScheme"), //$NON-NLS-1$
                    protocol,
                    resourcePath));
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
                LOG.warn(MessageFormat.format(
                        Messages.getString("DirectFlowCompiler.warnUnsupportedLibraryLocation"), //$NON-NLS-1$
                        resourceFile,
                        resourcePath));
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
                    Messages.getString("DirectFlowCompiler.warnUnexpedtedLibraryPath"), //$NON-NLS-1$
                    qualifier,
                    resourceName),
                    e);
            throw new UnsupportedOperationException(qualifier, e);
        }
        if (archive.getScheme().equals("file") == false) { //$NON-NLS-1$
            LOG.warn(MessageFormat.format(
                    Messages.getString("DirectFlowCompiler.warnUnsupportedLibraryScheme"), //$NON-NLS-1$
                    archive.getScheme(),
                    archive));
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
