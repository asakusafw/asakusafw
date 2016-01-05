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
package com.asakusafw.dmdl.thundergate.model;

import java.util.Set;

import com.asakusafw.utils.collections.Sets;

/**
 * プロパティの元になった情報。
 */
public class Source {

    private Aggregator aggregator;

    private ModelReference declaring;

    private String name;

    private PropertyType type;

    private Set<Attribute> attributes;

    /**
     * インスタンスを生成する。
     * @param aggregator 元のプロパティに適用された集約関数、
     *     存在しない場合は {@link Aggregator#IDENT}
     * @param declaring 元のプロパティを有するモデルへの参照
     * @param name 元のプロパティの名前
     * @param type 元のプロパティの型
     * @param attributes 元のプロパティの属性一覧
     */
    public Source(
            Aggregator aggregator,
            ModelReference declaring,
            String name,
            PropertyType type,
            Set<Attribute> attributes) {
        this.aggregator = aggregator;
        this.declaring = declaring;
        this.name = name;
        this.type = type;
        this.attributes = Sets.freeze(attributes);
    }

    /**
     * 元のプロパティに適用された集約関数を返す。
     * @return 元のプロパティに適用された集約関数、存在しない場合は {@link Aggregator#IDENT}
     */
    public Aggregator getAggregator() {
        return aggregator;
    }

    /**
     * 元のプロパティを有するモデル名を返す。
     * @return 元のプロパティを有するモデル名
     */
    public ModelReference getDeclaring() {
        return declaring;
    }

    /**
     * 元のプロパティの名前を返す。
     * @return 元のプロパティの名前
     */
    public String getName() {
        return name;
    }

    /**
     * 元のプロパティの型を返す。
     * @return 元のプロパティの型
     */
    public PropertyType getType() {
        return type;
    }

    /**
     * 元のプロパティの属性一覧を返す。
     * @return 元のプロパティの属性一覧、存在しない場合は空のセット
     */
    public Set<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + aggregator.hashCode();
        result = prime * result + attributes.hashCode();
        result = prime * result + declaring.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + type.hashCode();
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
        Source other = (Source) obj;
        if (aggregator != other.aggregator) {
            return false;
        }
        if (attributes.equals(other.attributes) == false) {
            return false;
        }
        if (declaring.equals(other.declaring) == false) {
            return false;
        }
        if (name.equals(other.name) == false) {
            return false;
        }
        if (type.equals(other.type) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Source [declaring=");
        builder.append(declaring);
        builder.append(", aggregator=");
        builder.append(aggregator);
        builder.append(", name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type);
        builder.append(", attributes=");
        builder.append(attributes);
        builder.append("]");
        return builder.toString();
    }
}
