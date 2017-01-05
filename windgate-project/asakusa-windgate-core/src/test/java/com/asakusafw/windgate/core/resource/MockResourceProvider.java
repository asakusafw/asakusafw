/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.resource;

import java.io.IOException;
import java.util.Collections;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProfileContext;

/**
 * Mock {@link ResourceProvider}.
 */
public class MockResourceProvider extends ResourceProvider {

    ResourceProfile configuredProfile;

    /**
     * Creates a new instance.
     */
    public MockResourceProvider() {
        return;
    }

    /**
     * Creates a new instance and configure.
     * @param name resource name
     */
    public MockResourceProvider(String name) {
        configure(createProfile(name));
    }

    /**
     * Creates a new profile for this resource provider.
     * @param name resource name
     * @return the created profile
     */
    public static ResourceProfile createProfile(String name) {
        assert name != null;
        return new ResourceProfile(
                name,
                MockResourceProvider.class,
                ProfileContext.system(MockResourceProvider.class.getClassLoader()),
                Collections.emptyMap());
    }

    @Override
    protected final void configure(ResourceProfile profile) {
        this.configuredProfile = profile;
    }

    @Override
    public ResourceMirror create(String sessionId, ParameterList arguments) throws IOException {
        return new MockResourceMirror(configuredProfile);
    }
}
