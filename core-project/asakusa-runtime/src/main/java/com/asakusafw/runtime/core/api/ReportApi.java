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
package com.asakusafw.runtime.core.api;

import com.asakusafw.runtime.core.Report.FailedException;

/**
 * The Report API.
 * @since 0.1.0
 */
public interface ReportApi {

    /**
     * Reports an <em>informative</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @throws FailedException if error was occurred while reporting the message
     */
    void info(String message);

    /**
     * Reports an <em>informative</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @param throwable the optional exception object (nullable)
     * @throws FailedException if error was occurred while reporting the message
     * @since 0.5.1
     */
    void info(String message, Throwable throwable);

    /**
     * Reports a <em>warning</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @throws FailedException if error was occurred while reporting the message
     */
    void warn(String message);

    /**
     * Reports a <em>warning</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * @param message the message
     * @param throwable the optional exception object (nullable)
     * @throws FailedException if error was occurred while reporting the message
     */
    void warn(String message, Throwable throwable);

    /**
     * Reports an <em>error</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * Please be careful that this method will <em>NOT</em> shutdown the running batch.
     * To shutdown the batch, throw an exception ({@link RuntimeException}) in operator methods.
     * @param message the message
     * @throws FailedException if error was occurred while reporting the message
     */
    void error(String message);

    /**
     * Reports an <em>error</em> message.
     * Clients should put <code>&#64;Sticky</code> annotation to the operator method that using this.
     * Please be careful that this method will <em>NOT</em> shutdown the running batch.
     * To shutdown the batch, throw an exception ({@link RuntimeException}) in operator methods.
     * @param message the message
     * @param throwable the optional exception object (nullable)
     * @throws FailedException if error was occurred while reporting the message
     */
    void error(String message, Throwable throwable);
}
