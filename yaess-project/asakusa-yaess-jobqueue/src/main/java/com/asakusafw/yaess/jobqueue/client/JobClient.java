/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.yaess.jobqueue.client;

import java.io.IOException;

/**
 * An abstract interface of job queue client.
 * @since 0.2.6
 */
public interface JobClient {

    /**
     * Creates a new job.
     * @param script the job script
     * @return the corresponded job ID
     * @throws IOException if failed to create a new job
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    JobId register(JobScript script) throws IOException, InterruptedException;

    /**
     * Request to execute the registered job.
     * @param id the target job ID
     * @throws IOException if failed to request the job
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    void submit(JobId id) throws IOException, InterruptedException;

    /**
     * Obtains and returns the job information.
     * @param id the target job ID
     * @return the corresponded information
     * @throws IOException if failed to obtain the information
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    JobStatus getStatus(JobId id) throws IOException, InterruptedException;
}
