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

import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.ReducerWithRuntimeResource;
import com.asakusafw.runtime.stage.output.StageOutputDriver;

/**
 * A skeletal implementation of Hadoop Reducer for partitioning by their slot.
 * @since 0.1.0
 * @version 0.5.1
 */
public abstract class SlotSorter extends ReducerWithRuntimeResource<
        SortableSlot, WritableSlot,
        Object, Object> {

    /**
     * The method name of {@link #getOutputNames()}.
     */
    public static final String NAME_GET_OUTPUT_NAMES = "getOutputNames"; //$NON-NLS-1$

    /**
     * The method name of {@link #createSlotObjects()}.
     */
    public static final String NAME_CREATE_SLOT_OBJECTS = "createSlotObjects"; //$NON-NLS-1$

    private StageOutputDriver output;

    private Writable[] objects;

    private Result<Writable>[] results;

    /**
     * Returns the array of buffer object for each slot.
     * @return the buffer objects
     */
    protected abstract Writable[] createSlotObjects();

    /**
     * Returns the array of names for each slot.
     * @return the slot names
     */
    protected abstract String[] getOutputNames();

    @Override
    @SuppressWarnings("unchecked")
    protected void setup(Context context) throws IOException, InterruptedException {
        this.objects = createSlotObjects();
        String[] names = getOutputNames();
        if (objects.length != names.length) {
            throw new AssertionError("inconsistent slot object and output"); //$NON-NLS-1$
        }
        this.output = new StageOutputDriver(context);
        this.results = new Result[objects.length];
        for (int i = 0; i < objects.length; i++) {
            String name = names[i];
            if (name != null) {
                results[i] = output.getResultSink(name);
            }
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        this.output.close();
        this.output = null;
        this.objects = null;
        this.results = null;
    }

    @Override
    protected void reduce(
            SortableSlot key,
            Iterable<WritableSlot> values,
            Context context) throws IOException, InterruptedException {
        int slot = key.getSlot();
        Writable cache = objects[slot];
        Result<Writable> result = results[slot];
        for (WritableSlot holder : values) {
            holder.loadTo(cache);
            result.add(cache);
        }
    }
}
