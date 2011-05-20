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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * テストドライバのテスト入力データの親クラス。
 * @since 0.2.0
 * 
 * @param <T> モデルクラス
 */
public abstract class DriverInputBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DriverInputBase.class);

    /** データ名 */
    protected String name;
    /** モデル型 */
    protected Class<T> modelType;
    /** テストドライバコンテキスト */
    protected TestDriverContext driverContext;

    /** ソースURI */
    protected URI sourceUri;
    /** インポータ記述 */
    protected ImporterDescription importerDescription;

    /**
     * @return the name
     */
    protected String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * @return the modelType
     */
    protected Class<T> getModelType() {
        return modelType;
    }

    /**
     * @param modelType the modelType to set
     */
    protected void setModelType(Class<T> modelType) {
        this.modelType = modelType;
    }

    /**
     * @return the driverContext
     */
    protected TestDriverContext getDriverContext() {
        return driverContext;
    }

    /**
     * @param driverContext the driverContext to set
     */
    protected void setDriverContext(TestDriverContext driverContext) {
        this.driverContext = driverContext;
    }

    /**
     * @return the sourceUri
     */
    protected URI getSourceUri() {
        return sourceUri;
    }

    /**
     * @param sourceUri the sourceUri to set
     */
    protected void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
    }

    /**
     * @return the importerDescription
     */
    protected ImporterDescription getImporterDescription() {
        return importerDescription;
    }

    /**
     * @param importerDescription the importerDescription to set
     */
    protected void setImporterDescription(ImporterDescription importerDescription) {
        this.importerDescription = importerDescription;
    }

    /**
     * set source URI from source path.
     * 
     * @param sourcePath source path.
     */
    protected void setSourceUri(String sourcePath) {
        try {
            sourceUri = toUri(sourcePath);
            LOG.info("Source URI:" + sourceUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid source URI:" + sourcePath, e);
        }
    }

    /**
     * パス文字列からURIを生成する。
     * 
     * @param path パス文字列
     * @return ワーキングのリソース位置
     * @throws URISyntaxException 引数の値がURIとして不正な値であった場合
     */
    public URI toUri(String path) throws URISyntaxException {
        URI uri = new URI(path);
        if (uri.getScheme() != null) {
            return uri;
        }

        URL url = driverContext.getCallerClass().getResource(uri.getPath());
        URI resourceUri = url.toURI();
        URI withFragmentUri = new URI(resourceUri.getScheme(), resourceUri.getUserInfo(), resourceUri.getHost(), resourceUri.getPort(),
                resourceUri.getPath(), resourceUri.getQuery(), uri.getFragment());
        return withFragmentUri;
    }

}