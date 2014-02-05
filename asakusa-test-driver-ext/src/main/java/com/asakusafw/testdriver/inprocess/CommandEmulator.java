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
package com.asakusafw.testdriver.inprocess;

import java.io.IOException;

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * Executes a command job in emulation mode.
 * <p>
 * To configure testing environment, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.testdriver.inprocess.CommandEmulator}.
 * </p>
 * @since 0.6.0
 */
public abstract class CommandEmulator {

    /**
     * Returns the name of this component.
     * @return the component name
     */
    public abstract String getName();

    /**
     * Detects whether this executor accepts the target command or not.
     * @param context the current testing context
     * @param configurations the current configurations provider
     * @param command the target command
     * @return {@code true} if this executor accepts the target command, otherwise {@code false}
     */
    public abstract boolean accepts(
            TestDriverContext context,
            ConfigurationFactory configurations,
            TestExecutionPlan.Command command);

    /**
     * Executes the target command.
     * @param context the current testing context
     * @param configurations the current configurations provider
     * @param command the target command
     * @throws IOException if failed to executes the command
     * @throws InterruptedException if interrupted while preparing/executing the command
     */
    public abstract void execute(
            TestDriverContext context,
            ConfigurationFactory configurations,
            TestExecutionPlan.Command command) throws IOException, InterruptedException;
}
