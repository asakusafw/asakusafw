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
package com.asakusafw.runtime.stage;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Job;

/**
 * Executes jobs.
 * @since 0.7.1
 */
@FunctionalInterface
public interface JobRunner {

    /**
     * Runs the target job.
     * @param job the configured job object
     * @return {@code true} if the job was successfully completed, or {@code false} otherwise
     * @throws IOException if failed to execute the job by I/O error
     * @throws InterruptedException if interrupted while executing the job
     * @throws ClassNotFoundException if failed to load related classes
     */
    boolean run(Job job) throws IOException, InterruptedException, ClassNotFoundException;
}
