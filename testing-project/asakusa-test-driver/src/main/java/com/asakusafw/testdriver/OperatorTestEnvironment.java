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

import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.asakusafw.runtime.core.BatchContext;
import com.asakusafw.runtime.flow.RuntimeResourceManager;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.testdriver.core.TestToolRepository;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.utils.collections.Maps;

// TODO i18n
/**
 * 演算子のテスト時に各種プラグインを利用可能にするためのリソース。
 * <p>
 * テストクラス内で以下のように利用する:
 * </p>
<pre><code>
&#64;Rule
public OperatorTestEnvironment resource = new OperatorTestEnvironment();
</code></pre>
 * <p>
 * 上記のように記述することで、クラスパス上の{@code asakusa-resources.xml}というファイルがあれば読みだし、
 * 各種プラグインが提供するクラスを利用できるようになる。
 * </p>
 * <p>
 * 別の設定ファイルを利用する場合は、下記のようにクラスパス上のファイル名を指定する。
 * </p>
<pre><code>
&#64;Rule
public OperatorTestEnvironment resource = new OperatorTestEnvironment("com/example/testing.xml");
</code></pre>
 * <p>
 * さらに設定をアドホックに変更する場合、次のように行う。
 * </p>
<pre><code>
&#64;Rule
public OperatorTestEnvironment resource = new OperatorTestEnvironment(...);

&#64;Test
public void sometest() {
    resource.configure("key", "value");
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
     * 既定の設定ファイルを利用して、インスタンスを生成する。
     */
    public OperatorTestEnvironment() {
        this(RuntimeResourceManager.CONFIGURATION_FILE_NAME, false);
    }

    /**
     * 設定ファイルのパスを指定して、インスタンスを生成する。
     * @param configurationPath クラスパス上の設定ファイルのパス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
        this.extraConfigurations = Maps.create();
        this.batchArguments = Maps.create();
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
     * 設定項目を追加する。
     * <p>
     * このメソッドは、プラグインの設定を変更するために主に利用する。
     * </p>
     * <p>
     * このメソッドを実行した場合、テスト実行前に
     * {@link #reload()}メソッドを起動して設定を再読み込みする必要がある。
     * </p>
     * @param key 追加する項目のキー名
     * @param value 追加する項目の値、{@code null}の場合には項目を削除する
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
     * バッチ実行時引数(変数表)を設定する。
     * <p>
     * このメソッドは、変数表(${...}で指定する変数)を変更するために主に利用する。
     * ここで設定した値は、{@link BatchContext#get(String)}やインポーターの設定の中などで利用できる。
     * </p>
     * <p>
     * このメソッドを実行した場合、テスト実行前に
     * {@link #reload()}メソッドを起動して設定を再読み込みする必要がある。
     * </p>
     * @param key 追加する項目のキー名
     * @param value 追加する項目の値、{@code null}の場合には項目を削除する
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
     * 設定内容を再読み込みする。
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
