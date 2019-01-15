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
package com.asakusafw.utils.java.jsr199.testing;

import java.util.HashMap;
import java.util.Map;

/**
 * A class loader that handles class files on the heap.
 */
public class DirectClassLoader extends ClassLoader {

    private final Map<String, byte[]> classes;

    /**
     * Creates a new instance.
     * @param parent the parent class loader
     */
    public DirectClassLoader(ClassLoader parent) {
        super(parent);
        this.classes = new HashMap<>();
    }

    /**
     * Adds a class file for this class loader.
     * @param name the binary name of the target class
     * @param content the class file contents
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public synchronized void add(String name, byte[] content) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (content == null) {
            throw new IllegalArgumentException("content must not be null"); //$NON-NLS-1$
        }
        classes.put(name, content);
    }

    @Override
    protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classes.remove(name);
        if (bytes == null) {
            return super.findClass(name);
        }
        return defineClass(name, bytes, 0, bytes.length, null);
    }
}
