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

/**
 * {@code FROM ...}の構文木。
 */
public class From {

    /**
     * 元となるテーブルの名前。
     */
    public final Name table;

    /**
     * 元となるテーブルのエイリアス名、未指定の場合は{@code null}。
     */
    public final String alias;

    /**
     * 結合式、未指定の場合は{@code null}。
     */
    public final Join join;

    /**
     * インスタンスを生成する。
     * @param table 元となるテーブルの名前
     * @param alias 元となるテーブルのエイリアス名 (省略可)
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public From(Name table, String alias) {
        this(table, alias, null);
    }

    /**
     * インスタンスを生成する。
     * @param table 元となるテーブルの名前
     * @param alias 元となるテーブルのエイリアス名 (省略可)
     * @param join 結合式 (省略可)
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public From(Name table, String alias, Join join) {
        if (table == null) {
            throw new IllegalArgumentException("table must not be null"); //$NON-NLS-1$
        }
        this.table = table;
        this.alias = alias;
        this.join = join;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + table.hashCode();
        result = prime * result + ((join == null) ? 0 : join.hashCode());
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
        From other = (From) obj;
        if (!table.equals(other.table)) {
            return false;
        }
        if (join == null) {
            if (other.join != null) {
                return false;
            }
        } else if (!join.equals(other.join)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (join == null) {
            return MessageFormat.format(
                    "FROM {0}",
                    table);
        } else {
            return MessageFormat.format(
                    "FROM {0} {1}",
                    table,
                    join);
        }
    }
}
