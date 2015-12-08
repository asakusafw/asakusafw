/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses strings which may contain variables.
 * <p>
 * This can parse a string that contain variable in form of <code>${variable-name}</code>, and substitute them with
 * defined variables by using {@link #defineVariable(String, String)}, etc.
 * </p>
 */
public class VariableTable {

    private static final Pattern VARIABLE = Pattern.compile("\\$\\{(.*?)\\}"); //$NON-NLS-1$

    private final RedefineStrategy redefineStrategy;

    private final Map<String, String> variables = new HashMap<>();

    /**
     * Creates a new empty instance.
     * This instance does not allow redefine any variables, or raises exceptions in such cases.
     */
    public VariableTable() {
        this(RedefineStrategy.ERROR);
    }

    /**
     * Creates a new empty instance.
     * @param redefineStrategy a strategy for redefining variables
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public VariableTable(RedefineStrategy redefineStrategy) {
        if (redefineStrategy == null) {
            throw new IllegalArgumentException("redefineStrategy must not be null"); //$NON-NLS-1$
        }
        this.redefineStrategy = redefineStrategy;
    }

    /**
     * Returns the variable expression for this parser.
     * @param name the variable name
     * @return the variable expression for the specified name
     * @throws IllegalArgumentException if the variable name is not valid
     */
    public static String toVariable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        String expr = "${" + name + "}";
        if (VARIABLE.matcher(expr).matches() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}\" is not a valid variable name",
                    name));
        }
        return expr;
    }

    /**
     * Adds a new variable into this.
     * @param name the variable name
     * @param replacement the replacement
     * @throws IllegalArgumentException if this rejects the target variable, or some parameters are {@code null}
     */
    public void defineVariable(String name, String replacement) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (replacement == null) {
            throw new IllegalArgumentException("replacement must not be null"); //$NON-NLS-1$
        }
        if (redefineStrategy != RedefineStrategy.OVERWRITE && variables.containsKey(name)) {
            if (redefineStrategy == RedefineStrategy.ERROR) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "variable \"{0}\" is already defined in the variable table: {1}",
                        name,
                        this));
            }
        } else {
            this.variables.put(name, replacement);
        }
    }

    /**
     * Adds new variables into this.
     * @param variableMap the variable map ({@code name -> value})
     * @throws IllegalArgumentException if this rejects some variables, or some parameters are {@code null}
     * @since 0.2.2
     */
    public void defineVariables(Map<String, String> variableMap) {
        if (variableMap == null) {
            throw new IllegalArgumentException("variableMap must not be null"); //$NON-NLS-1$
        }
        for (Map.Entry<String, String> entry : variableMap.entrySet()) {
            defineVariable(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Serializes this variable table.
     * Clients can restore variables using {@link #defineVariables(String)}.
     * @return the serialized value
     * @see #defineVariables(String)
     */
    public String toSerialString() {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            buf.append(escape(entry.getKey()));
            buf.append("="); //$NON-NLS-1$
            buf.append(escape(entry.getValue()));
            buf.append(","); //$NON-NLS-1$
        }
        if (buf.length() >= 1) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    private static final Pattern TO_ESCAPED = Pattern.compile("[=,\\\\]"); //$NON-NLS-1$
    private String escape(String string) {
        assert string != null;
        return TO_ESCAPED.matcher(string).replaceAll("\\\\$0"); //$NON-NLS-1$
    }

    private static final Pattern PAIRS = Pattern.compile("(?<!\\\\),"); //$NON-NLS-1$
    private static final Pattern KEY_VALUE = Pattern.compile("(?<!\\\\)="); //$NON-NLS-1$

    /**
     * Adds variables from the serialized variable table.
     * The serialized string must be a form as like following:
<pre><code>
VariableList:
    VariableList "," Variable
    Variable
    ","
    (Empty)

Variable:
    Value_key "=" Value_value

Value:
    Character*

Character:
    any character except ",", "=", "\\"
    "\" any character
</code></pre>
     * @param variableList the serialized variable table
     * @throws IllegalArgumentException if this rejects some variables, or some parameters are {@code null}
     */
    public void defineVariables(String variableList) {
        if (variableList == null) {
            throw new IllegalArgumentException("variableList must not be null"); //$NON-NLS-1$
        }
        String[] pairs = PAIRS.split(variableList);
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            String[] kv = KEY_VALUE.split(pair);
            if (kv.length == 0) {
                // "=" returns an empty array
                defineVariable("", ""); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (kv.length == 1 && kv[0].equals(pair) == false) {
                // "key=" returns only its key
                defineVariable(unescape(kv[0]), ""); //$NON-NLS-1$
            } else if (kv.length == 2) {
                defineVariable(unescape(kv[0]), unescape(kv[1]));
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid variable record \"{0}\", in \"{1}\"",
                        pair,
                        variableList));
            }
        }
    }

    private String unescape(String string) {
        assert string != null;
        StringBuilder buf = new StringBuilder();
        int start = 0;
        while (true) {
            int index = string.indexOf('\\', start);
            if (index < 0) {
                break;
            }
            buf.append(string.substring(start, index));
            if (index != string.length() - 1) {
                buf.append(string.charAt(index + 1));
                start = index + 2;
            } else {
                buf.append(string.charAt(index));
                start = index + 1;
            }
        }
        if (start < string.length()) {
            buf.append(string.substring(start));
        }
        return buf.toString();
    }

    /**
     * Returns the variables names and their replacements in this table.
     * @return the variable map
     */
    public Map<String, String> getVariables() {
        return new TreeMap<>(variables);
    }

    /**
     * Substitutes variables in the string and returns the substituted one.
     * @param string the target string
     * @return the substituted string
     * @throws IllegalArgumentException if the string contains some undefined variables, or it is {@code null}
     * @see #defineVariable(String, String)
     */
    public String parse(String string) {
        return parse(string, true);
    }

    /**
     * Substitutes variables in the string and returns the substituted one.
     * @param string the target string
     * @param strict {@code true} to raise error if the string contain undefined variables, or {@code false} to
     *     leave such variables
     * @return the substituted string
     * @throws IllegalArgumentException if the string contains some undefined variables (only if strict mode),
     *     or it is {@code null}
     * @see #defineVariable(String, String)
     */
    public String parse(String string, boolean strict) {
        if (string == null) {
            throw new IllegalArgumentException("string must not be null"); //$NON-NLS-1$
        }
        StringBuilder buf = new StringBuilder();
        int start = 0;
        Matcher matcher = VARIABLE.matcher(string);
        while (matcher.find(start)) {
            String name = matcher.group(1);
            String replacement = variables.get(name);
            if (replacement == null) {
                if (strict) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "variable \"{0}\" is not defined in the variable table: {1}",
                            name,
                            this));
                } else {
                    buf.append(string.substring(start, matcher.start()));
                    buf.append(matcher.group(0));
                }
            } else {
                buf.append(string.substring(start, matcher.start()));
                buf.append(replacement);
            }
            start = matcher.end();
        }
        buf.append(string.substring(start));
        return buf.toString();
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}{1}", //$NON-NLS-1$
                getClass().getSimpleName(),
                variables.toString());
    }

    /**
     * Represents strategies for redefining variables.
     */
    public enum RedefineStrategy {

        /**
         * Overwrites by the last one.
         */
        OVERWRITE,

        /**
         * Ignores except the first one.
         */
        IGNORE,

        /**
         * Raises errors.
         */
        ERROR,
    }
}
