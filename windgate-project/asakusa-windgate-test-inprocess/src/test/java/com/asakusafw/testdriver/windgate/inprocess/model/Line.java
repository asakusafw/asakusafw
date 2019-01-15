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
package com.asakusafw.testdriver.windgate.inprocess.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.value.StringOption;

/**
 * Data model for testing.
 */
@DataModelKind("DMDL")
@SuppressWarnings("deprecation")
public class Line implements DataModel<Line>, Writable {

    private final StringOption value = new StringOption();

    /**
     * Creates a new instance.
     */
    public Line() {
        return;
    }

    /**
     * Creates a new instance.
     * @param value the value
     */
    public Line(String value) {
        this.value.modify(value);
    }

    /**
     * Returns the value.
     * @return the value
     */
    public StringOption getValueOption() {
        return value;
    }

    @Override
    public void reset() {
        value.reset();
    }

    @Override
    public void copyFrom(Line other) {
        value.copyFrom(other.value);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(value.getAsString());
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        value.modify(in.readUTF());
    }
}
