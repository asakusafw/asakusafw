/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.util.Set;

/**
 * Represents an atomic job for YAESS.
 * @since 0.2.3
 */
public abstract class Job {

    /**
     * Executes this job.
     * @param monitor a progress monitor for this job
     * @param context current execution context
     * @throws InterruptedException if this execution is interrupted
     * @throws IOException if failed to execute this job
     */
    public abstract void execute(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException;

    /**
     * Returns the label of this job.
     * @return the label of this job
     */
    public abstract String getLabel();

    /**
     * Returns the ID of this job for other succeeding jobs.
     * @return ID of this job
     */
    public abstract String getId();

    /**
     * Returns job IDs which blocks executing this job.
     * Any schedulers can execute this job only if all blocker jobs are completed.
     * @return the blocker job IDs
     */
    public abstract Set<String> getBlockerIds();

    /**
     * Returns the resource ID which this job requires in execution.
     * @return the resource ID required in this job
     */
    public abstract String getResourceId();
}
