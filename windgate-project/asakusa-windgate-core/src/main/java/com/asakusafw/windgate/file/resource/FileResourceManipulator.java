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
package com.asakusafw.windgate.file.resource;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * {@link ResourceManipulator} corresponded to {@link FileResourceMirror}.
 * @since 0.2.2
 */
public class FileResourceManipulator extends ResourceManipulator {

    private final String name;

    /**
     * Creates a new instance.
     * @param name the resource name
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public FileResourceManipulator(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void cleanupSource(ProcessScript<?> script) throws IOException {
        cleanup(script, DriverScript.Kind.SOURCE);
    }

    @Override
    public void cleanupDrain(ProcessScript<?> script) throws IOException {
        cleanup(script, DriverScript.Kind.DRAIN);
    }

    private void cleanup(ProcessScript<?> script, DriverScript.Kind kind) throws IOException {
        assert script != null;
        assert kind != null;
        File file = FileResourceMirror.getPath(script, kind);
        if (file.exists() && file.delete() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to cleanup the {3}: {2} (resource={0}, process={1})",
                    getName(),
                    script.getName(),
                    file.getPath(),
                    kind.prefix));
        }
    }

    @Override
    public <T> SourceDriver<T> createSourceForSource(ProcessScript<T> script) throws IOException {
        File file = FileResourceMirror.getPath(script, DriverScript.Kind.SOURCE);
        return new FileSourceDriver<>(script.getDataClass(), file);
    }

    @Override
    public <T> DrainDriver<T> createDrainForSource(ProcessScript<T> script) throws IOException {
        File file = FileResourceMirror.getPath(script, DriverScript.Kind.SOURCE);
        return new FileDrainDriver<>(script.getDataClass(), file);
    }

    @Override
    public <T> SourceDriver<T> createSourceForDrain(ProcessScript<T> script) throws IOException {
        File file = FileResourceMirror.getPath(script, DriverScript.Kind.DRAIN);
        return new FileSourceDriver<>(script.getDataClass(), file);
    }

    @Override
    public <T> DrainDriver<T> createDrainForDrain(ProcessScript<T> script) throws IOException {
        File file = FileResourceMirror.getPath(script, DriverScript.Kind.DRAIN);
        return new FileDrainDriver<>(script.getDataClass(), file);
    }
}
