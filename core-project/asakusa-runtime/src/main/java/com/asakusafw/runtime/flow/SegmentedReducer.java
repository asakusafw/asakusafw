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
package com.asakusafw.runtime.flow;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Writable;

/**
 * A skeletal implementation of Hadoop Reducer class which uses {@link SegmentedWritable} as it input key/value.
 * @param <KEYIN> the input key type
 * @param <VALUEIN> the input value type
 * @param <KEYOUT> the output key type
 * @param <VALUEOUT> the output value type
 * @since 0.1.0
 * @version 0.5.1
 */
public abstract class SegmentedReducer<
        KEYIN extends SegmentedWritable,
        VALUEIN extends SegmentedWritable,
        KEYOUT extends Writable,
        VALUEOUT extends Writable>
        extends ReducerWithRuntimeResource<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

    /**
     * The method name of {@link #getRendezvous(SegmentedWritable)}.
     */
    public static final String GET_RENDEZVOUS = "getRendezvous"; //$NON-NLS-1$

    /**
     * Returns the {@link Rendezvous} object for processing the target segment.
     * @param key the segment information
     * @return the corresponded {@link Rendezvous} object
     */
    protected abstract Rendezvous<VALUEIN> getRendezvous(KEYIN key);

    @Override
    protected void reduce(
            KEYIN key,
            Iterable<VALUEIN> values,
            Context context) throws IOException, InterruptedException {
        Iterator<VALUEIN> iter = values.iterator();
        if (iter.hasNext() == false) {
            // may not occur
            return;
        }
        Rendezvous<VALUEIN> group = getRendezvous(key);
        group.begin();
        while (iter.hasNext()) {
            VALUEIN row = iter.next();
            group.process(row);
        }
        group.end();
    }
}
