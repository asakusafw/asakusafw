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
package com.asakusafw.windgate.hadoopfs.ssh;

import static com.asakusafw.windgate.core.vocabulary.FileProcess.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableFactories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.hadoopfs.sequencefile.SequenceFileDrainDriver;
import com.asakusafw.windgate.hadoopfs.sequencefile.SequenceFileProvider;
import com.asakusafw.windgate.hadoopfs.sequencefile.SequenceFileSourceDriver;
import com.asakusafw.windgate.hadoopfs.sequencefile.SequenceFileUtil;

/**
 * An abstract implementation of {@link ResourceMirror} using Hadoop File System via SSH connection.
 * @since 0.2.3
 * @see FileProcess
 */
public abstract class AbstractSshHadoopFsMirror extends ResourceMirror {

    static final Logger LOG = LoggerFactory.getLogger(AbstractSshHadoopFsMirror.class);

    private final Configuration configuration;

    private final SshProfile profile;

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

    @SuppressWarnings("unchecked")
    @Override
    public <T> SourceDriver<T> createSource(final ProcessScript<T> script) throws IOException {
        LOG.debug("Creating source driver for resource \"{}\" in process \"{}\"",
                getName(),
                script.getName());
        List<String> path = getPath(script, DriverScript.Kind.SOURCE);
        // TODO logging INFO
        NullWritable key = NullWritable.get();
        Writable value = newDataModel(script);
        final SshConnection connection = openGet(path);
        boolean succeeded = false;
        try {
            InputStream output = connection.openStandardOutput();
            // TODO logging INFO
            connection.connect();
            FileList.Reader fileList = FileList.createReader(output);
            SequenceFileProvider provider = new FileListSequenceFileProvider(configuration, fileList);
            SequenceFileSourceDriver<Writable, Writable> result =
                new SequenceFileSourceDriver<Writable, Writable>(provider, key, value) {
                @Override
                public void close() throws IOException {
                    try {
                        LOG.debug("Closing source driver for resource \"{}\" in process \"{}\"",
                                getName(),
                                script.getName());
                        super.close();
                        int exit = connection.waitForExit(TimeUnit.SECONDS.toMillis(30));
                        if (exit != 0) {
                            throw new IOException(MessageFormat.format(
                                    "SSH connection returns unexpected exit code: (code={0}, process={1}:source)",
                                    String.valueOf(exit),
                                    script.getName()));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Failed to exit remote process", e);
                    } finally {
                        try {
                            connection.close();
                        } catch (IOException e) {
                            // TODO logging WARN
                        }
                    }
                }
            };
            succeeded = true;
            return (SourceDriver<T>) result;
        } finally {
            if (succeeded == false) {
                connection.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> DrainDriver<T> createDrain(final ProcessScript<T> script) throws IOException {
        LOG.debug("Creating drain driver for resource \"{}\" in process \"{}\"",
                getName(),
                script.getName());
        List<String> path = getPath(script, DriverScript.Kind.DRAIN);
        // TODO logging INFO
        final SshConnection connection = openPut();
        boolean succeeded = false;
        try {
            OutputStream input = connection.openStandardInput();
            // TODO logging INFO
            connection.connect();
            final FileList.Writer fileList = FileList.createWriter(input);
            SequenceFile.Writer writer = SequenceFileUtil.openWriter(
                    fileList.openNext(FileList.createFileStatus(new Path(path.get(0)))),
                    configuration,
                    NullWritable.class,
                    script.getDataClass(),
                    profile.getCompressionCodec());
            SequenceFileDrainDriver<Writable, Writable> result =
                new SequenceFileDrainDriver<Writable, Writable>(writer, NullWritable.get()) {
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
                            throw new IOException(MessageFormat.format(
                                    "SSH connection returns unexpected exit code: (code={0}, process={1}:drain)",
                                    String.valueOf(exit),
                                    script.getName()));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Failed to exit remote process", e);
                    } finally {
                        try {
                            connection.close();
                        } catch (IOException e) {
                            // TODO logging WARN
                        }
                    }
                }
            };
            succeeded = true;
            return (DrainDriver<T>) result;
        } finally {
            if (succeeded == false) {
                connection.close();
            }
        }
    }

    private SshConnection openGet(List<String> paths) throws IOException {
        assert paths != null;
        StringBuilder buf = new StringBuilder();
        buf.append(profile.getGetCommand());
        for (String path : paths) {
            buf.append(' ');
            buf.append(path);
        }
        SshConnection connection = openConnection(profile, buf.toString());
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
        SshConnection connection = openConnection(profile, profile.getPutCommand());
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
    protected abstract SshConnection openConnection(SshProfile sshProfile, String command) throws IOException;

    private List<String> getPath(ProcessScript<?> proc, DriverScript.Kind kind) throws IOException {
        assert proc != null;
        assert kind != null;
        DriverScript script = proc.getDriverScript(kind);
        String pathString = script.getConfiguration().get(FILE.key());
        if (pathString == null) {
            throw new IOException(MessageFormat.format(
                    "Process \"{1}\" must declare \"{3}\": (resource={0}, kind={2})",
                    getName(),
                    proc.getName(),
                    kind.toString(),
                    FILE.key()));
        }
        String[] paths = pathString.split("[ \t\r\n]+");
        List<String> results = new ArrayList<String>();
        for (String path : paths) {
            if (path.isEmpty()) {
                continue;
            }
            try {
                String resolved = arguments.replace(path, true);
                results.add(resolved);
            } catch (IllegalArgumentException e) {
                throw new IOException(MessageFormat.format(
                        "Failed to resolve the {2} path: {3} (resource={0}, process={1})",
                        getName(),
                        proc.getName(),
                        kind.toString(),
                        path));
            }
        }
        if (kind == DriverScript.Kind.SOURCE && results.size() <= 0) {
            throw new IOException(MessageFormat.format(
                    "source path must be greater than 0: {2} (resource={0}, process={1})",
                    getName(),
                    proc.getName(),
                    results));
        }
        if (kind == DriverScript.Kind.DRAIN && results.size() != 1) {
            throw new IOException(MessageFormat.format(
                    "drain path must be one: {2} (resource={0}, process={1})",
                    getName(),
                    proc.getName(),
                    results));
        }
        return results;
    }

    private Writable newDataModel(ProcessScript<?> script) throws IOException {
        assert script != null;
        Class<?> dataClass = script.getDataClass();
        LOG.debug("Creating data model object: {} (resource={}, process={})", new Object[] {
                dataClass.getName(),
                getName(),
                script.getName(),
        });
        if (Writable.class.isAssignableFrom(dataClass) == false) {
            throw new IOException(MessageFormat.format(
                    "Data model class {2} must be a subtype of {3} (resource={0}, process={1})",
                    getName(),
                    script.getName(),
                    dataClass.getName(),
                    Writable.class.getName()));
        }
        try {
            return WritableFactories.newInstance(
                    dataClass.asSubclass(Writable.class),
                    configuration);
        } catch (Exception e) {
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
}
