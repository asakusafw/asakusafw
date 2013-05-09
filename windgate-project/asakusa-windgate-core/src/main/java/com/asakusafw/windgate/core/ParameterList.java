/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.windgate.core;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The parameter list.
 * @since 0.2.2
 */
public class ParameterList {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(ParameterList.class);

    static final Logger LOG = LoggerFactory.getLogger(ParameterList.class);

    private static final Pattern VARIABLE = Pattern.compile("\\$\\{(.*?)\\}");

    private final Map<String, String> parameters;

    /**
     * Creates a new empty instance.
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public ParameterList() {
        this(Collections.<String, String>emptyMap());
    }

    /**
     * Creates a new instance.
     * @param parameters the key value pairs
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public ParameterList(Map<String, String> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null"); //$NON-NLS-1$
        }
        this.parameters = Collections.unmodifiableMap(new TreeMap<String, String>(parameters));
    }


    /**
     * Returns parameters as key value pairs.
     * @return the parameters
     */
    public Map<String, String> getPairs() {
        return parameters;
    }

    /**
     * Replaces parameters in the target string.
     * The parameters are represented as <code>${variable-name}</code>.
     * @param string target string
     * @param strict {@code true} to keep undefined parameters,
     *     or {@code false} to raise an exception
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
            String name = matcher.group(1);
            String replacement = parameters.get(name);
            if (replacement == null) {
                if (strict) {
                    WGLOG.error("E99001",
                            name);
                    throw new IllegalArgumentException(MessageFormat.format(
                            "parameter \"{0}\" is not defined in the list: {1}",
                            name,
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

    @Override
    public String toString() {
        return parameters.toString();
    }
}
