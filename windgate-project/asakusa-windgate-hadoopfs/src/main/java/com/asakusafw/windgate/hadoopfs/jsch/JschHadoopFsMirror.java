/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.context.SimulationSupport;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.hadoopfs.ssh.AbstractSshHadoopFsMirror;
import com.asakusafw.windgate.hadoopfs.ssh.SshConnection;
import com.asakusafw.windgate.hadoopfs.ssh.SshProfile;

/**
 * An implementation of {@link ResourceMirror} using Hadoop File System via JSch connection.
 * @since 0.2.2
 * @version 0.4.0
 * @see FileProcess
 */
@SimulationSupport
public class JschHadoopFsMirror extends AbstractSshHadoopFsMirror {

    static final Logger LOG = LoggerFactory.getLogger(JschHadoopFsMirror.class);

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
    protected SshConnection openConnection(SshProfile profile, List<String> command) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (command == null) {
            throw new IllegalArgumentException("command must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Opening JSch connection: {}@{}:{} - {}", new Object[] {
                profile.getUser(),
                profile.getHost(),
                String.valueOf(profile.getPort()),
                command,
        });
        return new JschConnection(profile, command);
    }
}
