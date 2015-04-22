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
package com.asakusafw.yaess.multidispatch;

import java.io.IOException;

import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.CommandScriptHandler;
import com.asakusafw.yaess.core.ExecutionContext;

/**
 * Mock {@link CommandScriptHandler}.
 */
public class FailCommandScriptHandler extends MockCommandScriptHandler {

    /**
     * Execution hook for testing.
     * @param context context
     * @param script script
     * @throws InterruptedException if interrupted
     * @throws IOException if failed
     */
    @Override
    void hook(ExecutionContext context, CommandScript script) throws InterruptedException, IOException {
        String resourceId = getResourceId(context, script);
        throw new MessageException(context, resourceId);
    }

    static final class MessageException extends IOException {

        private static final long serialVersionUID = 1L;

        final ExecutionContext context;

        final String message;

        MessageException(ExecutionContext context, String message) {
            this.context = context;
            this.message = message;
        }
    }
}
