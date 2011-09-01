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
package com.asakusafw.windgate.hadoopfs.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import com.asakusafw.windgate.hadoopfs.ssh.SshConnection;
import com.asakusafw.windgate.hadoopfs.ssh.SshProfile;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * A remote command execution via SSH.
 * @since 0.2.3
 */
class JschConnection implements SshConnection {

    private final Session session;

    private final ChannelExec channel;

    private final SshProfile profile;

    /**
     * Creates a new instance.
     * @param profile target profile
     * @param command command to execute
     * @throws IOException if failed to create a new connection
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public JschConnection(SshProfile profile, String command) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (command == null) {
            throw new IllegalArgumentException("command must not be null"); //$NON-NLS-1$
        }
        this.profile = profile;
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(profile.getPrivateKey(), profile.getPassPhrase());
            session = jsch.getSession(profile.getUser(), profile.getHost(), profile.getPort());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout((int) TimeUnit.SECONDS.toMillis(60));
            session.connect();
            boolean succeeded = false;
            try {
                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(command);
                channel.setErrStream(System.err, true);
                succeeded = true;
            } finally {
                if (succeeded == false) {
                    session.disconnect();
                }
            }
        } catch (JSchException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to open ssh session: {0}@{1}:{2}",
                    profile.getUser(),
                    profile.getHost(),
                    String.valueOf(profile.getPort())), e);
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            channel.connect((int) TimeUnit.SECONDS.toMillis(60));
        } catch (JSchException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to open ssh session: {0}@{1}:{2}",
                    profile.getUser(),
                    profile.getHost(),
                    String.valueOf(profile.getPort())), e);
        }
    }

    @Override
    public OutputStream openStandardInput() throws IOException {
        return channel.getOutputStream();
    }

    @Override
    public InputStream openStandardOutput() throws IOException {
        return channel.getInputStream();
    }

    @Override
    public void redirectStandardOutput(OutputStream output, boolean dontClose) {
        channel.setOutputStream(output, dontClose);
    }

    @Override
    public int waitForExit(long timeout) throws InterruptedException, IOException {
        long until = System.currentTimeMillis() + timeout;
        while (until > System.currentTimeMillis()) {
            if (channel.isClosed()) {
                break;
            }
            Thread.sleep(100);
        }
        if (channel.isClosed() == false) {
            // TODO logging
            throw new IOException("Exit time out");
        }
        return channel.getExitStatus();
    }

    @Override
    public void close() throws IOException {
        try {
            channel.disconnect();
        } finally {
            session.disconnect();
        }
    }
}
