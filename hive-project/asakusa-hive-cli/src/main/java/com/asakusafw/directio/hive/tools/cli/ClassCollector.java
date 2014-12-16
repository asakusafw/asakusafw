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
package com.asakusafw.directio.hive.tools.cli;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects classes in class-path.
 * @since 0.7.0
 */
public class ClassCollector {

    static final Logger LOG = LoggerFactory.getLogger(ClassCollector.class);

    private static final Selector THROUGH_SELECTOR = new Selector() {

        @Override
        public boolean accept(Class<?> aClass) {
            return true;
        }
    };

    private static final String CLASS_EXTENSION = ".class"; //$NON-NLS-1$

    private final ClassLoader classLoader;

    private final Selector selector;

    private final Map<String, Class<?>> classes;

    /**
     * Creates a new instance with a filter which accepts any classes.
     * @param classLoader the project class loader
     */
    public ClassCollector(ClassLoader classLoader) {
        this(classLoader, THROUGH_SELECTOR);
    }

    /**
     * Creates a new instance.
     * @param classLoader the project class loader
     * @param selector the class selector
     */
    public ClassCollector(ClassLoader classLoader, Selector selector) {
        this.classLoader = classLoader;
        this.selector = selector;
        this.classes = new HashMap<String, Class<?>>();
    }

    /**
     * Inspects class-path and collect classes in the target path.
     * @param classPath target class file or directory
     */
    public void inspect(File classPath) {
        LinkedList<Entry> work = new LinkedList<Entry>();
        work.add(new Entry(Collections.<String>emptyList(), classPath));
        while (work.isEmpty() == false) {
            Entry next = work.removeFirst();
            if (isHidden(next.file)) {
                continue;
            } else if (next.file.isDirectory()) {
                for (File child : next.file.listFiles()) {
                    List<String> segments = new ArrayList<String>(next.segments);
                    segments.add(child.getName());
                    Entry entry = new Entry(segments, child);
                    work.add(entry);
                }
            } else if (isClassFile(next.file)) {
                assert next.segments.isEmpty() == false;
                String className = toClassName(next.segments);
                register(className);
            }
        }
    }

    /**
     * Returns the collected classes.
     * @return classes
     */
    public Collection<Class<?>> getClasses() {
        return classes.values();
    }

    /**
     * Returns a class which {@link #inspect(File) collected} by this.
     * @param name the fully-qualified class name
     * @return the related class, otherwise {@code null} if it is not found
     */
    public Class<?> findClass(String name) {
        return classes.get(name);
    }

    private void register(String className) {
        LOG.debug("Registering: {}", className); //$NON-NLS-1$
        try {
            Class<?> aClass = classLoader.loadClass(className);
            if (selector.accept(aClass)) {
                classes.put(className, aClass);
                LOG.debug("Registered: {}", className); //$NON-NLS-1$
            } else {
                LOG.debug("Filtered: {}", className); //$NON-NLS-1$
            }
        } catch (ClassNotFoundException e) {
            LOG.warn(MessageFormat.format(
                    "Failed to load a class: {0}",
                    className));
        }
    }

    private static boolean isHidden(File file) {
        return file.getName().startsWith(".") //$NON-NLS-1$
                || file.exists() == false
                || file.isHidden();
    }

    private static boolean isClassFile(File file) {
        return file.getName().endsWith(CLASS_EXTENSION) && file.isFile();
    }

    private static String toClassName(List<String> segments) {
        assert segments.isEmpty() == false;
        String lastSegment = segments.get(segments.size() - 1);
        assert lastSegment.endsWith(CLASS_EXTENSION);
        StringBuilder buf = new StringBuilder();
        for (int i = 0, n = segments.size() - 1; i < n; i++) {
            buf.append(segments.get(i));
            buf.append('.');
        }
        buf.append(lastSegment.substring(0, lastSegment.length() - CLASS_EXTENSION.length()));
        return buf.toString();
    }


    private static final class Entry {

        final List<String> segments;

        final File file;

        public Entry(List<String> segments, File file) {
            super();
            this.segments = segments;
            this.file = file;
        }
    }

    /**
     * Filters classes to collect.
     */
    public interface Selector {

        /**
         * Returns whether or not accepts target class.
         * @param aClass target class
         * @return {@code true} if this accepts the target class, otherwise {@code false}
         */
        boolean accept(Class<?> aClass);
    }
}
