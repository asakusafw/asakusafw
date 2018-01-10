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
package com.asakusafw.operation.tools.portal;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.cli.list.ListBatchCommand;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing list of batch.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "version",
        commandDescriptionKey = "command.version",
        resourceBundle = "com.asakusafw.operation.tools.portal.jcommander"
)
public class VersionCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ListBatchCommand.class);

    static final String ENV_ASAKUSA_HOME = "ASAKUSA_HOME"; //$NON-NLS-1$

    static final String ENV_PATH = "PATH"; //$NON-NLS-1$

    static final String ENV_HADOOP_CMD = "HADOOP_CMD"; //$NON-NLS-1$

    static final String ENV_HADOOP_HOME = "HADOOP_HOME"; //$NON-NLS-1$

    static final String PATH_VERSION = "VERSION"; //$NON-NLS-1$

    static final String PATH_HADOOP_CMD_NAME = "hadoop";

    static final String PATH_HADOOP_CMD = "bin/" + PATH_HADOOP_CMD_NAME; //$NON-NLS-1$

    static final String PATH_EMBED_HADOOP = "hadoop/lib"; //$NON-NLS-1$

    static final String KEY_VERSION = "asakusafw.version"; //$NON-NLS-1$

    static final String[] VERBOSE_SYSTEM_PROPERTIES = {
            "java.version", //$NON-NLS-1$
            "java.vendor", //$NON-NLS-1$
    };

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName()); //$NON-NLS-1$
        Path home = Optional.ofNullable(System.getenv(ENV_ASAKUSA_HOME))
                .map(Paths::get)
                .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                        Messages.getString("VersionCommand.errorMissingEnvironmentVariable"), //$NON-NLS-1$
                        ENV_ASAKUSA_HOME)));
        if (Files.isDirectory(home) == false) {
            throw new CommandConfigurationException(MessageFormat.format(
                    Messages.getString("VersionCommand.errorMissingFrameworkInstallation"), //$NON-NLS-1$
                    home));
        }
        Path file = home.resolve(PATH_VERSION);
        if (Files.isRegularFile(file) == false) {
            throw new CommandConfigurationException(MessageFormat.format(
                    Messages.getString("VersionCommand.errorBrokenFrameworkInstallation"), //$NON-NLS-1$
                    home,
                    PATH_VERSION));
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new CommandException(MessageFormat.format(
                    Messages.getString("VersionCommand.errorInvalidVersionFile"), //$NON-NLS-1$
                    file), e);
        }
        Map<String, String> pairs = lines.stream()
                .map(String::trim)
                .map(it -> it.split("[=:]", 2))
                .filter(it -> it.length == 2)
                .collect(Collectors.toMap(it -> it[0].trim(), it -> it[1].trim()));

        String version = Optional.ofNullable(pairs.get(KEY_VERSION))
                .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                        "framework installation may be broken (missing \"{1}\"): {0}",
                        home,
                        KEY_VERSION)));

        try (PrintWriter writer = outputParameter.open()) {
            writer.println(version);
            if (verboseParameter.isRequired()) {
                writer.printf("ASAKUSA_HOME: %s%n", home);
                writer.printf("Hadoop: %s%n", findHadoop(home).map(Path::toString).orElse("N/A")); //$NON-NLS-2$
                for (String k : VERBOSE_SYSTEM_PROPERTIES) {
                    writer.printf("%s: %s%n", k, System.getProperty(k, "N/A")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
    }

    private static Optional<Path> findHadoop(Path home) {
        Path embed = home.resolve(PATH_EMBED_HADOOP);
        if (Files.isDirectory(embed)) {
            return Optional.of(embed);
        }
        Optional<Path> hadoopCmd = findPath(ENV_HADOOP_CMD)
                .filter(Files::isExecutable);
        if (hadoopCmd.isPresent()) {
            return hadoopCmd;
        }
        Optional<Path> hadoopHomeCmd = findPath(ENV_HADOOP_HOME)
                .filter(Files::isDirectory)
                .map(it -> it.resolve(PATH_HADOOP_CMD))
                .filter(Files::isRegularFile)
                .filter(Files::isExecutable);
        if (hadoopHomeCmd.isPresent()) {
            return hadoopHomeCmd;
        }
        return Optional.ofNullable(System.getenv(ENV_PATH))
                .map(it -> Arrays.stream(it.split(Pattern.quote(File.pathSeparator))))
                .orElse(Stream.empty())
                .map(String::trim)
                .filter(s -> s.isEmpty() == false)
                .map(Paths::get)
                .filter(Files::isDirectory)
                .map(it -> it.resolve(PATH_HADOOP_CMD_NAME))
                .filter(Files::isRegularFile)
                .filter(Files::isExecutable)
                .findFirst();
    }

    private static Optional<Path> findPath(String key) {
        return Optional.ofNullable(System.getenv(key))
                .map(String::trim)
                .filter(s -> s.isEmpty() == false)
                .map(Paths::get);
    }
}
