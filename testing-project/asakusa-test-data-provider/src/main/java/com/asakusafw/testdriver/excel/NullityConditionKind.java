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
package com.asakusafw.testdriver.excel;

/**
 * Kind of nullity predicate represented in each cell.
 * @since 0.2.0
 */
public enum NullityConditionKind {

    /**
     * Accepts if .
     */
    NORMAL("-", Messages.getString("NullityConditionKind.titleNormal")), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Accepts if actual value is null.
     */
    ACCEPT_ABSENT("AA", Messages.getString("NullityConditionKind.titleAcceptAbsent")), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Deny if actual value is null.
     */
    DENY_ABSENT("DA", Messages.getString("NullityConditionKind.titleDenyAbsent")), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Accepts if actual value is not null.
     */
    ACCEPT_PRESENT("AP", Messages.getString("NullityConditionKind.titleAcceptPresent")), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Deny if actual value is not null.
     */
    DENY_PRESENT("DP", Messages.getString("NullityConditionKind.titleDenyPresent")), //$NON-NLS-1$ //$NON-NLS-2$
    ;

    private final String symbol;

    private final String title;

    private final String text;

    private NullityConditionKind(String symbol, String title) {
        assert symbol != null;
        assert title != null;
        this.symbol = symbol;
        this.title = title;
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
    public static NullityConditionKind fromOption(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null"); //$NON-NLS-1$
        }
        String symbol = Util.extractSymbol(text);
        for (NullityConditionKind kind : values()) {
            if (kind.symbol.equalsIgnoreCase(symbol)) {
                return kind;
            }
        }
        return null;
    }

    /**
     * Returns options as text.
     * @return the option
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static String[] getOptions() {
        NullityConditionKind[] values = values();
        String[] options = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            options[i] = values[i].text;
        }
        return options;
    }
}
