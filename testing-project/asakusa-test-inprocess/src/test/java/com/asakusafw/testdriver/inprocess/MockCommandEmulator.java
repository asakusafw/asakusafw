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
package com.asakusafw.testdriver.inprocess;

import java.io.IOException;
import java.util.List;

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * Mock implementation of {@link CommandEmulator}.
 */
public class MockCommandEmulator extends CommandEmulator {

    private static Callback callback;

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public boolean accepts(
            TestDriverContext context,
            ConfigurationFactory configurations,
            TestExecutionPlan.Command command) {
        return command.getModuleName().equals(getName());
    }

    @Override
    public void execute(
            TestDriverContext context,
            ConfigurationFactory configurations,
            TestExecutionPlan.Command command) throws IOException, InterruptedException {
        take().run(command.getCommandTokens());
    }

    private synchronized Callback take() {
        Callback result = callback;
        callback = null;
        if (result == null) {
            throw new IllegalStateException();
        }
        return result;
    }

    static void callback(Callback tool) {
        callback = tool;
    }

    interface Callback {
        void run(List<String> args) throws IOException, InterruptedException;
    }
}
