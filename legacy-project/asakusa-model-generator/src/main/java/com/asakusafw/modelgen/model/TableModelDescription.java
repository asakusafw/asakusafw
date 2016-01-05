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
package com.asakusafw.modelgen.model;

import java.text.MessageFormat;
import java.util.List;

/**
 * テーブルの構造を表現するモデル。
 */
public class TableModelDescription extends ModelDescription {

    /**
     * インスタンスを生成する。
     * @param reference 自身への参照
     * @param properties プロパティの一覧
     */
    public TableModelDescription(
            ModelReference reference,
            List<ModelProperty> properties) {
        super(reference, properties);
        for (ModelProperty property : properties) {
            if (property.getJoined() != null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "結合が指定されたプロパティが存在します ({0}.{1})",
                        reference,
                        property.getName()));
            }
            Source source = property.getFrom();
            if (source.getAggregator() != Aggregator.IDENT) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "集約関数が指定されています ({0}.{1})",
                        reference,
                        property.getName()));
            }
        }
    }

    @Override
    protected Source convertPropertyToSource(ModelProperty property) {
        Source source = property.getFrom();
        return new Source(
                Aggregator.IDENT,
                getReference(),
                property.getName(),
                source.getType(),
                source.getAttributes());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result += result * prime + getReference().hashCode();
        result += result * prime + getProperties().hashCode();
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
        TableModelDescription other = (TableModelDescription) obj;
        if (getReference().equals(other.getReference()) == false) {
            return false;
        }
        if (getProperties().equals(other.getProperties()) == false) {
            return false;
        }
        return true;
    }
}
