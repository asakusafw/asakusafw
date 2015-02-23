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

/**
 * Flowコンパイラのオプション設定。
 * @since 0.1.0
 * @version 0.7.3
 */
public class FlowCompilerOptions {

    static final Logger LOG = LoggerFactory.getLogger(FlowCompilerOptions.class);

    /**
     * プロパティに指定する際の設定名。
     */
    public static final String K_OPTIONS = "com.asakusafw.compiler.options"; //$NON-NLS-1$

    /**
     * The key prefix for extra options.
     */
    public static final String PREFIX_EXTRA_OPTION = "X"; //$NON-NLS-1$

    /**
     * オプションの項目一覧。
     */
    public enum Item {

        /**
         * オプション項目: Combinerを有効にする。
         * <p>
         * デフォルトでは無効 (false)。
         * </p>
         */
        enableCombiner(false) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setEnableCombiner(value);
            }
        },

        /**
         * オプション項目: フロー部品のインライン化の際に、可能な限りステージ数が少なくなるようにインライン化する。
         * <p>
         * デフォルトではステージが少なくなるように修正する (true)。
         * </p>
         */
        compressFlowPart(true) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setCompressFlowPart(value);
            }
        },

        /**
         * オプション項目: 互いに影響のないステージを合成する。
         * <p>
         * デフォルトではステージを合成する (true)。
         * </p>
         */
        compressConcurrentStage(true) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setCompressConcurrentStage(value);
            }
        },

        /**
         * オプション項目: TINY指定のデータをハッシュ表で結合する。
         * <p>
         * デフォルトではハッシュ表で結合する (true)。
         * </p>
         */
        hashJoinForTiny(true) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setHashJoinForTiny(value);
            }
        },

        /**
         * オプション項目: SMALL指定のデータをハッシュ表で結合する。
         * <p>
         * デフォルトではマージで結合する (false)。
         * </p>
         */
        hashJoinForSmall(false) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setHashJoinForSmall(value);
            }
        },

        /**
         * オプション項目: デバッグロギングを有効にする。
         * <p>
         * デフォルトでは無効 (false)。
         * </p>
         */
        enableDebugLogging(false) {
            @Override public void setTo(FlowCompilerOptions options, boolean value) {
                options.setEnableDebugLogging(value);
            }
        },
        ;

        /**
         * この項目の既定値。
         */
        public final boolean defaultValue;

        private Item(boolean defaultValue) {
            this.defaultValue = defaultValue;
        }

        /**
         * オプション一覧のこの項目に対して値を設定する。
         * @param options 対象のオプション一覧
         * @param value 設定する値
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

        private GenericOptionValue(String symbols) {
            assert symbols != null;
            String first = null;
            this.symbols = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
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

    private final Map<String, String> extraAttributes = new ConcurrentHashMap<String, String>();

    /**
     * デフォルトの設定でインスタンスを生成する。
     */
    public FlowCompilerOptions() {
        for (Item item : Item.values()) {
            item.setTo(this, item.defaultValue);
        }
    }

    private static final Pattern OPTION = Pattern.compile("\\s*(\\+|-)\\s*([0-9A-Za-z_\\-]+)\\s*"); //$NON-NLS-1$

    private static final Pattern EXTRA_OPTION = Pattern.compile("\\s*X([0-9A-Za-z_\\-]+)\\s*=([^,]*)"); //$NON-NLS-1$

    /**
     * デフォルトの設定をプロパティからロードする。
     * <p>
     * 利用するプロパティのキーは現在のところ{@link #K_OPTIONS}のみで、
     * 以下のような{@code OptionList}の形式で指定すること。
     * </p>
<pre><code>
OptionList:
    OptionList "," OptionItem
    OptionItem
    (Empty)

OptionItem:
    "+" OptionName           ; OptionNameの項目をtrueに設定する
    "-" OptionName           ; OptionNameの項目をfalseに設定する
    "X" OptionName "=" Value ; Name, Valueのペアを extra attributes に設定する

OptionName:
    (項目名)
</code></pre>
     * <p>
     * また、利用可能なオプション名は{@link FlowCompilerOptions.Item}
     * に定義される列挙定数の名前に等しい。
     * また、{@code D<key>=<value>}から始まる項目名は{@code X}を取り除いた上で
     *
     * </p>
     * @param properties プロパティ一覧
     * @return オプション設定
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
                            "コンパイラオプション\"{0}\"を解釈できません",
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
                            "コンパイラオプション\"{0}\"を解釈できません",
                            option));
                }
            }
        }
        return results;
    }

    /**
     * Combinerを有効にする。
     * @return 設定値
     */
    public boolean isEnableCombiner() {
        return enableCombiner;
    }

    /**
     * Combinerを有効にする。
     * @param enable 設定値
     */
    public void setEnableCombiner(boolean enable) {
        this.enableCombiner = enable;
    }

    /**
     * フロー部品のインライン化の際に、可能な限りステージ数が少なくなるようにインライン化する。
     * @return 設定値
     */
    public boolean isCompressFlowPart() {
        return compressFlowPart;
    }

    /**
     * フロー部品のインライン化の際に、可能な限りステージ数が少なくなるようにインライン化する。
     * @param enable 設定値
     */
    public void setCompressFlowPart(boolean enable) {
        this.compressFlowPart = enable;
    }

    /**
     * 互いに影響のないステージを合成する。
     * @return 設定値
     */
    public boolean isCompressConcurrentStage() {
        return compressConcurrentStage;
    }

    /**
     * 互いに影響のないステージを合成する。
     * @param enable 設定値
     */
    public void setCompressConcurrentStage(boolean enable) {
        this.compressConcurrentStage = enable;
    }

    /**
     * VERY_SMALL指定のデータをハッシュ表で結合する。
     * @return 設定値
     */
    public boolean isHashJoinForTiny() {
        return hashJoinForTiny;
    }

    /**
     * VERY_SMALL指定のデータをハッシュ表で結合する。
     * @param enable 設定値
     */
    public void setHashJoinForTiny(boolean enable) {
        this.hashJoinForTiny = enable;
    }

    /**
     * SMALL指定のデータをハッシュ表で結合する。
     * @return 設定値
     */
    public boolean isHashJoinForSmall() {
        return hashJoinForSmall;
    }

    /**
     * SMALL指定のデータをハッシュ表で結合する。
     * @param enable 設定値
     */
    public void setHashJoinForSmall(boolean enable) {
        this.hashJoinForSmall = enable;
    }

    /**
     * デバッグロギングを利用する。
     * @return 設定値
     */
    public boolean isEnableDebugLogging() {
        return this.enableDebugLogging;
    }

    /**
     * デバッグロギングを利用する。
     * @param enable 設定値
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
