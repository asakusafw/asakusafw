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
import java.util.List;
import java.util.Map;

import com.asakusafw.bulkloader.common.Constants;

/**
 * Creates an instance of {@link FileListProvider} for remote access.
 * @since 0.7.1
 */
public class RemoteFileListProviderFactory {

    private final String sshCommandPath;

    private final String remoteHostName;

    private final String remoteUserName;

    /**
     * Creates a new instance.
     * @param sshCommandPath the SSH command full path
     * @param remoteHostName the target remote host name
     * @param remoteUserName the target remote user name
     */
    public RemoteFileListProviderFactory(String sshCommandPath, String remoteHostName, String remoteUserName) {
        if (sshCommandPath == null) {
            throw new IllegalArgumentException("sshCommandPath must not be null"); //$NON-NLS-1$
        }
        if (remoteHostName == null) {
            throw new IllegalArgumentException("remoteHostName must not be null"); //$NON-NLS-1$
        }
        if (remoteUserName == null) {
            throw new IllegalArgumentException("remoteUserName must not be null"); //$NON-NLS-1$
        }
        this.sshCommandPath = sshCommandPath;
        this.remoteHostName = remoteHostName;
        this.remoteUserName = remoteUserName;
    }

    /**
     * Creates a new instance.
     * @param command command line tokens
     * @param extraEnv extra environment variables (append/override environment variables)
     * @return the created provider
     * @throws IOException if failed to establish connection
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileListProvider newInstance(List<String> command, Map<String, String> extraEnv) throws IOException {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null"); //$NON-NLS-1$
        }
        if (extraEnv == null) {
            throw new IllegalArgumentException("extraEnv must not be null"); //$NON-NLS-1$
        }
        if (isRemote()) {
            return new OpenSshFileListProvider(sshCommandPath, remoteUserName, remoteHostName, command, extraEnv);
        } else {
            return new ProcessFileListProvider(command, extraEnv);
        }
    }

    private boolean isRemote() {
        return remoteHostName.equals(Constants.PROP_VALUE_NON_REMOTE_HOST) == false;
    }
}
