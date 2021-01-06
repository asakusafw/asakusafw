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
package com.asakusafw.runtime.core;

import com.asakusafw.runtime.core.api.ApiStub;
import com.asakusafw.runtime.core.api.BatchContextApi;
import com.asakusafw.runtime.core.legacy.LegacyBatchContext;

/**
 * Context API entry class.
 * The context API provides the batch arguments and others about the current batch execution.
 * Clients can use this class <em>only in operator methods</em>, not in flow, importer, nor descriptions.
 * @since 0.1.0
 * @version 0.9.0
 */
public final class BatchContext {

    private static final ApiStub<BatchContextApi> STUB = new ApiStub<>(LegacyBatchContext.API);

    private BatchContext() {
        return;
    }

    /**
     * Returns a value of the context variable (which includes batch arguments).
     * @param name the target variable name
     * @return the value of the target variable, or {@code null} if it is not defined in this context
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static String get(String name) {
        return STUB.get().get(name);
    }

    /**
     * Returns the API stub.
     * Application developer must not use this directly.
     * @return the API stub
     * @since 0.9.0
     */
    public static ApiStub<BatchContextApi> getStub() {
        return STUB;
    }
}
