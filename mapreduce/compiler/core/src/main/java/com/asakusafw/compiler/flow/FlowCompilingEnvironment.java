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
package com.asakusafw.compiler.flow;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Represents a compiler environment for flow DSL compiler.
 * @since 0.1.0
 * @version 0.2.6
 */
public class FlowCompilingEnvironment {

    static final Logger LOG = LoggerFactory.getLogger(FlowCompilingEnvironment.class);

    private final FlowCompilerConfiguration config;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final AtomicInteger counter = new AtomicInteger();

    private String firstError;

    /**
     * Creates a new instance.
     * @param configuration the compiler settings
     * @throws IllegalArgumentException if the parameter {@code null}
     */
    public FlowCompilingEnvironment(FlowCompilerConfiguration configuration) {
        Precondition.checkMustNotBeNull(configuration, "configuration"); //$NON-NLS-1$
        this.config = configuration;
        clearError();
    }

    /**
     * Initializes this object.
     * @return this
     */
    public FlowCompilingEnvironment bless() {
        if (initialized.compareAndSet(false, true) == false) {
            return this;
        }
        config.getDataClasses().initialize(this);
        config.getExternals().initialize(this);
        config.getPackager().initialize(this);
        config.getProcessors().initialize(this);
        config.getGraphRewriters().initialize(this);
        clearError();
        return this;
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
    public final boolean hasError() {
        return firstError != null;
    }

    /**
     * Clears compile errors.
     * @see #hasError()
     */
    public final void clearError() {
        firstError = null;
    }

    /**
     * Returns the Java DOM factory.
     * @return the Java DOM factory
     */
    public ModelFactory getModelFactory() {
        return config.getFactory();
    }

    /**
     * Returns the repository of available flow element processors in this environment.
     * @return the repository of available
     */
    public FlowElementProcessor.Repository getProcessors() {
        return config.getProcessors();
    }

    /**
     * Returns the repository of available data model classes in this environment.
     * @return the repository of available
     */
    public DataClassRepository getDataClasses() {
        return config.getDataClasses();
    }

    /**
     * Returns the repository of available external I/O processors in this environment.
     * @return the repository of available
     */
    public ExternalIoDescriptionProcessor.Repository getExternals() {
        return config.getExternals();
    }

    /**
     * Returns the repository of available flow graph rewriters in this environment.
     * @return the repository of available
     */
    public FlowGraphRewriter.Repository getGraphRewriters() {
        return config.getGraphRewriters();
    }

    /**
     * Returns the ID of the target batch.
     * @return the batch ID
     */
    public String getBatchId() {
        return config.getBatchId();
    }

    /**
     * Returns the flow ID of the target jobflow.
     * @return the flow ID
     */
    public String getFlowId() {
        return config.getFlowId();
    }

    /**
     * Returns the qualified flow ID of the target jobflow.
     * @return the qualified flow ID
     */
    public String getTargetId() {
        return MessageFormat.format("{0}.{1}", getBatchId(), getFlowId()); //$NON-NLS-1$
    }

    /**
     * Returns the base package name for generated Java classes.
     * @return the package name
     */
    public Name getTargetPackageName() {
        Name root = Models.toName(getModelFactory(), config.getRootPackageName());
        Name batch = Models.toName(getModelFactory(), normalize(getBatchId()));
        Name flow = Models.toName(getModelFactory(), normalize(getFlowId()));
        return Models.append(getModelFactory(), root, batch, flow);
    }

    private String normalize(String name) {
        assert name != null;
        StringBuilder buf = new StringBuilder();
        String[] segments = name.split(Pattern.quote(".")); //$NON-NLS-1$
        buf.append(memberName(segments[0]));
        for (int i = 1; i < segments.length; i++) {
            buf.append('.');
            buf.append(memberName(segments[i]));
        }
        return buf.toString();
    }

    private String memberName(String string) {
        assert string != null;
        if (string.isEmpty()) {
            return "_"; //$NON-NLS-1$
        }
        return JavaName.of(string).toMemberName();
    }

    /**
     * Returns the Java package name for generating classes about main phase.
     * @param stageNumber the target stage number
     * @return the target package name
     * @throws IllegalArgumentException if the stage number is negative
     */
    public Name getStagePackageName(int stageNumber) {
        if (stageNumber < 0) {
            throw new IllegalArgumentException("stageNumber must be a positive integer"); //$NON-NLS-1$
        }
        return Models.append(
                config.getFactory(),
                getTargetPackageName(),
                String.format("stage%04d", stageNumber)); //$NON-NLS-1$
    }

    /**
     * Returns the Java package name for generating resources.
     * @param resourceKind the resource kind
     * @return the target package name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Name getResourcePackage(String resourceKind) {
        Precondition.checkMustNotBeNull(resourceKind, "resourceKind"); //$NON-NLS-1$
        return Models.append(getModelFactory(),
                getTargetPackageName(),
                normalize(resourceKind));
    }

    /**
     * Returns a unique name using the internal sequence number.
     * @param prefix the prefix
     * @return the unique name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public SimpleName createUniqueName(String prefix) {
        Precondition.checkMustNotBeNull(prefix, "prefix"); //$NON-NLS-1$
        return getModelFactory().newSimpleName(prefix + counter.incrementAndGet());
    }

    /**
     * Returns the Java package name for generating classes about prologue phase.
     * @param moduleId the target module ID
     * @return the target package name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Name getProloguePackageName(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return Models.append(
                config.getFactory(),
                getTargetPackageName(),
                MessageFormat.format("{0}.prologue", memberName(moduleId))); //$NON-NLS-1$
    }

    /**
     * Returns the Java package name for generating classes about epilogue phase.
     * @param moduleId the target module ID
     * @return the target package name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Name getEpiloguePackageName(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return Models.append(
                config.getFactory(),
                getTargetPackageName(),
                MessageFormat.format("{0}.epilogue", memberName(moduleId))); //$NON-NLS-1$
    }

    /**
     * Returns the base location of runtime working area.
     * @return the base location of runtime working area
     */
    public Location getTargetLocation() {
        return config
            .getRootLocation()
            .append(getBatchId())
            .append(getFlowId());
    }

    /**
     * Returns the location of runtime working area for the stage.
     * @param stageNumber the target stage number
     * @return the target resource location
     * @throws IllegalArgumentException if the stage number is negative
     */
    public Location getStageLocation(int stageNumber) {
        if (stageNumber < 0) {
            throw new IllegalArgumentException("stageNumber must be a positive integer"); //$NON-NLS-1$
        }
        String stageSuffix = String.format("stage%04d", stageNumber); //$NON-NLS-1$
        return getTargetLocation().append(stageSuffix);
    }

    /**
     * Returns the location of runtime working area for the prologue stage.
     * @param moduleId the module ID
     * @return the target resource location
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Location getPrologueLocation(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return getTargetLocation()
            .append("prologue") //$NON-NLS-1$
            .append(moduleId);
    }

    /**
     * Returns the location of runtime working area for the epilogue stage.
     * @param moduleId the module ID
     * @return the target resource location
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Location getEpilogueLocation(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return getTargetLocation()
            .append("epilogue") //$NON-NLS-1$
            .append(moduleId);
    }

    /**
     * Emits a Java source program into the jobflow package.
     * @param source the source program
     * @throws IOException if failed to output
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void emit(CompilationUnit source) throws IOException {
        Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
        try (PrintWriter writer = config.getPackager().openWriter(source)) {
            Models.emit(source, writer);
        }
    }

    /**
     * Creates a generic resource file into the jobflow package.
     * @param packageNameOrNull the target package name
     * @param subPath the sub-path from the target package
     * @return the output stream for writing contents of the created resource
     * @throws IOException if failed to output
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public OutputStream openResource(Name packageNameOrNull, String subPath) throws IOException {
        Precondition.checkMustNotBeNull(subPath, "subPath"); //$NON-NLS-1$
        return config.getPackager().openStream(packageNameOrNull, subPath);
    }

    /**
     * Returns the class loader for loading compiler services.
     * @return the class loader
     */
    public ClassLoader getServiceClassLoader() {
        return config.getServiceClassLoader();
    }

    /**
     * Returns the compiler options.
     * @return the compiler options
     */
    public FlowCompilerOptions getOptions() {
        return config.getOptions();
    }

    /**
     * Returns the current build ID.
     * @return current build ID, or {@code null} if not defined
     * @since 0.4.0
     */
    public String getBuildId() {
        return config.getBuildId();
    }

    /**
     * Adds an erroneous information to this environment.
     * @param format the message format ({@link MessageFormat} style)
     * @param arguments the message arguments
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public void error(String format, Object...arguments) {
        Precondition.checkMustNotBeNull(format, "format"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(arguments, "arguments"); //$NON-NLS-1$
        String text = format(format, arguments);
        LOG.error(text);
        if (firstError == null) {
            firstError = text;
        }
    }

    private String format(String format, Object[] args) {
        assert format != null;
        assert args != null;
        if (args.length == 0) {
            return format;
        }
        return MessageFormat.format(format, args);
    }

    /**
     * An extension interface for classes that requires a {@link FlowCompilingEnvironment} for its initialization.
     */
    public interface Initializable {

        /**
         * Initializes this object.
         * @param environment the current environment
         */
        void initialize(FlowCompilingEnvironment environment);
    }

    /**
     * A skeletal implementation of {@link Initializable}.
     */
    public abstract static class Initialized implements Initializable {

        private FlowCompilingEnvironment environment;

        @Override
        public final void initialize(FlowCompilingEnvironment env) {
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
        protected FlowCompilingEnvironment getEnvironment() {
            return environment;
        }
    }
}
