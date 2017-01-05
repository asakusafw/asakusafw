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
package com.asakusafw.compiler.windgate;

import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Dual input/output identity flow.
 * @param <T> data type
 */
public class DualIdentityFlow<T> extends FlowDescription {

    private final In<T> in1;

    private final In<T> in2;

    private final Out<T> out1;

    private final Out<T> out2;

    /**
     * Creates a new instance.
     * @param in1 input
     * @param in2 input
     * @param out1 output
     * @param out2 output
     */
    public DualIdentityFlow(In<T> in1, In<T> in2, Out<T> out1, Out<T> out2) {
        this.in1 = in1;
        this.in2 = in2;
        this.out1 = out1;
        this.out2 = out2;
    }

    @Override
    protected void describe() {
        out1.add(in1);
        out2.add(in2);
    }
}
