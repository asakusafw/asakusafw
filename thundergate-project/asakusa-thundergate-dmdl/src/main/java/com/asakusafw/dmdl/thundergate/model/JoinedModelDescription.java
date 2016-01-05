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
import java.util.Collections;
import java.util.List;

import com.asakusafw.utils.collections.Lists;

/**
 * 別のモデルを結合した構造を表現するモデル。
 */
public class JoinedModelDescription extends ModelDescription {

    private List<Source> leftCondition;

    private List<Source> rightCondition;

    /**
     * インスタンスを生成する。
     * @param reference 自身への参照
     * @param properties 結合結果のプロパティ一覧
     * @param leftCondition 左側の結合条件に利用するソースの一覧
     * @param rightCondition 右側の結合条件に利用するソースの一覧
     */
    public JoinedModelDescription(
            ModelReference reference,
            List<ModelProperty> properties,
            List<Source> leftCondition,
            List<Source> rightCondition) {
        super(reference, properties);
        if (leftCondition.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "結合条件が指定されていません ({0})",
                    reference));
        }
        if (leftCondition.size() != rightCondition.size()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "結合条件が左右で異なります ({0})",
                    reference));
        }
        this.leftCondition = Lists.freeze(leftCondition);
        this.rightCondition = Lists.freeze(rightCondition);
    }

    @Override
    protected Source convertPropertyToSource(ModelProperty property) {
        return new Source(
                Aggregator.IDENT,
                getReference(),
                property.getName(),
                property.getType(),
                Collections.<Attribute>emptySet());
    }

    /**
     * 左側のモデル(FROM _)の情報を返す。
     * @return 左側のモデル名
     */
    public ModelReference getFromModel() {
        return leftCondition.get(0).getDeclaring();
    }

    /**
     * 右側のモデル(JOIN _)の情報を返す。
     * @return 右側のモデル名
     */
    public ModelReference getJoinModel() {
        return rightCondition.get(0).getDeclaring();
    }

    /**
     * 左側の結合条件に利用するソースの一覧を返す。
     * @return 左側の結合条件に利用するソースの一覧
     */
    public List<Source> getFromCondition() {
        return leftCondition;
    }

    /**
     * 右側の結合条件に利用するソースの一覧を返す。
     * @return 右側の結合条件に利用するソースの一覧
     */
    public List<Source> getJoinCondition() {
        return rightCondition;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result += result * prime + getReference().hashCode();
        result += result * prime + getProperties().hashCode();
        result += result * prime + leftCondition.hashCode();
        result += result * prime + rightCondition.hashCode();
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
        JoinedModelDescription other = (JoinedModelDescription) obj;
        if (getReference().equals(other.getReference()) == false) {
            return false;
        }
        if (getProperties().equals(other.getProperties()) == false) {
            return false;
        }
        if (leftCondition.equals(other.leftCondition) == false) {
            return false;
        }
        if (rightCondition.equals(other.rightCondition) == false) {
            return false;
        }
        return true;
    }
}
