/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a classpath of WindGate bootstrap.
 * @since 0.10.0
 */
public class Classpath {

    private final List<Path> entries = new ArrayList<>();

    /**
     * Adds an entry into this classpath.
     * @param entry the target file or directory
     * @param mandatory {@code true} if it is mandatory, otherwise {@code false}
     * @return this
     * @throws IllegalStateException if the mandatory entry is not found
     */
    public Classpath add(Path entry, boolean mandatory) {
        if (Files.exists(entry)) {
            entries.add(entry);
        } else if (mandatory) {
            throw new IllegalStateException(MessageFormat.format(
                    "classpath entry must exist: {0}",
                    entry));
        }
        return this;
    }

    /**
     * Adds entries in the given directory into this classpath.
     * @param directory the target directory
     * @param mandatory {@code true} if it is mandatory, otherwise {@code false}
     * @return this
     * @throws IllegalStateException if the mandatory directory is not found
     */
    public Classpath addEntries(Path directory, boolean mandatory) {
        if (Files.isDirectory(directory)) {
            try {
                Files.list(directory).forEach(entries::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (mandatory) {
            throw new IllegalStateException(MessageFormat.format(
                    "classpath entry must exist: {0}",
                    directory));
        }
        return this;
    }

    /**
     * Executes the given main class.
     * @param base the base class loader
     * @param className the main class name
     * @param args the program arguments
     * @throws IllegalStateException if error occurred while running main class
     */
    public void exec(ClassLoader base, String className, String... args) {
        try (Session session = session(base)) {
            session.exec(className, args);
        }
    }

    /**
     * Returns a new classpath session.
     * @param base the base class loader
     * @return the created session
     */
    public Session session(ClassLoader base) {
        return new Session(URLClassLoader.newInstance(
                entries.stream()
                        .filter(Files::exists)
                        .flatMap(it -> {
                            try {
                                return Stream.of(it.toUri().toURL());
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                return Stream.empty();
                            }
                        })
                        .toArray(URL[]::new),
                base));
    }

    @Override
    public String toString() {
        return entries.stream()
                .map(Path::toString)
                .collect(Collectors.joining(", ", "Classpath(", ")"));
    }

    static ClassLoader setContextClassLoader(ClassLoader classLoader) {
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            return old;
        });
    }

    /**
     * Represents a session of {@link Classpath}.
     * @since 0.5.0
     */
    public static class Session implements Closeable {

        private final ClassLoader previous;

        private URLClassLoader active;

        Session(URLClassLoader active) {
            this.previous = setContextClassLoader(active);
            this.active = active;
        }

        /**
         * Returns the class loader of this session.
         * @return the class loader
         */
        public ClassLoader getClassLoader() {
            ClassLoader cl = active;
            if (cl == null) {
                throw new IllegalStateException();
            }
            return cl;
        }

        /**
         * Executes the given main class.
         * @param className the main class name
         * @param args the program arguments
         * @throws IllegalStateException if error occurred while running main class
         */
        public void exec(String className, String... args) {
            try {
                Class<?> mainClass = Class.forName(className, false, getClassLoader());
                Method mainMethod = mainClass.getMethod("main", String[].class);
                mainMethod.invoke(null, new Object[] { args });
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw new IllegalStateException(MessageFormat.format(
                        "error occurred while running {0}.main({1})",
                        className,
                        String.join(", ", args)), cause);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(MessageFormat.format(
                        "error occurred while launching main class: {0}",
                        className), e);
            }
        }

        @Override
        public void close() {
            if (active != null) {
                // NOTE: we never close the class loader, because Hadoop may register some shutdown hooks
                setContextClassLoader(previous);
                active = null;
            }
        }
    }

}
