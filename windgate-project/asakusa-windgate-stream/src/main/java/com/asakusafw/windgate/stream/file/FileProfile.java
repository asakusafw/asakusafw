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
package com.asakusafw.windgate.stream.file;

import java.io.File;
import java.text.MessageFormat;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.stream.WindGateStreamLogger;

/**
 * A structured profile for {@link FileResourceMirror}.
 * @since 0.2.4
 */
public class FileProfile {

    static final WindGateLogger WGLOG = new WindGateStreamLogger(FileProfile.class);

    /**
     * The profile key of base path.
     * This value can includes environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    public static final String KEY_BASE_PATH = "basePath";

    private final String resourceName;

    private final ClassLoader classLoader;

    private final File basePath;

    /**
     * Creates a new instance.
     * @param resourceName the resource name
     * @param classLoader current class loader
     * @param basePath base path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileProfile(String resourceName, ClassLoader classLoader, File basePath) {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName must not be null"); //$NON-NLS-1$
        }
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null"); //$NON-NLS-1$
        }
        if (basePath == null) {
            throw new IllegalArgumentException("basePath must not be null"); //$NON-NLS-1$
        }
        this.resourceName = resourceName;
        this.classLoader = classLoader;
        this.basePath = basePath;
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
     * Returns the base path of target storage.
     * @return the base path
     */
    public File getBasePath() {
        return basePath;
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
        ClassLoader classLoader = profile.getContext().getClassLoader();
        String basePath = extract(profile, KEY_BASE_PATH, false);
        return new FileProfile(resourceName, classLoader, new File(basePath));
    }

    private static String extract(ResourceProfile profile, String configKey, boolean mandatory) {
        assert profile != null;
        assert configKey != null;
        String value = profile.getConfiguration().get(configKey);
        if (value == null) {
            if (mandatory == false) {
                return null;
            } else {
                WGLOG.error("E00001",
                        profile.getName(),
                        configKey,
                        null);
                throw new IllegalArgumentException(MessageFormat.format(
                        "Resource \"{0}\" must declare \"{1}\"",
                        profile.getName(),
                        configKey));
            }
        }
        try {
            return profile.getContext().getContextParameters().replace(value.trim(), true);
        } catch (IllegalArgumentException e) {
            WGLOG.error(e, "E00001",
                    profile.getName(),
                    configKey,
                    value);
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to resolve environment variables: {2} (resource={0}, property={1})",
                    profile.getName(),
                    configKey,
                    value), e);
        }
    }
}