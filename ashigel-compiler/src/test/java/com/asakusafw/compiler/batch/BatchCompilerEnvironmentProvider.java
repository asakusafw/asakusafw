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
package com.asakusafw.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.runtime.stage.StageConstants;

/**
 * {@link BatchCompilingEnvironment}をテスト向けに提供する。
 */
public class BatchCompilerEnvironmentProvider implements MethodRule {

    private BatchCompilerConfiguration config;

    private BatchCompilingEnvironment environment;

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        try {
            config = DirectBatchCompiler.createConfig(
                    method.getName(),
                    "com.example",
                    Location.fromPath(String.format(
                            "target/testing/%s_%s",
                            method.getMethod().getDeclaringClass().getName(),
                            method.getMethod().getName()), '/'),
                    new File(String.format(
                            "target/testing/%s_%s/output",
                            method.getMethod().getDeclaringClass().getName(),
                            method.getMethod().getName())),
                    new File(String.format(
                            "target/testing/%s_%s/build",
                            method.getMethod().getDeclaringClass().getName(),
                            method.getMethod().getName())),
                    Arrays.asList(new File[] {
                           DirectFlowCompiler.toLibraryPath(getClass()),
                           DirectFlowCompiler.toLibraryPath(StageConstants.class),
                    }),
                    target.getClass().getClassLoader(),
                    FlowCompilerOptions.load(System.getProperties()));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return base;
    }

    /**
     * コンパイラの環境設定を返す。
     * @return コンパイラの環境設定
     */
    public BatchCompilerConfiguration getConfig() {
        return config;
    }

    /**
     * 環境オブジェクトを返す。
     * @return 環境オブジェクト
     */
    public BatchCompilingEnvironment getEnvironment() {
        if (environment == null) {
            environment = new BatchCompilingEnvironment(config).bless();
        }
        return environment;
    }
}
