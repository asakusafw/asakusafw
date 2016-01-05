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

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;

/**
 * 別のモデルを集約した構造を表現するモデル。
 */
public class SummarizedModelDescription extends ModelDescription {

    private final List<Source> groupBy;

    /**
     * インスタンスを生成する。
     * @param reference 自身への参照
     * @param properties プロパティの一覧
     * @param groupBy グループ化に利用するソースの一覧
     */
    public SummarizedModelDescription(
            ModelReference reference,
            List<ModelProperty> properties,
            List<Source> groupBy) {
        super(reference, properties);
        this.groupBy = Lists.freeze(groupBy);
        Set<String> groupKeys = Sets.create();
        for (Source source : groupBy) {
            groupKeys.add(source.getName());
        }
        for (ModelProperty property : properties) {
            if (property.getJoined() != null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "結合が指定されたプロパティが存在します ({0}.{1})",
                        reference,
                        property.getName()));
            }
            Source source = property.getFrom();
            if (source.getAggregator() == Aggregator.IDENT) {
                if (groupKeys.contains(source.getName()) == false) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "集約関数が指定されていません ({0}.{1})",
                            reference,
                            property.getName()));
                }
            } else if (source.getAggregator() != Aggregator.COUNT) {
                if (groupKeys.contains(source.getName())) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "グループ化プロパティにCOUNT以外の集約関数が指定されています ({0}.{1})",
                            reference,
                            property.getName()));
                }
            }
        }
    }

    /**
     * 集計前のモデルの名前を返す。
     * @return 集計前のモデルの名前
     */
    public ModelReference getOriginalModel() {
        return getProperties().get(0).getFrom().getDeclaring();
    }

    /**
     * グループ化に利用したソースの一覧を返す。
     * @return グループ化に利用したソースの一覧
     */
    public List<Source> getGroupBy() {
        return groupBy;
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
        result += result * prime + groupBy.hashCode();
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
        SummarizedModelDescription other = (SummarizedModelDescription) obj;
        if (getReference().equals(other.getReference()) == false) {
            return false;
        }
        if (getProperties().equals(other.getProperties()) == false) {
            return false;
        }
        if (getGroupBy().equals(other.getGroupBy()) == false) {
            return false;
        }
        return true;
    }
}
