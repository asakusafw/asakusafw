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
package com.asakusafw.windgate.bootstrap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.WindGateLogger;

/**
 * Utilities for command line interfaces.
 * @since 0.2.2
 */
public final class CommandLineUtil {

    static final WindGateLogger WGLOG = new WindGateBootstrapLogger(CommandLineUtil.class);

    static final Logger LOG = LoggerFactory.getLogger(CommandLineUtil.class);

    /**
     * Prefix of system properties used for log context.
     */
    public static final String LOG_CONTEXT_PREFIX = "com.asakusafw.windgate.log.";

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
        Map<String, String> registered = new TreeMap<String, String>();
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
     * Returns the name of URI for hint.
     * @param uri the target URI
     * @return the name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static String toName(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null"); //$NON-NLS-1$
        }
        String path = uri.getSchemeSpecificPart();
        if (path == null) {
            return uri.toString();
        }
        String name = path.substring(path.lastIndexOf('/') + 1);
        if (name.endsWith(".properties")) {
            return name.substring(0, name.length() - ".properties".length());
        } else {
            return name;
        }
    }

    /**
     * Converts the path to the related URI.
     * @param path target path
     * @return the related URI
     * @throws URISyntaxException if failed to convert the path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static URI toUri(String path) throws URISyntaxException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        URI uri = new URI(path);
        if (uri.getScheme() == null || uri.getScheme().length() != 1) {
            return uri;
        }
        String os = System.getProperty("os.name", "UNKNOWN");
        LOG.debug("Current OS name: {}",
                os);
        if (os.toLowerCase().startsWith("windows") == false) {
            return uri;
        }

        File file = new File(path);
        uri = file.toURI();
        LOG.debug("Path \"{}\" may be an absolute path on Windows, converted into URI: {}",
                path,
                uri);
        return uri;
    }

    /**
     * Loads properties from the specified URI.
     * URI can have following forms.
     * <ul>
     * <li> no scheme - relative path from the current working directory (local file system) </li>
     * <li> &quot;classpath&quot; scheme - absolute path on {@code loader}'s class path </li>
     * <li> other scheme - as a URL </li>
     * </ul>
     * @param path path to the target properties
     * @param loader the class loader for the scheme &quot;classpath&quot;,
     *     or {@code null} to use the system class loader
     * @return the loaded properties
     * @throws IOException if failed to load the properties
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public static Properties loadProperties(URI path, ClassLoader loader) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Loading properties: {}", path);
        String scheme = path.getScheme();
        if (scheme == null) {
            File file = new File(path.getPath());
            LOG.debug("Loading properties from local path: {}", file);
            FileInputStream in = new FileInputStream(file);
            return loadProperties(path, in);
        } else if (scheme.equals(SCHEME_CLASSPATH)) {
            ClassLoader cl = loader == null ? ClassLoader.getSystemClassLoader() : loader;
            String rest = path.getSchemeSpecificPart();
            LOG.debug("Loading properties from class path: {}", rest);
            InputStream in = cl.getResourceAsStream(rest);
            if (in == null) {
                throw new FileNotFoundException(MessageFormat.format(
                        "Failed to load properties \"{0}\"",
                        path.toString()));
            }
            return loadProperties(path, in);
        } else {
            URL url = path.toURL();
            LOG.debug("Loading properties from URL: {}", url);
            InputStream in = url.openStream();
            return loadProperties(path, in);
        }
    }

    private static Properties loadProperties(URI uri, InputStream in) throws IOException {
        assert uri != null;
        assert in != null;
        try {
            Properties properties = new Properties();
            properties.load(new BufferedInputStream(in));
            return properties;
        } finally {
            in.close();
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
        List<File> results = new ArrayList<File>();
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
    public static ClassLoader buildPluginLoader(final ClassLoader parent, List<File> files) {
        if (files == null) {
            throw new IllegalArgumentException("files must not be null"); //$NON-NLS-1$
        }
        final List<URL> pluginLocations = new ArrayList<URL>();
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
                WGLOG.warn(e, "W99001",
                        file.getAbsolutePath());
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
    private static final Pattern PAIRS = Pattern.compile("(?<!\\\\),");
    private static final Pattern KEY_VALUE = Pattern.compile("(?<!\\\\)=");

    /**
     * Parses the specified arguments string and returns key-value pairs.
     * The arguments string is represented as following syntax with
     * {@code ArgumentList} as the goal symbol.
     * The each result pair will have {@code Value_key} as its key,
     * and {@code Value_value} as value.
<pre><code>
ArgumentList:
    ArgumentList "," Argument
    Argument
    ","
    (Empty)

Argument:
    Value_key "=" Value_value

Value:
    Character*

Character:
    any character except ",", "=", "\\"
    "\" any character
</code></pre>
     * @param arguments the arguments represented in a string, or {@code null} as empty arguments
     * @return the parsed key-value pairs
     */
    public static ParameterList parseArguments(String arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return new ParameterList();
        }
        Map<String, String> results = new LinkedHashMap<String, String>();
        String[] pairs = PAIRS.split(arguments);
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            String[] kv = KEY_VALUE.split(pair);
            if (kv.length == 0) {
                // in the case of "=", the regex engine returns an empty array
                addArgument(results, "", "");
            } else if (kv.length == 1 && kv[0].equals(pair) == false) {
                // in the case of "key=", the regex engine return returns only a key
                addArgument(results, unescape(kv[0]), "");
            } else if (kv.length == 2) {
                addArgument(results, unescape(kv[0]), unescape(kv[1]));
            } else {
                WGLOG.warn("W99002",
                        pair);
            }
        }
        return new ParameterList(results);
    }

    private static void addArgument(Map<String, String> results, String key, String value) {
        assert results != null;
        assert key != null;
        assert value != null;
        if (results.containsKey(key)) {
            WGLOG.warn("W99003",
                    key,
                    value);
        } else {
            results.put(key, value);
        }
    }

    private static String unescape(String string) {
        assert string != null;
        StringBuilder buf = new StringBuilder();
        int start = 0;
        while (true) {
            int index = string.indexOf('\\', start);
            if (index < 0) {
                break;
            }
            buf.append(string.substring(start, index));
            if (index != string.length() - 1) {
                buf.append(string.charAt(index + 1));
                start = index + 2;
            } else {
                buf.append(string.charAt(index));
                start = index + 1;
            }
        }
        if (start < string.length()) {
            buf.append(string.substring(start));
        }
        return buf.toString();
    }

    private CommandLineUtil() {
        return;
    }
}
