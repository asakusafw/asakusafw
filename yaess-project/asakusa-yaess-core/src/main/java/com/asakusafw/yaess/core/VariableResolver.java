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
package com.asakusafw.yaess.core;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves variables in form of <code>${variable_name}</code>.
 * @since 0.2.2
 * @version 0.7.4
 */
public class VariableResolver {

    static final Logger LOG = LoggerFactory.getLogger(VariableResolver.class);

    private static final Pattern VARIABLE = Pattern.compile("\\$\\{(.*?)\\}");

    private static final char SEPARATOR_DEFAULT_VALUE = '-';

    private final Map<String, String> entries;

    /**
     * Creates a new instance.
     * @param entries the variable key and its value pairs
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public VariableResolver(Map<String, String> entries) {
        if (entries == null) {
            throw new IllegalArgumentException("entries must not be null"); //$NON-NLS-1$
        }
        this.entries = Collections.unmodifiableMap(new TreeMap<>(entries));
    }

    /**
     * Creates a new instance from environment variables and system properties.
     * If both have same entry, the system properties takes precedence over the environment variables.
     * @return the created instance
     */
    public static VariableResolver system() {
        Map<String, String> entries = new HashMap<>();
        entries.putAll(System.getenv());
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key instanceof String && value instanceof String) {
                entries.put((String) key, (String) value);
            }
        }
        return new VariableResolver(entries);
    }

    /**
     * Replaces parameters in the target string.
     * The parameters are represented as <code>${variable-name}</code>.
     * @param string target string
     * @param strict {@code false} to keep undefined parameters,
     *     or {@code true} to raise an exception
     * @return replaced string
     * @throws IllegalArgumentException if undefined parameters exist on strict mode,
     *     or any parameters contain {@code null}
     */
    public String replace(String string, boolean strict) {
        if (string == null) {
            throw new IllegalArgumentException("string must not be null"); //$NON-NLS-1$
        }
        StringBuilder buf = new StringBuilder();
        int start = 0;
        Matcher matcher = VARIABLE.matcher(string);
        while (matcher.find(start)) {
            String placeholder = matcher.group(1);
            String replacement = resolve(placeholder);
            if (replacement == null) {
                if (strict) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "parameter \"{0}\" is not defined in the list: {1}",
                            placeholder,
                            this));
                } else {
                    buf.append(string.substring(start, matcher.start() + 1));
                }
                start = matcher.start() + 1;
            } else {
                buf.append(string.substring(start, matcher.start()));
                buf.append(replacement);
                start = matcher.end();
            }
        }
        buf.append(string.substring(start));
        return buf.toString();
    }

    private String resolve(String placeholder) {
        String name;
        String defaultValue;
        int defaultAt = placeholder.indexOf(SEPARATOR_DEFAULT_VALUE);
        if (defaultAt < 0) {
            name = placeholder;
            defaultValue = null;
        } else {
            name = placeholder.substring(0, defaultAt);
            defaultValue = placeholder.substring(defaultAt + 1);
        }
        String replacement = entries.get(name);
        if (replacement != null) {
            return replacement;
        }
        return defaultValue;
    }


    @Override
    public String toString() {
        return entries.toString();
    }
}
