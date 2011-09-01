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

import org.apache.hadoop.conf.Configuration;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.hadoopfs.ssh.AbstractSshHadoopFsMirror;
import com.asakusafw.windgate.hadoopfs.ssh.SshConnection;
import com.asakusafw.windgate.hadoopfs.ssh.SshProfile;

/**
 * An implementation of {@link ResourceMirror} using Hadoop File System via JSch connection.
 * @since 0.2.3
 * @see FileProcess
 */
public class JschHadoopFsMirror extends AbstractSshHadoopFsMirror {

    /**
     * Creates a new instance.
     * @param configuration the hadoop configuration
     * @param profile the profile
     * @param arguments the arguments
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public JschHadoopFsMirror(Configuration configuration, SshProfile profile, ParameterList arguments) {
        super(configuration, profile, arguments);
    }

    @Override
    protected SshConnection openConnection(SshProfile sshProfile, String command) throws IOException {
        if (sshProfile == null) {
            throw new IllegalArgumentException("sshProfile must not be null"); //$NON-NLS-1$
        }
        if (command == null) {
            throw new IllegalArgumentException("command must not be null"); //$NON-NLS-1$
        }
        return new JschConnection(sshProfile, command);
    }
}
