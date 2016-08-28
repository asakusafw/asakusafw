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
package com.asakusafw.yaess.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.asakusafw.yaess.core.Extension;
import com.asakusafw.yaess.core.ExtensionHandler;
import com.asakusafw.yaess.core.YaessLogger;

/**
 * Utilities for command line interfaces.
 * @since 0.2.3
 * @version 0.8.0
 */
public final class CommandLineUtil {

    static final YaessLogger YSLOG = new YaessBootstrapLogger(CommandLineUtil.class);

    static final Logger LOG = LoggerFactory.getLogger(CommandLineUtil.class);

    /**
     * Prefix of system properties used for log context.
     */
    public static final String LOG_CONTEXT_PREFIX = "com.asakusafw.yaess.log.";

    /**
     * The scheme name of Java class path.
     */
    public static final String SCHEME_CLASSPATH = "classpath";

    /**
     * Prepares log context.
     * If current system properties contain keys with a special prefix
     *  (described in {@link #LOG_CONTEXT_PREFIX}),
     *  then this method will put each key-value pairs into log MDC.
     */
    public static void prepareLogContext() {
        Map<String, String> registered = new TreeMap<>();
        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if ((entry.getKey() instanceof String) == false || (entry.getValue() instanceof String) == false) {
                continue;
            }
            String key = (String) entry.getKey();
            if (key.startsWith(LOG_CONTEXT_PREFIX) == false) {
                continue;
            }
            String value = (String) entry.getValue();
            String name = key.substring(LOG_CONTEXT_PREFIX.length());
            MDC.put(name, value);
            registered.put(name, value);
        }
        LOG.debug("Log context is prepared: {}",
                registered);
    }

    /**
     * Loads properties from the specified file.
     * @param path path to the target properties
     * @return the loaded properties
     * @throws IOException if failed to load the properties
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public static Properties loadProperties(File path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Loading properties: {}", path);
        try (FileInputStream in = new FileInputStream(path)) {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    /**
     * Parses a string of file list separated by the platform dependent path separator.
     * @param fileListOrNull target string, or {@code null}
     * @return the represented file list, or an empty list if not specified
     */
    public static List<File> parseFileList(String fileListOrNull) {
        if (fileListOrNull == null || fileListOrNull.isEmpty()) {
            return Collections.emptyList();
        }
        List<File> results = new ArrayList<>();
        int start = 0;
        while (true) {
            int index = fileListOrNull.indexOf(File.pathSeparatorChar, start);
            if (index < 0) {
                break;
            }
            if (start != index) {
                results.add(new File(fileListOrNull.substring(start, index).trim()));
            }
            start = index + 1;
        }
        results.add(new File(fileListOrNull.substring(start).trim()));
        return results;
    }

    /**
     * Creates a class loader for loading plug-ins.
     * @param parent parent class loader, or {@code null} to use the system class loader
     * @param files plug-in class paths (*.jar file or class path directory)
     * @return the created class loader
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ClassLoader buildPluginLoader(ClassLoader parent, List<File> files) {
        if (files == null) {
            throw new IllegalArgumentException("files must not be null"); //$NON-NLS-1$
        }
        List<URL> pluginLocations = new ArrayList<>();
        for (File file : files) {
            try {
                if (file.exists() == false) {
                    throw new FileNotFoundException(MessageFormat.format(
                            "Plug-in file/directory is not found \"{0}\"",
                            file.getAbsolutePath()));
                }
                URL url = file.toURI().toURL();
                pluginLocations.add(url);
            } catch (IOException e) {
                YSLOG.warn(e, "W99001", file.getAbsolutePath());
            }
        }
        ClassLoader serviceLoader = AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
            URLClassLoader loader = new URLClassLoader(
                    pluginLocations.toArray(new URL[pluginLocations.size()]),
                    parent);
            return loader;
        });
        return serviceLoader;
    }

    /**
     * Loads extensions from extended arguments.
     * @param classLoader the extension class loader
     * @param arguments the target arguments
     * @return the loaded extensions
     * @throws IllegalArgumentException if extensions are something wrong
     */
    public static List<Extension> loadExtensions(ClassLoader classLoader, List<ExtendedArgument> arguments) {
        List<Extension> results = new ArrayList<>();
        ServiceLoader<ExtensionHandler> services = ServiceLoader.load(ExtensionHandler.class, classLoader);
        for (ExtendedArgument argument : arguments) {
            String name = argument.getName();
            String value = argument.getValue();
            boolean consumed = false;
            for (ExtensionHandler handler : services) {
                try {
                    Extension extension = handler.handle(name, value);
                    if (extension != null) {
                        results.add(extension);
                        consumed = true;
                        break;
                    }
                } catch (IOException e) {
                    YSLOG.error(e, "E99001", name, value);
                    for (Extension ext : results) {
                        try {
                            ext.close();
                        } catch (IOException inner) {
                            e.addSuppressed(inner);
                        }
                    }
                    throw new IllegalArgumentException(MessageFormat.format(
                            "invalid extended argument \"{0}\"",
                            argument), e);
                }
            }
            if (consumed == false) {
                YSLOG.error("E99001", name, value);
                throw new IllegalArgumentException(MessageFormat.format(
                        "there is no available extension handler \"{0}\"",
                        argument));
            }
        }
        return results;
    }

    private CommandLineUtil() {
        return;
    }
}
