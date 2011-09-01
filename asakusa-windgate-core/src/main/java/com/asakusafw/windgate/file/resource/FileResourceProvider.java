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
package com.asakusafw.windgate.file.resource;

import java.io.IOException;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;

/**
 * An implementation {@link ResourceProvider} using file system.
 * @since 0.2.3
 */
public class FileResourceProvider extends ResourceProvider {

    private ResourceProfile profile;

    @Override
    protected void configure(ResourceProfile prf) throws IOException {
        this.profile = prf;
    }

    @Override
    public ResourceMirror create(String sessionId, ParameterList arguments) throws IOException {
        return new FileResourceMirror(profile.getName());
    }

    @Override
    public void abort(String sessionId) throws IOException {
        return;
    }
}
