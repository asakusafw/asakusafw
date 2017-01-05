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
package com.asakusafw.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.runtime.stage.StageConstants;

/**
 * Provides {@link BatchCompilingEnvironment} for testing.
 */
public class BatchCompilerEnvironmentProvider implements TestRule {

    private BatchCompilerConfiguration config;

    private BatchCompilingEnvironment environment;


    @Override
    public Statement apply(Statement base, Description description) {
        try {
            config = DirectBatchCompiler.createConfig(
                    description.getMethodName(),
                    "com.example",
                    Location.fromPath(String.format(
                            "target/testing/%s_%s",
                            description.getTestClass().getName(),
                            description.getMethodName()), '/'),
                    new File(String.format(
                            "target/testing/%s_%s/output",
                            description.getTestClass().getName(),
                            description.getMethodName())),
                    new File(String.format(
                            "target/testing/%s_%s/build",
                            description.getTestClass().getName(),
                            description.getMethodName())),
                    Arrays.asList(new File[] {
                           DirectFlowCompiler.toLibraryPath(getClass()),
                           DirectFlowCompiler.toLibraryPath(StageConstants.class),
                    }),
                    description.getTestClass().getClassLoader(),
                    FlowCompilerOptions.load(System.getProperties()));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return base;
    }

    /**
     * Returns the compiler configuration.
     * @return the compiler configuration
     */
    public BatchCompilerConfiguration getConfig() {
        return config;
    }

    /**
     * Returns the environment object.
     * @return the environment object
     */
    public BatchCompilingEnvironment getEnvironment() {
        if (environment == null) {
            environment = new BatchCompilingEnvironment(config).bless();
        }
        return environment;
    }
}
