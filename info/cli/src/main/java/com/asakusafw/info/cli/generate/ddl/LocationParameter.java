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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.hive.LocationInfo;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.DynamicParameter;

/**
 * Handles parameters about table locations.
 * @since 0.10.0
 */
public class LocationParameter {

    static final Logger LOG = LoggerFactory.getLogger(LocationParameter.class);

    @DynamicParameter(
            names = { "-L", "--location" },
            description = "Table location mapping (base/path/prefix=hdfs://path/to/prefix)")
    Map<String, String> locations = new LinkedHashMap<>();

    private LocationMapper mapper;

    /**
     * Resolves the logical location information into physical one.
     * @param source the logical location information
     * @return related physical location, or {@code empty} if no mapping rules are registered
     */
    public Optional<String> resolve(LocationInfo source) {
        LOG.debug("resolve {}: {}", source, locations);
        if (mapper == null) {
            if (locations.isEmpty()) {
                return Optional.empty();
            }
            mapper = new LocationMapper(locations);
        }
        String result = mapper.apply(source)
                .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                        "there are no suitable location mapping patterns for {0}: {1}",
                        source,
                        locations)));
        mapper.findInvalidSequence(result).ifPresent(it -> {
            throw new CommandConfigurationException(MessageFormat.format(
                    "mapping result contains invalid sequence \"{2}\": {0} -> {1}",
                    source,
                    result,
                    it));
        });
        return Optional.of(result);
    }

    /**
     * Resolves the candidates of logical locations into physical one.
     * @param sources candidates of logical locations
     * @return resolved physical location
     */
    public Optional<String> resolve(Collection<? extends LocationInfo> sources) {
        List<Optional<String>> candidates = sources.stream()
                .map(this::resolve)
                .distinct()
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return Optional.empty();
        } else if (candidates.size() == 1) {
            return candidates.get(0);
        } else {
            throw new CommandConfigurationException(MessageFormat.format(
                    "ambiguous location of table: {0}",
                    candidates.stream()
                        .map(it -> it.orElse("(N/A)"))
                        .collect(Collectors.joining(", "))));
        }
    }
}
