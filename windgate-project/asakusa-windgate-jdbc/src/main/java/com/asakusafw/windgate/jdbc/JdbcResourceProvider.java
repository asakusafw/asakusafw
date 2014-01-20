/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.windgate.jdbc;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;

/**
 * An implementation of {@link ResourceProvider} using JDBC.
 * @since 0.2.2
 */
public class JdbcResourceProvider extends ResourceProvider {

    static final Logger LOG = LoggerFactory.getLogger(JdbcResourceProvider.class);

    private volatile JdbcProfile jdbcProfile;

    @Override
    protected void configure(ResourceProfile profile) throws IOException {
        LOG.debug("Configuring JDBC resource \"{}\"",
                profile.getName());
        try {
            this.jdbcProfile = JdbcProfile.convert(profile);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure {0}",
                    profile.getName()), e);
        }
    }

    @Override
    public ResourceMirror create(String sessionId, ParameterList arguments) throws IOException {
        LOG.debug("Creating JDBC resource {} for session {}",
                jdbcProfile.getResourceName(),
                sessionId);
        return new JdbcResourceMirror(jdbcProfile, arguments);
    }

    @Override
    public ResourceManipulator createManipulator(ParameterList arguments) throws IOException {
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating JDBC resource manipulator {}",
                jdbcProfile.getResourceName());
        return new JdbcResourceManipulator(jdbcProfile, arguments);
    }
}
