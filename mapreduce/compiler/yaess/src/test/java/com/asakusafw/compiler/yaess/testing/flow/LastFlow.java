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
package com.asakusafw.compiler.yaess.testing.flow;

import com.asakusafw.compiler.yaess.testing.mock.MockExporterDescription;
import com.asakusafw.compiler.yaess.testing.mock.MockImporterDescription;
import com.asakusafw.compiler.yaess.testing.model.Dummy;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Restructure;

/**
 * Complex flow.
 */
@JobFlow(name = "last")
public class LastFlow extends FlowDescription {

    private final In<Dummy> in;

    private final Out<Dummy> out;

    /**
     * Creates a new instance.
     * @param in input
     * @param out output
     */
    public LastFlow(
            @Import(name = "data", description = MockImporterDescription.class)
            In<Dummy> in,
            @Export(name = "data", description = MockExporterDescription.class)
            Out<Dummy> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        CoreOperatorFactory op = new CoreOperatorFactory();
        Restructure<Dummy> first = op.restructure(in, Dummy.class);
        Restructure<Dummy> second = op.restructure(op.checkpoint(first), Dummy.class);
        Restructure<Dummy> last = op.restructure(op.checkpoint(second), Dummy.class);
        out.add(last);
    }
}
