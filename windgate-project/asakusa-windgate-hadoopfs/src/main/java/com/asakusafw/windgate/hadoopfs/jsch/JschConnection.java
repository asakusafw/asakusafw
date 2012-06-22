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
package com.asakusafw.windgate.hadoopfs.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;
import com.asakusafw.windgate.hadoopfs.ssh.SshConnection;
import com.asakusafw.windgate.hadoopfs.ssh.SshProfile;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * A remote command execution via SSH.
 * @since 0.2.2
 * @version 0.4.0
 */
class JschConnection implements SshConnection {

    static final WindGateLogger WGLOG = new HadoopFsLogger(JschConnection.class);

    static final Logger LOG = LoggerFactory.getLogger(JschConnection.class);

    private static final Pattern SH_NAME = Pattern.compile("[A-Za-z_][0-9A-Za-z_]*");

    private static final Pattern SH_METACHARACTERS = Pattern.compile("[\\$`\"\\\\\n]");

    private final Session session;

    private final ChannelExec channel;

    private final SshProfile profile;

    private final String command;

    /**
     * Creates a new instance.
     * @param profile target profile
     * @param commandLineTokens the command line tokens
     * @throws IOException if failed to create a new connection
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public JschConnection(SshProfile profile, List<String> commandLineTokens) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (commandLineTokens == null) {
            throw new IllegalArgumentException("commandLineTokens must not be null"); //$NON-NLS-1$
        }
        this.profile = profile;
        this.command = buildCommand(commandLineTokens, profile.getEnvironmentVariables());
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(profile.getPrivateKey(), profile.getPassPhrase());
            session = jsch.getSession(profile.getUser(), profile.getHost(), profile.getPort());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setServerAliveInterval((int) TimeUnit.SECONDS.toMillis(10));
            session.setTimeout((int) TimeUnit.SECONDS.toMillis(60));

            WGLOG.info("I30001",
                    profile.getResourceName(),
                    profile.getUser(),
                    profile.getHost(),
                    profile.getPort());
            session.connect();
            WGLOG.info("I30002",
                    profile.getResourceName(),
                    profile.getUser(),
                    profile.getHost(),
                    profile.getPort());

            boolean succeeded = false;
            try {
                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(command);
                channel.setErrStream(System.err, true);
                succeeded = true;
            } finally {
                if (succeeded == false) {
                    LOG.debug("Disconnecting SSH session (failed to initialize command channel)");
                    session.disconnect();
                }
            }
        } catch (JSchException e) {
            WGLOG.error("E30001",
                    profile.getResourceName(),
                    profile.getUser(),
                    profile.getHost(),
                    profile.getPort());
            throw new IOException(MessageFormat.format(
                    "Failed to open ssh session: {0}@{1}:{2} - {3}",
                    profile.getUser(),
                    profile.getHost(),
                    String.valueOf(profile.getPort()),
                    command), e);
        }
    }

    private String buildCommand(List<String> commandLineTokens, Map<String, String> environmentVariables) {
        assert commandLineTokens != null;
        assert environmentVariables != null;

        Map<String, String> env = environmentVariables;

        // FIXME for bsh only
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            if (SH_NAME.matcher(entry.getKey()).matches() == false) {
                WGLOG.warn("W30001",
                        profile.getResourceName(),
                        profile.getUser(),
                        profile.getHost(),
                        String.valueOf(profile.getPort()),
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

    @Override
    public void connect() throws IOException {
        try {
            WGLOG.info("I30003",
                    profile.getResourceName(),
                    profile.getUser(),
                    profile.getHost(),
                    profile.getPort(),
                    command);
            channel.connect((int) TimeUnit.SECONDS.toMillis(60));
            WGLOG.info("I30004",
                    profile.getResourceName(),
                    profile.getUser(),
                    profile.getHost(),
                    profile.getPort(),
                    command);
        } catch (JSchException e) {
            WGLOG.error("E30002",
                    profile.getResourceName(),
                    profile.getUser(),
                    profile.getHost(),
                    profile.getPort(),
                    command);
            throw new IOException(MessageFormat.format(
                    "Failed to open ssh session: {0}@{1}:{2}",
                    profile.getUser(),
                    profile.getHost(),
                    String.valueOf(profile.getPort())), e);
        }
    }

    @Override
    public OutputStream openStandardInput() throws IOException {
        LOG.debug("Opening remote standard input: {}",
                command);
        return channel.getOutputStream();
    }

    @Override
    public InputStream openStandardOutput() throws IOException {
        LOG.debug("Opening remote standard output: {}",
                command);
        return channel.getInputStream();
    }

    @Override
    public void redirectStandardOutput(OutputStream output, boolean dontClose) {
        LOG.debug("Redirecting remote standard output: {}",
                command);
        channel.setOutputStream(output, dontClose);
    }

    @Override
    public int waitForExit(long timeout) throws InterruptedException, IOException {
        LOG.debug("Waiting for remote command exit: {}",
                command);
        long until = System.currentTimeMillis() + timeout;
        while (until > System.currentTimeMillis()) {
            if (channel.isClosed()) {
                break;
            }
            Thread.sleep(100);
        }
        if (channel.isClosed() == false) {
            WGLOG.error("E30003",
                    profile.getResourceName(),
                    profile.getUser(),
                    profile.getHost(),
                    profile.getPort(),
                    command);
            throw new IOException(MessageFormat.format(
                    "Failed to wait for exit remote command: {0}@{1}:{2} - {3}",
                    profile.getUser(),
                    profile.getHost(),
                    String.valueOf(profile.getPort()),
                    command));
        }
        return channel.getExitStatus();
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing SSH connection: {}",
                command);
        try {
            channel.disconnect();
        } finally {
            session.disconnect();
        }
    }
}
