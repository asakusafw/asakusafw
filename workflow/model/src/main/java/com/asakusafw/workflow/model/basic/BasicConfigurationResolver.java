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
package com.asakusafw.workflow.model.basic;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.workflow.model.CommandTaskInfo;
import com.asakusafw.workflow.model.CommandToken;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A basic implementation of configuration resolver for {@link CommandTaskInfo}.
 * @since 0.10.0
 */
public class BasicConfigurationResolver implements CommandTaskInfo.ConfigurationResolver {

    static final Pattern PATTERN_VARIABLE = Pattern.compile("\\$\\{(.*?)\\}"); //$NON-NLS-1$

    static final String VARIABLE_KEY = "key";

    static final String VARIABLE_VALUE = "value";

    @JsonProperty("tokens")
    private final List<String> tokens;

    /**
     * Creates a new instance.
     * @param tokens the template tokens for each key-value pair
     */
    public BasicConfigurationResolver(List<String> tokens) {
        this.tokens = Collections.unmodifiableList(new ArrayList<>(tokens));
    }

    /**
     * Creates a new instance.
     * @param tokens the template tokens for each key-value pair
     */
    public BasicConfigurationResolver(String... tokens) {
        this(Arrays.asList(tokens));
    }

    @JsonCreator
    static BasicConfigurationResolver restore(@JsonProperty("tokens") List<String> tokens) {
        return new BasicConfigurationResolver(tokens);
    }

    /**
     * Returns the template tokens.
     * @return the template tokens
     */
    public List<String> getTokens() {
        return tokens;
    }

    @Override
    public List<CommandToken> apply(Map<String, String> configurations) {
        List<CommandToken> results = new ArrayList<>();
        configurations.forEach((key, value) -> tokens.forEach(template -> {
            results.add(CommandToken.of(expand(template, key, value)));
        }));
        return results;
    }

    static String expand(String template, String key, String value) {
        Matcher matcher = PATTERN_VARIABLE.matcher(template);
        int start = 0;
        StringBuilder buf = new StringBuilder();
        while (matcher.find(start)) {
            buf.append(template, start, matcher.start());
            switch (matcher.group(1)) {
            case VARIABLE_KEY:
                buf.append(key);
                break;
            case VARIABLE_VALUE:
                buf.append(value);
                break;
            default:
                throw new IllegalArgumentException(MessageFormat.format(
                        "unknown variable \"{1}\": {0}",
                        template,
                        matcher.group()));
            }
            start = matcher.end();
        }
        buf.append(template, start, template.length());
        return buf.toString();
    }
}
