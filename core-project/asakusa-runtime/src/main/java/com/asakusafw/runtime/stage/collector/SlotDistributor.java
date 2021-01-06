/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.flow.MapperWithRuntimeResource;

/**
 * The skeletal implementation of Hadoop Mapper for distributing values into {@link SlotSorter}.
 * @param <T> the data type
 * @since 0.1.0
 * @version 0.5.1
 */
public abstract class SlotDistributor<T extends Writable> extends MapperWithRuntimeResource<
        Object, T,
        SortableSlot, WritableSlot> {

    /**
     * The method name of {@link #setSlotSpec(Writable, SortableSlot)}.
     */
    public static final String NAME_SET_SLOT_SPEC = "setSlotSpec"; //$NON-NLS-1$

    private final SortableSlot keyOut = new SortableSlot();

    private final WritableSlot valueOut = new WritableSlot();

    /**
     * Sets sorting information into the target slot.
     * Sub-class must set its slot number before write any properties as like following:
<pre><code>
slot.begin(MY_SLOT_NUMBER);
slot.addWritable(value.getPrimaryKey());
slot.addWritable(value.getSecondaryKey());
...
</code></pre>
     * @param value the target value
     * @param slot the target slot
     * @throws IOException if failed to write
     */
    protected abstract void setSlotSpec(T value, SortableSlot slot) throws IOException;

    @Override
    protected void map(Object key, T value, Context context) throws IOException, InterruptedException {
        valueOut.store(value);
        setSlotSpec(value, keyOut);
        context.write(keyOut, valueOut);
    }
}
