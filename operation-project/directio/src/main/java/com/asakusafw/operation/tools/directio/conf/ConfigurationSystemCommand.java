/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.operation.tools.directio.conf;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.common.ConfigurationParameter;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing Direct I/O system configuration.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "system",
        commandDescriptionKey = "command.configuration-system",
        resourceBundle = "com.asakusafw.operation.tools.directio.jcommander"
)
public class ConfigurationSystemCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ConfigurationSystemCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @ParametersDelegate
    final ConfigurationParameter configurationParameter = new ConfigurationParameter();

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        Configuration conf = configurationParameter.getConfiguration();
        try (PrintWriter writer = outputParameter.open()) {
            try {
                org.apache.hadoop.fs.Path systemDir = getSystemDir(conf);
                org.apache.hadoop.fs.Path localTemp = HadoopDataSourceUtil.getLocalTemporaryDirectory(
                        FileSystem.getLocal(conf));
                writer.printf("configuration: %s%n", configurationParameter.getPath()
                        .map(Path::toString)
                        .orElse("N/A"));
                writer.printf("system directory: %s%n", systemDir);
                writer.printf("local temporary: %s%n", Optional.ofNullable(localTemp)
                        .map(it -> it.toString())
                        .orElse("N/A"));
            } catch (IOException e) {
                LOG.warn("error occurred while loading system configuration", e);
            }
        }
    }

    private static org.apache.hadoop.fs.Path getSystemDir(Configuration conf) throws IOException {
        try {
            return HadoopDataSourceUtil.getSystemDir(conf, true);
        } catch (IOException e) {
            org.apache.hadoop.fs.Path raw = HadoopDataSourceUtil.getSystemDir(conf, false);
            LOG.warn(MessageFormat.format(
                    "cannot resolve Direct I/O system directory: {0} ({1})",
                    raw,
                    Optional.ofNullable(e.getMessage()).orElseGet(() -> e.toString())));
            return raw;
        }
    }
}
