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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
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
    /** フロー記述ドライバ */
    protected FlowDescriptionDriver descDriver;
    /** テストドライバコンテキスト */
    protected TestDriverContext driverContext;

    /** ソースURI */
    protected URI sourceUri;
    /** インポータ記述 */
    protected ImporterDescription importerDescription;

    /**
     * コンストラクタ
     * 
     * @param driverContext テストドライバコンテキスト。
     * @param descDriver フロー定義ドライバ。
     * @param name 入力の名前。
     * @param modelType モデルクラス。
     */
    public DriverInputBase(TestDriverContext driverContext, FlowDescriptionDriver descDriver, String name,
            Class<T> modelType) {
        this.name = name;
        this.modelType = modelType;
        this.descDriver = descDriver;
        this.driverContext = driverContext;
    }
    
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
     * @return the descDriver
     */
    protected FlowDescriptionDriver getDescDriver() {
        return descDriver;
    }

    /**
     * @param descDriver the descDriver to set
     */
    protected void setDescDriver(FlowDescriptionDriver descDriver) {
        this.descDriver = descDriver;
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
     * set source URI from source path and fragment string.
     * 
     * @param sourcePath source path.
     * @param fragment fragment id.
     */
    protected void setSourceUri(String sourcePath, String fragment) {
        try {
            sourceUri = DriverInputBase.toUri(sourcePath, fragment);
            LOG.info("Source URI:" + sourceUri + ", Fragment:" + fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid source URI:" + sourcePath + ", fragment:" + fragment, e);
        }
    }

    /**
     * パス文字列からURIを生成する。
     * 
     * @param path パス文字列
     * @param fragment URIに付加するフラグメント識別子
     * @return ワーキングのリソース位置
     * @throws URISyntaxException 引数の値がURIとして不正な値であった場合
     */
    public static URI toUri(String path, String fragment) throws URISyntaxException {
        URI resource = new File(path).toURI();
        URI uri = new URI(resource.getScheme(), resource.getUserInfo(), resource.getHost(), resource.getPort(),
                resource.getPath(), resource.getQuery(), fragment);
        return uri;
    }

}