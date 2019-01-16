/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.batch;

import java.text.MessageFormat;

import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.JobFlow;

/**
 * A description of jobflow in batch.
 */
public class JobFlowWorkDescription extends WorkDescription {

    private final String name;

    private final Class<? extends FlowDescription> flowClass;

    /**
     * Creates a new instance.
     * @param flowClass the jobflow class
     * @throws IllegalArgumentException if the {@code jobflowClass} does not represent a jobflow
     */
    public JobFlowWorkDescription(Class<? extends FlowDescription> flowClass) {
        if (flowClass == null) {
            throw new IllegalArgumentException("flowClass must not be null"); //$NON-NLS-1$
        }
        if (FlowDescription.isJobFlow(flowClass) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("JobFlowWorkDescription.errorMissingAnnotation"), //$NON-NLS-1$
                    flowClass.getName(),
                    JobFlow.class.getSimpleName()));
        }
        this.name = FlowDescription.getJobFlowName(flowClass);
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("JobFlowWorkDescription.errorInvalidId"), //$NON-NLS-1$
                    name,
                    flowClass.getName()));
        }
        this.flowClass = flowClass;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the jobflow class.
     * @return the jobflow class
     */
    public Class<? extends FlowDescription> getFlowClass() {
        return flowClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + flowClass.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JobFlowWorkDescription other = (JobFlowWorkDescription) obj;
        if (flowClass.equals(other.flowClass) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "JobFlow({0})", //$NON-NLS-1$
                getFlowClass().getName());
    }
}
