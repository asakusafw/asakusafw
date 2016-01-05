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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.testdriver.core.BaseExporterRetriever;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.ExporterRetriever;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.vocabulary.external.FileExporterDescription;

/**
 * Implementation of {@link ExporterRetriever} for {@link FileExporterDescription}s.
 * @since 0.2.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FileExporterRetriever extends BaseExporterRetriever<FileExporterDescription> {

    static final Logger LOG = LoggerFactory.getLogger(FileExporterRetriever.class);

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
    public void truncate(
            FileExporterDescription description,
            TestContext context) throws IOException {
        LOG.info("cleaning output files: {}", description);
        VariableTable variables = createVariables(context);
        Configuration config = configurations.newInstance();
        String resolved = variables.parse(description.getPathPrefix(), false);
        Path path = new Path(resolved);
        FileSystem fs = path.getFileSystem(config);
        Path output = path.getParent();
        Path target;
        if (output == null) {
            LOG.warn("エクスポート先のディレクトリはベースディレクトリなので削除されません: {}", path);
            target = fs.makeQualified(path);
        } else {
            LOG.warn("エクスポート先をディレクトリごと削除します: {}", output);
            target = fs.makeQualified(output);
        }
        LOG.debug("start removing file: {}", target);
        boolean succeed = fs.delete(target, true);
        LOG.debug("finish removing file (succeed={}): {}", succeed, target);
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            FileExporterDescription description,
            TestContext context) throws IOException {
        LOG.info("creating output file: {}", description);
        checkType(definition, description);
        VariableTable variables = createVariables(context);
        String destination = description.getPathPrefix().replace('*', '_');
        String resolved = variables.parse(destination, false);
        Configuration conf = configurations.newInstance();
        FileOutputFormat output = ReflectionUtils.newInstance(description.getOutputFormat(), conf);
        FileDeployer deployer = new FileDeployer(conf);
        return deployer.openOutput(definition, resolved, output);
    }

    @Override
    public <V> DataModelSource createSource(
            DataModelDefinition<V> definition,
            FileExporterDescription description,
            TestContext context) throws IOException {
        LOG.info("エクスポート結果を取得します: {}", description);
        VariableTable variables = createVariables(context);
        checkType(definition, description);
        Configuration conf = configurations.newInstance();
        Job job = JobCompatibility.newJob(conf);
        String resolved = variables.parse(description.getPathPrefix(), false);
        FileInputFormat.setInputPaths(job, new Path(resolved));
        TaskAttemptContext taskContext = JobCompatibility.newTaskAttemptContext(
                job.getConfiguration(),
                JobCompatibility.newTaskAttemptId(JobCompatibility.newTaskId(JobCompatibility.newJobId())));
        FileInputFormat<?, V> format = getOpposite(conf, description.getOutputFormat());
        FileInputFormatDriver<V> result = new FileInputFormatDriver<V>(definition, taskContext, format);
        return result;
    }

    private VariableTable createVariables(TestContext context) {
        assert context != null;
        VariableTable result = new VariableTable();
        result.defineVariables(context.getArguments());
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
        LOG.debug("Finding opposite InputFormat: {}", outputFormat.getName());
        String outputFormatName = outputFormat.getName();
        String inputFormatName = infer(outputFormatName);
        if (inputFormatName == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to infer opposite OutputFormat: {0}",
                    outputFormat.getName()));
        }
        LOG.debug("Inferred opposite InputFormat: {}", inputFormatName);
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
        if (start == 0) {
            // failed to infer
            return null;
        }
        buf.append(outputFormatName.substring(start));
        return buf.toString();
    }
}
