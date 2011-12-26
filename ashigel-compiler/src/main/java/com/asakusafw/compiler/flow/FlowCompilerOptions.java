/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;

/**
 * Flowコンパイラのオプション設定。
 */
public class FlowCompilerOptions {

    static final Logger LOG = LoggerFactory.getLogger(FlowCompilerOptions.class);

    /**
     * プロパティに指定する際の設定名。
     */
    public static final String K_OPTIONS = "com.asakusafw.compiler.options";

    /**
     * オプションの項目一覧。
     */
    public enum Item {

        /**
         * オプション項目: Combinerを有効にする (Experimental)。
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

    private static final Pattern OPTION = Pattern.compile("\\s*(\\+|-)\\s*([0-9A-Za-z_\\-]+)\\s*");

    private static final Pattern EXTRA_OPTION = Pattern.compile("\\s*X([0-9A-Za-z_\\-]+)\\s*=([^,]*)");

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
        String[] options = properties.getProperty(K_OPTIONS, "").split("\\s*,\\s*");
        FlowCompilerOptions results = new FlowCompilerOptions();
        for (String option : options) {
            if (option.isEmpty()) {
                continue;
            }
            Matcher optionMatcher = OPTION.matcher(option);
            if (optionMatcher.matches()) {
                boolean value = optionMatcher.group(1).equals("+");
                String name = optionMatcher.group(2);
                try {
                    Item item = Item.valueOf(name);
                    item.setTo(results, value);
                } catch (NoSuchElementException e) {
                    LOG.warn("コンパイラオプション\"{}\"を解釈できません", option);
                }
            } else {
                Matcher extraMatcher = EXTRA_OPTION.matcher(option);
                if (extraMatcher.matches()) {
                    String key = extraMatcher.group(1).trim();
                    String value = extraMatcher.group(2).trim();
                    results.extraAttributes.put(key, value);
                } else {
                    LOG.warn("コンパイラオプション\"{}\"を解釈できません", option);
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
     * Returns the extra attribute.
     * @param name attribute name
     * @return related value, or {@code null} if not configured
     */
    public String getExtraAttribute(String name) {
        return this.extraAttributes.get(name);
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
