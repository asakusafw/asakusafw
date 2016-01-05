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

import com.asakusafw.modelgen.model.Aggregator;


/**
 * 射影の個々の要素。
 */
public class Select {

    /**
     * 要素の名前。
     */
    public final Name name;

    /**
     * 集約関数。
     */
    public final Aggregator aggregator;

    /**
     * エイリアス名。
     */
    public final Name alias;

    /**
     * インスタンスを生成する。
     * @param name 要素の名前
     * @param aggregator 集約関数
     * @param alias 別名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Select(Name name, Aggregator aggregator, Name alias) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (aggregator == null) {
            throw new IllegalArgumentException("aggregator must not be null"); //$NON-NLS-1$
        }
        if (alias == null) {
            throw new IllegalArgumentException("alias must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.aggregator = aggregator;
        this.alias = alias;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + aggregator.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + alias.hashCode();
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
        Select other = (Select) obj;
        if (aggregator != other.aggregator) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        if (!alias.equals(other.alias)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (aggregator == Aggregator.IDENT) {
            return MessageFormat.format(
                    "{0} AS {1}",
                    name,
                    alias);
        } else {
            return MessageFormat.format(
                    "{1}({0}) AS {2}",
                    name,
                    aggregator,
                    alias);
        }
    }
}
