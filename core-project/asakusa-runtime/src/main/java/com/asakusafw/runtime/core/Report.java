/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import java.io.IOException;
import java.text.MessageFormat;

import com.asakusafw.runtime.core.api.ApiStub;
import com.asakusafw.runtime.core.api.ReportApi;
import com.asakusafw.runtime.core.legacy.LegacyReport;
import com.asakusafw.runtime.core.legacy.RuntimeResource;

/**
 * Report API entry class.
 * The Report API enables to notify some messages in operator methods, to the runtime reporting system
 * (e.g. logger, standard output, or etc.).
 * Generally, the Report API does not have any effect on the batch execution, for example, the batch execution will
 * continue even if {@link Report#error(String)} is invoked.
 * Clients should put <code>&#64;Sticky</code> annotation for operator methods using this API, otherwise the Asakusa
 * DSL compiler optimization may remove the target operator.
<pre><code>
&#64;Sticky
&#64;Update
public void updateWithReport(Hoge hoge) {
    if (hoge.getValue() &lt; 0) {
        Report.error("invalid value");
    } else {
        hoge.setValue(0);
    }
}
</code></pre>
 * @since 0.1.0
 * @version 0.9.0
 */
public final class Report {

    /**
     * The Hadoop property name of the custom implementation class name of {@link Report.Delegate}.
     * To use a default implementation, clients should set {@code com.asakusafw.runtime.core.Report$Default} to it.
     */
    public static final String K_DELEGATE_CLASS = "com.asakusafw.runtime.core.Report.Delegate"; //$NON-NLS-1$

    private static final ApiStub<ReportApi> STUB = new ApiStub<>(LegacyReport.API);

    private Report() {
        return;
    }

    /**
     * Reports an <em>informative</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @throws Report.FailedException if error was occurred while reporting tReportessage
     * @see Report
     */
    public static void info(String message) {
        STUB.get().info(message);
    }

    /**
     * Reports an <em>informative</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @param throwable the optional exception object (nullable)
     * @throws Report.FailedException if error was occurred while reporting the message
     * @see Report
     * @since 0.5.1
     */
    public static void info(String message, Throwable throwable) {
        STUB.get().info(message, throwable);
    }

    /**
     * Reports a <em>warning</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @throws Report.FailedException if error was occurred while reporting the message
     * @see Report
     */
    public static void warn(String message) {
        STUB.get().warn(message);
    }

    /**
     * Reports a <em>warning</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @param throwable the optional exception object (nullable)
     * @throws Report.FailedException if error was occurred while reporting the message
     * @see Report
     * @since 0.5.1
     */
    public static void warn(String message, Throwable throwable) {
        STUB.get().warn(message, throwable);
    }

    /**
     * Reports an <em>error</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * Please be careful that this method will <em>NOT</em> shutdown the running batch.
     * To shutdown the batch, throw an exception ({@link RuntimeException}) in operator methods.
     * @param message the message
     * @throws Report.FailedException if error was occurred while reporting the message
     * @see Report
     */
    public static void error(String message) {
        STUB.get().error(message);
    }

    /**
     * Reports an <em>error</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * Please be careful that this method will <em>NOT</em> shutdown the running batch.
     * To shutdown the batch, throw an exception ({@link RuntimeException}) in operator methods.
     * @param message the message
     * @param throwable the optional exception object (nullable)
     * @throws Report.FailedException if error was occurred while reporting the message
     * @see Report
     * @since 0.5.1
     */
    public static void error(String message, Throwable throwable) {
        STUB.get().error(message, throwable);
    }

    /**
     * Returns the API stub.
     * Application developer must not use this directly.
     * @return the API stub
     * @since 0.9.0
     */
    public static ApiStub<ReportApi> getStub() {
        return STUB;
    }

    /**
     * {@link FailedException} is thrown when an exception was occurred while processing messages in {@link Report}.
     */
    public static class FailedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance.
         */
        public FailedException() {
            super();
        }

        /**
         * Creates a new instance.
         * @param message the exception message (nullable)
         * @param cause the original cause (nullable)
         */
        public FailedException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Creates a new instance.
         * @param message the exception message (nullable)
         */
        public FailedException(String message) {
            super(message);
        }

        /**
         * Creates a new instance.
         * @param cause the original cause (nullable)
         */
        public FailedException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * An abstract super class of delegation objects for {@link Report}.
     * Application developers can inherit this class, and set the fully qualified name to the property
     * {@link Report#K_DELEGATE_CLASS} to use the custom implementation for the Report API.
     * @since 0.1.0
     * @version 0.7.4
     */
    public abstract static class Delegate implements RuntimeResource {

        /**
         * Notifies a report.
         * @param level report level
         * @param message report message
         * @throws IOException if failed to notify this report by I/O error
         */
        public abstract void report(Level level, String message) throws IOException;

        /**
         * Notifies a report.
         * @param level report level
         * @param message report message
         * @param throwable optional exception info (nullable)
         * @throws IOException if failed to notify this report by I/O error
         * @since 0.5.1
         */
        public void report(Level level, String message, Throwable throwable) throws IOException {
            report(level, message);
        }
    }

    /**
     * Represents levels of reporting.
     */
    public enum Level {

        /**
         * Informative level.
         */
        INFO,

        /**
         * Warning level.
         */
        WARN,

        /**
         * Erroneous level.
         */
        ERROR,
    }

    /**
     * A basic implementation of {@link Delegate}.
     * @since 0.1.0
     * @version 0.5.1
     */
    public static class Default extends Delegate {

        @Override
        public void report(Level level, String message) {
            switch (level) {
            case INFO:
                System.out.println(message);
                break;
            case WARN:
                System.err.println(message);
                new Exception("Warning").printStackTrace();
                break;
            case ERROR:
                System.err.println(message);
                new Exception("Error").printStackTrace();
                break;
            default:
                throw new AssertionError(MessageFormat.format(
                        "[{0}] {1}", //$NON-NLS-1$
                        level,
                        message));
            }
        }

        @Override
        public void report(Level level, String message, Throwable throwable) {
            switch (level) {
            case INFO:
                System.out.println(message);
                if (throwable != null) {
                    throwable.printStackTrace(System.out);
                }
                break;
            case WARN:
            case ERROR:
                System.err.println(message);
                if (throwable != null) {
                    throwable.printStackTrace(System.err);
                }
                break;
            default:
                throw new AssertionError(MessageFormat.format(
                        "[{0}] {1}", //$NON-NLS-1$
                        level,
                        message));
            }
        }
    }
}
