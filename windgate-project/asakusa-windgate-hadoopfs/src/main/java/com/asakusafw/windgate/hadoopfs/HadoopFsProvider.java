/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.windgate.hadoopfs;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;

/**
 * Provides {@link HadoopFsMirror}.
 * @since 0.2.2
 */
public class HadoopFsProvider extends ResourceProvider {

    static final Logger LOG = LoggerFactory.getLogger(HadoopFsProvider.class);

    private volatile Configuration configuration;

    private volatile HadoopFsProfile hfsProfile;

    @Override
    protected void configure(ResourceProfile profile) throws IOException {
        LOG.debug("Configuring Hadoop FS resource \"{}\"",
                profile.getName());
        this.configuration = new Configuration();
        try {
            this.hfsProfile = HadoopFsProfile.convert(configuration, profile);
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
        LOG.debug("Creating Hadoop FS resource {} for session {}",
                hfsProfile.getResourceName(),
                sessionId);
        return new HadoopFsMirror(configuration, hfsProfile, arguments);
    }
}
