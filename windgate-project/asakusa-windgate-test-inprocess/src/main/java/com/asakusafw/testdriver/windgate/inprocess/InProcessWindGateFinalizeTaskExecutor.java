/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.testdriver.windgate.inprocess;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.AbortTask;
import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.model.CommandTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * In-process {@link TaskExecutor} for {@code windgate/bin/finalize}.
 * @since 0.10.0
 */
public class InProcessWindGateFinalizeTaskExecutor implements TaskExecutor {

    static final Logger LOG = LoggerFactory.getLogger(InProcessWindGateFinalizeTaskExecutor.class);

    static final String COMMAND_SUFFIX = WindGateTaskExecutors.PATH_WINDGATE + "/bin/finalize"; //$NON-NLS-1$

    static final int ARG_BATCH_ID = WindGateTaskExecutors.ARG_PROFILE + 1;

    static final int ARG_FLOW_ID = ARG_BATCH_ID + 1;

    static final int ARG_EXECUTION_ID = ARG_FLOW_ID + 1;

    static final int MINIMUM_TOKENS = WindGateTaskExecutors.ARG_PROFILE + 1;

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return WindGateTaskExecutors.isSupported(context, task, COMMAND_SUFFIX, MINIMUM_TOKENS);
    }

    @Override
    public void execute(TaskExecutionContext context, TaskInfo task) throws InterruptedException, IOException {
        CommandTaskInfo mirror = (CommandTaskInfo) task;
        WindGateTaskExecutors.withLibraries(context, classLoadeer -> {
            GateProfile profile = WindGateTaskExecutors.loadProfile(context, classLoadeer, mirror);
            AbortTask windgate = new AbortTask(profile, context.getExecutionId());
            windgate.execute();
        });
    }
}
