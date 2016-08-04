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
package com.asakusafw.operator;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule for annotation processing.
 */
public class AnnotationProcessing extends Callback implements TestRule {

    private final AtomicBoolean proceeed = new AtomicBoolean();

    private Statement statement;

    @Override
    public Statement apply(Statement base, Description description) {
        this.statement = base;
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                OperatorCompilerTestRoot runner = new OperatorCompilerTestRoot();
                try {
                    beforeCompile(runner);
                    runner.start(AnnotationProcessing.this);
                } finally {
                    runner.tearDown();
                }
            }
        };
    }

    /**
     * Invoked before compile.
     * @param runner current runner
     */
    protected void beforeCompile(OperatorCompilerTestRoot runner) {
        return;
    }

    @Override
    protected void test() {
        if (proceeed.compareAndSet(false, true)) {
            try {
                statement.evaluate();
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new AssertionError(e);
            }
        }
    }
}
