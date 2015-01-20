/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import static com.asakusafw.runtime.stage.StageConstants.*;

import java.text.MessageFormat;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

import com.asakusafw.runtime.compatibility.CoreCompatibility;
import com.asakusafw.runtime.core.context.RuntimeContext;

/**
 * A skeletal implementation of stage clients.
 * @since 0.2.6
 */
public abstract class BaseStageClient extends Configured implements Tool {

    static {
        CoreCompatibility.verifyFrameworkVersion();
    }

    /**
     * {@link #getBatchId()}のメソッド名。
     */
    public static final String METHOD_BATCH_ID = "getBatchId";

    /**
     * {@link #getFlowId()}のメソッド名。
     */
    public static final String METHOD_FLOW_ID = "getFlowId";

    /**
     * {@link #getStageId()}のメソッド名。
     */
    public static final String METHOD_STAGE_ID = "getStageId";

    /**
     * 処理中のユーザー名を返す。
     * @return 処理中のユーザー名
     */
    protected String getUser() {
        return getMandatoryProperty(PROP_USER);
    }

    /**
     * このフロー全体(一連のステージ)の実行時識別子を返す。
     * @return 実行時識別子
     */
    protected String getExecutionId() {
        return getMandatoryProperty(PROP_EXECUTION_ID);
    }

    private String getMandatoryProperty(String key) {
        assert key != null;
        String value = getConf().get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    "Missing a mandatory configuration \"{0}\"",
                    key));
        }
        return value;
    }

    /**
     * このクライアントによって起動されるジョブのバッチIDを返す。
     * @return バッチID
     */
    protected abstract String getBatchId();

    /**
     * このクライアントによって起動されるジョブのフローIDを返す。
     * @return フローID
     */
    protected abstract String getFlowId();

    /**
     * このクライアントによって起動されるジョブのステージ名を返す。
     * @return ステージ名
     */
    protected abstract String getStageId();

    /**
     * このクライアントによって起動されるジョブの定義IDを返す。
     * @return 定義ID
     */
    protected String getDefinitionId() {
        String batchId = getBatchId();
        String flowId = getFlowId();
        String stageId = getStageId();
        return StageConstants.getDefinitionId(batchId, flowId, stageId);
    }

    /**
     * Returns the job operation ID which this client will invoke.
     * @return the job operation ID
     * @since 0.5.0
     */
    protected String getOperationId() {
        String trackingId = getConf().get(PROP_TRACKING_ID);
        if (trackingId != null) {
            return trackingId;
        }
        return getDefinitionId();
    }

    @Override
    public final int run(String[] args) throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        RuntimeContext.get().verifyApplication(getConf().getClassLoader());
        return execute(args);
    }

    /**
     * Performs stage execution.
     * @param args arguments
     * @return exit code of the execution
     * @throws Exception if failed to execute
     */
    protected abstract int execute(String[] args) throws Exception;
}
