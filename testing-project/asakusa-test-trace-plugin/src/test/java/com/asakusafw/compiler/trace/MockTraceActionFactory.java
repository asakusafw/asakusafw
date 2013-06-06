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
package com.asakusafw.compiler.trace;

import java.io.IOException;

import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.trace.TraceAction;
import com.asakusafw.runtime.trace.TraceActionFactory;
import com.asakusafw.runtime.trace.TraceContext;

/**
 * Mock implementation of {@link TraceActionFactory}.
 */
public class MockTraceActionFactory implements TraceActionFactory {

    @Override
    public TraceAction createTracepointTraceAction(ResourceConfiguration configuration, TraceContext context) {
        return new MockTraceAction(context);
    }

    @Override
    public TraceAction createErrorTraceAction(ResourceConfiguration configuration) {
        return new MockErrorTraceAction();
    }

    static class MockTraceAction implements TraceAction {

        private final TraceContext context;

        MockTraceAction(TraceContext context) {
            this.context = context;
        }

        @Override
        public void trace(Object data) throws IOException, InterruptedException {
            System.out.printf("[[MOCK-TRACE]] %s - %s%n", context, data);
        }

        @Override
        public void close() {
            return;
        }
    }

    static class MockErrorTraceAction implements TraceAction {

        @Override
        public void trace(Object data) throws IOException, InterruptedException {
            System.out.printf("[[MOCK-ERROR-TRACE]]%n");
            ((Throwable) data).printStackTrace(System.out);
        }

        @Override
        public void close() {
            return;
        }
    }
}
