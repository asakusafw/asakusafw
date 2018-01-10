/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * @version 0.7.1
 */
public final class StageConstants {

    /**
     * The property key name of the user name.
     */
    public static final String PROP_USER = "com.asakusafw.user"; //$NON-NLS-1$

    /**
     * The property key name of the execution ID.
     */
    public static final String PROP_EXECUTION_ID = "com.asakusafw.executionId"; //$NON-NLS-1$

    /**
     * The property key name of the batch ID.
     */
    public static final String PROP_BATCH_ID = "com.asakusafw.batchId"; //$NON-NLS-1$

    /**
     * The property key name of the flow ID.
     */
    public static final String PROP_FLOW_ID = "com.asakusafw.flowId"; //$NON-NLS-1$

    /**
     * The property key name of the tracking ID.
     * @since 0.5.0
     */
    public static final String PROP_TRACKING_ID = "com.asakusafw.trackingId"; //$NON-NLS-1$

    /**
     * The property key name of the batch arguments.
     */
    public static final String PROP_ASAKUSA_BATCH_ARGS = "com.asakusafw.batchArgs"; //$NON-NLS-1$

    /**
     * The application JAR path.
     * @since 0.7.0
     */
    public static final String PROP_APPLICATION_JAR = "com.asakusafw.appJar"; //$NON-NLS-1$

    /**
     * The property key name of the implementation class of {@link JobRunner}.
     * @since 0.7.1
     */
    public static final String PROP_JOB_RUNNER = "com.asakusafw.jobRunner"; //$NON-NLS-1$

    /**
     * The variable name of the user name.
     * @see #PROP_USER
     */
    public static final String VAR_USER = "user"; //$NON-NLS-1$

    /**
     * The variable name of the execution ID.
     */
    public static final String VAR_EXECUTION_ID = "execution_id"; //$NON-NLS-1$

    /**
     * The variable name of the batch ID.
     */
    public static final String VAR_BATCH_ID = "batch_id"; //$NON-NLS-1$

    /**
     * The variable name of the flow ID.
     */
    public static final String VAR_FLOW_ID = "flow_id"; //$NON-NLS-1$

    /**
     * The variable name of the definition ID.
     */
    public static final String VAR_DEFINITION_ID = "jobflow_name"; //$NON-NLS-1$

    /**
     * The variable name of the stage ID.
     */
    public static final String VAR_STAGE_ID = "stage_name"; //$NON-NLS-1$

    /**
     * The expression of the user name.
     * @see VariableTable
     */
    public static final String EXPR_USER = VariableTable.toVariable(VAR_USER);

    /**
     * The expression of the execution ID.
     * @see VariableTable
     */
    public static final String EXPR_EXECUTION_ID = VariableTable.toVariable(VAR_EXECUTION_ID);

    /**
     * The expression of the qualified stage ID.
     * @see VariableTable
     */
    public static final String EXPR_DEFINITION_ID = VariableTable.toVariable(VAR_DEFINITION_ID);

    /**
     * The expression of the stage ID.
     * @see VariableTable
     */
    public static final String EXPR_STAGE_ID = VariableTable.toVariable(VAR_STAGE_ID);

    /**
     * Returns the qualified stage ID from individual IDs.
     * @param batchId the batch ID
     * @param flowId the flow ID
     * @param stageId the stage ID
     * @return the qualified stage ID (a.k.a. stage definition ID)
     * @throws IllegalArgumentException if some parameters are {@code null}
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
        return MessageFormat.format("{0}.{1}.{2}", batchId, flowId, stageId); //$NON-NLS-1$
    }

    private StageConstants() {
        return;
    }
}
