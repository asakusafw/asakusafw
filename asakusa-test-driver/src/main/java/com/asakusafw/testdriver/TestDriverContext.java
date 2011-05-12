package com.asakusafw.testdriver;

import java.util.Map;

import com.asakusafw.compiler.flow.FlowCompilerOptions;

public class TestDriverContext {
    /** OSのユーザ名。 */
    private String osUser;
    /** DSLCompilerのローカルワークディレクトリ。 */
    private String compileWorkBaseDir;
    /** DSLCompilerのクラスタワークディレクトリ。 */
    private String clusterWorkDir;
    /** テストクラスのクラス名。 */
    private String className;
    /** テストクラスのメソッド名。 */
    private String methodName;
    /**
     * ジョブフローの実行ID。 (テストドライバでダミーの値をセットする)
     */
    private String executionId;
    /**
     * 実行時の追加設定一覧 (Property Name, Property Value)。
     */
    private Map<String, String> extraConfigurations;
    /**
     * バッチ実行時引数 (ASAKUSA_BATCH_ARGS)。
     */
    private Map<String, String> batchArgs;
    /**
     * コンパイラオプション。
     */
    private FlowCompilerOptions options;

    public TestDriverContext(Map<String, String> extraConfigurations,
            Map<String, String> batchArgs, FlowCompilerOptions options) {
        this.extraConfigurations = extraConfigurations;
        this.batchArgs = batchArgs;
        this.options = options;
    }

    public String getOsUser() {
        return osUser;
    }

    public void setOsUser(String osUser) {
        this.osUser = osUser;
    }

    public String getCompileWorkBaseDir() {
        return compileWorkBaseDir;
    }

    public void setCompileWorkBaseDir(String compileWorkBaseDir) {
        this.compileWorkBaseDir = compileWorkBaseDir;
    }

    public String getClusterWorkDir() {
        return clusterWorkDir;
    }

    public void setClusterWorkDir(String clusterWorkDir) {
        this.clusterWorkDir = clusterWorkDir;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public Map<String, String> getExtraConfigurations() {
        return extraConfigurations;
    }

    public void setExtraConfigurations(Map<String, String> extraConfigurations) {
        this.extraConfigurations = extraConfigurations;
    }

    public Map<String, String> getBatchArgs() {
        return batchArgs;
    }

    public void setBatchArgs(Map<String, String> batchArgs) {
        this.batchArgs = batchArgs;
    }

    public FlowCompilerOptions getOptions() {
        return options;
    }

    public void setOptions(FlowCompilerOptions options) {
        this.options = options;
    }
}