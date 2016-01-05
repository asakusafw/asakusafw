/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.modelgen.view.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code JOIN ... ON ...}の構文木。
 */
public class Join {

    /**
     * 結合するテーブルの名前。
     */
    public final Name table;

    /**
     * 結合するテーブルのエイリアス名、未指定の場合は{@code null}。
     */
    public final String alias;

    /**
     * 結合条件の等価条件一覧。
     */
    public final List<On> condition;

    /**
     * インスタンスを生成する。
     * @param table 結合するテーブルの名前
     * @param alias 結合するテーブルのエイリアス名 (省略可)
     * @param condition 結合条件の等価条件一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Join(Name table, String alias, List<On> condition) {
        if (table == null) {
            throw new IllegalArgumentException("table must not be null"); //$NON-NLS-1$
        }
        if (condition == null) {
            throw new IllegalArgumentException("condition must not be null"); //$NON-NLS-1$
        }
        this.table = table;
        this.alias = alias;
        this.condition = Collections.unmodifiableList(new ArrayList<On>(condition));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + condition.hashCode();
        result = prime * result + table.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Join other = (Join) obj;
        if (!table.equals(other.table)) {
            return false;
        }
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        if (!condition.equals(other.condition)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (alias != null) {
            return MessageFormat.format(
                    "JOIN {0} {2} ON {1}",
                    table,
                    condition,
                    alias);
        } else {
            return MessageFormat.format(
                    "JOIN {0} ON {1}",
                    table,
                    condition);
        }
    }
}
