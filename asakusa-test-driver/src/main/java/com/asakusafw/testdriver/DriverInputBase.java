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
package com.asakusafw.testdriver;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * テストドライバのテスト入力データの親クラス。
 * @since 0.2.0
 *
 * @param <T> モデルクラス
 */
public abstract class DriverInputBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DriverInputBase.class);

    /** データ名。 */
    protected String name;

    /** モデル型。 */
    protected Class<T> modelType;

    /** テストドライバコンテキスト。 */
    protected TestDriverContext driverContext;

    /** ソース。 */
    protected DataModelSourceFactory source;

    /** インポータ記述。 */
    protected ImporterDescription importerDescription;

    /**
     * Returns the name of this port.
     * @return the name
     */
    protected String getName() {
        return name;
    }

    /**
     * Sets the name of this port.
     * @param name the name
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the data type of this port.
     * @return the data type
     */
    protected Class<T> getModelType() {
        return modelType;
    }

    /**
     * Sets the data type of this port.
     * @param modelType the data type
     */
    protected void setModelType(Class<T> modelType) {
        this.modelType = modelType;
    }

    /**
     * Returns the current context.
     * @return the context
     */
    protected TestDriverContext getDriverContext() {
        return driverContext;
    }

    /**
     * Sets the current context.
     * @param driverContext the context
     */
    protected void setDriverContext(TestDriverContext driverContext) {
        this.driverContext = driverContext;
    }

    /**
     * Returns the importer description for this input.
     * @return the description, or {@code null} if not set
     */
    protected ImporterDescription getImporterDescription() {
        return importerDescription;
    }

    /**
     * Sets the importer description for this input.
     * @param importerDescription the description
     */
    protected void setImporterDescription(ImporterDescription importerDescription) {
        this.importerDescription = importerDescription;
    }

    /**
     * Returns the source for jobflow input.
     * @return the source, or {@code null} if not defined
     * @since 0.2.3
     */
    protected DataModelSourceFactory getSource() {
        return source;
    }

    /**
     * set source for jobflow input.
     * @param sourcePath source path
     */
    protected void setSourceUri(String sourcePath) {
        try {
            URI sourceUri = toUri(sourcePath);
            setSourceUri(sourceUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid source URI:" + sourcePath, e);
        }
    }

    /**
     * Sets the source for jobflow input.
     * @param sourceUri the source uri
     */
    protected void setSourceUri(URI sourceUri) {
        LOG.info("Source URI:" + sourceUri);
        this.source = driverContext.getRepository().getDataModelSourceFactory(sourceUri);
    }

    /**
     * パス文字列からURIを生成する。
     *
     * @param path パス文字列
     * @return ワーキングのリソース位置
     * @throws URISyntaxException 引数の値がURIとして不正な値であった場合
     */
    public URI toUri(String path) throws URISyntaxException {
        // FIXME for invalid characters
        URI uri = URI.create(path);
        if (uri.getScheme() != null) {
            return uri;
        }

        URL url = driverContext.getCallerClass().getResource(uri.getPath());
        if (url == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "指定されたリソースが見つかりません: {0} (検索クラス: {1})",
                    path,
                    driverContext.getCallerClass().getName()));
        }
        URI resourceUri = url.toURI();
        if (uri.getFragment() == null) {
            return resourceUri;
        } else {
            URI resolvedUri = URI.create(resourceUri.toString() + '#' + uri.getFragment());
            return resolvedUri;
        }
    }

}