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
package com.asakusafw.runtime.core.legacy;

import java.io.IOException;

import com.asakusafw.runtime.core.Report;
import com.asakusafw.runtime.core.Report.Delegate;
import com.asakusafw.runtime.core.Report.FailedException;
import com.asakusafw.runtime.core.Report.Level;
import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.core.api.ReportApi;

/**
 * A legacy implementation of Report API entry class.
 * @since 0.9.0
 * @see Report
 */
public final class LegacyReport {

    /**
     * The API of this implementation.
     */
    public static final ReportApi API = new ReportApi() {
        @Override
        public void info(String message, Throwable throwable) {
            LegacyReport.info(message, throwable);
        }
        @Override
        public void info(String message) {
            LegacyReport.info(message);
        }
        @Override
        public void warn(String message, Throwable throwable) {
            LegacyReport.warn(message, throwable);
        }
        @Override
        public void warn(String message) {
            LegacyReport.warn(message);
        }
        @Override
        public void error(String message, Throwable throwable) {
            LegacyReport.error(message, throwable);
        }
        @Override
        public void error(String message) {
            LegacyReport.error(message);
        }
    };

    private static final ThreadLocal<Delegate> DELEGATE = ThreadLocal.withInitial(() -> {
        throw new FailedException("Report is not initialized (report plugin may be not registered)");
    });

    /**
     * Sets a custom implementation for the current thread.
     * After the implementation was set, each report API method will be redirected to the implementation.
     * Application developers should not use this method directly.
     * @param delegate the custom implementation, or {@code null} to unregister the implementation
     */
    public static void setDelegate(Delegate delegate) {
        if (delegate == null) {
            DELEGATE.remove();
        } else {
            DELEGATE.set(delegate);
        }
    }

    /**
     * Reports an <em>informative</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @throws FailedException if error was occurred while reporting the message
     */
    public static void info(String message) {
        try {
            DELEGATE.get().report(Level.INFO, message);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * Reports an <em>informative</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @param throwable the optional exception object (nullable)
     * @throws FailedException if error was occurred while reporting the message
     */
    public static void info(String message, Throwable throwable) {
        try {
            DELEGATE.get().report(Level.INFO, message, throwable);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * Reports a <em>warning</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @throws FailedException if error was occurred while reporting the message
     */
    public static void warn(String message) {
        try {
            DELEGATE.get().report(Level.WARN, message);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * Reports a <em>warning</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @param throwable the optional exception object (nullable)
     * @throws FailedException if error was occurred while reporting the message
     */
    public static void warn(String message, Throwable throwable) {
        try {
            DELEGATE.get().report(Level.WARN, message, throwable);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * Reports an <em>error</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * Please be careful that this method will <em>NOT</em> shutdown the running batch.
     * To shutdown the batch, throw an exception ({@link RuntimeException}) in operator methods.
     * @param message the message
     * @throws FailedException if error was occurred while reporting the message
     */
    public static void error(String message) {
        try {
            DELEGATE.get().report(Level.ERROR, message);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * Reports an <em>error</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * Please be careful that this method will <em>NOT</em> shutdown the running batch.
     * To shutdown the batch, throw an exception ({@link RuntimeException}) in operator methods.
     * @param message the message
     * @param throwable the optional exception object (nullable)
     * @throws FailedException if error was occurred while reporting the message
     */
    public static void error(String message, Throwable throwable) {
        try {
            DELEGATE.get().report(Level.ERROR, message, throwable);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * An initializer for {@link Delegate}.
     */
    public static class Initializer extends RuntimeResource.DelegateRegisterer<Delegate> {

        @Override
        protected String getClassNameKey() {
            return Report.K_DELEGATE_CLASS;
        }

        @Override
        protected Class<? extends Delegate> getInterfaceType() {
            return Delegate.class;
        }

        @Override
        protected void register(Delegate delegate, ResourceConfiguration configuration) throws IOException,
                InterruptedException {
            delegate.setup(configuration);
            setDelegate(delegate);
        }

        @Override
        protected void unregister(Delegate delegate, ResourceConfiguration configuration) throws IOException,
                InterruptedException {
            setDelegate(null);
            delegate.cleanup(configuration);
        }
    }

    private LegacyReport() {
        throw new AssertionError();
    }
}
