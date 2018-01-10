/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
    ANY("-", Messages.getString("ValueConditionKind.titleAny"), //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("ValueConditionKind.typeAny")), //$NON-NLS-1$

    /**
     * Used as comparing key.
     */
    KEY("Key", Messages.getString("ValueConditionKind.titleKey"), //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("ValueConditionKind.typeKey")), //$NON-NLS-1$

    /**
     * Accepts if matched.
     */
    EQUAL("=", Messages.getString("ValueConditionKind.titleEqual"), //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("ValueConditionKind.typeEqual")), //$NON-NLS-1$

    /**
     * Accepts if expected data appears in the actual data.
     */
    CONTAIN("<=", Messages.getString("ValueConditionKind.titleContain"), //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("ValueConditionKind.typeContain")), //$NON-NLS-1$

    /**
     * Accepts if actual date/time is between the test started date and its finished date.
     */
    TODAY("Today", Messages.getString("ValueConditionKind.titleToday"), //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("ValueConditionKind.typeToday")), //$NON-NLS-1$

    /**
     * Accepts if actual date/time is between the test started time and its finished time.
     */
    NOW("Now", Messages.getString("ValueConditionKind.titleNow"), //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("ValueConditionKind.typeNow")), //$NON-NLS-1$

    /**
     * Accepts if the user defined rule recognize the actual data.
     * @since 0.7.0
     */
    EXPRESSION("Expr", Messages.getString("ValueConditionKind.titleExpression"), //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("ValueConditionKind.typeExpression")), //$NON-NLS-1$
    ;

    private final String symbol;

    private final String title;

    private final String expectedType;

    private final String text;

    ValueConditionKind(String symbol, String title, String expectedType) {
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
     * Returns a constant of this enum from the corresponded textual representation.
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
