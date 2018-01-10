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
package com.example;

import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.Batch.Parameter;
import com.asakusafw.vocabulary.batch.BatchDescription;

/**
 * The average application using partitioned sort.
 */
@Batch(
        name = "perf.average.cogroup",
        parameters = {
                @Parameter(key = "input", comment = "input base path"),
                @Parameter(key = "output", comment = "output base path")
        },
        strict = true
)
public class KsvCogroupBatch extends BatchDescription {

    @Override
    protected void describe() {
        run(KsvCogroupJob.class).soon();
    }

}
