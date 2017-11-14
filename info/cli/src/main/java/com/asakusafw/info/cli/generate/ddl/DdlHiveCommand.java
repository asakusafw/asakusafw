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
package com.asakusafw.info.cli.generate.ddl;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.hive.LocationInfo;
import com.asakusafw.info.hive.TableInfo;
import com.asakusafw.info.hive.syntax.HiveCreateTable;
import com.asakusafw.info.hive.syntax.HiveQlEmitter;
import com.asakusafw.info.hive.syntax.SimpleCreateTable;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing Hive DDL.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "hive",
        commandDescriptionKey = "command.generate-ddl-hive",
        resourceBundle = "com.asakusafw.info.cli.jcommander"
)
public class DdlHiveCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(DdlHiveCommand.class);

    static final String STATEMENT_SEPARATOR = ";"; //$NON-NLS-1$

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @ParametersDelegate
    final HiveTableParameter hiveTableParameter = new HiveTableParameter();

    @ParametersDelegate
    final LocationParameter locationParameter = new LocationParameter();

    @Parameter(
            names = { "--database" },
            descriptionKey = "parameter.database",
            required = false)
    String databaseParameter = null;

    @Parameter(
            names = { "--external" },
            descriptionKey = "parameter.external",
            required = false)
    boolean externalParameter = false;

    @Parameter(
            names = { "--if-not-exists" },
            descriptionKey = "parameter.if-not-exists",
            required = false)
    boolean ifNotExistsParameter = false;

    @Parameter(
            names = { "--on-error" },
            descriptionKey = "parameter.on-error",
            required = false)
    ErrorAction errorAction = ErrorAction.FAIL;

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());
        List<HiveCreateTable> statements = getStatements();
        if (statements.isEmpty()) {
            throw new CommandConfigurationException("there are no available tables to generate DDL");
        }
        try (PrintWriter writer = outputParameter.open()) {
            for (HiveCreateTable statement : statements) {
                HiveQlEmitter.emit(statement, writer);
                writer.printf("%s%n%n", STATEMENT_SEPARATOR);
            }
        } catch (IOException e) {
            throw new CommandException("error occurred while generating Hive DDL", e);
        }
    }

    private List<HiveCreateTable> getStatements() {
        List<HiveCreateTable> candidates = hiveTableParameter.collect().entrySet().stream()
                .map(e -> toStatement(e.getKey(), e.getValue()))
                .flatMap(it -> it.map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());

        List<HiveCreateTable> results = validate(candidates);
        return results;
    }

    private Optional<HiveCreateTable> toStatement(TableInfo table, Set<LocationInfo> locations) {
        String location;
        try {
            location = locationParameter.resolve(locations).orElse(null);
        } catch (CommandConfigurationException ex) {
            switch (errorAction) {
            case SKIP:
                LOG.warn("cannot resolve location of table {} (skip generate DDL)",
                        table.getName(), ex);
                return Optional.empty();
            case FORCE:
                LOG.warn("cannot resolve location of table {} (disable table location)",
                        table.getName(), ex);
                location = null;
                break;
            case FAIL:
                throw ex;
            default:
                throw new AssertionError(ex);
            }
        }
        return Optional.of(new SimpleCreateTable(table)
                .withDatabaseName(databaseParameter)
                .withExternal(externalParameter)
                .withSkipPresentTable(ifNotExistsParameter)
                .withLocation(location));
    }

    private List<HiveCreateTable> validate(List<HiveCreateTable> candidates) {
        Map<String, Long> occurrences = candidates.stream().collect(Collectors.groupingBy(
                it -> it.getTableInfo().getName(),
                Collectors.counting()));

        List<HiveCreateTable> results = new ArrayList<>();
        Set<String> saw = new HashSet<>();
        for (HiveCreateTable candidate : candidates) {
            String name = candidate.getTableInfo().getName();
            long occurrence = occurrences.getOrDefault(name, 0L);
            assert occurrence > 0;
            if (occurrence == 1) {
                results.add(candidate);
            } else {
                switch (errorAction) {
                case SKIP:
                    LOG.warn("table \"{}\" is ambiguous (skip this table)", name);
                    break;
                case FORCE:
                    if (saw.contains(name) == false) {
                        saw.add(name);
                        LOG.warn("table \"{}\" is ambiguous (force generate statemets)", name);
                    }
                    results.add(candidate);
                    break;
                case FAIL:
                    throw new CommandConfigurationException(MessageFormat.format(
                            "table \"{0}\" is ambiguous: {1}",
                            candidates.stream()
                                    .filter(it -> it.getTableInfo().getName().equals(name))
                                    .collect(Collectors.toList())));
                default:
                    throw new AssertionError(errorAction);
                }
            }
        }
        return results;
    }

    enum ErrorAction {

        SKIP,

        FORCE,

        FAIL,
    }
}
