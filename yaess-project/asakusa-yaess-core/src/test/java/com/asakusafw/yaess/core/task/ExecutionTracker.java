/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.yaess.core.task;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScriptHandler;

/**
 * Tracking execution in testing.
 * @since 0.2.3
 */
@FunctionalInterface
public interface ExecutionTracker {

    /**
     * The key name of this implementation class name for handlers.
     */
    String KEY_CLASS = "tracker.class";

    /**
     * The key name of tracking ID for handlers.
     */
    String KEY_ID = "tracker.id";

    /**
     * Adds an execution script which is executed in handler.
     * @param id tracking ID
     * @param record tracking record
     * @throws IOException to fail handler's execution
     * @throws InterruptedException to fail handler's execution
     * @see ExecutionTracker.Id#get(String)
     */
    void add(Id id, Record record) throws IOException, InterruptedException;

    /**
     * A tracking record.
     */
    public class Record {

        /**
         * Current context.
         */
        public final ExecutionContext context;

        /**
         * Current script.
         */
        public final ExecutionScript script;

        /**
         * Current handler.
         */
        public final ExecutionScriptHandler<?> handler;

        /**
         * Creates a new instance.
         * @param context current context
         * @param script executed script
         * @param handler executor handler
         */
        public Record(ExecutionContext context, ExecutionScript script, ExecutionScriptHandler<?> handler) {
            this.context = context;
            this.script = script;
            this.handler = handler;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0}/{1}/{2}",
                    context.getFlowId(),
                    context.getPhase(),
                    script == null ? handler.getHandlerId() : script.getId());
        }
    }

    /**
     * Tracking ID.
     */
    public class Id {

        private static final Map<String, Id> CACHE = new HashMap<>();

        private final String token;

        /**
         * Creates a new instance.
         * @param token ID token
         */
        private Id(String token) {
            this.token = token;
        }

        /**
         * Returns a created ID for the specified token, or create a new ID if does not exist.
         * @param token ID token
         * @return identity for token
         */
        public static Id get(String token) {
            synchronized (CACHE) {
                Id cached = CACHE.get(token);
                if (cached != null) {
                    return cached;
                }
                Id id = new Id(token);
                CACHE.put(token, id);
                return id;
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + token.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Id other = (Id) obj;
            if (!token.equals(other.token)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return token;
        }
    }
}
