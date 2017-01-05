/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.testing;

import java.io.File;
import java.util.List;

import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.common.Precondition;

/**
 * Represents information of batch structure for testing.
 */
public class BatchInfo {

    private final Workflow workflow;

    private final List<JobflowInfo> jobflows;

    private final File output;

    /**
     * Creates a new instance.
     * @param workflow the original workflow
     * @param output the output directory
     * @param jobflows the information of jobflows in the target batch
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public BatchInfo(
            Workflow workflow,
            File output,
            List<JobflowInfo> jobflows) {
        Precondition.checkMustNotBeNull(workflow, "workflow"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(jobflows, "jobflows"); //$NON-NLS-1$
        this.workflow = workflow;
        this.output = output;
        this.jobflows = jobflows;
    }

    /**
     * Returns a {@link JobflowInfo} object in this batch which has the specified {@code Flow ID}.
     * @param flowId target Flow ID
     * @return the found {@link JobflowInfo}, or {@code null} if no such a jobflow exists
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JobflowInfo findJobflow(String flowId) {
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        for (JobflowInfo info : jobflows) {
            if (info.getJobflow().getFlowId().equals(flowId)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Returns the output directory.
     * @return the output directory
     */
    public File getOutputDirectory() {
        return output;
    }

    /**
     * Returns the original workflow object.
     * @return the original workflow object
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * Returns the jobflows in the target batch.
     * @return the jobflows in the target batch
     */
    public List<JobflowInfo> getJobflows() {
        return this.jobflows;
    }
}
