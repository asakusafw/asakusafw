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
package com.asakusafw.testdriver;

import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.asakusafw.runtime.core.BatchContext;
import com.asakusafw.runtime.core.Report;
import com.asakusafw.runtime.flow.RuntimeResourceManager;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.testdriver.core.TestToolRepository;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * An <em>Operator DSL</em> test helper which enables framework APIs.
 * Application developers can use this class like as following:
<pre><code>
&#64;Rule
public OperatorTestEnvironment resource = new OperatorTestEnvironment();
</code></pre>
 * The above activates a configuration file {@code asakusa-resources.xml} on the current class-path,
 * and enables framework APIs (e.g. {@link Report Report API}) using the configuration.
 * Clients can also use alternative configuration files by specifying their paths:
<pre><code>
&#64;Rule
public OperatorTestEnvironment resource = new OperatorTestEnvironment("com/example/testing.xml");
</code></pre>
 * Additionally, clients can also put batch arguments or extra configuration items:
<pre><code>
&#64;Rule
public OperatorTestEnvironment resource = new OperatorTestEnvironment(...);

&#64;Test
public void sometest() {
    resource.configure("key", "value");
    resource.setBatchArg("date", "2011/03/31");
    ...
    resource.reload();

    &lt;test code&gt;
}
</code></pre>
 * @since 0.1.0
 * @version 0.7.3
 */
public class OperatorTestEnvironment extends DriverElementBase implements TestRule {

    static {
        TestingEnvironmentConfigurator.initialize();
    }

    /**
     * The embedded default configuration file.
     * @since 0.7.0
     */
    static final String DEFAULT_CONFIGURATION_PATH = "default-asakusa-resources.xml"; //$NON-NLS-1$

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
    public Statement apply(final Statement base, Description description) {
        this.testClass = description.getTestClass();
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
        for (Map.Entry<String, String> entry : extraConfigurations.entrySet()) {
            conf.set(entry.getKey(), entry.getValue());
        }
        if (batchArguments.isEmpty() == false) {
            VariableTable variables = new VariableTable(RedefineStrategy.OVERWRITE);
            for (Map.Entry<String, String> entry : batchArguments.entrySet()) {
                variables.defineVariable(entry.getKey(), entry.getValue());
            }
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
        for (Map.Entry<String, String> entry : extraConfigurations.entrySet()) {
            conf.set(entry.getKey(), entry.getValue());
        }
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
