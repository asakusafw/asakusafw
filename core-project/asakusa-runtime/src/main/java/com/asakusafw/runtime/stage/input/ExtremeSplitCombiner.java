/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;

import com.asakusafw.runtime.stage.input.StageInputSplit.Source;

/**
 * Combines into minimum splits.
 * @since 0.2.6
 */
public final class ExtremeSplitCombiner extends SplitCombiner {

    @Override
    protected List<StageInputSplit> combine(JobContext context, List<StageInputSplit> splits) {
        Map<Class<? extends Mapper<?, ?, ?, ?>>, List<Source>> groups = Util.groupByMapper(splits);
        List<StageInputSplit> results = new ArrayList<StageInputSplit>();
        for (Map.Entry<Class<? extends Mapper<?, ?, ?, ?>>, List<Source>> entry : groups.entrySet()) {
            results.add(new StageInputSplit(entry.getKey(), entry.getValue()));
        }
        return results;
    }
}
