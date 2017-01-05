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
package com.asakusafw.compiler.flow.mapreduce.copy;

import org.apache.hadoop.mapreduce.OutputFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.SourceInfo;

/**
 * Describes each input to be copied.
 * @since 0.2.5
 */
public class CopyDescription {

    private final String name;

    private final DataClass dataModel;

    private final SourceInfo input;

    @SuppressWarnings("rawtypes")
    private final Class<? extends OutputFormat> outputFormatType;

    /**
     * Creates a new instance.
     * @param name slot name; this is also used as output name
     * @param dataModel target data model type
     * @param input input information
     * @param outputFormatType output format type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @SuppressWarnings("rawtypes")
    public CopyDescription(
            String name,
            DataClass dataModel,
            SourceInfo input,
            Class<? extends OutputFormat> outputFormatType) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(dataModel, "dataModel"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputFormatType, "outputFormatType"); //$NON-NLS-1$
        this.name = name;
        this.dataModel = dataModel;
        this.input = input;
        this.outputFormatType = outputFormatType;
    }

    /**
     * Returns the slot name.
     * @return slot name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns input information.
     * @return input information
     */
    public SourceInfo getInput() {
        return input;
    }

    /**
     * Returns the target data model type.
     * @return the target data model type
     */
    public DataClass getDataModel() {
        return dataModel;
    }

    /**
     * Returns the output format type.
     * @return the output format type
     */
    @SuppressWarnings("rawtypes")
    public Class<? extends OutputFormat> getOutputFormatType() {
        return outputFormatType;
    }
}
