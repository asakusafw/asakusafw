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

import java.io.IOException;

import org.junit.Rule;
import org.xerial.snappy.Snappy;

import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.runtime.mapreduce.simple.SimpleJobRunner;
import com.asakusafw.runtime.stage.inprocess.InProcessStageConfigurator;
import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;
import com.asakusafw.runtime.workaround.RuntimeWorkaround;
import com.asakusafw.runtime.workaround.snappyjava.MacSnappyJavaWorkaround;
import com.asakusafw.testdriver.compiler.ArtifactMirror;
import com.asakusafw.testdriver.compiler.TaskMirror;
import com.asakusafw.testdriver.compiler.basic.BasicHadoopTaskMirror;
import com.asakusafw.testdriver.compiler.basic.BasicPortMirror;
import com.asakusafw.testdriver.testing.compiler.MockCompilerToolkit;
import com.asakusafw.testdriver.testing.dsl.SimpleBatch;
import com.asakusafw.testdriver.testing.dsl.SimpleBatchAction;
import com.asakusafw.testdriver.testing.dsl.SimpleExporter;
import com.asakusafw.testdriver.testing.dsl.SimpleImporter;
import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.utils.io.Sources;

/**
 * Common super class of DSL element testers.
 */
public abstract class TesterTestRoot {

    private static final Class<?>[] LIBRARY_CLASSES = new Class<?>[] {
        InProcessStageConfigurator.class,
        SimpleJobRunner.class,
        Sources.class,
        RuntimeWorkaround.class,
    };

    /**
     * The compiler toolkit for this test.
     */
    protected final MockCompilerToolkit compiler = new MockCompilerToolkit();

    /**
     * Temporary framework installation target.
     */
    @Rule
    public final FrameworkDeployer framework = new FrameworkDeployer() {
        @Override
        protected void deploy() throws IOException {
            installTo(this);
        }
    };

    /**
     * Temporary compiler.
     */
    @Rule
    public final TempopraryCompiler tempopraryCompiler = new TempopraryCompiler(compiler);

    /**
     * Returns an artifact of {@link SimpleBatch}.
     * @return the created artifact
     * @throws IOException if failed
     */
    public final ArtifactMirror getSimpleArtifact() throws IOException {
        return getSimpleArtifact("simple", "simple", "simple");
    }

    /**
     * Returns an artifact of {@link SimpleBatch}.
     * @param flowId the flow ID
     * @param input the input name
     * @param output the output name
     * @return the created artifact
     * @throws IOException if failed
     */
    public final ArtifactMirror getSimpleArtifact(String flowId, String input, String output) throws IOException {
        return tempopraryCompiler.artifact(
                flowId,
                jobflow -> {
                    jobflow.addTask(
                            TaskMirror.Phase.MAIN,
                            new BasicHadoopTaskMirror(SimpleBatchAction.class.getName()));
                    jobflow.addInput(new BasicPortMirror<>(input, Simple.class, new SimpleImporter()));
                    jobflow.addOutput(new BasicPortMirror<>(output, Simple.class, new SimpleExporter()));
                },
                SimpleBatchAction.class,
                ApplicationLauncher.class,
                Snappy.class,
                MacSnappyJavaWorkaround.class);
    }

    /**
     * Installs helper libraries via the framework deployer.
     * @param framework the deployer
     * @throws IOException if failed to install libraries
     */
    public static void installTo(FrameworkDeployer framework) throws IOException {
        for (Class<?> aClass : LIBRARY_CLASSES) {
            framework.deployLibrary(aClass, String.format("ext/lib/%s.jar", aClass.getSimpleName()));
        }
    }
}
