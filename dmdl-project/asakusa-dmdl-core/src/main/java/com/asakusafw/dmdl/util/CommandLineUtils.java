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
package com.asakusafw.dmdl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.source.CompositeSourceRepository;
import com.asakusafw.dmdl.source.DmdlSourceDirectory;
import com.asakusafw.dmdl.source.DmdlSourceFile;
import com.asakusafw.dmdl.source.DmdlSourceRepository;

/**
 * Utilities for command line interfaces.
 * @since 0.2.0
 */
public final class CommandLineUtils {

    static final Logger LOG = LoggerFactory.getLogger(CommandLineUtils.class);

    /**
     * Parses a name of charcter-set.
     * @param charsetNameOrNull target name, or {@code null}
     * @return the represented charset, or the default charset if a name is not specified
     */
    public static Charset parseCharset(String charsetNameOrNull) {
        if (charsetNameOrNull == null || charsetNameOrNull.isEmpty()) {
            return Charset.defaultCharset();
        }
        return Charset.forName(charsetNameOrNull);
    }

    /**
     * Parses a name of locale.
     * @param localeNameOrNull target name, or {@code null}
     * @return the represented locale, or the default locale if a name is not specified
     */
    public static Locale parseLocale(String localeNameOrNull) {
        if (localeNameOrNull == null || localeNameOrNull.isEmpty()) {
            return Locale.getDefault();
        }
        String[] segments = localeNameOrNull.trim().split("_", 3); //$NON-NLS-1$
        if (segments.length == 0 || segments[0].isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid locale name: {0}", //$NON-NLS-1$
                    localeNameOrNull));
        }
        String language = segments[0];
        String country = segments.length > 1 ? segments[1] : ""; //$NON-NLS-1$
        String variant = segments.length > 2 ? segments[2] : ""; //$NON-NLS-1$
        assert segments.length <= 3;
        return new Locale(language, country, variant);
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
     * Creates a source repository from the specified file list.
     * @param files target file list
     * @param cs source charset encoding
     * @return the created repository which traverses the specified source paths
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static DmdlSourceRepository buildRepository(List<File> files, Charset cs) {
        if (files == null) {
            throw new IllegalArgumentException("files must not be null"); //$NON-NLS-1$
        }
        if (cs == null) {
            throw new IllegalArgumentException("cs must not be null"); //$NON-NLS-1$
        }
        List<DmdlSourceRepository> repositories = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                repositories.add(new DmdlSourceFile(Collections.singletonList(file), cs));
            } else if (file.isDirectory()) {
                repositories.add(new DmdlSourceDirectory(
                        file,
                        cs,
                        Pattern.compile(".*\\.dmdl"), //$NON-NLS-1$
                        Pattern.compile("^\\..*"))); //$NON-NLS-1$
            } else {
                LOG.warn(MessageFormat.format(
                        Messages.getString("CommandLineUtils.warnMissingFile"), file)); //$NON-NLS-1$
            }
        }
        if (repositories.size() == 1) {
            return repositories.get(0);
        }
        return new CompositeSourceRepository(repositories);
    }

    /**
     * Creates a class loader for loading plug-ins.
     * @param parent parent class loader, or {@code null} to use the system class loader
     * @param files plug-in class paths (*.jar file or class path directory)
     * @return the created class loader
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ClassLoader buildPluginLoader(final ClassLoader parent, List<File> files) {
        if (files == null) {
            throw new IllegalArgumentException("files must not be null"); //$NON-NLS-1$
        }
        final List<URL> pluginLocations = new ArrayList<>();
        for (File file : files) {
            try {
                if (file.exists() == false) {
                    throw new FileNotFoundException(file.getAbsolutePath());
                }
                URL url = file.toURI().toURL();
                pluginLocations.add(url);
            } catch (IOException e) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("CommandLineUtils.warnInvalidPlugIn"), //$NON-NLS-1$
                        file),
                        e);
            }
        }
        ClassLoader serviceLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                URLClassLoader loader = new URLClassLoader(
                        pluginLocations.toArray(new URL[pluginLocations.size()]),
                        parent);
                return loader;
            }
        });
        return serviceLoader;
    }

    private CommandLineUtils() {
        return;
    }
}
