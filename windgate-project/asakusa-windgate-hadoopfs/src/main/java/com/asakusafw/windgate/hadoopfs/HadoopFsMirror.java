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
package com.asakusafw.windgate.hadoopfs;

import static com.asakusafw.windgate.core.vocabulary.FileProcess.*;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.SimulationSupport;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.util.VoidModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.hadoopfs.temporary.FileSystemModelInputProvider;
import com.asakusafw.windgate.hadoopfs.temporary.ModelInputProvider;
import com.asakusafw.windgate.hadoopfs.temporary.ModelInputSourceDriver;
import com.asakusafw.windgate.hadoopfs.temporary.ModelOutputDrainDriver;

/**
 * An abstract implementation of {@link ResourceMirror} directly using Hadoop File System.
 * @since 0.2.2
 * @version 0.4.0
 * @see FileProcess
 */
@SimulationSupport
public class HadoopFsMirror extends ResourceMirror {

    static final WindGateLogger WGLOG = new HadoopFsLogger(HadoopFsMirror.class);

    static final Logger LOG = LoggerFactory.getLogger(HadoopFsMirror.class);

    private final Configuration configuration;

    private final HadoopFsProfile profile;

    private final ParameterList arguments;

    /**
     * Creates a new instance.
     * @param configuration the hadoop configuration
     * @param profile the profile
     * @param arguments the arguments
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public HadoopFsMirror(
            Configuration configuration,
            HadoopFsProfile profile,
            ParameterList arguments) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        this.configuration = configuration;
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
        LOG.debug("Preparing Direct Hadoop FS resource: {}",
                getName());
        for (ProcessScript<?> process : script.getProcesses()) {
            if (process.getSourceScript().getResourceName().equals(getName())) {
                getPath(process, DriverScript.Kind.SOURCE);
            }
            if (process.getDrainScript().getResourceName().equals(getName())) {
                getPath(process, DriverScript.Kind.DRAIN);
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
        List<Path> pathList = getPath(script, DriverScript.Kind.SOURCE);

        T value = newDataModel(script);
        ModelInputProvider<T> provider = null;
        boolean succeeded = false;
        try {
            FileSystem fs = FileSystem.get(profile.getBasePath().toUri(), configuration);
            provider = new FileSystemModelInputProvider<>(configuration, fs, pathList, script.getDataClass());
            SourceDriver<T> result = new ModelInputSourceDriver<>(provider, value);
            succeeded = true;
            return result;
        } finally {
            if (succeeded == false) {
                if (provider != null) {
                    try {
                        provider.close();
                    } catch (IOException e) {
                        WGLOG.warn(e, "W03001",
                                profile.getResourceName(),
                                script.getName(),
                                pathList);
                    }
                }
            }
        }
    }

    @Override
    public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating drain driver for resource \"{}\" in process \"{}\"",
                getName(),
                script.getName());
        List<Path> pathList = getPath(script, DriverScript.Kind.DRAIN);
        assert pathList.size() == 1;
        Path path = pathList.get(0);

        ModelOutput<T> output = null;
        boolean succeeded = false;
        try {
            if (RuntimeContext.get().isSimulation()) {
                output = new VoidModelOutput<>();
            } else {
                output = TemporaryStorage.openOutput(configuration, script.getDataClass(), path);
            }
            DrainDriver<T> result = new ModelOutputDrainDriver<>(output);
            succeeded = true;
            return result;
        } finally {
            if (succeeded == false) {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        WGLOG.warn(e, "W04001",
                                profile.getResourceName(),
                                script.getName(),
                                pathList);
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        return;
    }

    private List<Path> getPath(ProcessScript<?> proc, DriverScript.Kind kind) throws IOException {
        assert proc != null;
        assert kind != null;
        DriverScript script = proc.getDriverScript(kind);
        String pathString = script.getConfiguration().get(FILE.key());
        if (pathString == null) {
            WGLOG.error("E01001",
                    getName(),
                    proc.getName(),
                    kind.prefix,
                    FILE.key(),
                    null);
            throw new IOException(MessageFormat.format(
                    "Process \"{1}\" must declare \"{3}\": (resource={0}, kind={2})",
                    getName(),
                    proc.getName(),
                    kind.toString(),
                    FILE.key()));
        }
        List<Path> results = resolvePaths(proc, kind, pathString);
        if (kind == DriverScript.Kind.SOURCE && results.size() <= 0 && RuntimeContext.get().isSimulation() == false) {
            WGLOG.error("E01001",
                    getName(),
                    proc.getName(),
                    kind.prefix,
                    FILE.key(),
                    pathString);
            throw new IOException(MessageFormat.format(
                    "source path must be greater than 0: {2} (resource={0}, process={1})",
                    getName(),
                    proc.getName(),
                    results));
        }
        if (kind == DriverScript.Kind.DRAIN && results.size() != 1) {
            WGLOG.error("E01001",
                    getName(),
                    proc.getName(),
                    kind.prefix,
                    FILE.key(),
                    pathString);
            throw new IOException(MessageFormat.format(
                    "drain path must be one: {2} (resource={0}, process={1})",
                    getName(),
                    proc.getName(),
                    results));
        }
        return results;
    }

    private List<Path> resolvePaths(
            ProcessScript<?> proc,
            DriverScript.Kind kind,
            String pathString) throws IOException {
        assert proc != null;
        assert kind != null;
        assert pathString != null;
        Path basePath = profile.getBasePath();
        String[] paths = pathString.split("[ \t\r\n]+");
        List<Path> results = new ArrayList<>();
        for (String path : paths) {
            if (path.isEmpty()) {
                continue;
            }
            String resolved;
            try {
                resolved = arguments.replace(path, true);
            } catch (IllegalArgumentException e) {
                WGLOG.error(e, "E01001",
                        getName(),
                        proc.getName(),
                        kind.prefix,
                        FILE.key(),
                        pathString);
                throw new IOException(MessageFormat.format(
                        "Failed to resolve the {2} path: {3} (resource={0}, process={1})",
                        getName(),
                        proc.getName(),
                        kind.toString(),
                        path));
            }
            Path relative = new Path(resolved);
            if (relative.isAbsolute()) {
                WGLOG.warn("W01001",
                        getName(),
                        proc.getName(),
                        kind.prefix,
                        FILE.key(),
                        pathString);
            }
            results.add(new Path(basePath, relative));
        }
        return results;
    }

    private <T> T newDataModel(ProcessScript<T> script) throws IOException {
        assert script != null;
        Class<T> dataClass = script.getDataClass();
        LOG.debug("Creating data model object: {} (resource={}, process={})", new Object[] {
                dataClass.getName(),
                getName(),
                script.getName(),
        });
        try {
            return ReflectionUtils.newInstance(dataClass, configuration);
        } catch (Exception e) {
            WGLOG.error("E01002",
                    getName(),
                    script.getName(),
                    FILE.key(),
                    dataClass.getName());
            throw new IOException(MessageFormat.format(
                    "Failed to create a new instance: {2} (resource={0}, process={1})",
                    getName(),
                    script.getName(),
                    dataClass.getName()), e);
        }
    }
}
