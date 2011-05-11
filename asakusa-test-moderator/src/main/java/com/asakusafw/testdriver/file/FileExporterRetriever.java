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
package com.asakusafw.testdriver.file;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.AbstractExporterRetriever;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.ExporterRetriever;
import com.asakusafw.vocabulary.external.FileExporterDescription;

/**
 * Implementation of {@link ExporterRetriever} for {@link FileExporterDescription}s.
 * @since 0.2.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FileExporterRetriever extends AbstractExporterRetriever<FileExporterDescription> {

    private final ConfigurationFactory configurations;

    /**
     * Creates a new instance with default configurations.
     */
    public FileExporterRetriever() {
        this(ConfigurationFactory.getDefault());
    }

    /**
     * Creates a new instance.
     * @param configurations the configuration factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileExporterRetriever(ConfigurationFactory configurations) {
        if (configurations == null) {
            throw new IllegalArgumentException("configurations must not be null"); //$NON-NLS-1$
        }
        this.configurations = configurations;
    }

    @Override
    public <V> void truncate(
            DataModelDefinition<V> definition,
            FileExporterDescription description) throws IOException {
        checkType(definition, description);
        // do nothing
        return;
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            FileExporterDescription description) throws IOException {
        checkType(definition, description);
        String destination = description.getPathPrefix().replace('*', '_');
        Configuration conf = configurations.newInstance();
        FileOutputFormat output = ReflectionUtils.newInstance(description.getOutputFormat(), conf);
        FileDeployer deployer = new FileDeployer(conf);
        return deployer.openOutput(destination, output);
    }

    @Override
    public <V> DataModelSource createSource(
            DataModelDefinition<V> definition,
            FileExporterDescription description) throws IOException {
        checkType(definition, description);
        Configuration conf = configurations.newInstance();
        Job job = new Job(conf);
        FileInputFormat.setInputPaths(job, new Path(description.getPathPrefix()));
        TaskAttemptContext context = new TaskAttemptContext(job.getConfiguration(), new TaskAttemptID());
        InputFormat<?, V> format = getOpposite(conf, description.getOutputFormat());
        InputFormatDriver<V> result = new InputFormatDriver<V>(definition, context, format);
        return result;
    }

    private <V> void checkType(DataModelDefinition<V> definition,
            FileExporterDescription description) throws IOException {
        if (definition.getModelClass() != description.getModelType()) {
            throw new IOException(MessageFormat.format(
                    "型が一致しません: モデルの型={0}, エクスポータの型={1} ({2})",
                    definition.getModelClass().getName(),
                    description.getModelType().getName(),
                    description));
        }
    }

    private FileInputFormat getOpposite(Configuration conf, Class<?> outputFormat) throws IOException {
        assert conf != null;
        assert outputFormat != null;
        String outputFormatName = outputFormat.getName();
        String inputFormatName = infer(outputFormatName);
        if (inputFormatName == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to infer opposite OutputFormat: {0}",
                    outputFormat.getName()));
        }
        try {
            Class<?> loaded = outputFormat.getClassLoader().loadClass(inputFormatName);
            FileInputFormat instance = (FileInputFormat) ReflectionUtils.newInstance(loaded, conf);
            if (instance instanceof Configurable) {
                ((Configurable) instance).setConf(conf);
            }
            return instance;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to create opposite OutputFormat: {0}",
                    outputFormat.getName()), e);
        }
    }

    private static final Pattern OUTPUT = Pattern.compile("\\boutput\\b|Output(?![a-z])");
    private String infer(String outputFormatName) {
        assert outputFormatName != null;
        Matcher matcher = OUTPUT.matcher(outputFormatName);
        StringBuilder buf = new StringBuilder();
        int start = 0;
        while (matcher.find()) {
            String group = matcher.group();
            buf.append(outputFormatName.substring(start, matcher.start()));
            if (group.equals("output")) {
                buf.append("input");
            } else {
                buf.append("Input");
            }
            start = matcher.end();
        }
        buf.append(outputFormatName.substring(start));
        return buf.toString();
    }


}
