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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link StreamFileListProvider} using local command execution.
 * @since 0.2.3
 */
public class ProcessFileListProvider extends StreamFileListProvider {

    private final List<String> command;

    private final Process process;

    /**
     * Creates a new instance.
     * @param command command line tokens
     * @param extraEnv extra environment variables (append/override environment variables)
     * @throws IOException if failed to create SSH process
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ProcessFileListProvider(List<String> command, Map<String, String> extraEnv) throws IOException {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null"); //$NON-NLS-1$
        }
        if (extraEnv == null) {
            throw new IllegalArgumentException("extraEnv must not be null"); //$NON-NLS-1$
        }
        this.command = command;
        this.process = createProcess(command, extraEnv);
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

    private Process createProcess(List<String> localCommand, Map<String, String> extraEnv) throws IOException {
        assert localCommand != null;
        assert extraEnv != null;
        ProcessBuilder builder = new ProcessBuilder(localCommand);
        builder.directory(new File(System.getProperty("user.home", ".")));
        builder.environment().putAll(extraEnv);
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
}
