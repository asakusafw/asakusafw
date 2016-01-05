/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides lock feature.
 * This feature should be used only for controlling simultaneous executions.
 * Ordinally, locks are released when the JVM is terminated.
 * @since 0.2.3
 */
public abstract class ExecutionLockProvider implements Service {

    static final Logger LOG = LoggerFactory.getLogger(ExecutionLockProvider.class);

    /**
     * The configuration key name of the lock scope.
     * This must be one of {@link ExecutionLock.Scope}.
     * If this key is not specified, a default scope is selected.
     */
    public static final String KEY_SCOPE = "scope";

    private volatile ExecutionLock.Scope scope;

    @Override
    public final void configure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        try {
            configureScope(profile);
            doConfigure(profile);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure \"{0}\" ({1})",
                    profile.getPrefix(),
                    profile.getPrefix()), e);
        }
    }

    private void configureScope(ServiceProfile<?> profile) throws IOException {
        assert profile != null;
        String scopeSymbol = profile.getConfiguration(KEY_SCOPE, false, true);
        if (scopeSymbol == null) {
            scope = ExecutionLock.Scope.getDefault();
            LOG.debug("Lock scope is not defined, use default: {}",
                    scope.getSymbol());
        } else {
            scope = ExecutionLock.Scope.findFromSymbol(scopeSymbol);
            if (scope == null) {
                throw new IOException(MessageFormat.format(
                        "Unknown lock scope in \"{0}.{1}\": {2}",
                        profile.getPrefix(),
                        KEY_SCOPE,
                        scopeSymbol));
            }
        }
    }

    /**
     * Configures this provider internally (extention point).
     * @param profile the profile of this service
     * @throws InterruptedException if interrupted in configuration
     * @throws IOException if failed to configure this service
     */
    protected abstract void doConfigure(ServiceProfile<?> profile) throws InterruptedException, IOException;

    private ExecutionLock.Scope getScope() {
        if (scope == null) {
            throw new IllegalStateException();
        }
        return scope;
    }

    /**
     * Creates a new lock manager.
     * This operation may acquire a lock for the specified batch (only if the configured scope is supported).
     * @param batchId the current batch ID
     * @return the created instance
     * @throws IOException if failed to acquire the lock
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public final ExecutionLock newInstance(String batchId) throws IOException {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        return newInstance(getScope(), batchId);
    }

    /**
     * Creates a new lock manager.
     * @param lockScope target scope
     * @param batchId the current batch ID
     * @return the created instance
     * @throws IOException if failed to acquire the lock
     */
    protected abstract ExecutionLock newInstance(
            ExecutionLock.Scope lockScope,
            String batchId) throws IOException;
}
