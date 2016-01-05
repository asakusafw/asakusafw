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
 * Configures a {@link Job} in stages.
 * @since 0.6.0
 */
public abstract class StageConfigurator {

    /**
     * Configures the target job.
     * @param job the target {@link Job} object
     * @throws IOException if failed to configure the job
     * @throws InterruptedException if interrupted while configuring {@link Job} object
     */
    public abstract void configure(Job job) throws IOException, InterruptedException;
}
