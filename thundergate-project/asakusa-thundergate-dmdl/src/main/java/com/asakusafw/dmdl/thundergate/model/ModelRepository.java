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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;

/**
 * モデルオブジェクトを保持するリポジトリ。
 */
public class ModelRepository {

    private final LinkedHashMap<ModelReference, ModelDescription> models =
        new LinkedHashMap<ModelReference, ModelDescription>();

    private final Map<String, ModelDescription> simpleNames = Maps.create();

    /**
     * このリポジトリにモデルを追加する。
     * @param model 追加するモデル
     */
    public void add(ModelDescription model) {
        ModelReference ref = model.getReference();
        if (models.containsKey(ref)) {
            throw new IllegalStateException(MessageFormat.format(
                    "モデル \"{0}\" は既に登録されています",
                    ref));
        }
        models.put(ref, model);

        // 単純名での索引は、重複した場合にnullでつぶす
        if (simpleNames.containsKey(ref.getSimpleName())) {
            simpleNames.put(ref.getSimpleName(), null);
        } else {
            simpleNames.put(ref.getSimpleName(), model);
        }
    }

    /**
     * このリポジトリに登録された、指定のモデルを返す。
     * @param reference 対象モデルへの参照
     * @return 発見したモデル、存在しない場合は{@code null}
     */
    public ModelDescription find(ModelReference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null"); //$NON-NLS-1$
        }
        return models.get(reference);
    }

    /**
     * このリポジトリに登録された、指定の単純名のモデルを返す。
     * @param simpleName 対象のモデルの単純名
     * @return 発見したモデル、存在しない場合は{@code null}
     * @throws IllegalStateException 単純名が衝突している場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ModelDescription find(String simpleName) {
        if (simpleName == null) {
            throw new IllegalArgumentException("simpleName must not be null"); //$NON-NLS-1$
        }
        if (simpleNames.containsKey(simpleName) == false) {
            return null;
        }
        ModelDescription model = simpleNames.get(simpleName);
        if (model == null) {
            // 衝突
            List<ModelReference> conflicted = Lists.create();
            for (ModelReference ref : models.keySet()) {
                if (simpleName.equals(ref.getSimpleName())) {
                    conflicted.add(ref);
                }
            }
            throw new IllegalStateException(MessageFormat.format(
                    "単純名{0}はあいまいです: {1}",
                    simpleName,
                    conflicted));
        }
        return model;
    }

    /**
     * このリポジトリに登録されたモデル一覧を返す。
     * @return このリポジトリに登録されたモデル一覧
     */
    public List<ModelDescription> all() {
        return Lists.from(models.values());
    }

    /**
     * このリポジトリに登録されたモデルのうち、テーブルにマッピングされるもののみを返す。
     * @return このリポジトリに登録されたモデルのうち、テーブルにマッピングされるものの一覧
     */
    public List<TableModelDescription> allTables() {
        List<TableModelDescription> results = Lists.create();
        for (ModelDescription model : models.values()) {
            if (model instanceof TableModelDescription) {
                results.add((TableModelDescription) model);
            }
        }
        return results;
    }
}
