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
package com.asakusafw.windgate.hadoopfs;

import java.text.MessageFormat;

import org.apache.hadoop.conf.Configuration;
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
 */
public class HadoopFsProfile {

    static final WindGateLogger WGLOG = new HadoopFsLogger(HadoopFsProfile.class);

    static final Logger LOG = LoggerFactory.getLogger(HadoopFsProfile.class);

    /**
     * The key of {@link CompressionCodec} class name.
     */
    public static final String KEY_COMPRESSION = "compression";

    private final String resourceName;

    private final CompressionCodec compressionCodec;

    /**
     * Creates a new instance.
     * @param resourceName the resource name
     * @param compressionCodec the compression codec, or {@code null} if does not compress
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public HadoopFsProfile(String resourceName, CompressionCodec compressionCodec) {
        this.resourceName = resourceName;
        this.compressionCodec = compressionCodec;
    }

    /**
     * Converts {@link ResourceProfile} into {@link SshProfile}.
     * @param configuration the current configuration
     * @param profile target profile
     * @return the converted profile
     * @throws IllegalArgumentException if profile is not valid, or any parameter is {@code null}
     */
    public static HadoopFsProfile convert(Configuration configuration, ResourceProfile profile) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        String name = profile.getName();
        String compressionCodecString = profile.getConfiguration().get(KEY_COMPRESSION);
        CompressionCodec compressionCodec;
        try {
            if (compressionCodecString == null) {
                compressionCodec = null;
            } else {
                Class<?> codecClass = configuration.getClassByName(compressionCodecString);
                compressionCodec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, configuration);
            }
        } catch (Exception e) {
            WGLOG.error(e, "E00001",
                    profile.getName(),
                    KEY_COMPRESSION,
                    compressionCodecString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be a valid CompressionCodec class name: {2} (resource={0})",
                    profile.getName(),
                    KEY_COMPRESSION,
                    compressionCodecString), e);
        }
        WGLOG.info("I00001",
                profile.getName(),
                KEY_COMPRESSION,
                compressionCodec == null ? null : compressionCodecString);
        return new HadoopFsProfile(name, compressionCodec);
    }

    /**
     * Returns the resource name.
     * @return the resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the compression codec of putting sequence files.
     * @return the compression codec, or {@code null} if does not compress
     */
    public CompressionCodec getCompressionCodec() {
        return compressionCodec;
    }
}
