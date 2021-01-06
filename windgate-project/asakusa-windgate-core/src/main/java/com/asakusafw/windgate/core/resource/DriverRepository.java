/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateCoreLogger;
import com.asakusafw.windgate.core.WindGateLogger;

/**
 * Provides {@link SourceDriver}s and {@link DrainDriver}s from {@link ResourceMirror}s.
 * @since 0.2.2
 */
public class DriverRepository implements DriverFactory {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(GateProfile.class);

    private final Map<String, ResourceMirror> resources;

    /**
     * Creates a new instance.
     * @param resources original resources
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public DriverRepository(Iterable<? extends ResourceMirror> resources) {
        if (resources == null) {
            throw new IllegalArgumentException("resources must not be null"); //$NON-NLS-1$
        }
        HashMap<String, ResourceMirror> map = new HashMap<>();
        for (ResourceMirror resource : resources) {
            map.put(resource.getName(), resource);
        }
        this.resources = Collections.unmodifiableMap(map);
    }

    @Override
    public <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        String name = script.getSourceScript().getResourceName();
        ResourceMirror resource = resources.get(name);
        if (resource == null) {
            WGLOG.error("E04001",
                    script.getName(),
                    name);
            throw new IOException(MessageFormat.format(
                    "Resource \"{0}\" is not defined (source of \"{1}\")",
                    name,
                    script.getName()));
        }
        return resource.createSource(script);
    }

    @Override
    public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        String name = script.getDrainScript().getResourceName();
        ResourceMirror resource = resources.get(name);
        if (resource == null) {
            WGLOG.error("E04001",
                    script.getName(),
                    name);
            throw new IOException(MessageFormat.format(
                    "Resource \"{0}\" is not defined (drain of \"{1}\")",
                    name,
                    script.getName()));
        }
        return resource.createDrain(script);
    }
}
