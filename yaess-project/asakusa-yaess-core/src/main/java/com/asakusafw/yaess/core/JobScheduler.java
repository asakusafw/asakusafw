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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/**
 * Executes a series of {@link Job}s.
 * @since 0.2.3
 */
public abstract class JobScheduler implements Service {

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
     * Terminate execution on exception occurred.
     */
    public static final ErrorHandler STRICT = new ErrorHandler() {
        @Override
        public boolean handle(ExecutionContext context, IOException exception) {
            return false;
        }
    };

    /**
     * Continue execution on exception occurred.
     */
    public static final ErrorHandler BEST_EFFORT = new ErrorHandler() {
        @Override
        public boolean handle(ExecutionContext context, IOException exception) {
            return true;
        }
    };

    /**
     * Executes a series of jobs.
     * @param monitor the progress monitor of the execution
     * @param context current execution context
     * @param jobs jobs to be executed
     * @param errorHandler handles errors during each job execution
     * @throws InterruptedException if the execution was interrupted
     * @throws IOException if failed to execute the jobs
     */
    public abstract void execute(
            PhaseMonitor monitor,
            ExecutionContext context,
            List<? extends Job> jobs,
            ErrorHandler errorHandler) throws InterruptedException, IOException;

    /**
     * Handles errors during
     * {@link JobScheduler#execute(PhaseMonitor, ExecutionContext, List, ErrorHandler) each job execution}.
     * @since 0.2.3
     */
    public abstract static class ErrorHandler {

        /**
         * Invoked when failed to execute the job in
         * {@link JobScheduler#execute(PhaseMonitor, ExecutionContext, List, ErrorHandler)}.
         * Each {@link JobScheduler scheduler} <em>MAY</em> continue each job execution
         * if this returns {@code false} (except blocked by the failed job),
         * and the scheduler <em>MUST</em> raise an exception finally.
         * Otherwise if this returns {@code false}, the scheduler <em>SHOULD</em> terminate executing jobs
         * and raise an exception immediatelly.
         * @param context current execution context
         * @param exception the occurred exception
         * @return whether the scheduler can continue suceeding jobs
         */
        public abstract boolean handle(ExecutionContext context, IOException exception);
    }
}
