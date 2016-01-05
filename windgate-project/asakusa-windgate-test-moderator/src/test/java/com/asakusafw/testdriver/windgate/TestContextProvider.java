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
package com.asakusafw.testdriver.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.windgate.core.ProfileContext;

/**
 * Manipulate configuration and context class loader.
 */
public class TestContextProvider extends ExternalResource {

    private final TemporaryFolder folder = new TemporaryFolder();

    private File frameworkHome;

    private File classes;

    private ClassLoader classLoader;

    private final List<PluginClassLoader> loaders = new ArrayList<>();

    @Override
    protected void before() throws Throwable {
        folder.create();
        frameworkHome = folder.newFolder();
        classes = folder.newFolder();
        classLoader = new URLClassLoader(new URL[] { classes.toURI().toURL() }, getClass().getClassLoader());
    }

    @Override
    protected void after() {
        try {
            for (PluginClassLoader loader : loaders) {
                WindGateTestHelper.disposePluginClassLoader(loader);
            }
            System.gc();
        } finally {
            folder.delete();
        }
    }

    /**
     * Registers the profile context.
     * @param profile the profile context
     * @return the context
     */
    public ProfileContext register(ProfileContext profile) {
        ClassLoader loader = profile.getClassLoader();
        if (loader instanceof PluginClassLoader) {
            loaders.add((PluginClassLoader) loader);
        }
        return profile;
    }

    /**
     * Returns a test context.
     * @return a test context for current profile
     */
    public TestContext get() {
        return new MockTestContext(classLoader, frameworkHome);
    }

    /**
     * Returns template of WindGate profile.
     * @return the template
     */
    public Properties getTemplate() {
        Properties p = new Properties();

        try (InputStream in = TestContextProvider.class.getResourceAsStream("windgate-template.properties")) {
            assertThat(in, is(notNullValue()));
            p.load(in);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return p;
    }

    /**
     * Puts profile.
     * @param profileName target profile name
     * @param properties profile contents
     */
    public void put(String profileName, Properties properties) {
        File file = new File(classes, MessageFormat.format(
                WindGateTestHelper.TESTING_PROFILE_PATH,
                profileName));
        try (FileOutputStream out = new FileOutputStream(file)) {
            properties.store(out, "testing");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Puts plug-in library.
     * @param jarName target jar file name
     * @param source the source location
     */
    public void put(String jarName, URL source) {
        File plugins = new File(frameworkHome, WindGateTestHelper.PRODUCTION_PLUGIN_DIRECTORY);
        plugins.mkdirs();
        File file = new File(plugins, jarName);
        try (FileOutputStream out = new FileOutputStream(file);
                InputStream in = source.openStream();){
            byte[] buf = new byte[256];
            while (true) {
                int read = in.read(buf);
                if (read < 0) {
                    break;
                }
                out.write(buf, 0, read);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static final class MockTestContext implements TestContext {

        private static final TestContext DELEGATE = new TestContext.Empty();

        private final ClassLoader classLoader;

        private final Map<String, String> envp;

        public MockTestContext(ClassLoader classLoader, File frameworkHome) {
            this.classLoader = classLoader;
            this.envp = new HashMap<>();
            envp.putAll(DELEGATE.getEnvironmentVariables());
            envp.put(WindGateTestHelper.ENV_FRAMEWORK_HOME, frameworkHome.getAbsolutePath());
        }

        @Override
        public ClassLoader getClassLoader() {
            return classLoader;
        }

        @Override
        public Map<String, String> getEnvironmentVariables() {
            return Collections.unmodifiableMap(envp);
        }

        @Override
        public Map<String, String> getArguments() {
            return DELEGATE.getArguments();
        }
    }
}
