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
package com.asakusafw.thundergate.runtime.cache.mapreduce;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * Reducer class for ThunderGate Cache merging.
 * @since 0.2.3
 */
public class PatchApplyReducer extends Reducer<
        PatchApplyKey, ThunderGateCacheSupport,
        NullWritable, ThunderGateCacheSupport> {

    private static final NullWritable KEY = NullWritable.get();

    @Override
    protected void reduce(
            PatchApplyKey key,
            Iterable<ThunderGateCacheSupport> values,
            Context context) throws IOException, InterruptedException {
        Iterator<ThunderGateCacheSupport> iter = values.iterator();
        if (iter.hasNext()) {
            ThunderGateCacheSupport first = iter.next();
            if (first.__tgc__Deleted() == false) {
                context.write(KEY, first);
            }
        }
    }
}
