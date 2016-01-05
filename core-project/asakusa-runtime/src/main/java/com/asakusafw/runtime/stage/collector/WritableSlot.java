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
package com.asakusafw.runtime.stage.collector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

import com.asakusafw.runtime.io.util.DataBuffer;

/**
 * A data slot.
 * @see SortableSlot
 */
public class WritableSlot implements Writable {

    private final DataBuffer buffer = new DataBuffer();

    /**
     * Stores the copy of the target object.
     * @param data the target object
     * @throws IOException if failed to store the object
     */
    public void store(Writable data) throws IOException {
        buffer.reset(0, 0);
        data.write(buffer);
    }

    /**
     * Loads the contents in this object into the target object.
     * @param data the target object
     * @throws IOException if failed to load the contents
     */
    public void loadTo(Writable data) throws IOException {
        buffer.reset(0, buffer.getReadLimit());
        data.readFields(buffer);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        WritableUtils.writeVInt(out, buffer.getReadRemaining());
        out.write(buffer.getData(), buffer.getReadPosition(), buffer.getReadRemaining());
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        buffer.reset(0, 0);
        int length = WritableUtils.readVInt(in);
        buffer.write(in, length);
    }
}
