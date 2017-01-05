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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.testdriver.compiler.ArtifactMirror;
import com.asakusafw.testdriver.compiler.CompilerConstants;
import com.asakusafw.testdriver.compiler.CompilerToolkit;
import com.asakusafw.testdriver.compiler.basic.BasicArtifactMirror;
import com.asakusafw.testdriver.compiler.basic.BasicBatchMirror;
import com.asakusafw.testdriver.compiler.basic.BasicJobflowMirror;
import com.asakusafw.testdriver.compiler.util.DeploymentUtil;

/**
 * Configures the
 * @since 0.9.0
 */
public class TempopraryCompiler extends ExternalResource {

    private final CompilerToolkit compiler;

    private final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Creates a new instance.
     * @param compiler the compiler in this session
     */
    public TempopraryCompiler(CompilerToolkit compiler) {
        this.compiler = compiler;
    }

    /**
     * Returns the current session compiler.
     * @return the current session compiler
     */
    public CompilerToolkit getCompiler() {
        return compiler;
    }

    /**
     * Creates a new artifact.
     * @param flowId the flow ID
     * @param configurator the jobflow configurator
     * @param members other member classes
     * @return the created artifact
     * @throws IOException if I/O error was occurred
     */
    public ArtifactMirror artifact(
            String flowId,
            Consumer<? super BasicJobflowMirror> configurator,
            Class<?>... members) throws IOException {

        BasicJobflowMirror jobflow = new BasicJobflowMirror(flowId);
        configurator.accept(jobflow);

        BasicBatchMirror batch = new BasicBatchMirror("testing");
        batch.addElement(jobflow);

        File batchapp = folder.newFolder();
        List<File> entries = Stream.of(members)
                .map(DeploymentUtil::findLibraryPathFromClass)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        File jobflowLibrary = CompilerConstants.getJobflowLibraryPath(batchapp, flowId);
        DeploymentUtil.buildFatJar(entries, jobflowLibrary);
        return new BasicArtifactMirror(batch, batchapp);
    }

    @Override
    protected void before() throws Throwable {
        Util.setToolkit(compiler);
        folder.create();
    }

    @Override
    protected void after() {
        Util.setToolkit(null);
        folder.delete();
    }
}
