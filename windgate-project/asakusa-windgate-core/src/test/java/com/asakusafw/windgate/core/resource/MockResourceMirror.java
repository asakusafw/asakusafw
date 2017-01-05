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

import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;

/**
 * Mock {@link ResourceMirror}.
 */
public class MockResourceMirror extends ResourceMirror {

    private final ResourceProfile profile;

    /**
     * Creates a new instance.
     * @param profile the profile
     */
    public MockResourceMirror(ResourceProfile profile) {
        this.profile = profile;
    }

    /**
     * Creates a new instance.
     * @param name the name
     */
    public MockResourceMirror(String name) {
        this(MockResourceProvider.createProfile(name));
    }

    @Override
    public String getName() {
        return profile.getName();
    }

    @Override
    public void prepare(GateScript script) throws IOException {
        return;
    }

    @Override
    public <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException {
        return new MockSourceDriver<>(getName());
    }

    @Override
    public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
        return new MockDrainDriver<>(getName());
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
