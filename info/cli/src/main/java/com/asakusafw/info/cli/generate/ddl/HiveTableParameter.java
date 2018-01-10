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
package com.asakusafw.info.cli.generate.ddl;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.cli.generate.ddl.HiveIoParameter.TableLocationInfo;
import com.asakusafw.info.hive.LocationInfo;
import com.asakusafw.info.hive.TableInfo;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * Provides Direct I/O Hive table information.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.info.cli.jcommander")
public class HiveTableParameter {

    static final Logger LOG = LoggerFactory.getLogger(HiveTableParameter.class);

    /**
     * The batch information.
     */
    @ParametersDelegate
    public final HiveIoParameter hiveIoParameter = new HiveIoParameter();

    @Parameter(
            names = { "--table" },
            descriptionKey = "parameter.table",
            required = false)
    String tableNamePatternString = "*";

    private Pattern tableNamePattern;

    @Parameter(
            names = { "--direction" },
            descriptionKey = "parameter.direction",
            required = false)
    Direction direction = Direction.AUTO;

    /**
     * Returns the table name pattern string.
     * @return the table name pattern string
     */
    public String getTableNamePatternString() {
        return tableNamePatternString;
    }

    /**
     * Returns the target table name pattern.
     * @return the target table name pattern
     */
    public Pattern getTableNamePattern() {
        if (tableNamePattern == null) {
            LOG.debug("resolving table name pattern: {}", tableNamePatternString);
            this.tableNamePattern = parseSegment(tableNamePatternString);
        }
        return tableNamePattern;
    }

    private static Pattern parseSegment(String pattern) {
        StringBuilder buf = new StringBuilder();
        int start = 0;
        while (true) {
            int next = pattern.indexOf('*', start);
            if (next < 0) {
                break;
            }
            if (start < next) {
                buf.append(Pattern.quote(pattern.substring(start, next)));
            }
            buf.append(".*"); //$NON-NLS-1$
            start = next + 1;
        }
        if (start < pattern.length()) {
            buf.append(Pattern.quote(pattern.substring(start)));
        }
        try {
            return Pattern.compile(buf.toString());
        } catch (PatternSyntaxException e) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "cannot recognize pattern: {0}",
                    pattern), e);
        }
    }

    /**
     * Returns the target tables and their locations.
     * @return target tables and locations
     */
    public Map<TableInfo, Set<LocationInfo>> collect() {
        Map<TableInfo, Set<LocationInfo>> candidates = collect0(direction)
                .filter(it -> getTableNamePattern().matcher(it.getTable().getName()).matches())
                .collect(Collectors.groupingBy(
                        TableLocationInfo::getTable,
                        Collectors.mapping(TableLocationInfo::getLocation, Collectors.toSet())));
        if (candidates.isEmpty()) {
            if (collect0(direction).findAny().isPresent() == false) {
                throw new CommandConfigurationException("there are no available tables for pattern");
            } else {
                throw new CommandConfigurationException(MessageFormat.format(
                        "there are no available tables for pattern: {0}",
                        tableNamePatternString));
            }
        }
        return candidates;
    }

    Stream<TableLocationInfo> collect0(Direction d) {
        switch (d) {
        case INPUT:
            return hiveIoParameter.getInputs().stream();
        case OUTPUT:
            return hiveIoParameter.getOutputs().stream();
        case AUTO:
            return Stream.concat(
                    hiveIoParameter.getInputs().stream(),
                    hiveIoParameter.getOutputs().stream());
        default:
            throw new AssertionError(direction);
        }
    }

    /**
     * Represents I/O port direction.
     * @since 0.10.0
     */
    public enum Direction {

        /**
         * Select from input ports.
         */
        INPUT,

        /**
         * Select from output ports.
         */
        OUTPUT,

        /**
         * Select from input and output ports.
         */
        AUTO,
    }
}
