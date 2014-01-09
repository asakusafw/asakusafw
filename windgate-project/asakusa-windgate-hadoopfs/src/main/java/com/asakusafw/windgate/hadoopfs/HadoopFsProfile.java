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
package com.asakusafw.windgate.hadoopfs;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.hadoopfs.ssh.SshProfile;

/**
 * A structured profile for {@link HadoopFsMirror}.
 * @since 0.2.2
 * @version 0.4.0
 */
public class HadoopFsProfile {

    static final WindGateLogger WGLOG = new HadoopFsLogger(HadoopFsProfile.class);

    static final Logger LOG = LoggerFactory.getLogger(HadoopFsProfile.class);

    /**
     * The key of {@link #getBasePath() base path}.
     */
    public static final String KEY_BASE_PATH = "basePath";

    /**
     * The key of {@link CompressionCodec} class name.
     */
    public static final String KEY_COMPRESSION = "compression";

    private final String resourceName;

    private final Path basePath;

    private final CompressionCodec compressionCodec;

    /**
     * Creates a new instance.
     * @param resourceName the resource name
     * @param basePath the base path
     * @param compressionCodec the compression codec, or {@code null} if does not compress
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public HadoopFsProfile(
            String resourceName,
            Path basePath,
            CompressionCodec compressionCodec) {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName must not be null"); //$NON-NLS-1$
        }
        if (basePath == null) {
            throw new IllegalArgumentException("basePath must not be null"); //$NON-NLS-1$
        }
        this.resourceName = resourceName;
        this.basePath = basePath;
        this.compressionCodec = compressionCodec;
    }

    /**
     * Converts {@link ResourceProfile} into {@link SshProfile}.
     * @param configuration the current configuration
     * @param profile target profile
     * @return the converted profile
     * @throws IOException if failed to initialize file system
     * @throws IllegalArgumentException if profile is not valid, or any parameter is {@code null}
     */
    public static HadoopFsProfile convert(Configuration configuration, ResourceProfile profile) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        String name = profile.getName();
        Path basePath = extractBasePath(configuration, profile);
        CompressionCodec compressionCodec = extractCompressionCodec(configuration, profile);
        return new HadoopFsProfile(name, basePath, compressionCodec);
    }

    private static Path extractBasePath(Configuration configuration, ResourceProfile profile) throws IOException {
        assert configuration != null;
        assert profile != null;
        String result = extract(profile, KEY_BASE_PATH, false);
        try {
            if (result == null || result.isEmpty()) {
                FileSystem fileSystem = FileSystem.get(configuration);
                return fileSystem.getWorkingDirectory();
            }
            URI uri = URI.create(result);
            FileSystem fileSystem = FileSystem.get(uri, configuration);
            return fileSystem.makeQualified(new Path(uri));
        } catch (IOException e) {
            WGLOG.error(e, "E00002",
                    profile.getName(),
                    KEY_BASE_PATH,
                    result == null ? "(default)" : result);
            throw new IOException(MessageFormat.format(
                    "Failed to initialize the file system: {1} (resource={0})",
                    profile.getName(),
                    KEY_BASE_PATH,
                    result == null ? "(default)" : result), e);
        }
    }

    private static CompressionCodec extractCompressionCodec(Configuration configuration, ResourceProfile profile) {
        assert configuration != null;
        assert profile != null;
        String raw = extract(profile, KEY_COMPRESSION, false);
        CompressionCodec compressionCodec;
        try {
            if (raw == null) {
                compressionCodec = null;
            } else {
                Class<?> codecClass = configuration.getClassByName(raw);
                compressionCodec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, configuration);
            }
        } catch (Exception e) {
            WGLOG.error(e, "E00001",
                    profile.getName(),
                    KEY_COMPRESSION,
                    raw);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be a valid CompressionCodec class name: {2} (resource={0})",
                    profile.getName(),
                    KEY_COMPRESSION,
                    raw), e);
        }
        WGLOG.info("I00001",
                profile.getName(),
                KEY_COMPRESSION,
                compressionCodec == null ? null : raw);
        return compressionCodec;
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

    /**
     * Returns the resource name.
     * @return the resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the target base path.
     * @return the target base path
     * @since 0.4.0
     */
    public Path getBasePath() {
        return basePath;
    }

    /**
     * Returns the compression codec of putting sequence files.
     * @return the compression codec, or {@code null} if does not compress
     */
    public CompressionCodec getCompressionCodec() {
        return compressionCodec;
    }
}
