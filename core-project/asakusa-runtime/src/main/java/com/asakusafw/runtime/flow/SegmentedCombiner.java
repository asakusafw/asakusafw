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
package com.asakusafw.runtime.flow;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.mapreduce.Reducer;

/**
 * A skeletal implementation of Hadoop Combiner class which uses {@link SegmentedWritable} as it key/value.
 * @param <KEY> the key type
 * @param <VALUE> the value type
 * @since 0.1.0
 * @version 0.5.1
 */
public abstract class SegmentedCombiner<
        KEY extends SegmentedWritable,
        VALUE extends SegmentedWritable>
        extends Reducer<KEY, VALUE, KEY, VALUE> {

    /**
     * The method name of {@link #getRendezvous(SegmentedWritable)}.
     */
    public static final String GET_RENDEZVOUS = "getRendezvous"; //$NON-NLS-1$

    /**
     * Returns the {@link Rendezvous} object for processing the target segment.
     * @param key the segment information
     * @return the corresponded {@link Rendezvous} object
     */
    protected abstract Rendezvous<VALUE> getRendezvous(KEY key);

    @Override
    protected void reduce(KEY key, Iterable<VALUE> values, Context context) throws IOException, InterruptedException {
        Iterator<VALUE> iter = values.iterator();
        if (iter.hasNext() == false) {
            // may not occur
            return;
        }
        Rendezvous<VALUE> group = getRendezvous(key);
        if (group == null) {
            while (iter.hasNext()) {
                VALUE row = iter.next();
                KEY current = context.getCurrentKey();
                context.write(current, row);
            }
        } else {
            group.begin();
            while (iter.hasNext()) {
                VALUE row = iter.next();
                group.process(row);
            }
            group.end();
        }
    }
}
