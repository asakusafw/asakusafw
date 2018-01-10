/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.testdriver.executor;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.model.DeleteTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Accepts {@link DeleteTaskInfo} and does nothing.
 * @since 0.10.0
 */
public class VoidDeleteTaskExecutor implements TaskExecutor {

    static final Logger LOG = LoggerFactory.getLogger(VoidDeleteTaskExecutor.class);

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return task instanceof DeleteTaskInfo;
    }

    @Override
    public void execute(TaskExecutionContext context, TaskInfo task) {
        DeleteTaskInfo info = (DeleteTaskInfo) task;
        LOG.info(MessageFormat.format(
                "delete task was skipped: {0} ({1})",
                info.getPath(), info.getPathKind()));
    }
}
