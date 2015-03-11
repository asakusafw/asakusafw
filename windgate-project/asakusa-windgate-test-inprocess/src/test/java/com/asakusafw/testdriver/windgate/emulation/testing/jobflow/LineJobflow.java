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
package com.asakusafw.testdriver.windgate.emulation.testing.jobflow;

import com.asakusafw.testdriver.windgate.emulation.testing.model.Line;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Test target jobflow.
 */
@JobFlow(name = "line")
public class LineJobflow extends FlowDescription {

    final In<Line> in;

    final Out<Line> out;

    /**
     * Creates a new instance.
     * @param in the input
     * @param out the output
     */
    public LineJobflow(
            @Import(name = "line", description = LineImporterDescription.class) In<Line> in,
            @Export(name = "line", description = LineExporterDescription.class) Out<Line> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        out.add(in);
    }
}
