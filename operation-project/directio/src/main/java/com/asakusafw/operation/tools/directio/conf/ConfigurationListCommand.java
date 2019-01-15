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
package com.asakusafw.operation.tools.directio.conf;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.BasePath;
import com.asakusafw.operation.tools.directio.common.ConfigurationParameter;
import com.asakusafw.runtime.directio.DirectDataSourceProfile;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;

/**
 * A command for printing Direct I/O configuration.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "list",
        commandDescriptionKey = "command.configuration-list",
        resourceBundle = "com.asakusafw.operation.tools.directio.jcommander"
)
public class ConfigurationListCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ConfigurationListCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @ParametersDelegate
    final ConfigurationParameter configurationParameter = new ConfigurationParameter();

    @Parameter(
            description = "[data-source-ID]",
            required = false)
    String id;

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        Configuration conf = configurationParameter.getConfiguration();
        List<DirectDataSourceProfile> profiles = HadoopDataSourceUtil.loadProfiles(conf);
        DirectDataSourceProfile spec = null;
        if (id != null) {
            spec = profiles.stream()
                    .filter(it -> Objects.equal(it.getId(), id))
                    .findAny()
                    .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                            "data source \"{0}\" not found (available data source: {1})",
                            id,
                            profiles.stream()
                                    .map(DirectDataSourceProfile::getId)
                                    .collect(Collectors.joining(", ")))));
        }

        try (PrintWriter writer = outputParameter.open()) {
            if (spec == null) {
                verboseParameter.printf(writer, "total %,d%n", profiles.size());
                profiles.forEach(it -> {
                    writer.printf("%s%n", it.getId());
                    verboseParameter.ifRequired(() -> print(writer, it, 4));
                });
            } else {
                print(writer, spec, 0);
            }
        }
    }

    static void print(PrintWriter writer, DirectDataSourceProfile profile, int indent) {
        writer.printf("%sID: %s%n", indent(indent), profile.getId());
        writer.printf("%sbase-path: %s%n", indent(indent), BasePath.of(profile.getPath()));
        writer.printf("%sclass: %s%n", indent(indent), profile.getTargetClass().getName());
        writer.printf("%sattributes:%n", indent(indent));
        profile.getAttributes().forEach((k, v) -> writer.printf("%s- %s: %s%n", indent(indent), k, v));
    }

    private static CharSequence indent(int level) {
        if (level <= 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder(level);
        for (int i = 0; i < level; i++) {
            buf.append(' ');
        }
        return buf;
    }
}
