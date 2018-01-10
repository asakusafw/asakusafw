/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.text.MessageFormat;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;
import com.asakusafw.windgate.hadoopfs.HadoopFsProvider;
import com.asakusafw.windgate.hadoopfs.ssh.SshProfile;

/**
 * Provides {@link JschHadoopFsMirror}.
 * @since 0.2.2
 */
public class JschHadoopFsProvider extends ResourceProvider {

    static final Logger LOG = LoggerFactory.getLogger(JschHadoopFsProvider.class);

    private volatile Configuration configuration;

    private volatile SshProfile sshProfile;

    @Override
    protected void configure(ResourceProfile profile) throws IOException {
        LOG.debug("Configuring Hadoop FS via JSch resource \"{}\"",
                profile.getName());
        this.configuration = HadoopFsProvider.getConfiguration(profile.getContext());
        try {
            this.sshProfile = SshProfile.convert(configuration, profile);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure resource \"{0}\"",
                    profile.getName()));
        }
    }

    @Override
    public ResourceMirror create(String sessionId, ParameterList arguments) throws IOException {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating Hadoop FS via JSch resource {} for session {}",
                sshProfile.getResourceName(),
                sessionId);
        return new JschHadoopFsMirror(configuration, sshProfile, arguments);
    }
}
