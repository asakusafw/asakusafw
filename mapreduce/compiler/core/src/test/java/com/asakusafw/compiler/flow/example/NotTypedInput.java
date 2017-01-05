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

import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

/**
 * A jobflow class which has raw input type.
 */
@JobFlow(name = "testing")
public class NotTypedInput extends FlowDescription {

    private final In<MockHoge> in;

    private final Out<MockHoge> out;

    /**
     * Creates a new instance.
     * @param in input
     * @param out output
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public NotTypedInput(
            @Import(name = "hoge", description = MockHogeImporterDescription.class)
            In in,
            @Export(name = "hoge", description = MockHogeExporterDescription.class)
            Out<MockHoge> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        out.add(in);
    }
}
