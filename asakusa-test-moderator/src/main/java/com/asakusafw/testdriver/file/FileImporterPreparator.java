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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.AbstractImporterPreparator;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.ImporterPreparator;
import com.asakusafw.vocabulary.external.FileImporterDescription;

/**
 * Implementation of {@link ImporterPreparator} for {@link FileImporterDescription}s.
 * @since 0.2.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FileImporterPreparator extends AbstractImporterPreparator<FileImporterDescription> {

    static final Logger LOG = LoggerFactory.getLogger(FileImporterPreparator.class);

    private final ConfigurationFactory configurations;

    /**
     * Creates a new instance with default configurations.
     */
    public FileImporterPreparator() {
        this(ConfigurationFactory.getDefault());
    }

    /**
     * Creates a new instance.
     * @param configurations the configuration factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileImporterPreparator(ConfigurationFactory configurations) {
        if (configurations == null) {
            throw new IllegalArgumentException("configurations must not be null"); //$NON-NLS-1$
        }
        this.configurations = configurations;
    }

    @Override
    public <V> void truncate(
            DataModelDefinition<V> definition,
            FileImporterDescription description) throws IOException {
        checkType(definition, description);
        LOG.info("インポート元をクリアしています: {}", description);
        Configuration config = configurations.newInstance();
        FileSystem fs = FileSystem.get(config);
        try {
            for (String path : description.getPaths()) {
                LOG.debug("ファイルを削除しています: {}", path);
                boolean succeed = fs.delete(new Path(path), true);
                LOG.debug("ファイルを削除しました (succeed={}): {}", succeed, path);
            }
        } finally {
            fs.close();
        }
        return;
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            FileImporterDescription description) throws IOException {
        LOG.info("インポート元の初期値を設定します: {}", description);
        checkType(definition, description);
        Set<String> path = description.getPaths();
        if (path.isEmpty()) {
            return new ModelOutput<V>() {
                @Override
                public void close() throws IOException {
                    return;
                }
                @Override
                public void write(V model) throws IOException {
                    return;
                }
            };
        }
        String destination = path.iterator().next();
        Configuration conf = configurations.newInstance();
        FileOutputFormat output = getOpposite(conf, description.getInputFormat());
        FileDeployer deployer = new FileDeployer(conf);
        return deployer.openOutput(definition, destination, output);
    }

    private <V> void checkType(DataModelDefinition<V> definition,
            FileImporterDescription description) throws IOException {
        if (definition.getModelClass() != description.getModelType()) {
            throw new IOException(MessageFormat.format(
                    "型が一致しません: モデルの型={0}, エクスポータの型={1} ({2})",
                    definition.getModelClass().getName(),
                    description.getModelType().getName(),
                    description));
        }
    }

    private FileOutputFormat getOpposite(Configuration conf, Class<?> inputFormat) throws IOException {
        assert conf != null;
        assert inputFormat != null;
        LOG.debug("Finding opposite OutputFormat: {}", inputFormat.getName());
        String inputFormatName = inputFormat.getName();
        String outputFormatName = infer(inputFormatName);
        if (outputFormatName == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to infer opposite OutputFormat: {0}",
                    inputFormat.getName()));
        }
        LOG.debug("Inferred oppsite OutputFormat: {}", outputFormatName);
        try {
            Class<?> loaded = inputFormat.getClassLoader().loadClass(outputFormatName);
            FileOutputFormat instance = (FileOutputFormat) ReflectionUtils.newInstance(loaded, conf);
            if (instance instanceof Configurable) {
                ((Configurable) instance).setConf(conf);
            }
            return instance;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to create opposite OutputFormat: {0}",
                    inputFormat.getName()), e);
        }
    }

    private static final Pattern INPUT = Pattern.compile("\\binput\\b|Input(?![a-z])");
    private String infer(String inputFormatName) {
        assert inputFormatName != null;
        Matcher matcher = INPUT.matcher(inputFormatName);
        StringBuilder buf = new StringBuilder();
        int start = 0;
        while (matcher.find()) {
            String group = matcher.group();
            buf.append(inputFormatName.substring(start, matcher.start()));
            if (group.equals("input")) {
                buf.append("output");
            } else {
                buf.append("Output");
            }
            start = matcher.end();
        }
        buf.append(inputFormatName.substring(start));
        return buf.toString();
    }
}
