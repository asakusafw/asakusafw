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
package com.asakusafw.compiler.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Patterns about class names.
 * @since 0.8.0
 */
public class ClassNamePattern {

    static final Logger LOG = LoggerFactory.getLogger(ClassNamePattern.class);

    private final Pattern[] patterns;

    private final boolean defaultAccepts;

    /**
     * Creates a new instance.
     * @param patterns the recognizable patterns
     * @param defaultAccepts {@code true} to accept if {@code patterns} is empty, or {@code false} to reject
     */
    public ClassNamePattern(List<? extends Pattern> patterns, boolean defaultAccepts) {
        this.patterns = patterns.toArray(new Pattern[patterns.size()]);
        this.defaultAccepts = defaultAccepts;
    }

    /**
     * Parses the pattern list and returns the corresponded class name pattern.
     * @param patternList the class name pattern list ({@code "*"} as a wildcard, nullable)
     * @param defaultAccepts {@code true} to accept if the pattern list is empty, or {@code false} to reject
     * @return the parsed pattern
     */
    public static ClassNamePattern parse(String patternList, boolean defaultAccepts) {
        List<Pattern> patterns = parseSegments(patternList);
        return new ClassNamePattern(patterns, defaultAccepts);
    }

    private static List<Pattern> parseSegments(String patternList) {
        if (patternList == null) {
            return Collections.emptyList();
        }
        List<Pattern> results = new ArrayList<>();
        int start = 0;
        while (true) {
            int index = patternList.indexOf(',', start);
            if (index < 0) {
                String segment = patternList.substring(start).trim();
                if (segment.isEmpty() == false) {
                    results.add(parseSegment(segment));
                }
                break;
            } else {
                String segment = patternList.substring(start, index);
                if (segment.isEmpty() == false) {
                    results.add(parseSegment(segment));
                }
                start = index + 1;
            }
        }
        return results;
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
        return Pattern.compile(buf.toString());
    }

    /**
     * Returns whether this accepts the target class name or not.
     * @param name the target class name
     * @return {@code true} if this accepts the target class name, otherwise {@code false}
     */
    public boolean accepts(String name) {
        if (patterns.length == 0) {
            return defaultAccepts;
        }
        for (Pattern pattern : patterns) {
            if (pattern.matcher(name).matches()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("matched: {} ({})", name, pattern); //$NON-NLS-1$
                }
                return true;
            }
        }
        return false;
    }
}
