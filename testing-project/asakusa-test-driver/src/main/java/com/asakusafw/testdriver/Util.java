/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.compiler.ArtifactMirror;
import com.asakusafw.testdriver.compiler.BatchMirror;
import com.asakusafw.testdriver.compiler.CompilerConfiguration;
import com.asakusafw.testdriver.compiler.CompilerToolkit;
import com.asakusafw.testdriver.compiler.GraphElement;
import com.asakusafw.testdriver.compiler.JobflowMirror;
import com.asakusafw.testdriver.compiler.util.DeploymentUtil;
import com.asakusafw.testdriver.compiler.util.DeploymentUtil.DeployOption;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;

/**
 * Utilities for testers.
 * @since 0.8.0
 */
final class Util {

    static final Logger LOG = LoggerFactory.getLogger(Util.class);

    public static CompilerToolkit getToolkit(Class<?> contextClass) {
        Objects.requireNonNull(contextClass);
        return getToolkit(contextClass.getClassLoader());
    }

    public static CompilerToolkit getToolkit(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader);
        List<CompilerToolkit> candidates = new ArrayList<>();
        for (CompilerToolkit tk : ServiceLoader.load(CompilerToolkit.class, classLoader)) {
            candidates.add(tk);
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException("there are no available Asakusa DSL compilers");
        }

        CompilerToolkit result = candidates.get(0);
        if (candidates.size() >= 2) {
            List<String> conflicts = new ArrayList<>();
            for (CompilerToolkit tk : candidates) {
                conflicts.add(tk.getName());
            }
            LOG.warn(MessageFormat.format(
                    "multiple Asakusa DSL compilers are detected: {0}",
                    conflicts));
            LOG.warn(MessageFormat.format(
                    "actual compiler binding is: {0}",
                    result.getName()));
        }
        return result;
    }

    public static CompilerConfiguration getConfiguration(CompilerToolkit toolkit, TestDriverContext context) {
        CompilerConfiguration configuration = toolkit.newConfiguration();
        configuration.withClassLoader(context.getClassLoader());
        configuration.withWorkingDirectory(context.getCompilerWorkingDirectory());
        configuration.withOptimizeLevel(context.getCompilerOptimizeLevel());
        configuration.withDebugLevel(context.getCompilerDebugLevel());
        configuration.withOptions(context.getCompilerOptions());
        for (Class<?> type : context.getExtensionTypes()) {
            putExtension(context, configuration, type);
        }
        return configuration;
    }

    private static <T> void putExtension(TestDriverContext context, CompilerConfiguration conf, Class<T> type) {
        T extension = context.getExtension(type);
        conf.withExtension(type, extension);
    }

    public static JobflowMirror getJobflow(BatchMirror batch) {
        if (batch.getElements().size() != 1) {
            throw new IllegalArgumentException();
        }
        return batch.getElements().iterator().next();
    }

    public static void prepare(TestDriverContext context, BatchMirror batch, JobflowMirror jobflow) {
        context.setCurrentBatchId(batch.getBatchId());
        context.setCurrentFlowId(jobflow.getFlowId());
        context.setCurrentExecutionId(MessageFormat.format(
                "{0}-{1}-{2}", //$NON-NLS-1$
                context.getCallerClass().getSimpleName(),
                batch.getBatchId(),
                jobflow.getFlowId()));
    }

    public static void deploy(TestDriverContext context, ArtifactMirror artifact) throws IOException {
        File root = context.getBatchApplicationsInstallationPath();
        File target = new File(root, artifact.getBatch().getBatchId());
        if (root.mkdirs() == false && root.isDirectory() == false) {
            LOG.warn(MessageFormat.format(
                    Messages.getString("JobflowExecutor.warnFailedToCreateDirectory"), //$NON-NLS-1$
                    target.getAbsolutePath()));
        }
        DeploymentUtil.deploy(artifact.getContents(), target, DeployOption.DELETE_SOURCE);

        File dependenciesDest = context.getLibrariesPackageLocation(artifact.getBatch().getBatchId());
        if (dependenciesDest.exists()) {
            LOG.debug("Cleaning up dependency libraries: {}", dependenciesDest); //$NON-NLS-1$
            DeploymentUtil.delete(dependenciesDest);
        }
        File dependencies = context.getLibrariesPath();
        if (dependencies.exists()) {
            LOG.debug("Deplogying dependency libraries: {} -> {}", dependencies, dependenciesDest); //$NON-NLS-1$
            if (dependenciesDest.mkdirs() == false && dependenciesDest.isDirectory() == false) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("JobflowExecutor.warnFailedToCreateDirectory"), //$NON-NLS-1$
                        dependenciesDest.getAbsolutePath()));
            }
            for (File file : dependencies.listFiles()) {
                if (file.isFile() == false) {
                    continue;
                }
                LOG.debug("Copying a library: {} -> {}", file, dependenciesDest); //$NON-NLS-1$
                DeploymentUtil.deployToDirectory(file, dependenciesDest);
            }
        }
    }

    public static <E extends GraphElement<E>> List<E> sort(Collection<? extends E> elements) {
        Graph<E> graph = Graphs.newInstance();
        for (E element : elements) {
            graph.addNode(element);
            graph.addEdges(element, element.getBlockers());
        }
        return Graphs.sortPostOrder(graph);
    }

    private Util() {
        return;
    }
}
