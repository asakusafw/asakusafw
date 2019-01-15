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
package com.asakusafw.runtime.core.legacy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.runtime.core.BatchContext;
import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.core.api.BatchContextApi;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.util.VariableTable;

/**
 * A legacy implementation of context API entry class.
 * @since 0.9.0
 * @see BatchContext
 */
public class LegacyBatchContext {

    /**
     * The API of this implementation.
     */
    public static final BatchContextApi API = new BatchContextApi() {
        @Override
        public String get(String name) {
            return LegacyBatchContext.get(name);
        }
    };

    static final ThreadLocal<LegacyBatchContext> CONTEXTS = ThreadLocal.withInitial(() -> {
        throw new IllegalStateException("BatchContext is not yet initialized (internal error)");
    });

    private Map<String, String> variables = new HashMap<>();

    /**
     * Creates a new instance.
     * @param variables variable table
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    protected LegacyBatchContext(Map<String, String> variables) {
        if (variables == null) {
            throw new IllegalArgumentException("variables must not be null"); //$NON-NLS-1$
        }
        this.variables = new HashMap<>(variables);
    }

    /**
     * Returns a value of the context variable (which includes batch arguments).
     * @param name the target variable name
     * @return the value of the target variable, or {@code null} if it is not defined in this context
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static String get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return CONTEXTS.get().variables.get(name);
    }

    /**
     * Initializes {@link LegacyBatchContext}.
     */
    public static class Initializer implements RuntimeResource {

        @Override
        public void setup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            String arguments = configuration.get(StageConstants.PROP_ASAKUSA_BATCH_ARGS, ""); //$NON-NLS-1$
            VariableTable variables = new VariableTable(VariableTable.RedefineStrategy.IGNORE);
            variables.defineVariables(arguments);
            LegacyBatchContext context = new LegacyBatchContext(variables.getVariables());
            CONTEXTS.set(context);
        }

        @Override
        public void cleanup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            CONTEXTS.remove();
        }
    }
}
