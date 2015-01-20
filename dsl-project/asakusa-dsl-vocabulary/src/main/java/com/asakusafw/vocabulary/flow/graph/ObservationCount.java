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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;

/**
 * 要素に対して個々のデータを観測してよい回数に関する制約。
 */
public enum ObservationCount implements FlowElementAttribute {

    /**
     * どのように計測してもよい。
     */
    DONT_CARE(false, false) {
        @Override
        public ObservationCount and(ObservationCount other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
            }
            return other;
        }
    },

    /**
     * 個々のデータは高々1度のみ観測してよい。
     */
    AT_MOST_ONCE(true, false) {
        @Override
        public ObservationCount and(ObservationCount other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
            }
            if (other.atLeastOnce) {
                return EXACTLY_ONCE;
            }
            return AT_MOST_ONCE;
        }
    },

    /**
     * 個々のデータは最低1度は観測する必要がある。
     */
    AT_LEAST_ONCE(false, true) {
        @Override
        public ObservationCount and(ObservationCount other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
            }
            if (other.atMostOnce) {
                return EXACTLY_ONCE;
            }
            return AT_LEAST_ONCE;
        }
    },

    /**
     * 個々のデータはデータ数と同じ回数だけ観測する必要がある。
     */
    EXACTLY_ONCE(true, true) {
        @Override
        public ObservationCount and(ObservationCount other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
            }
            return EXACTLY_ONCE;
        }
    },

    ;
    /**
     * 高々一度のみ観測してよい。
     */
    public final boolean atMostOnce;

    /**
     * 最低一度は観測する必要がある。
     */
    public final boolean atLeastOnce;

    private ObservationCount(boolean atMostOnce, boolean atLeastOnce) {
        this.atMostOnce = atMostOnce;
        this.atLeastOnce = atLeastOnce;
    }

    /**
     * この定数に指定の制約を結合したものを返す。
     * @param other 結合する制約
     * @return 結合結果
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public abstract ObservationCount and(ObservationCount other);

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}.{1}",
                getDeclaringClass().getSimpleName(),
                name());
    }
}
