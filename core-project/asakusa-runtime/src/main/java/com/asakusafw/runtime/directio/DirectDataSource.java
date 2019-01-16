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
package com.asakusafw.runtime.directio;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * API of direct accessor to data sources.
 * Clients should not implement this interface directly.
 *
 * <h3 id="output-staging">Output Staging</h3>
 * Using this interface, output to resources is performed in the following sequence:
 * <ol>
 * <li>
 *     Prepare the staging output area in
 *     {@link #setupTransactionOutput(OutputTransactionContext)}.
 * </li>
 * <li>
 *     Prepare temporary output area for each attempt in
 *     {@link #setupAttemptOutput(OutputAttemptContext)}.
 * </li>
 * <li>
 *     Create output contents onto temporary output area in
 *     {@link #openOutput(OutputAttemptContext, DataDefinition, String, String, Counter)}.
 * </li>
 * <li>
 *     Move output contents from temporary area to staging area in
 *     {@link #commitAttemptOutput(OutputAttemptContext)}.
 * </li>
 * <li>
 *     Cleanup output contents on temporary area in
 *     {@link #cleanupAttemptOutput(OutputAttemptContext)}.
 * </li>
 * <li>
 *     Move output contents from staging area to final area in
 *     {@link #commitTransactionOutput(OutputTransactionContext)}.
 * </li>
 * <li>
 *     Cleanup output contents on staging area in
 *     {@link #cleanupTransactionOutput(OutputTransactionContext)}.
 * </li>
 * </ol>
 * @since 0.2.5
 * @version 0.10.0
 * @see AbstractDirectDataSource
 */
public interface DirectDataSource {

    /**
     * Returns a textually representation of the target path.
     * @param basePath base path of target resources
     * @return the corresponded path
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.10.0
     */
    String path(String basePath);

    /**
     * Returns a textually representation of the target path pattern.
     * @param basePath base path of target resources
     * @param resourcePattern search pattern of target resources from {@code basePath}
     * @return the corresponded path
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.8.1
     */
    String path(String basePath, ResourcePattern resourcePattern);

    /**
     * Collects input fragments about target resources.
     * @param <T> target data type
     * @param definition the data definition
     * @param basePath base path of target resources
     * @param resourcePattern search pattern of target resources from {@code basePath}
     * @return the found fragments of resources
     * @throws IOException if failed to find resources by I/O error
     * @throws InterruptedException if interrupted while finding fragments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T> List<DirectInputFragment> findInputFragments(
            DataDefinition<T> definition,
            String basePath,
            ResourcePattern resourcePattern) throws IOException, InterruptedException;

    /**
     * Opens a fragment for input.
     * @param <T> target data type
     * @param definition the data definition
     * @param fragment target fragment
     * @param counter counter object
     * @return {@link ModelInput} to obtain contents in the fragment
     * @throws IOException if failed to open the fragment by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T> ModelInput<T> openInput(
            DataDefinition<T> definition,
            DirectInputFragment fragment,
            Counter counter) throws IOException, InterruptedException;

    /**
     * Opens a resource for output.
     * @param <T> target data type
     * @param context current output attempt context
     * @param definition the data definition
     * @param basePath base path of target resource
     * @param resourcePath target resource path from {@code basePath}
     * @param counter counter object
     * @return {@link ModelOutput} to put contents to the resource
     * @throws IOException if failed to open the resource by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see <a href="#output-staging">Output Staging</a>
     */
    <T> ModelOutput<T> openOutput(
            OutputAttemptContext context,
            DataDefinition<T> definition,
            String basePath,
            String resourcePath,
            Counter counter) throws IOException, InterruptedException;

    /**
     * List resources.
     * @param basePath base path of target resources
     * @param resourcePattern search pattern of target resources from {@code basePath}
     * @param counter the resource counter
     * @return the found resources, or an empty list if there are no resources
     * @throws IOException if failed to list resources by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    List<ResourceInfo> list(
            String basePath,
            ResourcePattern resourcePattern,
            Counter counter) throws IOException, InterruptedException;

    /**
     * Deletes resources.
     * @param basePath base path of target resources
     * @param resourcePattern search pattern of target resources from {@code basePath}
     * @param recursive also deletes containers and thier components recursively
     *     (only if this datasource supports containers)
     * @param counter the resource counter
     * @return {@code true} if successfully deleted, otherwise {@code false}
     * @throws IOException if failed to delete resources by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    boolean delete(
            String basePath,
            ResourcePattern resourcePattern,
            boolean recursive,
            Counter counter) throws IOException, InterruptedException;

    /**
     * Prepares output area for the attempt.
     * @param context current attempt context
     * @throws IOException if failed by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see <a href="#output-staging">Output Staging</a>
     */
    void setupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException;

    /**
     * Commits output on the attempt.
     * @param context current attempt context
     * @throws IOException if failed by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see <a href="#output-staging">Output Staging</a>
     */
    void commitAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException;

    /**
     * Cleanup output on the attempt.
     * @param context current attempt context
     * @throws IOException if failed by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see <a href="#output-staging">Output Staging</a>
     */
    void cleanupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException;

    /**
     * Prepares output area for the transaction.
     * @param context current transaction context
     * @throws IOException if failed by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see <a href="#output-staging">Output Staging</a>
     */
    void setupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException;

    /**
     * Commits output on the transaction.
     * @param context current transaction context
     * @throws IOException if failed by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see <a href="#output-staging">Output Staging</a>
     */
    void commitTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException;

    /**
     * Cleanup output on the transaction.
     * @param context current transaction context
     * @throws IOException if failed by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see <a href="#output-staging">Output Staging</a>
     */
    void cleanupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException;

    /**
     * Returns the property of this data source.
     * @param <T> the property type
     * @param propertyType the property type
     * @return the corresponded property, or {@code empty} if it is not defined
     * @since 0.10.0
     */
    default <T> Optional<T> findProperty(Class<T> propertyType) {
        if (propertyType.isInstance(this)) {
            return Optional.of(propertyType.cast(this));
        }
        return Optional.empty();
    }
}
