/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.context.SimulationSupport;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.util.ProcessUtil;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.stream.StreamDrainDriver;
import com.asakusafw.windgate.stream.StreamSourceDriver;
import com.asakusafw.windgate.stream.WindGateStreamLogger;

/**
 * An implementation of {@link ResourceMirror} using local file system.
 * @since 0.2.4
 * @version 0.4.0
 */
@SimulationSupport
public class FileResourceMirror extends ResourceMirror {

    static final WindGateLogger WGLOG = new WindGateStreamLogger(FileResourceMirror.class);

    static final Logger LOG = LoggerFactory.getLogger(FileResourceMirror.class);

    private final FileProfile profile;

    private final ParameterList arguments;

    /**
     * Creates a new instance.
     * @param profile the current profile
     * @param arguments arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileResourceMirror(FileProfile profile, ParameterList arguments) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        this.profile = profile;
        this.arguments = arguments;
    }

    @Override
    public String getName() {
        return profile.getResourceName();
    }

    @Override
    public void prepare(GateScript script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Preparing file resource: {}",
                getName());
        for (ProcessScript<?> process : script.getProcesses()) {
            if (process.getSourceScript().getResourceName().equals(getName())) {
                FileResourceUtil.getPath(profile, process, arguments, DriverScript.Kind.SOURCE);
                FileResourceUtil.loadSupport(profile, process, DriverScript.Kind.SOURCE);
                ProcessUtil.newDataModel(profile.getResourceName(), process);
            }
            if (process.getDrainScript().getResourceName().equals(getName())) {
                FileResourceUtil.getPath(profile, process, arguments, DriverScript.Kind.DRAIN);
                FileResourceUtil.loadSupport(profile, process, DriverScript.Kind.DRAIN);
            }
        }
    }

    @Override
    public <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating source driver for resource \"{}\" in process \"{}\"",
                getName(),
                script.getName());
        File path = FileResourceUtil.getPath(profile, script, arguments, DriverScript.Kind.SOURCE);
        DataModelStreamSupport<? super T> support = FileResourceUtil.loadSupport(
                profile, script, DriverScript.Kind.SOURCE);
        T model = ProcessUtil.newDataModel(profile.getResourceName(), script);
        LOG.debug("Source driver uses file: {} (resource={}, process={})", new Object[] {
                path.getAbsolutePath(),
                getName(),
                script.getName(),
        });
        FileInputStreamProvider provider = new FileInputStreamProvider(path);
        return new StreamSourceDriver<>(getName(), script.getName(), provider, support, model);
    }

    @Override
    public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating drain driver for resource \"{}\" in process \"{}\"",
                getName(),
                script.getName());
        File path = FileResourceUtil.getPath(profile, script, arguments, DriverScript.Kind.DRAIN);
        DataModelStreamSupport<? super T> support = FileResourceUtil.loadSupport(
                profile, script, DriverScript.Kind.DRAIN);
        LOG.debug("Drain driver uses file: {} (resource={}, process={})", new Object[] {
                path.getAbsolutePath(),
                getName(),
                script.getName(),
        });
        FileOutputStreamProvider provider = new FileOutputStreamProvider(path);
        return new StreamDrainDriver<>(getName(), script.getName(), provider, support);
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
