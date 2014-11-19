/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.WeakHashMap;

import org.apache.hadoop.conf.Configurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateProcessDescription;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;
import com.asakusafw.windgate.file.resource.Preparable;

/**
 * Utilities for this package.
 * @since 0.2.2
 */
public final class WindGateTestHelper {

    static final Logger LOG = LoggerFactory.getLogger(WindGateTestHelper.class);

    /**
     * For testing, WindGate profile path pattern in form of {@link MessageFormat}.
     * <code>{0}</code> will be replaced as the its profile name.
     * This module will load these files from the class path.
     */
    public static final String TESTING_PROFILE_PATH = "windgate-{0}.properties";

    /**
     * WindGate plugin directory path from Asakusa installation path.
     */
    public static final String PRODUCTION_PLUGIN_DIRECTORY = "windgate/plugin";

    /**
     * For normal use, WindGate profile path pattern in form of {@link MessageFormat}.
     * <code>{0}</code> will be replaced as the its profile name.
     * This module will load these files in {@code ASAKUSA_HOME}
     * if there are not in {@link #TESTING_PROFILE_PATH}.
     */
    public static final String PRODUCTION_PROFILE_PATH = "windgate/profile/{0}.properties";

    private static final String PLUGIN_EXTENSION = ".jar";

    private static final String DUMMY_RESOURCE_NAME = "__DUMMY__";

    private static final String DUMMY_PROCESS_NAME = "test-moderator";

    private static File lastPluginDirectory;

    private static final WeakHashMap<ClassLoader, Reference<ClassLoader>> PLUGIN_REPOSITORY =
        new WeakHashMap<ClassLoader, Reference<ClassLoader>>();

    /**
     * Creates a new {@link ProcessScript} for testing.
     * The description is used for a source driver, and a dummy driver is set as its drain.
     * @param <T> the type of target data model
     * @param modelType the type of target data model
     * @param description target description
     * @return the created script
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <T> ProcessScript<T> createProcessScript(
            Class<T> modelType,
            WindGateImporterDescription description) {
        if (modelType == null) {
            throw new IllegalArgumentException("modelType must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Create process script: {}", description.getClass().getName());
        return new ProcessScript<T>(
                DUMMY_PROCESS_NAME,
                DUMMY_PROCESS_NAME,
                modelType,
                description.getDriverScript(),
                createDummyDriverScript());
    }

    /**
     * Creates a new {@link ProcessScript} for testing.
     * The description is used for a drain driver, and a dummy driver is set as its source.
     * @param <T> the type of target data model
     * @param modelType the type of target data model
     * @param description target description
     * @return the created script
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <T> ProcessScript<T> createProcessScript(
            Class<T> modelType,
            WindGateExporterDescription description) {
        if (modelType == null) {
            throw new IllegalArgumentException("modelType must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Create process script: {}", description.getClass().getName());
        return new ProcessScript<T>(
                DUMMY_PROCESS_NAME,
                DUMMY_PROCESS_NAME,
                modelType,
                createDummyDriverScript(),
                description.getDriverScript());
    }

    private static DriverScript createDummyDriverScript() {
        return new DriverScript(
                DUMMY_RESOURCE_NAME,
                Collections.<String, String>emptyMap());
    }

    /**
     * Creates a {@link ResourceMirror} for the description.
     * @param testContext current testing context
     * @param description the target description
     * @param arguments the arguments
     * @return the corresponded {@link ResourceManipulator}
     * @throws IOException if failed to create a manipulator
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ResourceManipulator createResourceManipulator(
            TestContext testContext,
            WindGateProcessDescription description,
            ParameterList arguments) throws IOException {
        if (testContext == null) {
            throw new IllegalArgumentException("testContext must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Create resource manipulator: {}", description.getClass().getName());
        GateProfile profile = loadProfile(testContext, description);
        String resourceName = description.getDriverScript().getResourceName();
        for (ResourceProfile resource : profile.getResources()) {
            if (resource.getName().equals(resourceName)) {
                return createManipulator(description, resource, arguments);
            }
        }
        throw new IOException(MessageFormat.format(
                "Failed to prepare WindGate resource: {2} (profile={1}, description={0})",
                description.getClass().getName(),
                description.getProfileName(),
                resourceName));
    }

    private static ResourceManipulator createManipulator(
            WindGateProcessDescription description,
            ResourceProfile resource,
            ParameterList arguments) throws IOException {
        assert description != null;
        assert resource != null;
        assert arguments != null;
        ResourceProvider provider = resource.createProvider();
        ResourceManipulator manipulator = provider.createManipulator(arguments);
        if (manipulator instanceof Configurable) {
            LOG.debug("Configuring resource manipulator: {}", manipulator);
            ConfigurationFactory configuration = ConfigurationFactory.getDefault();
            ((Configurable) manipulator).setConf(configuration.newInstance());
        }
        return manipulator;
    }

    private static GateProfile loadProfile(
            TestContext testContext,
            WindGateProcessDescription description) throws IOException {
        assert testContext != null;
        assert description != null;
        String profileName = description.getProfileName();
        LOG.debug("Searching for a WindGate profile: {}", profileName);

        ClassLoader classLoader = findClassLoader(testContext);

        URL url = classLoader.getResource(MessageFormat.format(
                TESTING_PROFILE_PATH,
                profileName));
        if (url == null) {
            url = findResourceOnHomePath(
                    testContext,
                    MessageFormat.format(
                        PRODUCTION_PROFILE_PATH,
                        profileName));
        }
        if (url == null) {
            throw new IOException(MessageFormat.format(
                    "WindGate profile is not found (profile={1}, description={0})",
                    description.getClass().getName(),
                    description.getProfileName()));
        }

        LOG.debug("Loading a WindGate profile: {}", url);
        try {
            Properties p = new Properties();
            InputStream input = url.openStream();
            try {
                p.load(input);
            } finally {
                input.close();
            }

            LOG.debug("Resolving a WindGate profile: {}", url);
            GateProfile profile = GateProfile.loadFrom(
                    profileName,
                    p,
                    new ProfileContext(classLoader, new ParameterList(testContext.getEnvironmentVariables())));
            return profile;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to load WindGate profile: {2} (profile={1}, description={0})",
                    description.getClass().getName(),
                    description.getProfileName(),
                    url), e);
        }
    }

    private static URL findResourceOnHomePath(TestContext testContext, String path) {
        assert testContext != null;
        assert path != null;
        File file = findFileOnHomePath(testContext, path);
        if (file != null && file.isFile() != false) {
            try {
                return file.toURI().toURL();
            } catch (IOException e) {
                LOG.warn(MessageFormat.format(
                        "Failed to convert a file path to URL: {0}",
                        file), e);
                return null;
            }
        }
        return null;
    }

    private static File findFileOnHomePath(TestContext testContext, String path) {
        assert testContext != null;
        assert path != null;
        String home = testContext.getEnvironmentVariables().get("ASAKUSA_HOME");
        if (home != null) {
            File file = new File(home, path);
            if (file.exists()) {
                return file;
            }
        } else {
            LOG.warn("ASAKUSA_HOME is not defined");
        }
        return null;
    }

    private static ClassLoader findClassLoader(TestContext testContext) {
        assert testContext != null;
        File pluginDirectory = findFileOnHomePath(testContext, PRODUCTION_PLUGIN_DIRECTORY);
        final ClassLoader baseClassLoader = getBareClassLoader();
        synchronized (PLUGIN_REPOSITORY) {
            if (lastPluginDirectory != null && lastPluginDirectory.equals(pluginDirectory) == false) {
                PLUGIN_REPOSITORY.clear();
                lastPluginDirectory = pluginDirectory;
            }
            Reference<ClassLoader> ref = PLUGIN_REPOSITORY.get(baseClassLoader);
            ClassLoader plugins = ref == null ? null : ref.get();
            if (plugins != null) {
                return plugins;
            }
            if (pluginDirectory == null || pluginDirectory.isDirectory() == false) {
                return baseClassLoader;
            }
            final List<URL> pluginLibraries = new ArrayList<URL>();
            for (File file : pluginDirectory.listFiles()) {
                if (file.isFile() && file.getName().endsWith(PLUGIN_EXTENSION)) {
                    try {
                        URL url = file.toURI().toURL();
                        pluginLibraries.add(url);
                    } catch (Exception e) {
                        LOG.warn(MessageFormat.format(
                                "Failed to convert a file path to URL: {0}",
                                file), e);
                    }
                }
            }
            if (pluginLibraries.isEmpty()) {
                return baseClassLoader;
            }
            ClassLoader pluginClassLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    URLClassLoader loader = new URLClassLoader(
                            pluginLibraries.toArray(new URL[pluginLibraries.size()]),
                            baseClassLoader);
                    return loader;
                }
            });
            PLUGIN_REPOSITORY.put(baseClassLoader, new WeakReference<ClassLoader>(pluginClassLoader));
            return pluginClassLoader;
        }
    }

    private static ClassLoader getBareClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            return contextClassLoader;
        }
        return ClassLoader.getSystemClassLoader();
    }

    /**
     * Invoke {@link Preparable#prepare() object.prepare()}, or {@link Closeable#close()} is failed.
     * @param <T> the target object type
     * @param object target object
     * @return the object passed to the parameter
     * @throws IOException if failed to prepare the object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <T extends Preparable & Closeable> T prepare(T object) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException("object must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Preparing object: {}", object);
        boolean succeed = false;
        try {
            object.prepare();
            succeed = true;
            return object;
        } finally {
            if (succeed == false) {
                LOG.warn("Failed to prepare object: {}", object);
                try {
                    object.close();
                } catch (IOException e) {
                    LOG.warn("Failed to close", e);
                }
            }
        }
    }

    private WindGateTestHelper() {
        return;
    }
}
