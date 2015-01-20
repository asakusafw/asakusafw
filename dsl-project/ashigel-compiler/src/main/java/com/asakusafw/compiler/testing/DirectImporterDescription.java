/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.compiler.testing;

import java.util.Collections;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Sets;

/**
 * Direct access API for {@link TemporaryInputDescription}.
 * @since 0.2.5
 */
public class DirectImporterDescription extends TemporaryInputDescription {

    private final Class<?> modelType;

    private final Set<String> paths;

    private DataSize dataSize;

    /**
     * インスタンスを生成する。
     * @param modelType インポートするモデルのデータ型
     * @param paths インポート対象のパス一覧 (相対パス)
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public DirectImporterDescription(Class<?> modelType, Set<String> paths) {
        Precondition.checkMustNotBeNull(modelType, "modelType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(paths, "paths"); //$NON-NLS-1$
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("paths must not be empty");
        }
        this.modelType = modelType;
        this.paths = Sets.freeze(paths);
    }

    /**
     * インスタンスを生成する。
     * @param modelType インポートするモデルのデータ型
     * @param path インポート対象のパス (相対パス)
     * @param pathRest インポート対象のパス一覧 (相対パス)
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public DirectImporterDescription(Class<?> modelType, String path, String... pathRest) {
        Precondition.checkMustNotBeNull(modelType, "modelType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(path, "path"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(pathRest, "pathRest"); //$NON-NLS-1$
        this.modelType = modelType;
        Set<String> pathSet = Sets.create();
        pathSet.add(path);
        Collections.addAll(pathSet, pathRest);
        this.paths = Collections.unmodifiableSet(pathSet);
    }

    @Override
    public Class<?> getModelType() {
        return modelType;
    }

    @Override
    public Set<String> getPaths() {
        return paths;
    }

    /**
     * データサイズのヒントを設定する。
     * @param dataSize データサイズのヒント、不明の場合は{@code null}
     */
    public void setDataSize(DataSize dataSize) {
        this.dataSize = dataSize;
    }

    @Override
    public DataSize getDataSize() {
        if (dataSize == null) {
            return DataSize.UNKNOWN;
        }
        return dataSize;
    }
}
