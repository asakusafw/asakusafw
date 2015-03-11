/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.FileProcess;

/**
 * {@link ResourceMirror} using files.
 * @since 0.2.2
 * @see FileProcess
 */
public class FileResourceMirror extends ResourceMirror {

    private final String name;

    /**
     * Creates a new instance.
     * @param name the resource name
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public FileResourceMirror(String name) {
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
    public void prepare(GateScript script) throws IOException {
        return;
    }

    @Override
    public <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException {
        File file = getPath(script, DriverScript.Kind.SOURCE);
        return new FileSourceDriver<T>(script.getDataClass(), file);
    }

    @Override
    public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
        File file = getPath(script, DriverScript.Kind.DRAIN);
        return new FileDrainDriver<T>(script.getDataClass(), file);
    }

    @Override
    public void close() throws IOException {
        return;
    }

    static File getPath(ProcessScript<?> script, DriverScript.Kind kind) throws IOException {
        assert script != null;
        assert kind != null;
        String path = script.getDriverScript(kind).getConfiguration().get(FileProcess.FILE.key());
        if (path == null) {
            throw new IOException(MessageFormat.format(
                    "The configuration \"{0}\" is not specified ({1}.{2}.{0}).",
                    FileProcess.FILE.key(),
                    script.getName(),
                    kind.prefix));
        }
        File file = new File(path);
        return file;
    }
}
