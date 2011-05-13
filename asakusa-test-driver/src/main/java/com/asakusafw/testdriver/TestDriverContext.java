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

import java.util.Map;

import com.asakusafw.compiler.flow.FlowCompilerOptions;

/**
 * テスト実行時のコンテキスト情報を管理する。
 * @since 0.2.0
 */
public class TestDriverContext {
    
    /** OSのユーザ名。 */
    private String osUser;
    /** AshigelCompilerのローカルワークディレクトリ。 */
    private String compileWorkBaseDir;
    /** Hadoopのワークディレクトリ。 */
    private String clusterWorkDir;
    /** テストクラスのクラス名。 */
    private String className;
    /** テストクラスのメソッド名。 */
    private String methodName;
    
    /** ジョブフローの実行ID。 (テストドライバでダミーの値をセットする)*/
    private String executionId;
    /** 実行時の追加設定一覧 (Property Name, Property Value)。*/
    private Map<String, String> extraConfigurations;
    /** バッチ実行時引数 (ASAKUSA_BATCH_ARGS)。*/
    private Map<String, String> batchArgs;
    /** コンパイラオプション。*/
    private FlowCompilerOptions options;

    /**
     * コンストラクタ 
     * @param extraConfigurations 実行時の追加設定一覧
     * @param batchArgs バッチ実行時引数
     * @param options コンパイラオプション
     */
    public TestDriverContext(Map<String, String> extraConfigurations,
            Map<String, String> batchArgs, FlowCompilerOptions options) {
        this.extraConfigurations = extraConfigurations;
        this.batchArgs = batchArgs;
        this.options = options;
    }

    /**
     * @return the osUser
     */
    public String getOsUser() {
        return osUser;
    }

    /**
     * @param osUser the osUser to set
     */
    public void setOsUser(String osUser) {
        this.osUser = osUser;
    }

    /**
     * @return the compileWorkBaseDir
     */
    public String getCompileWorkBaseDir() {
        return compileWorkBaseDir;
    }

    /**
     * @param compileWorkBaseDir the compileWorkBaseDir to set
     */
    public void setCompileWorkBaseDir(String compileWorkBaseDir) {
        this.compileWorkBaseDir = compileWorkBaseDir;
    }

    /**
     * @return the clusterWorkDir
     */
    public String getClusterWorkDir() {
        return clusterWorkDir;
    }

    /**
     * @param clusterWorkDir the clusterWorkDir to set
     */
    public void setClusterWorkDir(String clusterWorkDir) {
        this.clusterWorkDir = clusterWorkDir;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @return the executionId
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * @param executionId the executionId to set
     */
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    /**
     * @return the extraConfigurations
     */
    public Map<String, String> getExtraConfigurations() {
        return extraConfigurations;
    }

    /**
     * @param extraConfigurations the extraConfigurations to set
     */
    public void setExtraConfigurations(Map<String, String> extraConfigurations) {
        this.extraConfigurations = extraConfigurations;
    }

    /**
     * @return the batchArgs
     */
    public Map<String, String> getBatchArgs() {
        return batchArgs;
    }

    /**
     * @param batchArgs the batchArgs to set
     */
    public void setBatchArgs(Map<String, String> batchArgs) {
        this.batchArgs = batchArgs;
    }

    /**
     * @return the options
     */
    public FlowCompilerOptions getOptions() {
        return options;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(FlowCompilerOptions options) {
        this.options = options;
    }

}