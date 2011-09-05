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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A remote command execution via SSH.
 * @since 0.2.2
 */
public interface SshConnection extends Closeable {

    /**
     * Connects to this target.
     * @throws IOException if failed to connect.
     */
    void connect() throws IOException;

    /**
     * Opens the remote standard input.
     * @return the opened stream.
     * @throws IOException if failed to open
     */
    OutputStream openStandardInput() throws IOException;

    /**
     * Opens the remote standard output.
     * @return the opened stream.
     * @throws IOException if failed to open
     */
    InputStream openStandardOutput() throws IOException;

    /**
     * Opens the remote standar output and redirects into the specified output.
     * @param output the redirection target
     * @param dontClose keep opened on the remote output is closed
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    void redirectStandardOutput(OutputStream output, boolean dontClose);

    /**
     * Waits for the remote command exits.
     * @param timeout waiting duration in milliseconds
     * @return exit code
     * @throws IOException if failed to wait
     * @throws InterruptedException if interrupted
     */
    int waitForExit(long timeout) throws IOException, InterruptedException;
}
