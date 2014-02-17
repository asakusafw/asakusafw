/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow;

import com.asakusafw.utils.java.model.syntax.ModelFactory;

/**
 * 個々のジョブフローをコンパイルするコンパイラの設定。
 * @since 0.1.0
 * @version 0.4.0
 */
public class FlowCompilerConfiguration {

    private ModelFactory factory;

    private Packager packager;

    private FlowElementProcessor.Repository processors;

    private DataClassRepository dataClasses;

    private ExternalIoDescriptionProcessor.Repository externals;

    private FlowGraphRewriter.Repository graphRewriters;

    private String batchId;

    private String flowId;

    private String rootPackageName;

    private Location rootLocation;

    private ClassLoader serviceClassLoader;

    private FlowCompilerOptions options;

    private String buildId;

    /**
     * コンパイラが利用するJava DOMのファクトリーを返す。
     * @return Java DOMのファクトリー
     */
    public ModelFactory getFactory() {
        return factory;
    }

    /**
     * コンパイラが利用するJava DOMのファクトリーを設定する。
     * @param factory 設定するファクトリー
     */
    public void setFactory(ModelFactory factory) {
        this.factory = factory;
    }

    /**
     * コンパイラが利用するパッケージャーを返す。
     * @return コンパイラが利用するパッケージャー
     */
    public Packager getPackager() {
        return packager;
    }

    /**
     * コンパイラが利用するパッケージャーを設定する。
     * @param packager 設定するパッケージャー
     */
    public void setPackager(Packager packager) {
        this.packager = packager;
    }

    /**
     * フロー要素を処理するプロセッサーのリポジトリーを返す。
     * @return フロー要素を処理するプロセッサーのリポジトリー
     */
    public FlowElementProcessor.Repository getProcessors() {
        return processors;
    }

    /**
     * フロー要素を処理するプロセッサーのリポジトリーを設定する。
     * @param processors 設定するリポジトリー
     */
    public void setProcessors(FlowElementProcessor.Repository processors) {
        this.processors = processors;
    }

    /**
     * データモデルを操作するオブジェクトのリポジトリーを返す。
     * @return データモデルを操作するオブジェクトのリポジトリー
     */
    public DataClassRepository getDataClasses() {
        return dataClasses;
    }

    /**
     * データモデルを操作するオブジェクトのリポジトリーを設定する。
     * @param dataClasses 設定するリポジトリー
     */
    public void setDataClasses(DataClassRepository dataClasses) {
        this.dataClasses = dataClasses;
    }

    /**
     * 外部入出力の記述を処理するプロセッサーのリポジトリーを返す。
     * @return 外部入出力の記述を処理するプロセッサーのリポジトリー
     */
    public ExternalIoDescriptionProcessor.Repository getExternals() {
        return externals;
    }

    /**
     * 外部入出力の記述を処理するプロセッサーのリポジトリーを設定する。
     * @param externals 設定するリポジトリー
     */
    public void setExternals(ExternalIoDescriptionProcessor.Repository externals) {
        this.externals = externals;
    }

    /**
     * 演算子グラフを書き換えるエンジンのリポジトリーを返す。
     * @return 演算子グラフを書き換えるエンジンのリポジトリー
     */
    public FlowGraphRewriter.Repository getGraphRewriters() {
        return graphRewriters;
    }

    /**
     * 演算子グラフを書き換えるエンジンのリポジトリーを設定する。
     * @param graphRewriters 設定するリポジトリー
     */
    public void setGraphRewriters(FlowGraphRewriter.Repository graphRewriters) {
        this.graphRewriters = graphRewriters;
    }

    /**
     * コンパイラーが対象とするバッチの識別子を返す。
     * @return コンパイラーが対象とするバッチの識別子
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * コンパイラーが対象とするバッチの識別子を設定する。
     * @param batchId 設定する識別子
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    /**
     * コンパイラーが対象とするジョブフローの識別子を返す。
     * @return コンパイラーが対象とするジョブフローの識別子
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * コンパイラーが対象とするジョブフローの識別子を設定する。
     * @param flowId 設定する識別子
     */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /**
     * コンパイラーが生成するクラスの基底となるパッケージ名を返す。
     * @return コンパイラーが生成するクラスの基底となるパッケージ名
     */
    public String getRootPackageName() {
        return rootPackageName;
    }

    /**
     * コンパイラーが生成するクラスの基底となるパッケージ名を設定する。
     * @param rootPackageName 設定するパッケージ名
     */
    public void setRootPackageName(String rootPackageName) {
        this.rootPackageName = rootPackageName;
    }

    /**
     * ジョブフローが利用するファイルシステム上のルート位置を返す。
     * @return ジョブフローが利用するファイルシステム上のルート位置
     */
    public Location getRootLocation() {
        return rootLocation;
    }

    /**
     * ジョブフローが利用するファイルシステム上のルート位置を設定する。
     * @param rootLocation 設定するルート位置
     */
    public void setRootLocation(Location rootLocation) {
        this.rootLocation = rootLocation;
    }

    /**
     * サービスをロードするためのクラスローダを返す。
     * @return サービスをロードするためのクラスローダ
     */
    public ClassLoader getServiceClassLoader() {
        return serviceClassLoader;
    }

    /**
     * サービスをロードするためのクラスローダを設定する。
     * @param serviceClassLoader 設定するクラスローダ
     */
    public void setServiceClassLoader(ClassLoader serviceClassLoader) {
        this.serviceClassLoader = serviceClassLoader;
    }

    /**
     * コンパイラーオプションを返す。
     * @return コンパイラーオプション
     */
    public FlowCompilerOptions getOptions() {
        return options;
    }

    /**
     * コンパイラーのオプションを設定する。
     * @param options コンパイラーオプション
     */
    public void setOptions(FlowCompilerOptions options) {
        this.options = options;
    }

    /**
     * Returns the current build ID.
     * @return current build ID, or {@code null} if not defined
     * @since 0.4.0
     */
    public String getBuildId() {
        return buildId;
    }

    /**
     * Sets the current build ID.
     * @param buildId build ID
     * @since 0.4.0
     */
    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }
}
