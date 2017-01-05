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
package com.asakusafw.testdriver.excel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.asakusafw.testdriver.rule.DataModelCondition;

/**
 * Kind of predicate about all set of model object.
 * @since 0.2.0
 */
public enum TotalConditionKind {

    /**
     * Accepts iff all data matched.
     */
    STRICT("Strict", //$NON-NLS-1$
            Messages.getString("TotalConditionKind.titleStrict")), //$NON-NLS-1$

    /**
     * Accepts iff only existing expected data matched.
     */
    SKIP_UNEXPECTED("Expect", //$NON-NLS-1$
            Messages.getString("TotalConditionKind.titleExpect"), //$NON-NLS-1$
            DataModelCondition.IGNORE_UNEXPECTED),

    /**
     * Accepts iff only existing actual data matched.
     */
    SKIP_ABSENT("Actual", //$NON-NLS-1$
            Messages.getString("TotalConditionKind.titleActual"), //$NON-NLS-1$
            DataModelCondition.IGNORE_ABSENT),

    /**
     * Accepts iff only existing expected and actual data matched.
     */
    INTERSECT("Intersect", //$NON-NLS-1$
            Messages.getString("TotalConditionKind.titleIntersect"), //$NON-NLS-1$
            DataModelCondition.IGNORE_UNEXPECTED, DataModelCondition.IGNORE_ABSENT),

    /**
     * Always accepts.
     */
    SKIP_ALL("-", //$NON-NLS-1$
            Messages.getString("TotalConditionKind.titleIgnore"), //$NON-NLS-1$
            DataModelCondition.IGNORE_UNEXPECTED, DataModelCondition.IGNORE_ABSENT, DataModelCondition.IGNORE_MATCHED),

    ;
    private final String symbol;

    private final String title;

    private final Set<DataModelCondition> predicates;

    private final String text;

    TotalConditionKind(String symbol, String title, DataModelCondition... conditions) {
        assert symbol != null;
        assert title != null;
        assert conditions != null;
        this.symbol = symbol;
        this.title = title;
        this.predicates = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(conditions)));
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
     * Returns predicates corresponded to this.
     * @return the predicates
     */
    public Set<DataModelCondition> getPredicates() {
        return predicates;
    }

    /**
     * Retutns a constant of this enum from the corresponded textual representation.
     * @param text a textual representation
     * @return the corresponding constant, or {@code null} if does not exist
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TotalConditionKind fromOption(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null"); //$NON-NLS-1$
        }
        String symbol = Util.extractSymbol(text);
        for (TotalConditionKind kind : values()) {
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
        TotalConditionKind[] values = values();
        String[] options = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            options[i] = values[i].text;
        }
        return options;
    }
}
