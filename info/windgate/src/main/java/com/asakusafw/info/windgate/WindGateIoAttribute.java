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
package com.asakusafw.info.windgate;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.asakusafw.info.Attribute;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An attribute of WindGate.
 * @since 0.9.2
 */
public class WindGateIoAttribute implements Attribute {

    /**
     * The attribute ID.
     */
    public static final String ID = "windgate";

    private final List<? extends WindGateInputInfo> inputs;

    private final List<? extends WindGateOutputInfo> outputs;

    /**
     * Creates a new instance.
     * @param inputs the input ports
     * @param outputs the output ports
     */
    public WindGateIoAttribute(
            Collection<? extends WindGateInputInfo> inputs,
            Collection<? extends WindGateOutputInfo> outputs) {
        this.inputs = Util.freeze(inputs);
        this.outputs = Util.freeze(outputs);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    static WindGateIoAttribute of(
            @JsonProperty("id") String id,
            @JsonProperty("inputs") Collection<? extends WindGateInputInfo> inputs,
            @JsonProperty("outputs") Collection<? extends WindGateOutputInfo> outputs) {
        if (Objects.equals(id, ID) == false) {
            throw new IllegalArgumentException();
        }
        return new WindGateIoAttribute(inputs, outputs);
    }

    @Override
    public String getId() {
        return ID;
    }

    /**
     * Returns the inputs.
     * @return the inputs
     */
    @JsonProperty("inputs")
    public List<? extends WindGateInputInfo> getInputs() {
        return inputs;
    }

    /**
     * Returns the outputs.
     * @return the outputs
     */
    @JsonProperty("outputs")
    public List<? extends WindGateOutputInfo> getOutputs() {
        return outputs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(inputs);
        result = prime * result + Objects.hashCode(outputs);
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
        WindGateIoAttribute other = (WindGateIoAttribute) obj;
        return Objects.equals(inputs, other.inputs)
                && Objects.equals(outputs, other.outputs);
    }

    @Override
    public String toString() {
        return String.format(
                "WindGate(inputs={%s}, outputs={%s})",
                inputs.stream().map(WindGatePortInfo::getName).collect(Collectors.joining(", ")), //$NON-NLS-1$
                outputs.stream().map(WindGatePortInfo::getName).collect(Collectors.joining(", "))); //$NON-NLS-1$
    }
}
