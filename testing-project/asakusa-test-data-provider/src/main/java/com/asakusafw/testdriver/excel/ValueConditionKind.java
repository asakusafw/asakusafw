/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver.excel;

/**
 * Kind of value predicate represented in each cell.
 * @since 0.2.0
 * @version 0.7.0
 */
public enum ValueConditionKind {

    /**
     * Always accepts.
     */
    ANY("-", "検査対象外", "すべて"),

    /**
     * Used as comparing key.
     */
    KEY("Key", "検査キー", "すべて"),

    /**
     * Accepts if matched.
     */
    EQUAL("=", "完全一致", "すべて"),

    /**
     * Accepts if expected data appears in the actual data.
     */
    CONTAIN("<=", "部分一致", "文字列"),

    /**
     * Accepts if actual date/time is between test started date and its finished date.
     */
    TODAY("Today", "現在日付", "日付または時刻"),

    /**
     * Accepts if actual date/time is between test started time and its finished time.
     */
    NOW("Now", "現在時刻", "日付または時刻"),

    /**
     * Accepts if the user defined rule recognize the actual data.
     * @since 0.7.0
     */
    EXPRESSION("Expr", "特殊ルール", "特殊"),
    ;

    private final String symbol;

    private final String title;

    private final String expectedType;

    private final String text;

    private ValueConditionKind(String symbol, String title, String expectedType) {
        assert symbol != null;
        assert title != null;
        assert expectedType != null;
        this.symbol = symbol;
        this.title = title;
        this.expectedType = expectedType;
        this.text = Util.buildText(symbol, title);
    }

    /**
     * Returns a title of this kind.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns a textual representation of this kind.
     * @return a textual representation
     */
    public String getText() {
        return text;
    }

    /**
     * Retutns a constant of this enum from the corresponded textual representation.
     * @param text a textual representation
     * @return the corresponding constant, or {@code null} if does not exist
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ValueConditionKind fromOption(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null"); //$NON-NLS-1$
        }
        String symbol = Util.extractSymbol(text);
        for (ValueConditionKind kind : values()) {
            if (kind.symbol.equalsIgnoreCase(symbol)) {
                return kind;
            }
        }
        return null;
    }

    /**
     * Returns a description of expected type of this predicate.
     * @return the description
     */
    public String getExpectedType() {
        return expectedType;
    }

    /**
     * Returns options as text.
     * @return the option
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static String[] getOptions() {
        ValueConditionKind[] values = values();
        String[] options = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            options[i] = values[i].text;
        }
        return options;
    }
}
