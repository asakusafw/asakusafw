/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.utils.launcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Launches tool classes.
 * <p>
 * This application requires just the one argument, which denotes the path to the <em>launch script</em>.
 * Each launch script must be a Java properties file with following entries:
 * </p>
 * <!-- CHECKSTYLE:OFF JavadocStyle -->
 * <dl id="launch-script">
 *   <dt> {@code "main"} </dt>
 *     <dd> The application main class. </dd>
 *     <dd> It must have a valid <em>main method</em>. </dd>
 *   <dt> {@code "classpath.<integer>"} </dt>
 *     <dd> The classpath entry. </dd>
 *     <dd> It must be a valid path on the local file system. </dd>
 *   <dt> {@code "argument.<integer>"} </dt>
 *     <dd> The application argument. </dd>
 * </dl>
 * <!-- CHECKSTYLE:ON JavadocStyle -->
 */
public final class Launcher {

    static final String KEY_MAIN_CLASS = "main"; //$NON-NLS-1$

    static final String KEY_CLASSPATH_PREFIX = "classpath."; //$NON-NLS-1$

    static final String KEY_ARGUMENT_PREFIX = "argument."; //$NON-NLS-1$

    private Launcher() {
        return;
    }

    /**
     * The program entry.
     * @param args path to the launch script
     * @throws LauncherException if error occurred while launching the application
     * @throws Throwable if error occurred in the target application
     */
    public static void main(String... args) throws Throwable {
        if (args.length != 1) {
            throw new LauncherException("Usage: java -jar <this.jar> /path/to/launch-script.properties");
        }
        File script = new File(args[0]);
        Properties properties = new Properties();
        try (InputStream input = new BufferedInputStream(new FileInputStream(script))) {
            properties.load(input);
        } catch (IOException e) {
            throw new LauncherException(MessageFormat.format(
                    "exception occurred while loading launch script: {0}",
                    script), e);
        }
        launch(properties);
    }

    /**
     * Launches the target application.
     * @param properties the launch properties
     * @throws Throwable if error occurred while launching
     * @see <a href="#launch-script">launch script</a>
     */
    public static void launch(Properties properties) throws Throwable {
        String mainClass = properties.getProperty(KEY_MAIN_CLASS);
        if (mainClass == null) {
            throw new LauncherException(MessageFormat.format(
                    "the properties must contain {0}",
                    KEY_ARGUMENT_PREFIX));
        }
        List<String> arguments = parseList(KEY_ARGUMENT_PREFIX, properties);
        List<String> classpathStrings = parseList(KEY_CLASSPATH_PREFIX, properties);
        List<File> classpath = new ArrayList<>();
        for (String s : classpathStrings) {
            classpath.add(new File(s));
        }
        launch(classpath, mainClass, arguments);
    }

    /**
     * Launches the target application.
     * @param classpath the application class path
     * @param mainClass the application entry class
     * @param arguments the application arguments
     * @throws Throwable if error occurred while launching
     */
    public static void launch(List<File> classpath, String mainClass, List<String> arguments) throws Throwable {
        try (Context context = new Context(classpath, mainClass, arguments)) {
            context.launch();
        }
    }

    static List<String> parseList(String prefix, Map<?, ?> map) {
        SortedMap<Integer, String> numeric = new TreeMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if ((entry.getKey() instanceof String) == false) {
                continue;
            }
            String key = (String) entry.getKey();
            if (key.startsWith(prefix) == false) {
                continue;
            }
            try {
                Integer index = Integer.valueOf(key.substring(prefix.length()));
                if (numeric.containsKey(index)) {
                    throw new LauncherException(MessageFormat.format(
                            "duplicate index: {0}.{1,number}",
                            prefix,
                            index));
                }
                numeric.put(index, String.valueOf(entry.getValue()));
            } catch (NumberFormatException e) {
                throw new LauncherException(MessageFormat.format(
                        "invalid property key: {1} (must be the form of \"{0}<integer>\")",
                        prefix,
                        key), e);
            }
        }
        return new ArrayList<>(numeric.values());
    }

    private static final class Context implements AutoCloseable {

        private final URLClassLoader classLoader;

        private final String mainClass;

        private final List<String> arguments;

        Context(List<File> classpath, String mainClass, List<String> arguments) {
            List<URL> urls = new ArrayList<>();
            for (File file : classpath) {
                if (file.exists() == false) {
                    continue;
                }
                try {
                    urls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    // ignores invalid classpath entry
                    e.printStackTrace();
                }
            }
            this.classLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
            this.mainClass = mainClass;
            this.arguments = new ArrayList<>(arguments);
        }

        public void launch() throws Throwable {
            try (ClassLoaderContext context = new ClassLoaderContext(classLoader)) {
                Class<?> target = classLoader.loadClass(mainClass);
                Method method = target.getMethod("main", String[].class); //$NON-NLS-1$
                method.invoke(null, new Object[] { arguments.toArray(new String[arguments.size()]) });
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (ReflectiveOperationException e) {
                throw new LauncherException(MessageFormat.format(
                        "error occurred while loading application: {0}",
                        mainClass), e);
            }
        }

        @Override
        public void close() {
            try {
                classLoader.close();
            } catch (IOException e) {
                // ignores exception while closing class loaders
                e.printStackTrace();
            }
        }
    }
}
