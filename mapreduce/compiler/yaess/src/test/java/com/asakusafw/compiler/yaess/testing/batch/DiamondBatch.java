/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.yaess.testing.batch;

import com.asakusafw.compiler.yaess.testing.flow.FirstFlow;
import com.asakusafw.compiler.yaess.testing.flow.LastFlow;
import com.asakusafw.compiler.yaess.testing.flow.LeftFlow;
import com.asakusafw.compiler.yaess.testing.flow.RightFlow;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.Work;

/**
 * diamond dependencies.
 */
@Batch(name = "diamond")
public class DiamondBatch extends BatchDescription {

    @Override
    protected void describe() {
        Work first = run(FirstFlow.class).soon();
        Work left = run(LeftFlow.class).after(first);
        Work right = run(RightFlow.class).after(first);
        run(LastFlow.class).after(left, right);
    }
}
