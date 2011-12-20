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
package com.asakusafw.windgate.hadoopfs.ssh;

import java.text.MessageFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;

/**
 * A structured profile for {@link AbstractSshHadoopFsMirror}.
 * @since 0.2.2
 */
public class SshProfile {

    static final WindGateLogger WGLOG = new HadoopFsLogger(SshProfile.class);

    static final Logger LOG = LoggerFactory.getLogger(SshProfile.class);

    /**
     * The remote 'get' command path.
     */
    public static final String COMMAND_GET = "bin/get.sh";

    /**
     * The remote 'put' command path.
     */
    public static final String COMMAND_PUT = "bin/put.sh";

    /**
     * The remote 'delete' command path.
     */
    public static final String COMMAND_DELETE = "bin/delete.sh";

    /**
     * The key of remote target installed path.
     */
    public static final String KEY_TARGET = "target";

    /**
     * The key of user name.
     */
    public static final String KEY_USER = "user";

    /**
     * The key of host name.
     */
    public static final String KEY_HOST = "host";

    /**
     * The key of port number.
     */
    public static final String KEY_PORT = "port";

    /**
     * The key of path to the private key.
     * This value can includes environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    public static final String KEY_PRIVATE_KEY = "privateKey";

    /**
     * The key of passphrase.
     */
    public static final String KEY_PASS_PHRASE = "passPhrase";

    /**
     * The key of {@link CompressionCodec} class name.
     */
    public static final String KEY_COMPRESSION = "compression";

    private final String resourceName;

    private final String target;

    private final String user;

    private final String host;

    private final int port;

    private final String privateKey;

    private final String passPhrase;

    private final CompressionCodec compressionCodec;

    /**
     * Creates a new instance.
     * @param name the resource name
     * @param target the remote target installed path
     * @param user the connection user name
     * @param host the connection target host
     * @param port the connection target port
     * @param privateKey the path to the private key file
     * @param passPhrase the passphrase of target private key
     * @param compressionCodec the compression codec, or {@code null} if does not compress
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public SshProfile(
            String name,
            String target,
            String user,
            String host,
            int port,
            String privateKey,
            String passPhrase,
            CompressionCodec compressionCodec) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        if (user == null) {
            throw new IllegalArgumentException("user must not be null"); //$NON-NLS-1$
        }
        if (host == null) {
            throw new IllegalArgumentException("host must not be null"); //$NON-NLS-1$
        }
        if (privateKey == null) {
            throw new IllegalArgumentException("privateKey must not be null"); //$NON-NLS-1$
        }
        if (passPhrase == null) {
            throw new IllegalArgumentException("passPhrase must not be null"); //$NON-NLS-1$
        }
        this.resourceName = name;
        this.target = target;
        this.user = user;
        this.host = host;
        this.port = port;
        this.privateKey = privateKey;
        this.passPhrase = passPhrase;
        this.compressionCodec = compressionCodec;
    }

    /**
     * Converts {@link ResourceProfile} into {@link SshProfile}.
     * @param configuration the current configuration
     * @param profile target profile
     * @return the converted profile
     * @throws IllegalArgumentException if profile is not valid, or any parameter is {@code null}
     */
    public static SshProfile convert(Configuration configuration, ResourceProfile profile) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        String name = profile.getName();
        String target = extract(profile, KEY_TARGET);
        String user = extract(profile, KEY_USER);
        String host = extract(profile, KEY_HOST);
        int port;
        String portString = extract(profile, KEY_PORT);
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            WGLOG.error("E10001",
                    profile.getName(),
                    KEY_PORT,
                    portString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be a valid port number: {2} (resource={0})",
                    profile.getName(),
                    KEY_PORT,
                    portString));
        }
        String privateKey = extract(profile, KEY_PRIVATE_KEY);
        try {
            privateKey = profile.getContext().getContextParameters().replace(privateKey, true);
        } catch (IllegalArgumentException e) {
            WGLOG.error(e, "E10001",
                    profile.getName(),
                    KEY_PRIVATE_KEY,
                    privateKey);
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to resolve the private key \"{1}\": {2} (resource={0})",
                    profile.getName(),
                    KEY_PRIVATE_KEY,
                    privateKey), e);
        }
        String passPhrase = profile.getConfiguration().get(KEY_PASS_PHRASE);
        passPhrase = passPhrase == null ? "" : passPhrase;
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
            WGLOG.error(e, "E10001",
                    profile.getName(),
                    KEY_COMPRESSION,
                    compressionCodecString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be a valid CompressionCodec class name: {2} (resource={0})",
                    profile.getName(),
                    KEY_COMPRESSION,
                    compressionCodecString), e);
        }
        WGLOG.info("I10001",
                profile.getName(),
                KEY_COMPRESSION,
                compressionCodec == null ? null : compressionCodecString);
        return new SshProfile(
                name,
                target,
                user,
                host,
                port,
                privateKey,
                passPhrase,
                compressionCodec);
    }

    private static String extract(ResourceProfile profile, String configKey) {
        assert profile != null;
        assert configKey != null;
        String value = profile.getConfiguration().get(configKey);
        if (value == null) {
            WGLOG.error("E10001",
                    profile.getName(),
                    configKey,
                    null);
            throw new IllegalArgumentException(MessageFormat.format(
                    "Resource \"{0}\" must declare \"{1}\"",
                    profile.getName(),
                    configKey));
        }
        return value.trim();
    }

    /**
     * Returns the resource name.
     * @return the resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Return the remote installation path.
     * @return the remote installation path
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns the get command.
     * @return the get command
     */
    public String getGetCommand() {
        return getCommand(COMMAND_GET);
    }

    /**
     * Returns the put command.
     * @return the put command
     */
    public String getPutCommand() {
        return getCommand(COMMAND_PUT);
    }

    /**
     * Returns the delete command.
     * @return the delete command
     */
    public String getDeleteCommand() {
        return getCommand(COMMAND_DELETE);
    }

    private String getCommand(String command) {
        assert command != null;
        StringBuilder buf = new StringBuilder();
        buf.append(target);
        if (target.endsWith("/") == false) {
            buf.append('/');
        }
        buf.append(command);
        return buf.toString();
    }

    /**
     * Returns the user name.
     * @return the user name
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the target host name.
     * @return the target host
     */
    public String getHost() {
        return host;
    }

    /**
     * Return the target port.
     * @return the target port
     */
    public int getPort() {
        return port;
    }

    /**
     * Return the path to the private key file.
     * @return the path to the private key file
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Returns the pass phrase of the {@link #getPrivateKey()}.
     * @return the pass phrase
     */
    public String getPassPhrase() {
        return passPhrase;
    }

    /**
     * Returns the compression codec of putting sequence files.
     * @return the compression codec, or {@code null} if does not compress
     */
    public CompressionCodec getCompressionCodec() {
        return compressionCodec;
    }
}
