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
package com.asakusafw.testdriver.windgate;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.WeakHashMap;

import org.apache.hadoop.conf.Configurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;
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
 * @version 0.7.2
 */
public final class WindGateTestHelper {

    static final Logger LOG = LoggerFactory.getLogger(WindGateTestHelper.class);

    /**
     * The environment variable name / parameter name in profile context for the framework home path.
     * @since 0.7.2
     */
    public static final String ENV_FRAMEWORK_HOME = "ASAKUSA_HOME"; //$NON-NLS-1$

    /**
     * For testing, WindGate profile path pattern in form of {@link MessageFormat}.
     * <code>{0}</code> will be replaced as the its profile name.
     * This module will load these files from the class path.
     */
    public static final String TESTING_PROFILE_PATH = "windgate-{0}.properties"; //$NON-NLS-1$

    /**
     * WindGate plugin directory path from Asakusa installation path.
     */
    public static final String PRODUCTION_PLUGIN_DIRECTORY = "windgate/plugin"; //$NON-NLS-1$

    /**
     * For normal use, WindGate profile path pattern in form of {@link MessageFormat}.
     * <code>{0}</code> will be replaced as the its profile name.
     * This module will load these files in {@code ASAKUSA_HOME}
     * if there are not in {@link #TESTING_PROFILE_PATH}.
     * @see #ENV_FRAMEWORK_HOME
     */
    public static final String PRODUCTION_PROFILE_PATH = "windgate/profile/{0}.properties"; //$NON-NLS-1$

    private static final String PLUGIN_EXTENSION = ".jar"; //$NON-NLS-1$

    private static final String DUMMY_RESOURCE_NAME = "__DUMMY__"; //$NON-NLS-1$

    private static final String DUMMY_PROCESS_NAME = "test-moderator"; //$NON-NLS-1$

    private static final WeakHashMap<TestContext, Holder> CACHE_TEMPORARY_LOADER = new WeakHashMap<>();

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
        Holder.clean();
        LOG.debug("Create process script: {}", description.getClass().getName()); //$NON-NLS-1$
        return new ProcessScript<>(
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
        Holder.clean();
        LOG.debug("Create process script: {}", description.getClass().getName()); //$NON-NLS-1$
        return new ProcessScript<>(
                DUMMY_PROCESS_NAME,
                DUMMY_PROCESS_NAME,
                modelType,
                createDummyDriverScript(),
                description.getDriverScript());
    }

    private static DriverScript createDummyDriverScript() {
        return new DriverScript(
                DUMMY_RESOURCE_NAME,
                Collections.emptyMap());
    }

    /**
     * Creates a WindGate {@link ProfileContext} for the test context.
     * @param testContext the current test context
     * @return the created {@link ProfileContext}
     * @since 0.7.2
     */
    public static ProfileContext createProfileContext(TestContext testContext) {
        if (testContext == null) {
            throw new IllegalArgumentException("testContext must not be null"); //$NON-NLS-1$
        }
        Holder.clean();
        ClassLoader loader = findClassLoader(testContext);
        return new ProfileContext(loader, new ParameterList(testContext.getEnvironmentVariables()));
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
        Holder.clean();
        LOG.debug("Create resource manipulator: {}", description.getClass().getName()); //$NON-NLS-1$
        GateProfile profile = loadProfile(testContext, description);
        String resourceName = description.getDriverScript().getResourceName();
        for (ResourceProfile resource : profile.getResources()) {
            if (resource.getName().equals(resourceName)) {
                return createManipulator(description, resource, arguments);
            }
        }
        throw new IOException(MessageFormat.format(
                Messages.getString("WindGateTestHelper.errorFailedToCreateResourceManipulator"), //$NON-NLS-1$
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
            LOG.debug("Configuring resource manipulator: {}", manipulator); //$NON-NLS-1$
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
        LOG.debug("Searching for a WindGate profile: {}", profileName); //$NON-NLS-1$

        ProfileContext profileContext = createProfileContext(testContext);
        URL url = profileContext.getClassLoader().getResource(MessageFormat.format(
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
                    Messages.getString("WindGateTestHelper.errorMissingProfile"), //$NON-NLS-1$
                    description.getClass().getName(),
                    description.getProfileName()));
        }

        LOG.debug("Loading a WindGate profile: {}", url); //$NON-NLS-1$
        try {
            Properties p = new Properties();
            try (InputStream input = url.openStream()) {
                p.load(input);
            }
            LOG.debug("Resolving a WindGate profile: {}", url); //$NON-NLS-1$
            GateProfile profile = GateProfile.loadFrom(profileName, p, profileContext);
            return profile;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("WindGateTestHelper.errorFailedToLoadProfile"), //$NON-NLS-1$
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
                        Messages.getString("WindGateTestHelper.errorInvalidFilePath"), //$NON-NLS-1$
                        file), e);
                return null;
            }
        }
        return null;
    }

    private static File findFileOnHomePath(TestContext testContext, String path) {
        assert testContext != null;
        assert path != null;
        String home = testContext.getEnvironmentVariables().get(ENV_FRAMEWORK_HOME);
        if (home != null) {
            File file = new File(home, path);
            if (file.exists()) {
                return file;
            }
        } else {
            LOG.warn(MessageFormat.format(
                    Messages.getString("WindGateTestHelper.warnUndefinedEnvironmentVariable"), //$NON-NLS-1$
                    ENV_FRAMEWORK_HOME));
        }
        return null;
    }

    private static ClassLoader findClassLoader(TestContext testContext) {
        assert testContext != null;
        ClassLoader current = testContext.getClassLoader();
        File pluginDirectory = findFileOnHomePath(testContext, PRODUCTION_PLUGIN_DIRECTORY);
        if (pluginDirectory == null || pluginDirectory.isDirectory() == false) {
            return current;
        }
        synchronized (CACHE_TEMPORARY_LOADER) {
            Holder holder = CACHE_TEMPORARY_LOADER.get(testContext);
            if (holder != null) {
                assert holder.get() != null;
                assert holder.get() == testContext;
                if (holder.directory.equals(pluginDirectory)) {
                    return holder.loader;
                }
            }
            List<URL> pluginLibraries = new ArrayList<>();
            for (File file : list(pluginDirectory)) {
                if (file.isFile() && file.getName().endsWith(PLUGIN_EXTENSION)) {
                    try {
                        URL url = file.toURI().toURL();
                        pluginLibraries.add(url);
                    } catch (Exception e) {
                        LOG.warn(MessageFormat.format(
                                Messages.getString("WindGateTestHelper.errorInvalidPluginPath"), //$NON-NLS-1$
                                file), e);
                    }
                }
            }
            if (pluginLibraries.isEmpty()) {
                return current;
            }
            PluginClassLoader pluginClassLoader = PluginClassLoader.newInstance(current, pluginLibraries);
            CACHE_TEMPORARY_LOADER.put(testContext, new Holder(testContext, pluginClassLoader, pluginDirectory));
            return pluginClassLoader;
        }
    }

    private static List<File> list(File file) {
        return Optional.ofNullable(file.listFiles())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
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
        Holder.clean();
        LOG.debug("Preparing object: {}", object); //$NON-NLS-1$
        boolean succeed = false;
        try {
            object.prepare();
            succeed = true;
            return object;
        } finally {
            if (succeed == false) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("WindGateTestHelper.errorFailedToPrepareObject"), //$NON-NLS-1$
                        object));
                try {
                    object.close();
                } catch (IOException e) {
                    LOG.warn(Messages.getString("WindGateTestHelper.errorFailedToCloseObject"), e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Disposes WindGate plug-in class loader.
     * @param loader the target plug-in class loader
     * @since 0.7.2
     */
    public static void disposePluginClassLoader(PluginClassLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null"); //$NON-NLS-1$
        }
        disposePluginClassLoader0(loader);
        Holder.clean();
    }

    static void disposePluginClassLoader0(PluginClassLoader loader) {
        assert loader != null;
        JdbcDriverCleaner.runIn(loader);
        ApplicationLauncher.disposeClassLoader(loader);
    }

    private static final class Holder extends WeakReference<TestContext> {

        private static final ReferenceQueue<TestContext> QUEUE = new ReferenceQueue<>();

        final PluginClassLoader loader;

        final File directory;

        Holder(TestContext referent, PluginClassLoader loader, File directory) {
            super(referent, QUEUE);
            this.loader = loader;
            this.directory = directory;
        }

        public static void clean() {
            while (true) {
                Holder next = (Holder) QUEUE.poll();
                if (next == null) {
                    break;
                }
                disposePluginClassLoader0(next.loader);
            }
        }
    }

    private WindGateTestHelper() {
        return;
    }
}
