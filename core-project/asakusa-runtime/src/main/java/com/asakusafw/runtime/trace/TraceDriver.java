/**
 * Copyright 2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.trace;

import java.io.IOException;

import com.asakusafw.runtime.core.Result;

/**
 * Manages {@link TraceAction} object.
 * @since 0.5.1
 */
public class TraceDriver {

    private final TraceAction action;

    private TraceDriver(TraceAction action) {
        this.action = action;
    }

    /**
     * Returns the driver.
     * @param context the trace context
     * @return the driver
     */
    public static TraceDriver get(TraceContext context) {
        try {
            return new TraceDriver(TraceDriverLifecycleManager.getAction(context));
        } catch (IOException e) {
            throw new Result.OutputException(e);
        } catch (InterruptedException e) {
            throw new Result.OutputException(e);
        }
    }

    /**
     * Handles error event.
     * @param info error information
     */
    public static void error(Throwable info) {
        try {
            TraceDriverLifecycleManager.error(info);
        } catch (IOException e) {
            throw new Result.OutputException(e);
        } catch (InterruptedException e) {
            throw new Result.OutputException(e);
        }
    }

    /**
     * Handles trace event.
     * @param data trace data (is usually target data-model)
     */
    public void trace(Object data) {
        try {
            action.trace(data);
        } catch (IOException e) {
            throw new Result.OutputException(e);
        } catch (InterruptedException e) {
            throw new Result.OutputException(e);
        }
    }
}
