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

import java.util.List;

import com.asakusafw.utils.collections.Lists;

/**
 * モデルの構造。
 */
public abstract class ModelDescription {

    private ModelReference reference;

    private List<ModelProperty> properties;

    /**
     * インスタンスを生成する。
     * @param reference 自身への参照
     * @param properties プロパティの一覧
     */
    public ModelDescription(
            ModelReference reference,
            List<ModelProperty> properties) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null"); //$NON-NLS-1$
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        this.reference = reference;
        this.properties = Lists.freeze(properties);
    }

    /**
     * このモデルへの参照を返す。
     * @return このモデルへの参照
     */
    public ModelReference getReference() {
        return reference;
    }

    /**
     * このモデルが有するプロパティの一覧を返す。
     * @return プロパティの一覧
     */
    public List<ModelProperty> getProperties() {
        return properties;
    }

    /**
     * このモデルが有するプロパティの一覧を、ソースの一覧として返す。
     * @return このモデルが有するプロパティの一覧
     */
    public List<Source> getPropertiesAsSources() {
        List<Source> results = Lists.create();
        for (ModelProperty property : getProperties()) {
            Source source = convertPropertyToSource(property);
            results.add(source);
        }
        return results;
    }

    /**
     * このモデルが有するプロパティを、ソースとして再表現して返す。
     * @param property 対象のプロパティ
     * @return 対応するソース
     */
    protected abstract Source convertPropertyToSource(ModelProperty property);
}
