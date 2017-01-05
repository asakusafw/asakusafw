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
package com.asakusafw.compiler.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;

/**
 * Represents a compiler environment for batch DSL compiler.
 */
public class BatchCompilingEnvironment {

    static final Logger LOG = LoggerFactory.getLogger(BatchCompilingEnvironment.class);

    private final BatchCompilerConfiguration configuration;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final String buildId = UUID.randomUUID().toString();

    private String firstError;

    /**
     * Creates a new instance.
     * @param configuration the compiler settings
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public BatchCompilingEnvironment(BatchCompilerConfiguration configuration) {
        Precondition.checkMustNotBeNull(configuration, "configuration"); //$NON-NLS-1$
        this.configuration = configuration;
    }

    /**
     * Initializes this object.
     * @return this
     */
    public BatchCompilingEnvironment bless() {
        if (initialized.compareAndSet(false, true) == false) {
            return this;
        }
        configuration.getWorkflows().initialize(this);
        clearError();
        return this;
    }

    /**
     * Returns the build ID of this batch compilation.
     * @return the build ID
     * @since 0.4.0
     */
    public String getBuildId() {
        return buildId;
    }

    /**
     * Returns a previous error message.
     * @return a previous error message, or {@code null} if not error
     */
    public String getErrorMessage() {
        return firstError;
    }

    /**
     * Returns whether this saw any compile errors or not.
     * @return {@code true} if saw any compile errors, or otherwise {@code false}
     */
    public boolean hasError() {
        return firstError != null;
    }

    /**
     * Clears compile errors.
     * @see #hasError()
     */
    public void clearError() {
        firstError = null;
    }

    /**
     * Returns the configuration object.
     * @return the configuration object
     */
    public BatchCompilerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the repository of workflow processors.
     * @return the repository of workflow processors
     */
    public WorkflowProcessor.Repository getWorkflows() {
        return configuration.getWorkflows();
    }

    /**
     * Opens an output stream for creating the target resource file.
     * @param path the relative path from the compiler output
     * @return the created output
     * @throws IOException if failed to open the target resource
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public OutputStream openResource(String path) throws IOException {
        Precondition.checkMustNotBeNull(path, "path"); //$NON-NLS-1$
        File output = configuration.getOutputDirectory();
        File file = new File(output, path);
        File parent = file.getParentFile();
        if (parent.mkdirs() == false && parent.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("BatchCompilingEnvironment.errorFailedToCreateParentDirectory"), //$NON-NLS-1$
                    parent));
        }
        return new FileOutputStream(file);
    }

    /**
     * Adds an erroneous information to this environment.
     * @param format the message format ({@link MessageFormat} style)
     * @param arguments the message arguments
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public void error(String format, Object... arguments) {
        Precondition.checkMustNotBeNull(format, "format"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(arguments, "arguments"); //$NON-NLS-1$
        String text;
        if (arguments.length == 0) {
            text = format;
        } else {
            text = MessageFormat.format(format, arguments);
        }
        LOG.error(text);
        if (firstError == null) {
            firstError = text;
        }
    }

    /**
     * An extension interface for classes that requires a {@link BatchCompilingEnvironment} for its initialization.
     */
    public interface Initializable {

        /**
         * Initializes this object.
         * @param environment the current environment
         */
        void initialize(BatchCompilingEnvironment environment);
    }

    /**
     * A skeletal implementation of {@link Initializable}.
     */
    public abstract static class Initialized implements Initializable {

        private BatchCompilingEnvironment environment;

        @Override
        public final void initialize(BatchCompilingEnvironment env) {
            Precondition.checkMustNotBeNull(env, "env"); //$NON-NLS-1$
            this.environment = env;
            doInitialize();
        }

        /**
         * Initializes this object in sub-classes.
         */
        protected void doInitialize() {
            return;
        }

        /**
         * Returns the current environment object.
         * @return the current environment
         */
        protected BatchCompilingEnvironment getEnvironment() {
            return environment;
        }
    }
}
