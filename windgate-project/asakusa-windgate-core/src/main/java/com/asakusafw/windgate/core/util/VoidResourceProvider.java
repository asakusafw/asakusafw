/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.util;

import java.io.IOException;

import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;
import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * Provides void resources.
 * @since 0.8.1
 */
public class VoidResourceProvider extends ResourceProvider {

    private static final ResourceMirror MIRROR = new Mirror();

    private static final ResourceManipulator MANIPULATOR = new Manipulator();

    /**
     * The resource name.
     */
    public static final String NAME = "void"; //$NON-NLS-1$

    @Override
    public ResourceMirror create(String sessionId, ParameterList arguments) throws IOException {
        return MIRROR;
    }

    @Override
    public ResourceManipulator createManipulator(ParameterList arguments) throws IOException {
        return MANIPULATOR;
    }

    @Override
    protected void configure(ResourceProfile profile) throws IOException {
        return;
    }

    private static class Mirror extends ResourceMirror {

        Mirror() {
            return;
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public void prepare(GateScript script) throws IOException {
            return;
        }

        @Override
        public <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException {
            return new VoidSourceDriver<>();
        }

        @Override
        public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
            return new VoidDrainDriver<>();
        }

        @Override
        public void close() throws IOException {
            return;
        }
    }

    private static class Manipulator extends ResourceManipulator {

        Manipulator() {
            return;
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public void cleanupSource(ProcessScript<?> script) throws IOException {
            return;
        }

        @Override
        public void cleanupDrain(ProcessScript<?> script) throws IOException {
            return;
        }

        @Override
        public <T> SourceDriver<T> createSourceForSource(ProcessScript<T> script) throws IOException {
            return new VoidSourceDriver<>();
        }

        @Override
        public <T> DrainDriver<T> createDrainForSource(ProcessScript<T> script) throws IOException {
            return new VoidDrainDriver<>();
        }

        @Override
        public <T> SourceDriver<T> createSourceForDrain(ProcessScript<T> script) throws IOException {
            return new VoidSourceDriver<>();
        }

        @Override
        public <T> DrainDriver<T> createDrainForDrain(ProcessScript<T> script) throws IOException {
            return new VoidDrainDriver<>();
        }
    }
}
