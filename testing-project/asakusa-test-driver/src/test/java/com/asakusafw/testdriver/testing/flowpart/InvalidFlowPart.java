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
package com.asakusafw.testdriver.testing.flowpart;

import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.testdriver.testing.operator.InvalidOperatorFactory;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * A flow part with invalid operator.
 */
@FlowPart
public class InvalidFlowPart extends FlowDescription {

    final In<Simple> in;

    final Out<Simple> out;

    /**
     * Creates a new instance.
     * @param in source
     * @param out sink
     */
    public InvalidFlowPart(In<Simple> in, Out<Simple> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        InvalidOperatorFactory factory = new InvalidOperatorFactory();
        InvalidOperatorFactory.Error operator = factory.error(in);
        out.add(operator.out);
    }
}
