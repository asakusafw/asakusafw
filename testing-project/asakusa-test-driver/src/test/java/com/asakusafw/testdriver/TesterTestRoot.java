/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.runtime.mapreduce.simple.SimpleJobRunner;
import com.asakusafw.runtime.stage.inprocess.InProcessStageConfigurator;
import com.asakusafw.utils.io.Sources;

/**
 * Common super class of DSL element testers.
 */
public abstract class TesterTestRoot {

    private static final Class<?>[] LIBRARY_CLASSES = new Class<?>[] {
        InProcessStageConfigurator.class,
        SimpleJobRunner.class,
        Sources.class,
    };

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
