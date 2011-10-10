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

import java.io.Closeable;
import java.io.IOException;

/**
 * Receives execution's progress.
 * @since 0.2.3
 */
public interface ExecutionMonitor extends Closeable {

    /**
     * An empty implementation.
     */
    public static final ExecutionMonitor NULL = new ExecutionMonitor() {

        @Override
        public void progressed(double deltaSize) {
            return;
        }

        @Override
        public void setProgress(double workedSize) throws IOException {
            return;
        }

        @Override
        public void open(double taskSize) {
            return;
        }

        @Override
        public void checkCancelled() throws InterruptedException {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }

        @Override
        public void close() {
            return;
        }
    };

    /**
     * Begins a task and notify this event to the corresponding receiver.
     * This method can be invoked once for each object.
     * @param taskSize the total task size
     * @throws IOException if failed to notify this event
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    void open(double taskSize) throws IOException;

    /**
     * Raise {@link InterruptedException} if cancel has been requested.
     * @throws InterruptedException if cencel requested
     */
    void checkCancelled() throws InterruptedException;

    /**
     * Progresses the current task and notify this event to the corresponding receiver.
     * @param deltaSize the difference of progressed task size
     * @throws IOException if failed to notify this event
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract void progressed(double deltaSize) throws IOException;

    /**
     * Sets the progress of current task and notify this event to the corresponding receiver.
     * @param workedSize the absolute progressed task size
     * @throws IOException if failed to notify this event
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract void setProgress(double workedSize) throws IOException;

    /**
     * Ends the current task and notify this event to the corresponding receiver.
     * @throws IOException if failed to notify this event
     */
    @Override
    public abstract void close() throws IOException;
}
