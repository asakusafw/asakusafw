/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

/**
 * Represents a resource used in runtime, and it has resource lifecycle.
 */
public interface FlowResource {

    /**
     * Initializes this resource.
     * @param configuration the current configuration
     * @throws IOException if failed to initialize this resource
     * @throws InterruptedException if interrupted while initializing this resource
     * @throws IllegalArgumentException if configuration is not valid
     * @throws IllegalStateException if resource lifecycle has something wrong
     */
    void setup(Configuration configuration) throws IOException, InterruptedException;

    /**
     * Finalizes this resource.
     * @param configuration the current configuration
     * @throws IOException if failed to finalizing this resource
     * @throws InterruptedException if interrupted while finalizing this resource
     * @throws IllegalArgumentException if configuration is not valid
     * @throws IllegalStateException if resource lifecycle has something wrong
     */
    void cleanup(Configuration configuration) throws IOException, InterruptedException;
}
