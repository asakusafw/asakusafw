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
package com.asakusafw.yaess.jsch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.basic.ProcessExecutor;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.VariableResolver;
import com.asakusafw.yaess.core.YaessLogger;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * An implementation of {@link ProcessExecutor} using JSch.
 * @since 0.2.3
 */
public class JschProcessExecutor implements ProcessExecutor {

    static final YaessLogger YSLOG = new YaessJschLogger(JschProcessExecutor.class);

    static final Logger LOG = LoggerFactory.getLogger(JschProcessExecutor.class);

    private static final String PREFIX = "ssh.";

    /**
     * The key of user name.
     * This value can includes environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    public static final String KEY_USER = PREFIX + "user";

    /**
     * The key of host name.
     * This value can includes environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    public static final String KEY_HOST = PREFIX + "host";

    /**
     * The key of port number.
     */
    public static final String KEY_PORT = PREFIX + "port";

    /**
     * The key of path to the private key.
     * This value can includes environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    public static final String KEY_PRIVATE_KEY = PREFIX + "privateKey";

    /**
     * The key of passphrase.
     */
    public static final String KEY_PASS_PHRASE = PREFIX + "passPhrase";

    // see
    // man bash > DEFINITIONS
    private static final Pattern SH_NAME = Pattern.compile("[A-Za-z_][0-9A-Za-z_]*");

    // see
    // man bash > QUOTING
    // $, `, ", \, or <newline>
    private static final Pattern SH_METACHARACTERS = Pattern.compile("[\\$`\"\\\\\n]");

    private final String user;

    private final String host;

    private final Integer port;

    private final String privateKey;

    private final String passPhrase;

    private final JSch jsch;

    /**
     * Creates a new instance.
     * @param user remote user name
     * @param host remote host name
     * @param portOrNull remote port number (nullable)
     * @param privateKeyPath path to private key file
     * @param passPhraseOrNull passphrase for the private key (nullable)
     * @throws JSchException if failed to initialize SSH client
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JschProcessExecutor(
            String user,
            String host,
            Integer portOrNull,
            String privateKeyPath,
            String passPhraseOrNull) throws JSchException {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null"); //$NON-NLS-1$
        }
        if (host == null) {
            throw new IllegalArgumentException("host must not be null"); //$NON-NLS-1$
        }
        if (privateKeyPath == null) {
            throw new IllegalArgumentException("privateKeyPath must not be null"); //$NON-NLS-1$
        }
        this.user = user;
        this.host = host;
        this.port = portOrNull;
        this.jsch = new JSch();
        this.privateKey = privateKeyPath;
        this.passPhrase = passPhraseOrNull;
        jsch.addIdentity(privateKeyPath, passPhraseOrNull);
    }

    /**
     * Returns the remote user name.
     * @return the user name
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the remote host name.
     * @return the host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the remote port number.
     * @return the port number, or {@code null} if is not specified
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Returns the path to the private key file.
     * @return the path to the private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Returns a pass phrase for private key.
     * @return the pass phrase, or {@code null} if is not specified
     */
    public String getPassPhrase() {
        return passPhrase;
    }

    /**
     * Extracts SSH profiles from configuration and returns a related executor.
     * This operation extracts following entries from {@code configuration}:
     * <ul>
     * <li> {@link #KEY_USER remote user name} </li>
     * <li> {@link #KEY_HOST remote host name} </li>
     * <li> {@link #KEY_PORT remote port number} (can omit) </li>
     * <li> {@link #KEY_PRIVATE_KEY private key path} </li>
     * <li> {@link #KEY_PASS_PHRASE} (can omit) </li>
     * </ul>
     * @param servicePrefix prefix of configuration keys
     * @param configuration target configuration
     * @param variables variable resolver
     * @return the created executor
     * @throws JSchException if failed to initialize SSH client
     * @throws IllegalArgumentException if configuration is invalid
     */
    public static JschProcessExecutor extract(
            String servicePrefix,
            Map<String, String> configuration,
            VariableResolver variables) throws JSchException {
        if (servicePrefix == null) {
            throw new IllegalArgumentException("servicePrefix must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (variables == null) {
            throw new IllegalArgumentException("variables must not be null"); //$NON-NLS-1$
        }
        String user = extract(KEY_USER, servicePrefix, configuration, variables, true);
        String host = extract(KEY_HOST, servicePrefix, configuration, variables, true);
        String portString = extract(KEY_PORT, servicePrefix, configuration, variables, false);
        String privateKey = extract(KEY_PRIVATE_KEY, servicePrefix, configuration, variables, false);
        String passPhrase = extract(KEY_PASS_PHRASE, servicePrefix, configuration, variables, false);
        Integer port = null;
        if (portString != null) {
            try {
                port = Integer.valueOf(portString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid port number in \"{0}\": {1}",
                        servicePrefix + '.' + KEY_PORT,
                        portString));
            }
        }
        return new JschProcessExecutor(user, host, port, privateKey, passPhrase);
    }

    private static String extract(
            String key,
            String prefix,
            Map<String, String> configuration,
            VariableResolver variables,
            boolean mandatory) {
        assert key != null;
        assert prefix != null;
        assert configuration != null;
        assert variables != null;
        String value = configuration.get(key);
        if (value == null) {
            if (mandatory) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Mandatory entry \"{0}\" is not set",
                        prefix + '.' + key));
            } else {
                return null;
            }
        }
        try {
            return variables.replace(value, true);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to resolve variables in \"{0}\": {1}",
                    prefix + '.' + key,
                    value));
        }
    }

    @Override
    public int execute(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables) throws InterruptedException, IOException {
        return execute(context, commandLineTokens, environmentVariables, System.out);
    }

    @Override
    public int execute(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables,
            OutputStream output) throws InterruptedException, IOException {
        try {
            return execute0(context, commandLineTokens, environmentVariables, output);
        } catch (JSchException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to execute command via SSH ({0}@{1}:{2})",
                    user,
                    host,
                    String.valueOf(port)), e);
        }
    }

    private int execute0(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables,
            OutputStream output) throws JSchException, InterruptedException {
        assert context != null;
        assert commandLineTokens != null;
        assert environmentVariables != null;
        assert output != null;
        Session session = jsch.getSession(user, host);
        if (port != null) {
            session.setPort(port);
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.setServerAliveInterval((int) TimeUnit.SECONDS.toMillis(30));

        try {
            YSLOG.info("I00001", user, host, port, privateKey);
            long sessionStart = System.currentTimeMillis();
            session.connect((int) TimeUnit.SECONDS.toMillis(60));
            long sessionEnd = System.currentTimeMillis();
            YSLOG.info("I00002", user, host, port, privateKey, sessionEnd - sessionStart);

            int exitStatus;
            try {
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(buildCommand(commandLineTokens, environmentVariables));
                channel.setInputStream(new ByteArrayInputStream(new byte[0]), true);
                channel.setOutputStream(output, true);
                channel.setErrStream(output, true);

                YSLOG.info("I00003", user, host, port, privateKey, commandLineTokens.get(0));
                long channelStart = System.currentTimeMillis();
                channel.connect((int) TimeUnit.SECONDS.toMillis(60));
                long channelEnd = System.currentTimeMillis();
                YSLOG.info("I00004", user, host, port, privateKey, commandLineTokens.get(0), channelEnd - channelStart);

                try {
                    while (true) {
                        if (channel.isClosed()) {
                            break;
                        }
                        Thread.sleep(100);
                    }
                    exitStatus = channel.getExitStatus();
                } finally {
                    channel.disconnect();
                }
            } finally {
                session.disconnect();
            }
            YSLOG.info("I00005", user, host, port, privateKey, commandLineTokens.get(0), exitStatus);
            return exitStatus;
        } catch (JSchException e) {
            YSLOG.error(e, "E00001", user, host, port, privateKey);
            throw e;
        }
    }

    private String buildCommand(List<String> commandLineTokens, Map<String, String> environmentVariables) {
        assert commandLineTokens != null;
        assert environmentVariables != null;

        // FIXME for bsh only
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            if (SH_NAME.matcher(entry.getKey()).matches() == false) {
                YSLOG.warn("W00001",
                        entry.getKey(),
                        entry.getValue());
                continue;
            }
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append(entry.getKey());
            String replaced = SH_METACHARACTERS.matcher(entry.getValue()).replaceAll("\\\\$0");
            buf.append('=');
            buf.append('"');
            buf.append(replaced);
            buf.append('"');
        }
        for (String token : commandLineTokens) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            String replaced = SH_METACHARACTERS.matcher(token).replaceAll("\\\\$0");
            buf.append('"');
            buf.append(replaced);
            buf.append('"');
        }
        return buf.toString();
    }
}
