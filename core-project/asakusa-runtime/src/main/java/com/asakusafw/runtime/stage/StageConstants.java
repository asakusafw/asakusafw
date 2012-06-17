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
package com.asakusafw.runtime.stage;

import java.text.MessageFormat;

import com.asakusafw.runtime.util.VariableTable;

/**
 * Constants for each stage.
 * @since 0.2.5
 */
public final class StageConstants {

    /**
     * 実行ユーザー名のプロパティキー。
     */
    public static final String PROP_USER = "com.asakusafw.user";

    /**
     * 実行IDのプロパティキー。
     */
    public static final String PROP_EXECUTION_ID = "com.asakusafw.executionId";

    /**
     * The property key name of batch ID.
     */
    public static final String PROP_BATCH_ID = "com.asakusafw.batchId";

    /**
     * The property key name of flow ID.
     */
    public static final String PROP_FLOW_ID = "com.asakusafw.flowId";

    /**
     * 環境変数表のプロパティキー。
     */
    public static final String PROP_ASAKUSA_BATCH_ARGS = "com.asakusafw.batchArgs";

    /**
     * 実行ユーザー名の変数名。
     */
    public static final String VAR_USER = "user";

    /**
     * 実行IDの変数名。
     */
    public static final String VAR_EXECUTION_ID = "execution_id";

    /**
     * バッチIDの変数名。
     */
    public static final String VAR_BATCH_ID = "batch_id";

    /**
     * フローIDの変数名。
     */
    public static final String VAR_FLOW_ID = "flow_id";

    /**
     * 定義IDの変数名。
     */
    public static final String VAR_DEFINITION_ID = "jobflow_name";

    /**
     * ステージ名の変数名。
     */
    public static final String VAR_STAGE_ID = "stage_name";

    /**
     * 実行ユーザー名の変数表記。
     */
    public static final String EXPR_USER = VariableTable.toVariable(VAR_USER);

    /**
     * 実行IDの変数表記。
     */
    public static final String EXPR_EXECUTION_ID = VariableTable.toVariable(VAR_EXECUTION_ID);

    /**
     * 定義IDの変数表記。
     */
    public static final String EXPR_DEFINITION_ID = VariableTable.toVariable(VAR_DEFINITION_ID);

    /**
     * ステージ名の変数表記。
     */
    public static final String EXPR_STAGE_ID = VariableTable.toVariable(VAR_STAGE_ID);

    /**
     * 指定のIDの組から、対象ジョブの定義IDを算出して返す。
     * @param batchId バッチID
     * @param flowId フローID
     * @param stageId ステージID
     * @return ジョブの定義ID
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getDefinitionId(String batchId, String flowId, String stageId) {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        if (stageId == null) {
            throw new IllegalArgumentException("stageId must not be null"); //$NON-NLS-1$
        }
        return MessageFormat.format("{0}.{1}.{2}", batchId, flowId, stageId);
    }

    private StageConstants() {
        return;
    }
}
