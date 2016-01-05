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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.modelgen.model.Aggregator;


/**
 * {@code CREATE VIEW ...}。
 */
public class CreateView {

    /**
     * 作成するビューの名前。
     */
    public final Name name;

    /**
     * 射影する要素の一覧。
     */
    public final List<Select> selectList;

    /**
     * 対象のテーブル情報。
     */
    public final From from;

    /**
     * グループ化カラムの一覧。
     */
    public final List<Name> groupBy;

    /**
     * インスタンスを生成する。
     *
     * @param name
     *            作成するビューの名前
     * @param selectList
     *            射影する要素の一覧
     * @param from
     *            対象のテーブル情報
     * @param groupBy
     *            グループ化カラムの一覧
     * @throws IllegalArgumentException
     *             引数に{@code null}が指定された場合
     */
    public CreateView(
            Name name,
            List<Select> selectList,
            From from,
            List<Name> groupBy) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (selectList == null) {
            throw new IllegalArgumentException("selectList must not be null"); //$NON-NLS-1$
        }
        if (from == null) {
            throw new IllegalArgumentException("from must not be null"); //$NON-NLS-1$
        }
        if (groupBy == null) {
            throw new IllegalArgumentException("groupBy must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.selectList = Collections.unmodifiableList(new ArrayList<Select>(
                selectList));
        this.from = from;
        this.groupBy = Collections
                .unmodifiableList(new ArrayList<Name>(groupBy));
    }

    /**
     * このビューの種類を返す。
     *
     * @return このビューの種類
     */
    public CreateView.Kind getKind() {
        boolean join = (from.join != null);
        boolean summarize = (groupBy.isEmpty() == false);
        if (summarize == false) {
            for (Select select : selectList) {
                if (select.aggregator != Aggregator.IDENT) {
                    summarize = true;
                    break;
                }
            }
        }
        if (join && summarize == false) {
            return Kind.JOINED;
        }
        if (summarize && join == false) {
            return Kind.SUMMARIZED;
        }
        return Kind.UNKNOWN;
    }

    /**
     * このビューが参照する別のモデルの名前を返す。
     *
     * @return このビューが参照する別のモデルの名前
     */
    public Set<Name> getDependencies() {
        Set<Name> results = new HashSet<Name>();
        results.add(from.table);
        if (from.join != null) {
            results.add(from.join.table);
        }
        return results;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + from.hashCode();
        result = prime * result + groupBy.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + selectList.hashCode();
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
        CreateView other = (CreateView) obj;
        if (!from.equals(other.from)) {
            return false;
        }
        if (!groupBy.equals(other.groupBy)) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        if (!selectList.equals(other.selectList)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (groupBy.isEmpty()) {
            return MessageFormat.format(
                    "CREATE VIEW {0} SELECT {1} {2}",
                    name,
                    selectList,
                    from);
        } else {
            return MessageFormat.format(
                    "CREATE VIEW {0} SELECT {1} {2} GROUP BY {3}",
                    name,
                    selectList,
                    from,
                    groupBy);
        }
    }

    /**
     * {@link CreateView}の種類。
     */
    public enum Kind {

        /**
         * 結合モデルを構築するビュー。
         */
        JOINED,

        /**
         * 集計モデルを構築するビュー。
         */
        SUMMARIZED,

        /**
         * 不明なビュー。
         */
        UNKNOWN,
    }
}
