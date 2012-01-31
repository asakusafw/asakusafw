/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.windgate.file.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Array;

/**
 * Deserializes objects using a specified {@link ClassLoader}.
 * @since 0.2.2
 */
public class LoadingObjectInputStream extends ObjectInputStream {

    private final ClassLoader loader;

    /**
     * Creates a new instance.
     * @param in target input
     * @param loader the loader, or {@code null} to use the system class loader
     * @throws IOException if failed to initialize this object
     */
    public LoadingObjectInputStream(
            InputStream in,
            ClassLoader loader) throws IOException {
        super(in);
        if (loader == null) {
            this.loader = ClassLoader.getSystemClassLoader();
        } else {
            this.loader = loader;
        }
    }

    @Override
    protected Class<?> resolveClass(
            ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String name = desc.getName();
        if (name.startsWith("[")) {
            return resolveClassDesc(desc);
        }
        try {
            Class<?> loaded = loader.loadClass(name);
            return loaded;
        } catch (ClassNotFoundException e) {
            // may be primitive classes
            return super.resolveClass(desc);
        }
    }

    private Class<?> resolveClassDesc(
            ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        assert desc != null;
        String name = desc.getName();
        int dimensions = 0;
        for (int i = 0, n = name.length(); i < n; i++) {
            if (name.charAt(i) == '[') {
                dimensions++;
            } else {
                break;
            }
        }
        // invalid descriptor
        if (name.length() == dimensions) {
            return super.resolveClass(desc);
        }
        // not "L...;"
        if (name.charAt(dimensions) != 'L'
            || name.charAt(name.length() - 1) != ';') {
            return super.resolveClass(desc);
        }

        String internalName = name.substring(
                dimensions + 1,
                name.length() - 1);

        Class<?> loaded = loader.loadClass(internalName.replace('/', '.'));
        for (int i = 0; i < dimensions; i++) {
            loaded = Array.newInstance(loaded, 0).getClass();
        }
        return loaded;
    }
}