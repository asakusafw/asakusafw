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
package com.asakusafw.compiler.flow;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.util.VariableTable;

/**
 * ジョブフローを処理するためのコマンドを提供する。
 */
public class ExternalIoCommandProvider {

    /**
     * このコマンドプロバイダーの名前を返す。
     * @return このコマンドプロバイダーの名前
     */
    public String getName() {
        return "default";
    }

    /**
     * 出力するワークフローに対して、このインポーターに対するコマンド情報を返す。
     * @param context コマンドの文脈情報
     * @return 対応するコマンドの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Command> getImportCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * 出力するワークフローに対して、このエクスポーターに対するコマンド情報を返す。
     * @param context コマンドの文脈情報
     * @return 対応するコマンドの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Command> getExportCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * 出力するワークフローに対して、このインポーターおよび
     * エクスポーターに対するジョブ失敗時の復旧コマンド情報を返す。
     * @param context コマンドの文脈情報
     * @return 対応するコマンドの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Command> getRecoverCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * 出力するワークフローに対して、このインポーターおよび
     * エクスポーターに対するジョブ開始前の初期化コマンド情報を返す。
     * @param context コマンドの文脈情報
     * @return 対応するコマンドの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Command> getInitializeCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * 出力するワークフローに対して、このインポーターおよび
     * エクスポーターに対するジョブ終了後の最終コマンド情報を返す。
     * @param context コマンドの文脈情報
     * @return 対応するコマンドの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Command> getFinalizeCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * コマンドの文脈情報。
     */
    public static class CommandContext {

        private String homePathPrefix;

        private String executionId;

        private String variableList;

        /**
         * インスタンスを生成する。
         * @param homePathPrefix コマンドホームディレクトリの接頭辞
         * @param executionId 処理対象の実行ID
         * @param variableList 変数表
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public CommandContext(String homePathPrefix, String executionId, String variableList) {
            Precondition.checkMustNotBeNull(homePathPrefix, "homePathPrefix"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(executionId, "executionId"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(variableList, "variableList"); //$NON-NLS-1$
            this.homePathPrefix = homePathPrefix;
            this.executionId = executionId;
            this.variableList = variableList;
        }

        /**
         * インスタンスを生成する。
         * @param homePathPrefix コマンドホームディレクトリの接頭辞
         * @param executionId 処理対象の実行ID
         * @param variables 変数表
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public CommandContext(String homePathPrefix, String executionId, Map<String, String> variables) {
            Precondition.checkMustNotBeNull(homePathPrefix, "homePathPrefix"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(executionId, "executionId"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(variables, "variables"); //$NON-NLS-1$
            this.homePathPrefix = homePathPrefix;
            this.executionId = executionId;
            VariableTable table = new VariableTable();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                table.defineVariable(entry.getKey(), entry.getValue());
            }
            this.variableList = table.toSerialString();
        }

        /**
         * コマンドホームディレクトリの接頭辞を返す。
         * @return コマンドホームディレクトリの接頭辞
         */
        public String getHomePathPrefix() {
            return homePathPrefix;
        }

        /**
         * 処理中のフローに対する実行IDを返す。
         * @return 処理中のフローに対する実行ID
         */
        public String getExecutionId() {
            return executionId;
        }

        /**
         * 変数表の文字列表現を返す。
         * @return 変数表の文字列表現
         */
        public String getVariableList() {
            return variableList;
        }
    }

    /**
     * 各種コマンドを表す。
     */
    public static class Command {

        private List<String> commandLine;

        private String moduleName;

        private String profileName;

        private Map<String, String> environment;

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
            // TODO あらゆる文字に対応する
            for (Map.Entry<String, String> entry : environment.entrySet()) {
                buf.append("'" + entry.getKey() + "'");
                buf.append("=");
                buf.append("'" + entry.getValue() + "'");
                buf.append(" ");
            }
            Iterator<String> iter = commandLine.iterator();
            if (iter.hasNext()) {
                buf.append(iter.next());
                while (iter.hasNext()) {
                    buf.append(" ");
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
