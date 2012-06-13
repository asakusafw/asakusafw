/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.windgate.stream.file;

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
 * An implementation of {@link ResourceProfile} using local file system.
 * @since 0.2.4
 */
public class FileResourceProvider extends ResourceProvider {

    static final Logger LOG = LoggerFactory.getLogger(FileResourceProvider.class);

    private volatile FileProfile fileProfile;

    @Override
    protected void configure(ResourceProfile profile) throws IOException {
        LOG.debug("Configuring file resource \"{}\"",
                profile.getName());
        try {
            this.fileProfile = FileProfile.convert(profile);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure {0}",
                    profile.getName()), e);
        }
    }

    @Override
    public ResourceMirror create(String sessionId, ParameterList arguments) throws IOException {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null"); //$NON-NLS-1$
        }
        return new FileResourceMirror(fileProfile, arguments);
    }

    @Override
    public ResourceManipulator createManipulator(ParameterList arguments) throws IOException {
        return new FileResourceManipulator(fileProfile, arguments);
    }
}
