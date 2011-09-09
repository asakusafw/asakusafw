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
package com.asakusafw.windgate.stream.file;

import com.asakusafw.windgate.core.resource.ResourceProfile;

/**
 * A structured profile for {@link FileResourceMirror}.
 * @since 0.2.2
 */
public class FileProfile {

    private final String resourceName;

    private final ClassLoader classLoader;

    /**
     * Creates a new instance.
     * @param resourceName the resource name
     * @param classLoader current class loader
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileProfile(String resourceName, ClassLoader classLoader) {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName must not be null"); //$NON-NLS-1$
        }
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null"); //$NON-NLS-1$
        }
        this.resourceName = resourceName;
        this.classLoader = classLoader;
    }

    /**
     * Returns the resource name.
     * @return the resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the current class loader.
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Converts {@link ResourceProfile} into {@link FileProfile}.
     * @param profile target profile
     * @return the converted profile
     * @throws IllegalArgumentException if profile is not valid, or any parameter is {@code null}
     */
    public static FileProfile convert(ResourceProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        String resourceName = profile.getName();
        ClassLoader classLoader = profile.getClassLoader();
        return new FileProfile(resourceName, classLoader);
    }
}
