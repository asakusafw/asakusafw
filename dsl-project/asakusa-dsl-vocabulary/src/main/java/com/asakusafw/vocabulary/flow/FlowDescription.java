/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An abstract super class for describing the details of a data-flow.
 * Subclasses must override {@link #describe()} method and build a data-flow in the method.
 */
public abstract class FlowDescription {

    private final AtomicBoolean described = new AtomicBoolean(false);

    /**
     * Analyzes flow DSL using {@link #describe() flow description method}.
     * Application developers should not invoke this method directly.
     */
    public final void start() {
        if (described.compareAndSet(false, true) == false) {
            return;
        }
        describe();
    }

    /**
     * Describes data-flow structure.
     * Subclasses must override this method and build a data-flow using Asakusa flow DSL.
     */
    protected abstract void describe();

    /**
     * Returns whether this object represents a jobflow or not.
     * @return {@code true} if this represents a jobflow, otherwise {@code false}
     * @deprecated Use {@link #isJobFlow(Class)} instead
     */
    @Deprecated
    public boolean isJobFlow() {
        return isJobFlow(getClass());
    }

    /**
     * Returns whether this object represents a flow-part or not.
     * @return {@code true} if this represents a flow-part, otherwise {@code false}
     * @deprecated Use {@link #isFlowPart(Class)} instead
     */
    @Deprecated
    public boolean isFlowPart() {
        return isFlowPart(getClass());
    }

    /**
     * Returns whether the target class represents a jobflow or not.
     * @param aClass the target flow description class
     * @return {@code true} if the class represents a jobflow, otherwise {@code false}
     */
    public static boolean isJobFlow(Class<? extends FlowDescription> aClass) {
        if (aClass == null) {
            throw new IllegalArgumentException("aClass must not be null"); //$NON-NLS-1$
        }
        return aClass.isAnnotationPresent(JobFlow.class);
    }

    /**
     * Returns the flow ID of the target jobflow class.
     * @param aClass the target flow description class
     * @return the flow ID, or {@code null} if the target class is not a valid jobflow class
     */
    public static String getJobFlowName(Class<? extends FlowDescription> aClass) {
        if (aClass == null) {
            throw new IllegalArgumentException("aClass must not be null"); //$NON-NLS-1$
        }
        JobFlow jobflow = aClass.getAnnotation(JobFlow.class);
        if (jobflow == null) {
            return null;
        }
        return jobflow.name();
    }

    /**
     * Returns whether the target class represents a flow-part or not.
     * @param aClass the target flow description class
     * @return {@code true} if the class represents a flow-part, otherwise {@code false}
     */
    public boolean isFlowPart(Class<? extends FlowDescription> aClass) {
        if (aClass == null) {
            throw new IllegalArgumentException("aClass must not be null"); //$NON-NLS-1$
        }
        return aClass.isAnnotationPresent(FlowPart.class);
    }
}
