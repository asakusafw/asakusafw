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
package com.asakusafw.workflow.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Mock implementation of {@link TaskExecutor} that accepts {@link MockTaskInfo}.
 */
public class MockExecutor implements TaskExecutor {

    private final Predicate<? super MockTaskInfo> predicate;

    private final List<String> values = new ArrayList<>();

    /**
     * Creates a new instance.
     * @param predicate predicate of supported tasks
     */
    public MockExecutor(Predicate<? super MockTaskInfo> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return task instanceof MockTaskInfo && predicate.test((MockTaskInfo) task);
    }

    @Override
    public void execute(TaskExecutionContext context, TaskInfo task) throws InterruptedException, IOException {
        values.add(((MockTaskInfo) task).getValue());
    }

    /**
     * Returns the values.
     * @return the values
     */
    public List<String> getValues() {
        return values;
    }
}
