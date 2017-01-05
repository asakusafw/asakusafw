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
package com.asakusafw.compiler.flow;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.flow.graph.Inline;
import com.asakusafw.vocabulary.flow.processor.PartialAggregation;
import com.asakusafw.vocabulary.operator.Logging;

/**
 * Options for flow DSL compiler.
 * @since 0.1.0
 * @version 0.7.3
 */
public class FlowCompilerOptions {

    static final Logger LOG = LoggerFactory.getLogger(FlowCompilerOptions.class);

    /**
     * The system property key of serialized compiler options.
     * The serialized compiler options is the form of the following rule:
<pre><code>
OptionList:
    OptionList "," Option
    Option
Option:
    Key "=" Value
    (Empty String)
Key:
    EscapedCharacter+
Value:
    EscapedCharacter*
EscapedString:
    (character excludes "=", "," and "\")
    "\="
    "\,"
    "\\"
</code></pre>
     * Note that, the {@code Key} must be one of {@link Item} or start with {@link #PREFIX_EXTRA_OPTION}.
     */
    public static final String K_OPTIONS = "com.asakusafw.compiler.options"; //$NON-NLS-1$

    /**
     * The key prefix for extra options.
     */
    public static final String PREFIX_EXTRA_OPTION = "X"; //$NON-NLS-1$

    /**
     * Represents a kind of compiler option item.
     */
    public enum Item {

        /**
         * The default value of whether combine operation is enabled or not.
         * Default value: {@code false}.
         * @see PartialAggregation#DEFAULT
         */
        enableCombiner(false) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setEnableCombiner(value);
            }
        },

        /**
         * The default value of whether in-lining flow-part is enabled or not.
         * Default value: {@code true}.
         * @see Inline#DEFAULT
         */
        compressFlowPart(true) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setCompressFlowPart(value);
            }
        },

        /**
         * The default value of whether compressing concurrent stages is enabled or not.
         * Default value: {@code true}.
         */
        compressConcurrentStage(true) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setCompressConcurrentStage(value);
            }
        },

        /**
         * The default value of whether hash-join for tiny inputs is enabled or not.
         * Default value: {@code true}.
         */
        hashJoinForTiny(true) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setHashJoinForTiny(value);
            }
        },

        /**
         * The default value of whether hash-join for small inputs is enabled or not.
         * Default value: {@code false}.
         */
        hashJoinForSmall(false) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setHashJoinForSmall(value);
            }
        },

        /**
         * The default value of whether debug level logging is enabled or not.
         * Default value: {@code false}.
         * @see Logging#value()
         */
        enableDebugLogging(false) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setEnableDebugLogging(value);
            }
        },
        ;

        /**
         * The default value.
         */
        public final boolean defaultValue;

        Item(boolean defaultValue) {
            this.defaultValue = defaultValue;
        }

        /**
         * Sets this option item into the target options.
         * @param options the target options
         * @param value the option value
         * @throws IllegalArgumentException if any parameter is {@code null}
         */
        public abstract void setTo(FlowCompilerOptions options, boolean value);
    }

    /**
     * A common value for extra options.
     * @since 0.2.5
     */
    public enum GenericOptionValue {

        /**
         * The option is enabled.
         */
        ENABLED("enabled,enable,t,true,y,yes,on"), //$NON-NLS-1$

        /**
         * The option is disabled.
         */
        DISABLED("disabled,disable,f,false,n,no,off"), //$NON-NLS-1$

        /**
         * The option should be auto detected.
         */
        AUTO("auto"), //$NON-NLS-1$

        /**
         * The option which is invalid.
         */
        INVALID("invalid"), //$NON-NLS-1$
        ;

        private final String primary;

        private final Set<String> symbols;

        GenericOptionValue(String symbols) {
            assert symbols != null;
            String first = null;
            this.symbols = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            for (String s : symbols.split(",")) { //$NON-NLS-1$
                String token = s.trim();
                if (token.isEmpty() == false) {
                    if (first == null) {
                        first = token;
                    }
                    this.symbols.add(token);
                }
            }
            if (first == null) {
                throw new IllegalArgumentException();
            }
            this.primary = first;
            Collections.addAll(this.symbols, symbols);
        }

        /**
         * Returns the symbol of the value.
         * @return the symbol
         */
        public String getSymbol() {
            return primary;
        }

        /**
         * Returns a value corresponding to the symbol.
         * @param symbol target symbol
         * @return corresponded value, or {@link FlowCompilerOptions.GenericOptionValue#INVALID} if does not exist
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public static GenericOptionValue fromSymbol(String symbol) {
            if (symbol == null) {
                throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
            }
            for (GenericOptionValue value : values()) {
                if (value.symbols.contains(symbol)) {
                    return value;
                }
            }
            return INVALID;
        }
    }

    private volatile boolean enableCombiner;

    private volatile boolean compressFlowPart;

    private volatile boolean compressConcurrentStage;

    private volatile boolean hashJoinForTiny;

    private volatile boolean hashJoinForSmall;

    private volatile boolean enableDebugLogging;

    private final Map<String, String> extraAttributes = new ConcurrentHashMap<>();

    /**
     * Creates a new instance with default option values.
     */
    public FlowCompilerOptions() {
        for (Item item : Item.values()) {
            item.setTo(this, item.defaultValue);
        }
    }

    private static final Pattern OPTION = Pattern.compile("\\s*(\\+|-)\\s*([0-9A-Za-z_\\-]+)\\s*"); //$NON-NLS-1$

    private static final Pattern EXTRA_OPTION = Pattern.compile("\\s*X([0-9A-Za-z_\\-]+)\\s*=([^,]*)"); //$NON-NLS-1$

    /**
     * Creates a new instance from the properties object.
     * @param properties the target properties object
     * @return the created instance
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static FlowCompilerOptions load(Properties properties) {
        Precondition.checkMustNotBeNull(properties, "properties"); //$NON-NLS-1$
        String[] options = properties.getProperty(K_OPTIONS, "").split("\\s*,\\s*"); //$NON-NLS-1$ //$NON-NLS-2$
        FlowCompilerOptions results = new FlowCompilerOptions();
        for (String option : options) {
            if (option.isEmpty()) {
                continue;
            }
            Matcher optionMatcher = OPTION.matcher(option);
            if (optionMatcher.matches()) {
                boolean value = optionMatcher.group(1).equals("+"); //$NON-NLS-1$
                String name = optionMatcher.group(2);
                try {
                    Item item = Item.valueOf(name);
                    item.setTo(results, value);
                } catch (NoSuchElementException e) {
                    LOG.warn(MessageFormat.format(
                            Messages.getString("FlowCompilerOptions.warnUnknownOption"), //$NON-NLS-1$
                            option));
                }
            } else {
                Matcher extraMatcher = EXTRA_OPTION.matcher(option);
                if (extraMatcher.matches()) {
                    String key = extraMatcher.group(1).trim();
                    String value = extraMatcher.group(2).trim();
                    results.extraAttributes.put(key, value);
                } else {
                    LOG.warn(MessageFormat.format(
                            Messages.getString("FlowCompilerOptions.warnMalformedOption"), //$NON-NLS-1$
                            option));
                }
            }
        }
        return results;
    }

    /**
     * Returns the option value of {@link Item#enableCombiner}.
     * @return the option value
     */
    public boolean isEnableCombiner() {
        return enableCombiner;
    }

    /**
     * Sets the option value of {@link Item#enableCombiner}.
     * @param enable the option value
     */
    public void setEnableCombiner(boolean enable) {
        this.enableCombiner = enable;
    }

    /**
     * Returns the option value of {@link Item#compressFlowPart}.
     * @return the option value
     */
    public boolean isCompressFlowPart() {
        return compressFlowPart;
    }

    /**
     * Sets the option value of {@link Item#compressFlowPart}.
     * @param enable the option value
     */
    public void setCompressFlowPart(boolean enable) {
        this.compressFlowPart = enable;
    }

    /**
     * Returns the option value of {@link Item#compressConcurrentStage}.
     * @return the option value
     */
    public boolean isCompressConcurrentStage() {
        return compressConcurrentStage;
    }

    /**
     * Sets the option value of {@link Item#compressConcurrentStage}.
     * @param enable the option value
     */
    public void setCompressConcurrentStage(boolean enable) {
        this.compressConcurrentStage = enable;
    }

    /**
     * Returns the option value of {@link Item#hashJoinForTiny}.
     * @return the option value
     */
    public boolean isHashJoinForTiny() {
        return hashJoinForTiny;
    }

    /**
     * Sets the option value of {@link Item#hashJoinForTiny}.
     * @param enable the option value
     */
    public void setHashJoinForTiny(boolean enable) {
        this.hashJoinForTiny = enable;
    }

    /**
     * Returns the option value of {@link Item#hashJoinForSmall}.
     * @return the option value
     */
    public boolean isHashJoinForSmall() {
        return hashJoinForSmall;
    }

    /**
     * Sets the option value of {@link Item#hashJoinForSmall}.
     * @param enable the option value
     */
    public void setHashJoinForSmall(boolean enable) {
        this.hashJoinForSmall = enable;
    }

    /**
     * Returns the option value of {@link Item#enableDebugLogging}.
     * @return the option value
     */
    public boolean isEnableDebugLogging() {
        return this.enableDebugLogging;
    }

    /**
     * Sets the option value of {@link Item#enableDebugLogging}.
     * @param enable the option value
     */
    public void setEnableDebugLogging(boolean enable) {
        this.enableDebugLogging = enable;
    }

    /**
     * Returns the extra option key name for the option name.
     * @param optionName the original option name
     * @return the key name for the option
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public String getExtraAttributeKeyName(String optionName) {
        if (optionName == null) {
            throw new IllegalArgumentException("optionName must not be null"); //$NON-NLS-1$
        }
        return PREFIX_EXTRA_OPTION + optionName;
    }

    /**
     * Returns the extra attribute.
     * @param name attribute name
     * @return related value, or {@code null} if not configured
     */
    public String getExtraAttribute(String name) {
        return this.extraAttributes.get(name);
    }

    /**
     * Returns the extra attribute.
     * @param name attribute name
     * @param defaultValue the default value
     * @return related value, or the default value if the attribute is not set
     * @since 0.7.1
     */
    public String getExtraAttribute(String name, String defaultValue) {
        String value = this.extraAttributes.get(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Returns the extra attribute.
     * @param name attribute name
     * @param defaultValue the default value
     * @return related value, or {@code null} if not configured
     */
    public GenericOptionValue getGenericExtraAttribute(String name, GenericOptionValue defaultValue) {
        String value = this.extraAttributes.get(name);
        if (value == null) {
            return defaultValue;
        }
        GenericOptionValue symbol = GenericOptionValue.fromSymbol(value);
        return symbol;
    }

    /**
     * Configures the extra attribute.
     * @param name attribute name
     * @param value attribute value (nullable)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void putExtraAttribute(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (value == null) {
            this.extraAttributes.remove(name);
        } else {
            this.extraAttributes.put(name, value);
        }
    }
}
