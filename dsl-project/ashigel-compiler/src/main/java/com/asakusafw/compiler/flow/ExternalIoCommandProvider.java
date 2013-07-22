/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.IoContext;
import com.asakusafw.runtime.util.VariableTable;

/**
 * ジョブフローを処理するためのコマンドを提供する。
 */
public class ExternalIoCommandProvider implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * @deprecated Please use {@link #getFinalizeCommand(CommandContext)} instead
     */
    @Deprecated
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

        private final String homePathPrefix;

        private final String executionId;

        private final String variableList;

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
            table.defineVariables(variables);
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
     * @since 0.1.0
     * @version 0.5.1
     */
    public static class Command {

        private final String id;

        private final List<String> commandLine;

        private final String moduleName;

        private final String profileName;

        private final Map<String, String> environment;

        private final IoContext context;

        /**
         * インスタンスを生成する。
         * @param commandLine コマンドラインを構成するセグメント一覧
         * @param moduleName この機能を提供するモジュールのID
         * @param profileName この機能を利用するロールプロファイルのID、規定の場合は{@code null}
         * @param environment 環境変数の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         * @deprecated Use {@link IoContext} instead
         */
        @Deprecated
        public Command(
                List<String> commandLine,
                String moduleName,
                String profileName,
                Map<String, String> environment) {
            this(UUID.randomUUID().toString(), commandLine, moduleName, profileName, environment, IoContext.EMPTY);
        }

        /**
         * Creates a new instance.
         * @param id the command ID
         * @param commandLine the command line tokens
         * @param moduleName target module ID
         * @param profileName target profile ID, or {@code null} for the default profile
         * @param environment target environment variables
         * @param context I/O information for target command
         * @throws IllegalArgumentException if some parameters were {@code null}
         * @since 0.5.1
         */
        public Command(
                String id,
                List<String> commandLine,
                String moduleName,
                String profileName,
                Map<String, String> environment,
                IoContext context) {
            Precondition.checkMustNotBeNull(id, "id"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(commandLine, "commandLine"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(moduleName, "moduleName"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
            this.id = id;
            this.commandLine = commandLine;
            this.moduleName = moduleName;
            this.profileName = profileName;
            this.environment = environment;
            this.context = context;
        }

        /**
         * Returns the command ID.
         * @return the ID
         * @since 0.5.1
         */
        public String getId() {
            return id;
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

        /**
         * Returns I/O context for this command.
         * @return the I/O context
         * @since 0.5.1
         */
        public IoContext getContext() {
            return context;
        }
    }
}
