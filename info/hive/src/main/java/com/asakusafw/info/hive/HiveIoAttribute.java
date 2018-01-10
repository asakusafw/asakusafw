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
package com.asakusafw.info.hive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.asakusafw.info.Attribute;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An attribute of Direct I/O.
 * @since 0.10.0
 */
public class HiveIoAttribute implements Attribute {

    /**
     * The attribute ID.
     */
    public static final String ID = "hive";

    private final List<? extends HiveInputInfo> inputs;

    private final List<? extends HiveOutputInfo> outputs;

    /**
     * Creates a new instance.
     * @param inputs the input ports
     * @param outputs the output ports
     */
    public HiveIoAttribute(
            Collection<? extends HiveInputInfo> inputs,
            Collection<? extends HiveOutputInfo> outputs) {
        this.inputs = Collections.unmodifiableList(new ArrayList<>(inputs));
        this.outputs = Collections.unmodifiableList(new ArrayList<>(outputs));
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    static HiveIoAttribute of(
            @JsonProperty("id") String id,
            @JsonProperty("inputs") Collection<? extends HiveInputInfo> inputs,
            @JsonProperty("outputs") Collection<? extends HiveOutputInfo> outputs) {
        if (Objects.equals(id, ID) == false) {
            throw new IllegalArgumentException();
        }
        return new HiveIoAttribute(
                Optional.ofNullable(inputs).orElse(Collections.emptyList()),
                Optional.ofNullable(outputs).orElse(Collections.emptyList()));
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
    public List<? extends HiveInputInfo> getInputs() {
        return inputs;
    }

    /**
     * Returns the outputs.
     * @return the outputs
     */
    @JsonProperty("outputs")
    public List<? extends HiveOutputInfo> getOutputs() {
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
        HiveIoAttribute other = (HiveIoAttribute) obj;
        return Objects.equals(inputs, other.inputs)
                && Objects.equals(outputs, other.outputs);
    }

    @Override
    public String toString() {
        return String.format(
                "HiveIo(inputs={%s}, outputs={%s})",
                inputs.stream().map(it -> it.getSchema().getName()).collect(Collectors.joining()),
                outputs.stream().map(it -> it.getSchema().getName()).collect(Collectors.joining()));
    }
}
