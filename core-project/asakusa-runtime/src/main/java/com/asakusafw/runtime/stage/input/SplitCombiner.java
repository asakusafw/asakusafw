/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Combines {@link StageInputSplit}s.
 * @since 0.2.6
 */
public abstract class SplitCombiner {

    /**
     * Combines {@link StageInputSplit}s.
     * @param context current context
     * @param splits the original splits
     * @return the combined splits
     * @throws IOException if failed to combine by I/O error
     * @throws InterruptedException if interrupted while combine
     */
    protected abstract List<StageInputSplit> combine(
            JobContext context,
            List<StageInputSplit> splits) throws IOException, InterruptedException;

    /**
     * Utilities for {@link SplitCombiner}.
     * @since 0.2.6
     */
    public static final class Util {

        private Util() {
            return;
        }

        /**
         * Groups {@link StageInputSplit}s by their own {@link Mapper} class.
         * @param splits target input splits
         * @return the grouped sources
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public static Map<Class<? extends Mapper<?, ?, ?, ?>>, List<StageInputSplit.Source>> groupByMapper(
                List<StageInputSplit> splits) {
            if (splits == null) {
                throw new IllegalArgumentException("splits must not be null"); //$NON-NLS-1$
            }
            Map<Class<? extends Mapper<?, ?, ?, ?>>, List<StageInputSplit.Source>> results =
                new HashMap<Class<? extends Mapper<?, ?, ?, ?>>, List<StageInputSplit.Source>>();
            for (StageInputSplit split : splits) {
                Class<? extends Mapper<?, ?, ?, ?>> mapper = split.getMapperClass();
                List<StageInputSplit.Source> group = results.get(mapper);
                if (group == null) {
                    group = new ArrayList<StageInputSplit.Source>();
                    results.put(mapper, group);
                }
                group.addAll(split.getSources());
            }
            return results;
        }
    }
}
