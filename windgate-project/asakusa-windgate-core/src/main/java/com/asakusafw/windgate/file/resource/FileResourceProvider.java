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
package com.asakusafw.windgate.file.resource;

import java.io.IOException;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;

/**
 * An implementation {@link ResourceProvider} using file system.
 * @since 0.2.2
 */
public class FileResourceProvider extends ResourceProvider {

    private volatile String name;

    @Override
    protected void configure(ResourceProfile profile) throws IOException {
        this.name = profile.getName();
    }

    @Override
    public ResourceMirror create(String sessionId, ParameterList arguments) throws IOException {
        return new FileResourceMirror(name);
    }

    @Override
    public ResourceManipulator createManipulator(ParameterList arguments) throws IOException {
        return new FileResourceManipulator(name);
    }
}
