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
package com.asakusafw.info.cli.generate.ddl;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.hive.LocationInfo;

/**
 * Maps {@link LocationInfo} to Hadoop file location.
 * @since 0.10.0
 */
public class LocationMapper implements Function<LocationInfo, Optional<String>> {

    static final Logger LOG = LoggerFactory.getLogger(LocationMapper.class);

    static final Pattern PATTERN_META_CHARACTER = Pattern.compile("[\\*\\?\\[\\]\\{\\}\\\\\\$]");

    private static final char SEGMENT_SEPARATOR = '/';

    private static final Comparator<Element> GREEDY = Comparator
            .comparingInt((Element e) -> e.prefix.size())
            .reversed();

    private final Element[] elements;

    /**
     * Creates a new instance.
     * @param prefixMap the base path prefix into Hadoop file location
     */
    public LocationMapper(Map<String, String> prefixMap) {
        prefixMap.forEach((k, v) -> {
            findInvalidSequence(v).ifPresent(it -> {
                throw new IllegalArgumentException(MessageFormat.format(
                        "substitution must not contain meta-character \"{2}\": \"{0}\" -> \"{1}\"",
                        k, v, it));
            });
        });
        elements = prefixMap.entrySet().stream()
                .map(entry -> new Element(entry.getKey(), entry.getValue()))
                .sorted(GREEDY)
                .toArray(Element[]::new);
    }

    @Override
    public Optional<String> apply(LocationInfo location) {
        LOG.debug("finding location mapping: {}", location);
        for (Element element : elements) {
            LOG.debug("test location mapping: {}", element);
            Optional<String> result = element.apply(location);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    /**
     * Returns invalid sequence in the location.
     * @param location the location
     * @return invalid sequence in the location, or {@code empty} if it is valid
     */
    public Optional<String> findInvalidSequence(String location) {
        Matcher matcher = PATTERN_META_CHARACTER.matcher(location);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        } else {
            return Optional.empty();
        }
    }

    private static final class Element implements Function<LocationInfo, Optional<String>> {

        final List<String> prefix;

        final String target;

        Element(String prefix, String target) {
            this.prefix = split(prefix);
            this.target = trimTrailingSegmentSeparators(target);
        }

        static String trimTrailingSegmentSeparators(String path) {
            int end = path.length();
            while (end > 0) {
                if (path.charAt(end - 1) != SEGMENT_SEPARATOR) {
                    break;
                }
                end--;
            }
            if (end == path.length()) {
                return path;
            }
            return path.substring(0, end);
        }

        private static List<String> split(String s) {
            return Arrays.stream(s.split(String.valueOf(SEGMENT_SEPARATOR)))
                    .map(String::trim)
                    .filter(it -> it.isEmpty() == false)
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<String> apply(LocationInfo info) {
            List<String> path = split(info.getBasePath());
            if (path.size() < prefix.size() || prefix.equals(path.subList(0, prefix.size())) == false) {
                return Optional.empty();
            } else {
                return Optional.of(Stream.concat(
                        Stream.of(target),
                        path.subList(prefix.size(), path.size()).stream())
                        .collect(Collectors.joining(String.valueOf(SEGMENT_SEPARATOR))));
            }
        }

        @Override
        public String toString() {
            return String.format("\"%s\" -> \"%s\"", prefix, target);
        }
    }
}
