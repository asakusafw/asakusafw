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
package com.asakusafw.testdriver;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.Precondition;

/**
 * ジョブフローを直接実行する際に利用する計画。
 */
public class TestExecutionPlan implements Serializable {

    private static final long serialVersionUID = -8962301043507876930L;

    private final String definitionId;

    private final String executionId;

    private final List<Command> initializers;

    private final List<Command> importers;

    private final List<Job> jobs;

    private final List<Command> exporters;

    private final List<Command> finalizers;

    /**
     * インスタンスを生成する。
     * @param definitionId このトランザクションの定義識別子
     * @param executionId このトランザクションの実行識別子
     * @param initializers 初期化フェーズに実行するべきコマンド
     * @param importers インポートフェーズに実行するべきコマンド
     * @param jobs 本体フェーズに実行するべきジョブ
     * @param exporters エクスポートフェーズに実行するべきコマンド
     * @param finalizers 終了フェーズに実行するべきコマンド
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public TestExecutionPlan(
            String definitionId,
            String executionId,
            List<Command> initializers,
            List<Command> importers,
            List<Job> jobs,
            List<Command> exporters,
            List<Command> finalizers) {
        Precondition.checkMustNotBeNull(definitionId, "definitionId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(executionId, "executionId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(initializers, "initializers"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(importers, "importers"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(jobs, "jobs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(exporters, "exporters"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(finalizers, "finalizers"); //$NON-NLS-1$
        this.definitionId = definitionId;
        this.executionId = executionId;
        this.initializers = initializers;
        this.importers = importers;
        this.jobs = jobs;
        this.exporters = exporters;
        this.finalizers = finalizers;
    }

    /**
     * このトランザクション単位の定義識別子を返す。
     * @return このトランザクション単位の定義識別子
     */
    public String getDefinitionId() {
        return definitionId;
    }

    /**
     * このトランザクション単位の実行識別子を返す。
     * @return このトランザクション単位の実行識別子
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * フェーズの一覧を返す。
     * @return フェーズの一覧
     */
    public List<Command> getInitializers() {
        return initializers;
    }

    /**
     * フェーズの一覧を返す。
     * @return フェーズの一覧
     */
    public List<Command> getImporters() {
        return importers;
    }

    /**
     * フェーズの一覧を返す。
     * @return フェーズの一覧
     */
    public List<Job> getJobs() {
        return jobs;
    }

    /**
     * フェーズの一覧を返す。
     * @return フェーズの一覧
     */
    public List<Command> getExporters() {
        return exporters;
    }

    /**
     * フェーズの一覧を返す。
     * @return フェーズの一覧
     */
    public List<Command> getFinalizers() {
        return finalizers;
    }

    /**
     * 起動すべきジョブを表す。
     */
    public static class Job implements Serializable {

        private static final long serialVersionUID = -1707317463227716296L;

        private final String className;

        private final String executionId;

        private final Map<String, String> properties;

        /**
         * インスタンスを生成する。
         * @param className ジョブを起動するためのクライアントクラスの完全限定名
         * @param executionId このジョブの実行識別子
         * @param properties ジョブ起動時のプロパティ一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Job(String className, String executionId, Map<String, String> properties) {
            Precondition.checkMustNotBeNull(className, "className"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(executionId, "executionId"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(properties, "properties"); //$NON-NLS-1$
            this.className = className;
            this.executionId = executionId;
            this.properties = properties;
        }

        /**
         * ジョブを起動するためのクライアントクラス名を返す。
         * @return ジョブを起動するためのクライアントクラス名
         */
        public String getClassName() {
            return className;
        }

        /**
         * このジョブの実行識別子を返す。
         * @return このジョブの実行識別子
         */
        public String getExecutionId() {
            return executionId;
        }

        /**
         * ジョブ起動時のプロパティ一覧を返す。
         * @return ジョブ起動時のプロパティ一覧
         */
        public Map<String, String> getProperties() {
            return properties;
        }
    }

    /**
     * 起動すべきコマンドを表す。
     */
    public static class Command implements Serializable {

        private static final long serialVersionUID = -6594560296027009816L;

        private final List<String> commandLine;

        private final String moduleName;

        private final String profileName;

        private final Map<String, String> environment;

        /**
         * インスタンスを生成する。
         * @param commandLine コマンドラインを構成するセグメント一覧
         * @param moduleName この機能を提供するモジュールのID
         * @param profileName この機能を利用するロールプロファイルのID、規定の場合は{@code null}
         * @param environment 環境変数の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Command(
                List<String> commandLine,
                String moduleName,
                String profileName,
                Map<String, String> environment) {
            Precondition.checkMustNotBeNull(commandLine, "commandLine"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(moduleName, "moduleName"); //$NON-NLS-1$
            this.commandLine = commandLine;
            this.moduleName = moduleName;
            this.profileName = profileName;
            this.environment = environment;
        }

        /**
         * このコマンドのコマンドライントークンの一覧を返す。
         * @return このコマンドのコマンドライントークン
         */
        public List<String> getCommandTokens() {
            return commandLine;
        }

        /**
         * このコマンドのコマンドライン文字列を返す。
         * @return このコマンドのコマンドライン文字列
         */
        public String getCommandLineString() {
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, String> entry : environment.entrySet()) {
                buf.append("'" + entry.getKey() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                buf.append("="); //$NON-NLS-1$
                buf.append("'" + entry.getValue() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                buf.append(" "); //$NON-NLS-1$
            }
            Iterator<String> iter = commandLine.iterator();
            if (iter.hasNext()) {
                buf.append(iter.next());
                while (iter.hasNext()) {
                    buf.append(" "); //$NON-NLS-1$
                    buf.append(iter.next());
                }
            }
            return buf.toString();
        }

        /**
         * この機能を提供するモジュールのIDを返す。
         * @return この機能を提供するモジュールのID
         */
        public String getModuleName() {
            return moduleName;
        }

        /**
         * この機能を利用する際のロールプロファイルのIDを返す。
         * @return この機能を利用する際のロールプロファイルのID、デフォルトの場合は{@code null}
         */
        public String getProfileName() {
            return profileName;
        }

        /**
         * 環境変数の一覧を返す。
         * @return 環境変数の一覧
         */
        public Map<String, String> getEnvironment() {
            return environment;
        }
    }
}
