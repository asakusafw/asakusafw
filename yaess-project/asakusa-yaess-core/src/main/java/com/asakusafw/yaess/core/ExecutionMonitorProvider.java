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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Provides {@link ExecutionMonitor}.
 * @since 0.2.3
 */
public abstract class ExecutionMonitorProvider implements Service {

    @Override
    public final void configure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        try {
            doConfigure(profile);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure \"{0}\" ({1})",
                    profile.getPrefix(),
                    profile.getPrefix()), e);
        }
    }

    /**
     * Configures this service internally (extention point).
     * @param profile profile of this service
     * @throws InterruptedException if interrupted this configuration
     * @throws IOException if failed to configure
     */
    protected void doConfigure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        return;
    }

    /**
     * Creates a new {@link PhaseMonitor}.
     * @param context the current execution context
     * @return the created monitor
     * @throws InterruptedException if the creation is interrupted
     * @throws IOException if failed to create a {@link PhaseMonitor}
     */
    public abstract PhaseMonitor newInstance(ExecutionContext context) throws InterruptedException, IOException;
}
