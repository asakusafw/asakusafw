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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.hadoop.conf.Configuration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.BatchContext;
import com.asakusafw.runtime.core.Report;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.flow.RuntimeResourceManager;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.testdriver.core.TestToolRepository;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.testdriver.loader.BasicDataLoader;
import com.asakusafw.testdriver.loader.DataLoader;
import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;

/**
 * An <em>Operator DSL</em> test helper which enables framework APIs.
 * Application developers can use this class like as following:
<pre><code>
&#64;Rule
public OperatorTestEnvironment env = new OperatorTestEnvironment();
</code></pre>
 * The above activates a configuration file {@code asakusa-resources.xml} on the current class-path,
 * and enables framework APIs (e.g. {@link Report Report API}) using the configuration.
 * Clients can also use alternative configuration files by specifying their paths:
<pre><code>
&#64;Rule
public OperatorTestEnvironment env = new OperatorTestEnvironment("com/example/testing.xml");
</code></pre>
 * Additionally, clients can also put batch arguments or extra configuration items:
<pre><code>
&#64;Rule
public OperatorTestEnvironment env = new OperatorTestEnvironment(...);

&#64;Test
public void sometest() {
    env.configure("key", "value");
    env.setBatchArg("date", "2011/03/31");
    ...
    env.reload();

    TheOperatorClass op = env.newInstance(TheOperatorClass.class);
    &lt;... test code&gt;
}
</code></pre>
 * @since 0.1.0
 * @version 0.10.2
 */
public class OperatorTestEnvironment extends DriverElementBase implements TestRule {

    static {
        TestingEnvironmentConfigurator.initialize();
    }

    static final Logger LOG = LoggerFactory.getLogger(OperatorTestEnvironment.class);

    /**
     * The property key of suffix name of operator implementation classes.
     * @since 0.10.2
     */
    static final String KEY_IMPLEMENTATION_SUFFIX = "com.asakusafw.testdriver.operator.implementation"; //$NON-NLS-1$

    /**
     * The embedded default configuration file.
     * @since 0.7.0
     */
    static final String DEFAULT_CONFIGURATION_PATH = "default-asakusa-resources.xml"; //$NON-NLS-1$

    /**
     * The suffix name of operator implementation classes.
     * @since 0.10.2
     * @see #KEY_IMPLEMENTATION_SUFFIX
     */
    static final String IMPLEMENTATION_SUFFIX = System.getProperty(KEY_IMPLEMENTATION_SUFFIX, "Impl"); //$NON-NLS-1$

    private RuntimeResourceManager manager;

    private final String configurationPath;

    private final boolean explicitConfigurationPath;

    private final Map<String, String> batchArguments;

    private final Map<String, String> extraConfigurations;

    private boolean dirty;

    private volatile Class<?> testClass;

    private volatile TestToolRepository testTools;

    /**
     * Creates a new instance with the default configuration file.
     */
    public OperatorTestEnvironment() {
        this(RuntimeResourceManager.CONFIGURATION_FILE_NAME, false);
    }

    /**
     * Creates a new instance.
     * @param configurationPath the configuration file location (relative from the class-path)
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public OperatorTestEnvironment(String configurationPath) {
        this(configurationPath, true);
    }

    private OperatorTestEnvironment(String configurationPath, boolean explicit) {
        if (configurationPath == null) {
            throw new IllegalArgumentException("configurationPath must not be null"); //$NON-NLS-1$
        }
        this.configurationPath = configurationPath;
        this.explicitConfigurationPath = explicit;
        this.extraConfigurations = new HashMap<>();
        this.batchArguments = new HashMap<>();
        this.dirty = false;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        reset(description.getTestClass());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } finally {
                    after();
                }
            }
        };
    }

    OperatorTestEnvironment reset(Class<?> contextClass) {
        this.testClass = contextClass;
        return this;
    }

    @Override
    protected Class<?> getCallerClass() {
        if (testClass == null) {
            throw new IllegalStateException(
                    Messages.getString("OperatorTestEnvironment.errorNotInitialized")); //$NON-NLS-1$
        }
        return testClass;
    }

    @Override
    protected TestDataToolProvider getTestTools() {
        TestToolRepository result = testTools;
        if (result == null) {
            Class<?> caller = getCallerClass();
            result = new TestToolRepository(caller.getClassLoader());
            testTools = result;
        }
        return result;
    }

    /**
     * Invoked before running test case.
     */
    protected void before() {
        Configuration conf = createConfig();
        extraConfigurations.forEach(conf::set);
        if (batchArguments.isEmpty() == false) {
            VariableTable variables = new VariableTable(RedefineStrategy.OVERWRITE);
            variables.defineVariables(batchArguments);
            conf.set(StageConstants.PROP_ASAKUSA_BATCH_ARGS, variables.toSerialString());
        }

        manager = new RuntimeResourceManager(conf);
        try {
            manager.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an implementation of the given operator class.
     * @param <T> the operator type
     * @param operatorClass the operator class
     * @return the created instance of the implementation
     * @throws IllegalArgumentException if the method cannot detect its implementation, or cannot create an instance
     * @since 0.10.2
     */
    public <T> T newInstance(Class<T> operatorClass) {
        if (operatorClass == null) {
            throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
        }
        Class<? extends T> implementationClass = findImplementation(operatorClass);
        try {
            Constructor<? extends T> ctor = implementationClass.getConstructor();
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "cannot create an instance of operator implementation class: {0}",
                    implementationClass.getName()), e);
        }
    }

    private static <T> Class<? extends T> findImplementation(Class<T> operatorClass) {
        String implName = operatorClass.getName() + IMPLEMENTATION_SUFFIX;
        try {
            Class<?> implClass = Class.forName(implName, false, operatorClass.getClassLoader());
            if (operatorClass.isAssignableFrom(implClass)) {
                return implClass.asSubclass(operatorClass);
            }
        } catch (ClassNotFoundException e) {
            LOG.debug("cannot detect operator impl: {}", operatorClass, e);
        }

        // operator implementation may be absent if operator class is not abstract
        int modifiers = operatorClass.getModifiers();
        if (!operatorClass.isInterface()
                && !operatorClass.isLocalClass()
                && !operatorClass.isAnonymousClass()
                && !operatorClass.isEnum()
                && !operatorClass.isPrimitive()
                && !Modifier.isAbstract(modifiers)) {
            return operatorClass;
        }

        throw new IllegalArgumentException(MessageFormat.format(
                "cannot detect implementation class: {0}",
                operatorClass.getName()));
    }

    /**
     * Adds a configuration item.
     * Please invoke {@link #reload()} to active this change before executing the test target.
     * @param key the configuration key name
     * @param value the configuration value, or {@code null} to unset the target configuration
     * @throws IllegalArgumentException if {@code key} is {@code null}
     * @see #reload()
     */
    public void configure(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (value != null) {
            extraConfigurations.put(key, value);
        } else {
            extraConfigurations.remove(key);
        }
        dirty = true;
    }

    /**
     * Adds a batch argument.
     * Clients can obtain batch arguments via {@link BatchContext#get(String) context API}.
     * Please invoke {@link #reload()} to active this change before executing the test target.
     * @param key the argument name
     * @param value the argument value, or {@code null} to unset the target argument
     * @throws IllegalArgumentException if {@code key} is {@code null}
     * @see #reload()
     */
    public void setBatchArg(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (value != null) {
            batchArguments.put(key, value);
        } else {
            batchArguments.remove(key);
        }
        dirty = true;
    }

    /**
     * Reloads the configuration file and activates changes.
     */
    public void reload() {
        dirty = false;
        after();
        before();
    }

    /**
     * Returns a new configuration object for {@link RuntimeResourceManager}.
     * @return the created configuration object
     */
    protected Configuration createConfig() {
        Configuration conf = ConfigurationFactory.getDefault().newInstance();
        URL resource = conf.getClassLoader().getResource(configurationPath);
        if (resource == null && explicitConfigurationPath == false) {
            // if implicit configuration file is not found, we use the embedded default configuration file
            resource = OperatorTestEnvironment.class.getResource(DEFAULT_CONFIGURATION_PATH);
        }
        if (resource == null) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("OperatorTestEnvironment.errorMissingConfigurationFile"), //$NON-NLS-1$
                    configurationPath));
        }
        extraConfigurations.forEach(conf::set);
        conf.addResource(resource);
        return conf;
    }

    /**
     * Returns the {@link TestContext} for the current environment.
     * @return {@link TestContext} object
     * @since 0.7.3
     */
    public TestContext getTestContext() {
        return new Context(getCallerClass().getClassLoader(), batchArguments);
    }

    /**
     * Returns the Configuration object for the current environment.
     * @return the Configuration object
     * @since 0.7.3
     */
    public Configuration getConfiguration() {
        return createConfig();
    }

    /**
     * Returns a new {@link MockResult}.
     * The returned object will create copies of the incoming objects.
     * @param <T> the data type
     * @param dataType the data type
     * @return the created {@link MockResult}
     * @since 0.9.1
     */
    public <T extends DataModel<T>> MockResult<T> newResult(Class<T> dataType) {
        Objects.requireNonNull(dataType);
        return new MockResult<T>() {
            @Override
            protected T bless(T result) {
                try {
                    T copy = dataType.newInstance();
                    copy.copyFrom(result);
                    return copy;
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param sourcePath the path to test data set (relative from the current test case class)
     * @return the created loader
     * @since 0.9.1
     */
    public <T> DataLoader<T> loader(Class<T> dataType, String sourcePath) {
        Objects.requireNonNull(sourcePath);
        return loader(dataType, toDataModelSourceFactory(sourcePath));
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param objects the test data objects
     * @return the created loader
     * @since 0.9.1
     */
    public <T> DataLoader<T> loader(Class<T> dataType, Iterable<? extends T> objects) {
        Objects.requireNonNull(objects);
        return loader(dataType, toDataModelSourceFactory(toDataModelDefinition(dataType), objects));
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param provider the test data set provider
     * @return the created loader
     * @since 0.9.1
     */
    public <T> DataLoader<T> loader(Class<T> dataType, Provider<? extends Source<? extends T>> provider) {
        Objects.requireNonNull(provider);
        return loader(dataType, toDataModelSourceFactory(provider));
    }

    /**
     * Returns a new data loader.
     * Note that, the original source path may be changed if tracking source file name.
     * To keep the source file path information, please use {@link #loader(Class, Class, File)} instead.
     * @param <T> the data type
     * @param dataType the data type
     * @param formatClass the data format class
     * @param sourcePath the input file path on the class path
     * @return the created loader
     * @since 0.9.1
     */
    public <T> DataLoader<T> loader(
            Class<T> dataType, Class<? extends DataFormat<? super T>> formatClass, String sourcePath) {
        return loader(dataType, toDataModelSourceFactory(toDataModelDefinition(dataType), formatClass, sourcePath));
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param formatClass the data format class
     * @param file the input file path on the class path
     * @return the created loader
     * @since 0.9.1
     */
    public <T> DataLoader<T> loader(
            Class<T> dataType, Class<? extends DataFormat<? super T>> formatClass, File file) {
        return loader(dataType, toDataModelSourceFactory(toDataModelDefinition(dataType), formatClass, file));
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param factory factory which provides test data set
     * @return the created loader
     * @since 0.9.1
     */
    public <T> DataLoader<T> loader(Class<T> dataType, DataModelSourceFactory factory) {
        Objects.requireNonNull(factory);
        return new BasicDataLoader<>(getTestContext(), toDataModelDefinition(dataType), factory);
    }

    private <T> DataModelDefinition<T> toDataModelDefinition(Class<T> dataType) {
        try {
            return getTestTools().toDataModelDefinition(dataType);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "failed to analyze the data model type: {0}",
                    dataType.getName()), e);
        }
    }

    /**
     * Invoked after ran test case.
     */
    protected void after() {
        if (manager != null) {
            try {
                manager.cleanup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (dirty) {
            throw new AssertionError(MessageFormat.format(
                    Messages.getString("OperatorTestEnvironment.errorNotReloaded"), //$NON-NLS-1$
                    "configure()", //$NON-NLS-1$
                    "reload()")); //$NON-NLS-1$
        }
    }

    private static final class Context implements TestContext {

        private final ClassLoader classLoader;

        private final Map<String, String> arguments;

        Context(ClassLoader classLoader, Map<String, String> arguments) {
            this.classLoader = classLoader;
            this.arguments = arguments;
        }

        @Override
        public ClassLoader getClassLoader() {
            return classLoader;
        }

        @Override
        public Map<String, String> getEnvironmentVariables() {
            return System.getenv();
        }

        @Override
        public Map<String, String> getArguments() {
            return arguments;
        }
    }
}
