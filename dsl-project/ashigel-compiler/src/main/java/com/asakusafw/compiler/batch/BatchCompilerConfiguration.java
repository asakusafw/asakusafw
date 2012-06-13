/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;

import java.io.File;
import java.util.List;

import com.asakusafw.compiler.batch.WorkflowProcessor.Repository;
import com.asakusafw.compiler.flow.DataClassRepository;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.FlowGraphRewriter;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.utils.java.model.syntax.ModelFactory;

/**
 * 個々のジョブフローをコンパイルするコンパイラの設定。
 */
public class BatchCompilerConfiguration {

    private ModelFactory factory;

    private FlowElementProcessor.Repository flowElements;

    private DataClassRepository dataClasses;

    private ExternalIoDescriptionProcessor.Repository externals;

    private FlowGraphRewriter.Repository graphRewriters;

    private String batchId;

    private String rootPackageName;

    private Location rootLocation;

    private File workingDirectory;

    private List<? extends ResourceRepository> linkingResources;

    private File outputDirectory;

    private Repository workflows;

    private ClassLoader serviceClassLoader;

    private FlowCompilerOptions flowCompilerOptions;

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
     * ワークフロー全体を処理するプロセッサーのリポジトリーを返す。
     * @return ワークフロー全体を処理するプロセッサーのリポジトリー
     */
    public WorkflowProcessor.Repository getWorkflows() {
        return workflows;
    }

    /**
     * ワークフロー全体を処理するプロセッサーのリポジトリーを設定する。
     * @param workflows 設定するリポジトリー
     */
    public void setWorkflows(Repository workflows) {
        this.workflows = workflows;
    }

    /**
     * フロー要素を処理するプロセッサーのリポジトリーを返す。
     * @return フロー要素を処理するプロセッサーのリポジトリー
     */
    public FlowElementProcessor.Repository getFlowElements() {
        return flowElements;
    }

    /**
     * フロー要素を処理するプロセッサーのリポジトリーを設定する。
     * @param flowElements 設定するリポジトリー
     */
    public void setFlowElements(FlowElementProcessor.Repository flowElements) {
        this.flowElements = flowElements;
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
     * 外部入出力の記述を処理するプロセッサーのリポジトリーを返す。
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
     * コンパイラのワーキングディレクトリを返す。
     * @return コンパイラのワーキングディレクトリ
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * コンパイラのワーキングディレクトリを設定する。
     * @param workingDirectory コンパイラのワーキングディレクトリ
     */
    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * それぞれのジョブフローのコンパイル結果に含めるリソースの一覧を返す。
     * @return それぞれのジョブフローのコンパイル結果に含めるリソースの一覧
     */
    public List<? extends ResourceRepository> getLinkingResources() {
        return linkingResources;
    }

    /**
     * それぞれのジョブフローのコンパイル結果に含めるリソースの一覧を設定する。
     * @param linkingResources 設定するリソースの一覧
     */
    public void setLinkingResources(
            List<? extends ResourceRepository> linkingResources) {
        this.linkingResources = linkingResources;
    }

    /**
     * コンパイル結果の出力先ディレクトリを返す。
     * @return コンパイル結果の出力先ディレクトリ
     */
    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * コンパイル結果の出力先ディレクトリを設定する。
     * @param outputDirectory コンパイル結果の出力先ディレクトリ
     */
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
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
     * フローDSLコンパイラのオプション設定情報を返す。
     * @return フローDSLコンパイラのオプション設定情報
     */
    public FlowCompilerOptions getFlowCompilerOptions() {
        return flowCompilerOptions;
    }

    /**
     * フローDSLコンパイラのオプション設定情報を設定する。
     * @param flowCompilerOptions 設定するオプションの一覧
     */
    public void setFlowCompilerOptions(FlowCompilerOptions flowCompilerOptions) {
        this.flowCompilerOptions = flowCompilerOptions;
    }
}
