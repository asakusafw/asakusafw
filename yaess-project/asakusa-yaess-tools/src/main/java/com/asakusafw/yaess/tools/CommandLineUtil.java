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
package com.asakusafw.yaess.tools;

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
import java.util.Properties;

/**
 * Utilities for command line interfaces.
 * @since 0.2.3
 */
public final class CommandLineUtil {

    /**
     * The scheme name of Java class path.
     */
    public static final String SCHEME_CLASSPATH = "classpath";

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
                            "Failed to load plugin \"{0}\"",
                            file.getAbsolutePath()));
                }
                URL url = file.toURI().toURL();
                pluginLocations.add(url);
            } catch (IOException e) {
                e.printStackTrace();
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

    private CommandLineUtil() {
        return;
    }
}
