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
package com.asakusafw.bulkloader.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * An implementation of {@link StreamFileListProvider} using Open SSH connection.
 * @since 0.2.3
 * @version 0.4.0
 */
public class OpenSshFileListProvider extends StreamFileListProvider {

    private static final Pattern SH_NAME = Pattern.compile("[A-Za-z_][0-9A-Za-z_]*");

    private static final Pattern SH_METACHARACTERS = Pattern.compile("[\\$`\"\\\\\n]");

    private final List<String> command;

    private final Process process;

    /**
     * Creates a new instance.
     * @param sshExec the path to the Open SSH client
     * @param userName remote user name
     * @param hostName remote host name
     * @param command remote command line tokens
     * @param env environment variables
     * @throws IOException if failed to create SSH process
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OpenSshFileListProvider(
            String sshExec,
            String userName,
            String hostName,
            List<String> command,
            Map<String, String> env) throws IOException {
        if (sshExec == null) {
            throw new IllegalArgumentException("sshExec must not be null"); //$NON-NLS-1$
        }
        if (userName == null) {
            throw new IllegalArgumentException("userName must not be null"); //$NON-NLS-1$
        }
        if (hostName == null) {
            throw new IllegalArgumentException("hostName must not be null"); //$NON-NLS-1$
        }
        if (command == null) {
            throw new IllegalArgumentException("command must not be null"); //$NON-NLS-1$
        }
        if (env == null) {
            throw new IllegalArgumentException("env must not be null"); //$NON-NLS-1$
        }
        this.command = command;
        this.process = createProcess(sshExec, userName, hostName, command, env);
        boolean succeed = false;
        try {
            redirect(process.getErrorStream(), System.err);
            succeed = true;
        } finally {
            if (succeed == false) {
                process.destroy();
            }
        }
    }

    private Process createProcess(
            String sshExec,
            String userName,
            String hostName,
            List<String> remoteCommand,
            Map<String, String> remoteEnv) throws IOException {
        assert sshExec != null;
        assert userName != null;
        assert hostName != null;
        assert remoteCommand != null;
        assert remoteEnv != null;
        List<String> localCommand = new ArrayList<String>();
        localCommand.add(sshExec);
        localCommand.add("-l");
        localCommand.add(userName);
        localCommand.add(hostName);
        localCommand.add(buildCommand(remoteCommand, remoteEnv));
        ProcessBuilder builder = new ProcessBuilder(localCommand);
        return builder.start();
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return process.getInputStream();
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return process.getOutputStream();
    }

    @Override
    protected void waitForDone() throws IOException, InterruptedException {
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException(MessageFormat.format(
                    "Failed to wait for process exit: code={0}, command={1}",
                    exitCode,
                    command));
        }
    }

    @Override
    public void close() {
        process.destroy();
    }

    private String buildCommand(List<String> commandLineTokens, Map<String, String> environmentVariables) {
        assert commandLineTokens != null;
        assert environmentVariables != null;

        // FIXME for bsh only
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            if (SH_NAME.matcher(entry.getKey()).matches() == false) {
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
