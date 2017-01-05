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
package com.asakusafw.windgate.hadoopfs.ssh;

import static com.asakusafw.windgate.core.vocabulary.FileProcess.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.context.SimulationSupport;
import com.asakusafw.runtime.io.ModelOutput;
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
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;
import com.asakusafw.windgate.hadoopfs.ssh.FileList.Writer;
import com.asakusafw.windgate.hadoopfs.temporary.ModelInputProvider;
import com.asakusafw.windgate.hadoopfs.temporary.ModelInputSourceDriver;
import com.asakusafw.windgate.hadoopfs.temporary.ModelOutputDrainDriver;

/**
 * An abstract implementation of {@link ResourceMirror} using Hadoop File System via SSH connection.
 * @since 0.2.2
 * @see FileProcess
 */
public abstract class AbstractSshHadoopFsMirror extends ResourceMirror {

    static final WindGateLogger WGLOG = new HadoopFsLogger(AbstractSshHadoopFsMirror.class);

    static final Logger LOG = LoggerFactory.getLogger(AbstractSshHadoopFsMirror.class);

    private final Configuration configuration;

    final SshProfile profile;

    private final ParameterList arguments;

    /**
     * Creates a new instance.
     * @param configuration the hadoop configuration
     * @param profile the profile
     * @param arguments the arguments
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public AbstractSshHadoopFsMirror(Configuration configuration, SshProfile profile, ParameterList arguments) {
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
        LOG.debug("Preparing Hadoop FS via SSH resource: {}",
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
        List<String> path = getPath(script, DriverScript.Kind.SOURCE);
        T value = newDataModel(script);
        SshConnection connection = openGet(path);
        boolean succeeded = false;
        try {
            InputStream output = connection.openStandardOutput();
            connection.connect();
            FileList.Reader fileList = FileList.createReader(output);
            ModelInputProvider<T> provider = new FileListModelInputProvider<>(
                    configuration, fileList, script.getDataClass());
            ModelInputSourceDriver<T> result = new SshSourceDriver<>(provider, value, script, connection, path);
            succeeded = true;
            return result;
        } finally {
            if (succeeded == false) {
                try {
                    connection.close();
                } catch (IOException e) {
                    WGLOG.warn(e, "W13001",
                            profile.getResourceName(),
                            script.getName(),
                            path);
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
        List<String> path = getPath(script, DriverScript.Kind.DRAIN);
        SshConnection connection = openPut();
        boolean succeeded = false;
        try {
            OutputStream input = connection.openStandardInput();
            connection.connect();
            FileList.Writer fileList = FileList.createWriter(input);
            ModelOutput<T> output = TemporaryStorage.openOutput(
                    configuration,
                    script.getDataClass(),
                    fileList.openNext(new Path(path.get(0))));
            ModelOutputDrainDriver<T> result = new SshDrainDriver<>(output, connection, path, fileList, script);
            succeeded = true;
            return result;
        } finally {
            if (succeeded == false) {
                try {
                    connection.close();
                } catch (IOException e) {
                    WGLOG.warn(e, "W14001",
                            profile.getResourceName(),
                            script.getName(),
                            path);
                }
            }
        }
    }

    private SshConnection openGet(List<String> paths) throws IOException {
        assert paths != null;
        List<String> tokens = new ArrayList<>();
        tokens.add(profile.getGetCommand());
        tokens.addAll(paths);
        SshConnection connection = openConnection(profile, tokens);
        boolean succeed = false;
        try {
            connection.openStandardInput().close();
            succeed = true;
            return connection;
        } finally {
            if (succeed == false) {
                connection.close();
            }
        }
    }

    private SshConnection openPut() throws IOException {
        SshConnection connection = openConnection(profile, Collections.singletonList(profile.getPutCommand()));
        boolean succeed = false;
        try {
            connection.redirectStandardOutput(System.out, true);
            succeed = true;
            return connection;
        } finally {
            if (succeed == false) {
                connection.close();
            }
        }
    }

    /**
     * Opens a new SSH command connection.
     * @param sshProfile the ssh profile
     * @param command target command
     * @return the opened connection
     * @throws IOException if failed to open
     */
    protected abstract SshConnection openConnection(SshProfile sshProfile, List<String> command) throws IOException;

    private List<String> getPath(ProcessScript<?> proc, DriverScript.Kind kind) throws IOException {
        assert proc != null;
        assert kind != null;
        DriverScript script = proc.getDriverScript(kind);
        String pathString = script.getConfiguration().get(FILE.key());
        if (pathString == null) {
            WGLOG.error("E11001",
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
        String[] paths = pathString.split("[ \t\r\n]+");
        List<String> results = new ArrayList<>();
        for (String path : paths) {
            if (path.isEmpty()) {
                continue;
            }
            try {
                String resolved = arguments.replace(path, true);
                results.add(resolved);
            } catch (IllegalArgumentException e) {
                WGLOG.error(e, "E11001",
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
        }
        if (kind == DriverScript.Kind.SOURCE && results.size() <= 0) {
            WGLOG.error("E11001",
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
            WGLOG.error("E11001",
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
            WGLOG.error("E11002",
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

    @Override
    public void close() throws IOException {
        LOG.debug("Closing Hadoop FS via SSH resource: {}",
                getName());
    }

    @SimulationSupport
    private final class SshSourceDriver<T> extends ModelInputSourceDriver<T> {

        private final ProcessScript<T> script;

        private final SshConnection connection;

        private final List<String> path;

        SshSourceDriver(
                ModelInputProvider<T> provider,
                T value,
                ProcessScript<T> script,
                SshConnection connection,
                List<String> path) {
            super(provider, value);
            this.script = script;
            this.connection = connection;
            this.path = path;
        }

        @Override
        public void close() throws IOException {
            try {
                LOG.debug("Closing source driver for resource \"{}\" in process \"{}\"",
                        getName(),
                        script.getName());
                super.close();
                int exit = connection.waitForExit(TimeUnit.SECONDS.toMillis(30));
                if (exit != 0) {
                    WGLOG.error("E13001",
                            profile.getResourceName(),
                            script.getName(),
                            path);
                    throw new IOException(MessageFormat.format(
                            "SSH connection returns unexpected exit code: (code={0}, process={1}:source)",
                            String.valueOf(exit),
                            script.getName()));
                }
            } catch (InterruptedException e) {
                WGLOG.error(e, "E13001",
                        profile.getResourceName(),
                        script.getName(),
                        path);
                Thread.currentThread().interrupt();
                throw new IOException("Failed to exit remote process", e);
            } finally {
                try {
                    connection.close();
                } catch (IOException e) {
                    WGLOG.warn(e, "W13001",
                            profile.getResourceName(),
                            script.getName(),
                            path);
                }
            }
        }
    }

    @SimulationSupport
    private final class SshDrainDriver<T> extends ModelOutputDrainDriver<T> {

        private final SshConnection connection;

        private final List<String> path;

        private final Writer fileList;

        private final ProcessScript<T> script;

        SshDrainDriver(
                ModelOutput<T> output,
                SshConnection connection,
                List<String> path,
                FileList.Writer fileList,
                ProcessScript<T> script) {
            super(output);
            this.connection = connection;
            this.path = path;
            this.fileList = fileList;
            this.script = script;
        }

        @Override
        public void close() throws IOException {
            try {
                LOG.debug("Closing drain driver for resource \"{}\" in process \"{}\"",
                        getName(),
                        script.getName());
                super.close();
                fileList.close();
                int exit = connection.waitForExit(TimeUnit.SECONDS.toMillis(30));
                if (exit != 0) {
                    WGLOG.error("E14001",
                            profile.getResourceName(),
                            script.getName(),
                            path);
                    throw new IOException(MessageFormat.format(
                            "SSH connection returns unexpected exit code: (code={0}, process={1}:drain)",
                            String.valueOf(exit),
                            script.getName()));
                }
            } catch (InterruptedException e) {
                WGLOG.error(e, "E14001",
                        profile.getResourceName(),
                        script.getName(),
                        path);
                Thread.currentThread().interrupt();
                throw new IOException("Failed to exit remote process", e);
            } finally {
                try {
                    connection.close();
                } catch (IOException e) {
                    WGLOG.warn(e, "W14001",
                            profile.getResourceName(),
                            script.getName(),
                            path);
                }
            }
        }
    }
}
