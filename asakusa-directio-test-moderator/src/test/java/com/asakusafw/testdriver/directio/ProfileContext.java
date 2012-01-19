/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver.directio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.AbstractDirectDataSource;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.flow.RuntimeResourceManager;
import com.asakusafw.testdriver.core.TestContext;

/**
 * Manipulate configuration and context class loader.
 */
public class ProfileContext extends ExternalResource {

    private final TemporaryFolder folder = new TemporaryFolder();

    private final Configuration configuration = new Configuration(false);

    @Override
    protected void before() throws Throwable {
        folder.create();
    }

    @Override
    protected void after() {
        folder.delete();
    }

    /**
     * Creates a test context.
     * @param kvs key and value pairs
     * @return the created context
     */
    public TestContext getTextContext(String... kvs) {
        assert kvs.length % 2 == 0;
        final Map<String, String> env = new HashMap<String, String>(System.getenv());
        env.put("ASAKUSA_HOME", folder.getRoot().getAbsolutePath());
        final Map<String, String> args = new HashMap<String, String>();
        for (int i = 0; i < kvs.length; i += 2) {
            args.put(kvs[i], kvs[i + 1]);
        }
        return new TestContext() {

            @Override
            public Map<String, String> getEnvironmentVariables() {
                return env;
            }

            @Override
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();
            }

            @Override
            public Map<String, String> getArguments() {
                return args;
            }
        };
    }

    /**
     * Adds a datasource configuration.
     * @param id datasource ID
     * @param aClass datasource class
     * @param path target logical path
     */
    public void add(String id, Class<? extends AbstractDirectDataSource> aClass, String path) {
        configuration.setClass(
                MessageFormat.format(
                        "{0}{1}",
                        HadoopDataSourceUtil.PREFIX,
                        id),
                aClass,
                AbstractDirectDataSource.class);
        add(id, HadoopDataSourceUtil.KEY_PATH, path);
    }

    /**
     * Adds a datasource property.
     * @param id datasource ID
     * @param key property key
     * @param value property value
     */
    public void add(String id, String key, String value) {
        configuration.set(
                MessageFormat.format(
                        "{0}{1}.{2}",
                        HadoopDataSourceUtil.PREFIX,
                        id,
                        key),
                value);
    }

    /**
     * Puts configuration.
     */
    public void put() {
        try {
            File file = new File(folder.getRoot(), RuntimeResourceManager.CONFIGURATION_FILE_PATH);
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            try {
                configuration.writeXml(out);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
