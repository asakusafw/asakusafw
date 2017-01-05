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
package com.asakusafw.compiler.directio.hive.testing;

import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

@SuppressWarnings("javadoc")
@JobFlow(name = "simple")
public class SimpleJob extends FlowDescription {

    private final In<MockDataModel> in;

    private final Out<MockDataModel> out;

    public SimpleJob(
            @Import(name = "i0", description = MockInputDescription.A.class) In<MockDataModel> in,
            @Export(name = "o0", description = MockOutputDescription.A.class) Out<MockDataModel> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        out.add(in);
    }
}
