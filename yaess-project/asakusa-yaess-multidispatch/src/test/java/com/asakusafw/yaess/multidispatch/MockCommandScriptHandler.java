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
package com.asakusafw.yaess.multidispatch;

import java.io.IOException;
import java.util.Map;

import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.CommandScriptHandler;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScriptHandlerBase;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Mock {@link CommandScriptHandler}.
 */
public class MockCommandScriptHandler extends ExecutionScriptHandlerBase implements CommandScriptHandler {

    @Override
    protected void doConfigure(
            ServiceProfile<?> profile,
            Map<String, String> desiredProperties,
            Map<String, String> desiredEnvironmentVariables) throws InterruptedException, IOException {
        return;
    }

    @Override
    public void execute(
            ExecutionMonitor monitor,
            ExecutionContext context,
            CommandScript script) throws InterruptedException, IOException {
        monitor.open(1);
        try {
            hook(context, script);
        } finally {
            monitor.close();
        }
    }

    @Override
    public void setUp(ExecutionMonitor monitor, ExecutionContext context) throws InterruptedException, IOException {
        hook(context, null);
    }

    @Override
    public void cleanUp(ExecutionMonitor monitor, ExecutionContext context) throws InterruptedException, IOException {
        hook(context, null);
    }

    /**
     * Execution hook for testing.
     * @param context context
     * @param script script
     * @throws InterruptedException if interrupted
     * @throws IOException if failed
     */
    void hook(ExecutionContext context, CommandScript script) throws InterruptedException, IOException {
        return;
    }
}
