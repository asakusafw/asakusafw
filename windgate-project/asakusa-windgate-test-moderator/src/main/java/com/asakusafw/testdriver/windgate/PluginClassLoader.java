/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Class loader for loading plug-ins.
 * Clients should not use this class directly.
 * @since 0.7.2
 */
public class PluginClassLoader extends URLClassLoader {

    private static final Map<Class<?>, byte[]> CACHE_CLASS_BYTES = new WeakHashMap<Class<?>, byte[]>();

    private final Map<String, Class<?>> directLoaded = new HashMap<String, Class<?>>();

    /**
     * Creates a new instance.
     * @param parent the parent class loader
     * @param urls the plug-in URLs
     */
    public PluginClassLoader(ClassLoader parent, URL... urls) {
        super(urls, parent);
    }

    /**
     * Creates a new instance.
     * @param parent the parent class loader
     * @param urls the plug-in URLs
     */
    public PluginClassLoader(ClassLoader parent, List<URL> urls) {
        this(parent, toArray(urls));
    }

    /**
     * Creates a new instance within current context.
     * @param parent the parent class loader
     * @param urls the plug-in URLs
     * @return the created instance
     */
    public static PluginClassLoader newInstance(final ClassLoader parent, final List<URL> urls) {
        return AccessController.doPrivileged(new PrivilegedAction<PluginClassLoader>() {
            @Override
            public PluginClassLoader run() {
                return new PluginClassLoader(parent, urls);
            }
        });
    }

    private static URL[] toArray(List<URL> urls) {
        if (urls == null) {
            throw new IllegalArgumentException("urls must not be null"); //$NON-NLS-1$
        }
        return urls.toArray(new URL[urls.size()]);
    }

    /**
     * Directly load the class using this class loader.
     * @param aClass the target class
     * @return the loaded class
     * @throws IOException if failed to load the class by I/O error
     */
    public Class<?> loadDirect(Class<?> aClass) throws IOException {
        if (aClass.getClassLoader() == this) {
            return aClass;
        }
        String className = aClass.getName();
        synchronized (directLoaded) {
            if (directLoaded.containsKey(className)) {
                return directLoaded.get(aClass.getName());
            }
            byte[] bytes = getClassBytes(aClass);
            Class<?> defined = defineClass(aClass.getName(), bytes, 0, bytes.length);
            resolveClass(defined);
            directLoaded.put(className, defined);
            return defined;
        }
    }

    private byte[] getClassBytes(Class<?> aClass) throws IOException {
        synchronized (CACHE_CLASS_BYTES) {
            byte[] bytes = CACHE_CLASS_BYTES.get(aClass);
            if (bytes != null) {
                return bytes;
            }
        }
        ClassLoader cl = aClass.getClassLoader();
        InputStream input = cl.getResourceAsStream(aClass.getName().replace('.', '/') + ".class"); //$NON-NLS-1$
        if (input == null) {
            throw new FileNotFoundException(MessageFormat.format(
                    Messages.getString("PluginClassLoader.errorFailedToLoadClassBytes"), //$NON-NLS-1$
                    aClass.getName()));
        }
        byte[] results;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                byte[] buf = new byte[256];
                while (true) {
                    int read = input.read(buf);
                    if (read < 0) {
                        break;
                    }
                    output.write(buf, 0, read);
                }
            } finally {
                output.close();
            }
            results = output.toByteArray();
        } finally {
            input.close();
        }
        synchronized (CACHE_CLASS_BYTES) {
            CACHE_CLASS_BYTES.put(aClass, results);
        }
        return results;
    }
}
