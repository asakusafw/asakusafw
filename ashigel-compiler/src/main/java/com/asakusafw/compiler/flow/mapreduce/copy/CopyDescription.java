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
package com.asakusafw.compiler.flow.mapreduce.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.OutputFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.Location;

/**
 * Describes each input to be copied.
 * @since 0.2.5
 */
public class CopyDescription {

    private final String name;

    private final List<Location> inputPaths;

    private final DataClass dataModel;

    @SuppressWarnings("rawtypes")
    private final Class<? extends InputFormat> inputFormatType;

    @SuppressWarnings("rawtypes")
    private final Class<? extends OutputFormat> outputFormatType;

    /**
     * Creates a new instance.
     * @param name slot name; this is also used as output name
     * @param inputPaths input paths
     * @param dataModel target data model type
     * @param inputFormatType input format type
     * @param outputFormatType output format type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @SuppressWarnings("rawtypes")
    public CopyDescription(
            String name,
            Collection<Location> inputPaths,
            DataClass dataModel,
            Class<? extends InputFormat> inputFormatType,
            Class<? extends OutputFormat> outputFormatType) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(inputPaths, "inputPaths"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(dataModel, "dataModel"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(inputFormatType, "inputFormatType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputFormatType, "outputFormatType"); //$NON-NLS-1$
        this.name = name;
        this.inputPaths = new ArrayList<Location>(inputPaths);
        this.dataModel = dataModel;
        this.inputFormatType = inputFormatType;
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
     * Returns the input locations.
     * @return input locations
     */
    public List<Location> getInputPaths() {
        return inputPaths;
    }

    /**
     * Returns the target data model type.
     * @return the target data model type
     */
    public DataClass getDataModel() {
        return dataModel;
    }

    /**
     * Returns the input format type.
     * @return the input format type
     */
    @SuppressWarnings("rawtypes")
    public Class<? extends InputFormat> getInputFormatType() {
        return inputFormatType;
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
