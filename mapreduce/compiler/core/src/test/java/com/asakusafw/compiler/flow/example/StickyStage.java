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
package com.asakusafw.compiler.flow.example;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Error;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;

/**
 * A simple jobflow class w/ sticky operator.
 */
@FlowPart
public class StickyStage extends FlowDescription {

    private final In<Ex1> in;

    /**
     * Creates a new instance.
     * @param in input
     */
    public StickyStage(In<Ex1> in) {
        this.in = in;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        CoreOperatorFactory core = new CoreOperatorFactory();

        Error error = f.error(in);
        core.stop(error.out);
    }
}
