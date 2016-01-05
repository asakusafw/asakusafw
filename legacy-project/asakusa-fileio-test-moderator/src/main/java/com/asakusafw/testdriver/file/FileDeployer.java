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
package com.asakusafw.testdriver.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.DataModelDefinition;

/**
 * Open output and finally deployes result data.
 */
final class FileDeployer {

    static final Logger LOG = LoggerFactory.getLogger(FileDeployer.class);

    private final Configuration configuration;

    /**
     * Creates a new instance.
     * @param configuration configuration to be used
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileDeployer(Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.configuration = configuration;
    }

    /**
     * Opens output for the specified {@link OutputFormat}.
     * @param <V> value type
     * @param definition target model definition
     * @param destination output location
     * @param output format
     * @return the opened {@link ModelOutput}
     * @throws IOException if failed to open the target output
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <V> ModelOutput<V> openOutput(
            DataModelDefinition<V> definition,
            final String destination,
            FileOutputFormat<? super NullWritable, ? super V> output) throws IOException {
        assert destination != null;
        assert output != null;
        LOG.debug("Opening {} using {}", destination, output.getClass().getName());
        Job job = JobCompatibility.newJob(configuration);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(definition.getModelClass());
        final File temporaryDir = File.createTempFile("asakusa", ".tempdir");
        if (temporaryDir.delete() == false || temporaryDir.mkdirs() == false) {
            throw new IOException("Failed to create temporary directory");
        }
        LOG.debug("Using staging deploy target: {}", temporaryDir);
        URI uri = temporaryDir.toURI();
        FileOutputFormat.setOutputPath(job, new Path(uri));
        TaskAttemptContext context = JobCompatibility.newTaskAttemptContext(
                job.getConfiguration(),
                JobCompatibility.newTaskAttemptId(JobCompatibility.newTaskId(JobCompatibility.newJobId())));
        FileOutputFormatDriver<V> result = new FileOutputFormatDriver<V>(context, output, NullWritable.get()) {
            @Override
            public void close() throws IOException {
                super.close();
                deploy(destination, temporaryDir);
            }
        };
        return result;
    }

    void deploy(String destination, File temporaryDir) throws IOException {
        assert destination != null;
        assert temporaryDir != null;
        LOG.debug("Deploying staging results: {} -> {}", temporaryDir, destination);
        try {
            File result = findResult(temporaryDir);
            copy(result, destination);
        } finally {
            delete(temporaryDir);
        }
    }

    private File findResult(File temporaryDir) throws IOException {
        assert temporaryDir != null;
        for (File file : temporaryDir.listFiles()) {
            if (file.getName().startsWith("part-")) {
                return file;
            }
        }
        throw new FileNotFoundException("Cannot find commited result");
    }

    private void copy(File result, String destination) throws IOException {
        assert result != null;
        assert destination != null;
        Path target = new Path(destination);
        FileSystem fs = target.getFileSystem(configuration);
        fs.copyFromLocalFile(new Path(result.toURI()), target);
    }

    private void delete(File target) {
        assert target != null;
        if (target.isDirectory()) {
            for (File child : target.listFiles()) {
                delete(child);
            }
        }
        if (target.delete() == false) {
            LOG.warn("Failed to delete temporary resource: {}", target);
        }
    }
}
