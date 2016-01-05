/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.basic.BlobUtil;
import com.asakusafw.yaess.basic.ProcessExecutor;
import com.asakusafw.yaess.core.Blob;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.VariableResolver;
import com.asakusafw.yaess.core.YaessLogger;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * An implementation of {@link ProcessExecutor} using JSch.
 * @since 0.2.3
 * @version 0.8.0
 */
public class JschProcessExecutor implements ProcessExecutor {

    static final YaessLogger YSLOG = new YaessJschLogger(JschProcessExecutor.class);

    static final Logger LOG = LoggerFactory.getLogger(JschProcessExecutor.class);

    static final Charset ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

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

    /**
     * The key of BLOB storage path prefix.
     * @since 0.8.0
     */
    public static final String KEY_TEMPORARY_BLOB_PREFIX = PREFIX + "blob";

    /**
     * The default value of {@link #KEY_TEMPORARY_BLOB_PREFIX}.
     * @since 0.8.0
     */
    public static final String DEFAULT_TEMPORARY_BLOB_PREFIX = "/tmp/yaess-blob-";

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

    private final String temporaryBlobPrefix;

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
        this(user, host, portOrNull, privateKeyPath, passPhraseOrNull, DEFAULT_TEMPORARY_BLOB_PREFIX);
    }

    /**
     * Creates a new instance.
     * @param user remote user name
     * @param host remote host name
     * @param portOrNull remote port number (nullable)
     * @param privateKeyPath path to private key file
     * @param passPhraseOrNull passphrase for the private key (nullable)
     * @param temporaryBlobPrefix the temporary BLOB storage path prefix
     * @throws JSchException if failed to initialize SSH client
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.8.0
     */
    public JschProcessExecutor(
            String user,
            String host,
            Integer portOrNull,
            String privateKeyPath,
            String passPhraseOrNull,
            String temporaryBlobPrefix) throws JSchException {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null"); //$NON-NLS-1$
        }
        if (host == null) {
            throw new IllegalArgumentException("host must not be null"); //$NON-NLS-1$
        }
        if (privateKeyPath == null) {
            throw new IllegalArgumentException("privateKeyPath must not be null"); //$NON-NLS-1$
        }
        if (temporaryBlobPrefix == null) {
            throw new IllegalArgumentException("temporaryBlobPrefix must not be null"); //$NON-NLS-1$
        }
        this.user = user;
        this.host = host;
        this.port = portOrNull;
        this.jsch = new JSch();
        this.privateKey = privateKeyPath;
        this.passPhrase = passPhraseOrNull;
        this.temporaryBlobPrefix = temporaryBlobPrefix;
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
     * Returns the BLOB storage remote path prefix.
     * @return the BLOB storage remote path prefix
     */
    public String getTemporaryBlobPrefix() {
        return temporaryBlobPrefix;
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
        String tempBlob = extract(KEY_TEMPORARY_BLOB_PREFIX, servicePrefix, configuration, variables, false);
        tempBlob = tempBlob == null ? DEFAULT_TEMPORARY_BLOB_PREFIX : tempBlob;
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
        return new JschProcessExecutor(user, host, port, privateKey, passPhrase, tempBlob);
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
        return execute(
                context,
                commandLineTokens, environmentVariables,
                Collections.<String, Blob>emptyMap(), System.out);
    }

    @Override
    public int execute(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables,
            OutputStream output) throws InterruptedException, IOException {
        return execute(
                context,
                commandLineTokens, environmentVariables,
                Collections.<String, Blob>emptyMap(), output);
    }

    @Override
    public int execute(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables,
            Map<String, Blob> extensions,
            OutputStream output) throws InterruptedException, IOException {
        try {
            return execute0(context, commandLineTokens, environmentVariables, extensions, output);
        } catch (IOException | JSchException e) {
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
            Map<String, Blob> extensions,
            OutputStream output) throws IOException, JSchException, InterruptedException {
        assert context != null;
        assert commandLineTokens != null;
        assert environmentVariables != null;
        assert extensions != null;
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
            try {
                Map<String, String> newEnv = resolveBlobs(session, environmentVariables, extensions, output);
                return execute0(session, commandLineTokens, newEnv, output);
            } finally {
                session.disconnect();
            }
        } catch (IOException | JSchException e) {
            YSLOG.error(e, "E00001", user, host, port, privateKey);
            throw e;
        }
    }

    private Map<String, String> resolveBlobs(
            Session session,
            Map<String, String> environmentVariables,
            Map<String, Blob> extensions,
            OutputStream output) throws IOException, JSchException {
        if (extensions.isEmpty()) {
            return environmentVariables;
        }
        Map<String, String> results = new LinkedHashMap<>();
        results.putAll(environmentVariables);
        int index = 0;
        for (Map.Entry<String, Blob> entry : extensions.entrySet()) {
            String name = entry.getKey();
            Blob blob = entry.getValue();
            String suffix = BlobUtil.getSuffix(name, blob);
            String path = String.format("%s%s%s", temporaryBlobPrefix, UUID.randomUUID(), suffix); //$NON-NLS-1$
            send(session, blob, path, index++, output);
            results.put(ExecutionScriptHandler.ENV_EXTENSION_PREFIX + name, path);
        }
        return results;
    }

    private void send(
            Session session,
            Blob blob, String remotePath, int localIndex,
            OutputStream output) throws IOException, JSchException {
        long size = blob.getSize();
        String exec = String.format("scp -t \"%s\"", toLiteral(remotePath)); //$NON-NLS-1$
        String header = String.format("C0644 %d BLOB-%d", size, localIndex); //$NON-NLS-1$

        YSLOG.info("I01001", size, remotePath);
        long start = System.currentTimeMillis();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(exec);
        channel.setErrStream(output, true);
        try (InputStream stdin = channel.getInputStream();
                OutputStream stdout = channel.getOutputStream()) {
            channel.connect();

            stdout.write(header.getBytes(ENCODING));
            stdout.write('\n');
            stdout.flush();
            checkAck(blob, stdin);

            putBlob(blob, stdout);
            stdout.write(0);
            stdout.flush();
            checkAck(blob, stdin);
        } finally {
            channel.disconnect();
        }
        long end = System.currentTimeMillis();
        YSLOG.info("I01002", size, remotePath, end - start);
    }

    private void checkAck(Blob blob, InputStream stdin) throws IOException {
        int c = stdin.read();
        if (c != 0) {
            throw new IOException(MessageFormat.format(
                    "error occurred while sending blob via SSH: {0}",
                    blob.toString()));
        }
    }

    private void putBlob(Blob blob, OutputStream stdout) throws IOException {
        byte[] buf = new byte[1024];
        try (InputStream in = blob.open()) {
            while (true) {
                int read = in.read(buf);
                if (read < 0) {
                    break;
                }
                stdout.write(buf, 0, read);
            }
        }
    }

    private int execute0(Session session, List<String> commandLineTokens, Map<String, String> environmentVariables,
            OutputStream output) throws JSchException, InterruptedException {
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

        int exitStatus;
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
        YSLOG.info("I00005", user, host, port, privateKey, commandLineTokens.get(0), exitStatus);
        return exitStatus;
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
            buf.append('=');
            String replaced = toLiteral(entry.getValue());
            buf.append(replaced);
        }
        for (String token : commandLineTokens) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append(toLiteral(token));
        }
        return buf.toString();
    }

    private static String toLiteral(String token) {
        String replaced = SH_METACHARACTERS.matcher(token).replaceAll("\\\\$0");
        return '"' + replaced + '"';
    }
}
