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
package com.asakusafw.compiler.flow.mapreduce.parallel;

import java.lang.reflect.Type;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.SourceInfo;

/**
 * Represents a data-set slot for parallel sorting.
 */
public class Slot {

    private final String outputName;

    private final Type type;

    private final List<String> propertyNames;

    private final List<SourceInfo> inputs;

    private final Class<?> outputFormatType;

    /**
     * Creates a new instance.
     * @param outputName the slot output name
     * @param type the slot data type
     * @param propertyNames the sort key property names
     * @param inputs information of the target inputs
     * @param outputFormatType the Hadoop output format class
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public Slot(
            String outputName,
            Type type,
            List<String> propertyNames,
            List<SourceInfo> inputs,
            Class<?> outputFormatType) {
        Precondition.checkMustNotBeNull(outputName, "outputName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(propertyNames, "propertyNames"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputFormatType, "outputFormatType"); //$NON-NLS-1$
        this.outputName = outputName;
        this.type = type;
        this.propertyNames = propertyNames;
        this.inputs = inputs;
        this.outputFormatType = outputFormatType;
    }

    /**
     * Returns the slot output name.
     * @return the slot output name
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * Returns the slot data type.
     * @return the slot data type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the sort key property names.
     * @return the sort key property names
     */
    public List<String> getSortPropertyNames() {
        return propertyNames;
    }

    /**
     * Returns information of the target inputs.
     * @return information of the target inputs
     */
    public List<SourceInfo> getInputs() {
        return inputs;
    }

    /**
     * Returns the Hadoop output format class.
     * @return the Hadoop output format class
     */
    public Class<?> getOutputFormatType() {
        return outputFormatType;
    }
}
