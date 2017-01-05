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
@JobFlow(name = "dual")
public class DualJob extends FlowDescription {

    private final In<MockDataModel> i0;

    private final In<MockDataModel> i1;

    private final Out<MockDataModel> o0;

    private final Out<MockDataModel> o1;

    public DualJob(
            @Import(name = "i0", description = MockInputDescription.A.class) In<MockDataModel> i0,
            @Import(name = "i1", description = MockInputDescription.B.class) In<MockDataModel> i1,
            @Export(name = "o0", description = MockOutputDescription.C.class) Out<MockDataModel> o0,
            @Export(name = "o1", description = MockOutputDescription.D.class) Out<MockDataModel> o1) {
        this.i0 = i0;
        this.i1 = i1;
        this.o0 = o0;
        this.o1 = o1;
    }

    @Override
    protected void describe() {
        o0.add(i0);
        o1.add(i1);
    }
}
