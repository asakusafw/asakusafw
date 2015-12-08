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
package com.asakusafw.testdriver;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.trace.TracepointWeaveRewriter;
import com.asakusafw.runtime.core.Report;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;
import com.asakusafw.trace.io.TraceSettingSerializer;
import com.asakusafw.trace.model.TraceSetting;
import com.asakusafw.trace.model.TraceSetting.Mode;
import com.asakusafw.trace.model.Tracepoint;
import com.asakusafw.trace.model.Tracepoint.PortKind;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;

/**
 * An abstract super class of test-driver classes.
 * @since 0.2.0
 * @version 0.7.0
 */
public abstract class TestDriverBase extends DriverElementBase {

    private static final String FLOW_OPERATOR_FACTORY_METHOD_NAME = "create"; //$NON-NLS-1$

    static {
        TestingEnvironmentConfigurator.initialize();
    }

    /**
     * The internal test driver context object.
     */
    protected final TestDriverContext driverContext;

    /**
     * Creates a new instance.
     * @param callerClass the caller class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestDriverBase(Class<?> callerClass) {
        if (callerClass == null) {
            throw new IllegalArgumentException("callerClass must not be null"); //$NON-NLS-1$
        }
        this.driverContext = new TestDriverContext(callerClass);
    }

    @Override
    protected final Class<?> getCallerClass() {
        return driverContext.getCallerClass();
    }

    @Override
    protected final TestDataToolProvider getTestTools() {
        return driverContext.getRepository();
    }

    /**
     * Adds a runtime configuration item.
     * This may customize behavior of some framework APIs (e.g. {@link Report report API}).
     * @param key the configuration key name
     * @param value the configuration value, or {@code null} to unset the target configuration
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void configure(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (value != null) {
            driverContext.getExtraConfigurations().put(key, value);
        } else {
            driverContext.getExtraConfigurations().remove(key);
        }
    }

    /**
     * Adds a batch argument.
     * @param key the argument name
     * @param value the argument value, or {@code null} to unset the target argument
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void setBatchArg(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (value != null) {
            driverContext.getBatchArgs().put(key, value);
        } else {
            driverContext.getBatchArgs().remove(key);
        }
    }

    /**
     * Configures the compiler optimization level.
     * <ul>
     * <li> 0: disables all optimizations </li>
     * <li> 1: only enables default optimizations </li>
     * <li> 2~: enables aggressive optimizations </li>
     * </ul>
     * @param level the compiler optimization level
     */
    public void setOptimize(int level) {
        FlowCompilerOptions options = driverContext.getOptions();
        if (level <= 0) {
            options.setCompressConcurrentStage(false);
            options.setCompressFlowPart(false);
            options.setHashJoinForSmall(false);
            options.setHashJoinForTiny(false);
            options.setEnableCombiner(false);
        } else if (level == 1) {
            options.setCompressConcurrentStage(FlowCompilerOptions.Item.compressConcurrentStage.defaultValue);
            options.setCompressFlowPart(FlowCompilerOptions.Item.compressFlowPart.defaultValue);
            options.setHashJoinForSmall(FlowCompilerOptions.Item.hashJoinForSmall.defaultValue);
            options.setHashJoinForTiny(FlowCompilerOptions.Item.hashJoinForTiny.defaultValue);
            options.setEnableCombiner(FlowCompilerOptions.Item.enableCombiner.defaultValue);
        } else {
            options.setCompressConcurrentStage(true);
            options.setCompressFlowPart(true);
            options.setHashJoinForSmall(true);
            options.setHashJoinForTiny(true);
            options.setEnableCombiner(true);
        }
    }

    /**
     * Sets whether compiler should keep debugging information or not.
     * @param enable {@code true} to keep debugging information, otherwise {@code false}
     */
    public void setDebug(boolean enable) {
        driverContext.getOptions().setEnableDebugLogging(enable);
    }

    /**
     * Sets an extra compiler option ({@code X<name>=<value>}).
     * The compiler name MUST NOT start with {@code "X"}.
     * @param name the option name
     * @param value the option value
     * @since 0.7.3
     */
    public void setExtraCompilerOption(String name, String value) {
        if (name.startsWith(FlowCompilerOptions.PREFIX_EXTRA_OPTION)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("TestDriverBase.errorInvalidPrefix"), //$NON-NLS-1$
                    FlowCompilerOptions.PREFIX_EXTRA_OPTION,
                    name));
        }
        driverContext.getOptions().putExtraAttribute(name, value);
    }

    /**
     * Sets the Asakusa framework installation path ({@literal a.k.a.} {@code $ASAKUSA_HOME}).
     * If this is not set, the installation path will be computed from the environment variable.
     * @param frameworkHomePath the framework installation path
     */
    public void setFrameworkHomePath(File frameworkHomePath) {
        driverContext.setFrameworkHomePath(frameworkHomePath);
    }

    /**
     * Sets the search path of the external library files.
     * If this is not set, the search path will be {@link TestDriverContext#EXTERNAL_LIBRARIES_PATH}.
     * @param librariesPath the search path of the external library files
     * @since 0.5.1
     */
    public void setLibrariesPath(File librariesPath) {
        driverContext.setLibrariesPath(librariesPath);
    }

    /**
     * Sets the explicit compiler working directory.
     * If this is not set, the compiler will create the working directory into the temporary area,
     * and remove it after test was finished.
     * @param path the explicit compiler working directory
     * @since 0.5.2
     */
    public void setCompilerWorkingDirectory(File path) {
        driverContext.setCompilerWorkingDirectory(path);
    }

    /**
     * Sets whether skips verifying test conditions.
     * @param skip {@code true} if verifying test conditions, otherwise {@code false}
     * @since 0.7.0
     */
    public void skipValidateCondition(boolean skip) {
        driverContext.setSkipValidateCondition(skip);
    }

    /**
     * Sets whether skips truncating test input data.
     * @param skip {@code true} if truncating test input data, otherwise {@code false}
     */
    public void skipCleanInput(boolean skip) {
        driverContext.setSkipCleanInput(skip);
    }

    /**
     * Sets whether skips truncating test output data.
     * @param skip {@code true} if truncating test output data, otherwise {@code false}
     */
    public void skipCleanOutput(boolean skip) {
        driverContext.setSkipCleanOutput(skip);
    }

    /**
     * Sets whether skips preparing test input data.
     * @param skip {@code true} if preparing test input data, otherwise {@code false}
     */
    public void skipPrepareInput(boolean skip) {
        driverContext.setSkipPrepareInput(skip);
    }

    /**
     * Sets whether skips preparing test output data.
     * @param skip {@code true} if preparing test output data, otherwise {@code false}
     */
    public void skipPrepareOutput(boolean skip) {
        driverContext.setSkipPrepareOutput(skip);
    }

    /**
     * Sets whether skips executing jobflows.
     * @param skip {@code true} if executing jobflows, otherwise {@code false}
     */
    public void skipRunJobflow(boolean skip) {
        driverContext.setSkipRunJobflow(skip);
    }

    /**
     * Sets whether skips verifying test results.
     * @param skip {@code true} if verifying test results, otherwise {@code false}
     */
    public void skipVerify(boolean skip) {
        driverContext.setSkipVerify(skip);
    }

    /**
     * Adds a new trace-point to the target operator input.
     * @param operatorClass target operator class
     * @param methodName target operator method name
     * @param portName target operator input port name
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public void addInputTrace(Class<?> operatorClass, String methodName, String portName) {
        if (operatorClass == null) {
            throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
        }
        if (methodName == null) {
            throw new IllegalArgumentException("methodName must not be null"); //$NON-NLS-1$
        }
        if (portName == null) {
            throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
        }
        TraceSetting setting = createTraceSetting(
                operatorClass, methodName,
                PortKind.INPUT, portName,
                Collections.<String, String>emptyMap());
        appendTrace(setting);
    }

    /**
     * Adds a new trace-point to the target operator output.
     * @param operatorClass target operator class
     * @param methodName target operator method name
     * @param portName target operator input port name
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public void addOutputTrace(Class<?> operatorClass, String methodName, String portName) {
        if (operatorClass == null) {
            throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
        }
        if (methodName == null) {
            throw new IllegalArgumentException("methodName must not be null"); //$NON-NLS-1$
        }
        if (portName == null) {
            throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
        }
        TraceSetting setting = createTraceSetting(
                operatorClass, methodName,
                PortKind.OUTPUT, portName,
                Collections.<String, String>emptyMap());
        appendTrace(setting);
    }

    /**
     * Adds a new trace-point to the target operator input.
     * @param flowpartClass target flow-part class
     * @param portName target operator input port name
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public void addInputTrace(Class<? extends FlowDescription> flowpartClass, String portName) {
        if (flowpartClass == null) {
            throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
        }
        if (portName == null) {
            throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
        }
        checkFlowpart(flowpartClass);
        TraceSetting setting = createTraceSetting(
                flowpartClass, FLOW_OPERATOR_FACTORY_METHOD_NAME,
                PortKind.INPUT, portName,
                Collections.<String, String>emptyMap());
        appendTrace(setting);
    }

    private void checkFlowpart(Class<? extends FlowDescription> flowpartClass) {
        if (flowpartClass.isAnnotationPresent(FlowPart.class) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("TestDriverBase.errorInvalidFlowpartClass"), //$NON-NLS-1$
                    flowpartClass.getName()));
        }
    }

    /**
     * Adds a new trace-point to the target operator output.
     * @param flowpartClass target flow-part class
     * @param portName target operator input port name
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public void addOutputTrace(Class<? extends FlowDescription> flowpartClass, String portName) {
        if (flowpartClass == null) {
            throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
        }
        if (portName == null) {
            throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
        }
        checkFlowpart(flowpartClass);
        TraceSetting setting = createTraceSetting(
                flowpartClass, FLOW_OPERATOR_FACTORY_METHOD_NAME,
                PortKind.OUTPUT, portName,
                Collections.<String, String>emptyMap());
        appendTrace(setting);
    }

    private void appendTrace(TraceSetting setting) {
        assert setting != null;
        String optionValue = driverContext.getOptions().getExtraAttribute(TracepointWeaveRewriter.KEY_COMPILER_OPTION);
        optionValue = appendTraceSetting(optionValue, setting);
        driverContext.getOptions().putExtraAttribute(TracepointWeaveRewriter.KEY_COMPILER_OPTION, optionValue);
    }

    private String appendTraceSetting(String option, TraceSetting setting) {
        List<TraceSetting> settings = new ArrayList<>();
        if (option != null) {
            Collection<? extends TraceSetting> loaded = TraceSettingSerializer.deserialize(option);
            settings.addAll(loaded);
        }
        settings.add(setting);
        return TraceSettingSerializer.serialize(settings);
    }

    static TraceSetting createTraceSetting(
            Class<?> operatorClass,
            String methodName,
            PortKind portKind,
            String portName,
            Map<String, String> attributes) {
        assert operatorClass != null;
        assert methodName != null;
        assert portKind != null;
        assert portName != null;
        assert attributes != null;
        return new TraceSetting(
                new Tracepoint(operatorClass.getName(), methodName, portKind, portName),
                Mode.STRICT, attributes);
    }

    /**
     * Sets the {@link JobExecutorFactory} for executing jobs in this test.
     * @param factory the factory, or {@code null} to use a default implementation
     * @see TestDriverContext#KEY_JOB_EXECUTOR_FACTORY
     * @since 0.6.0
     */
    public void setJobExecutorFactory(JobExecutorFactory factory) {
        driverContext.setJobExecutorFactory(factory);
    }

    /**
     * Returns the current test driver context (for internal use only).
     * @return the current test driver context
     * @since 0.6.1
     */
    TestDriverContext getDriverContext() {
        return driverContext;
    }
}
