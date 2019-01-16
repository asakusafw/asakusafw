/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.operation.tools.directio.common;

import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Handles parameters about task execution.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.operation.tools.directio.jcommander")
public class ExecutorParameter {

    static final Logger LOG = LoggerFactory.getLogger(ExecutorParameter.class);

    @Parameter(
            names = { "-p", "--parallel" },
            descriptionKey = "parameter.parallel")
    boolean forkJoin = false;

    /**
     * Returns the task execution context.
     * @return the task execution context
     */
    public Task.Context getTaskContext() {
        return forkJoin ? TaskExecutor.FORK_JOIN : TaskExecutor.DEFAULT;
    }

    /**
     * Executes the tasks.
     * @param tasks the tasks
     */
    public void execute(Task... tasks) {
        execute(Arrays.asList(tasks));
    }

    /**
     * Executes the tasks.
     * @param tasks the tasks
     */
    public void execute(Collection<? extends Task> tasks) {
        Task.Context context = getTaskContext();
        LOG.debug("task executor: {}", context);
        tasks.stream()
                .map(context::submit)
                .forEach(Task.Wait::forDone);
    }
}
