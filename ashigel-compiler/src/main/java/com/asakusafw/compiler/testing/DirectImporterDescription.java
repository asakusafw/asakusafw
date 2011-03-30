/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.external.FileImporterDescription;

/**
 * {@link FileImporterDescription}のパラメーターを直接指定して生成する。
 */
public class DirectImporterDescription extends FileImporterDescription {

    private final Class<?> modelType;

    @SuppressWarnings("rawtypes")
    private final Class<? extends FileInputFormat> format;

    private final Set<String> paths;

    private DataSize dataSize;

    /**
     * インスタンスを生成する。
     * @param modelType インポートするモデルのデータ型
     * @param paths インポート対象のパス一覧 (相対パス)
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public DirectImporterDescription(Class<?> modelType, Set<String> paths) {
        this(modelType, SequenceFileInputFormat.class, paths);
    }

    /**
     * インスタンスを生成する。
     * @param modelType インポートするモデルのデータ型
     * @param path インポート対象のパス (相対パス)
     * @param pathRest インポート対象のパス一覧 (相対パス)
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public DirectImporterDescription(Class<?> modelType, String path, String... pathRest) {
        this(modelType, SequenceFileInputFormat.class, path, pathRest);
    }

    /**
     * インスタンスを生成する。
     * @param modelType インポートするモデルのデータ型
     * @param format 入力形式のフォーマット
     * @param paths インポート対象のパス一覧 (相対パス)
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public DirectImporterDescription(
            Class<?> modelType,
            @SuppressWarnings("rawtypes") Class<? extends FileInputFormat> format,
            Set<String> paths) {
        Precondition.checkMustNotBeNull(modelType, "modelType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(format, "format"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(paths, "paths"); //$NON-NLS-1$
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("paths must not be empty");
        }
        this.modelType = modelType;
        this.format = format;
        this.paths = Collections.unmodifiableSet(new HashSet<String>(paths));
    }

    /**
     * インスタンスを生成する。
     * @param modelType インポートするモデルのデータ型
     * @param format 入力形式のフォーマット
     * @param path インポート対象のパス (相対パス)
     * @param pathRest インポート対象のパス一覧 (相対パス)
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public DirectImporterDescription(
            Class<?> modelType,
            @SuppressWarnings("rawtypes") Class<? extends FileInputFormat> format,
            String path,
            String... pathRest) {
        Precondition.checkMustNotBeNull(modelType, "modelType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(format, "format"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(path, "path"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(pathRest, "pathRest"); //$NON-NLS-1$
        this.modelType = modelType;
        this.format = format;
        Set<String> pathSet = new HashSet<String>();
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

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends FileInputFormat> getInputFormat() {
        return format;
    }
}
